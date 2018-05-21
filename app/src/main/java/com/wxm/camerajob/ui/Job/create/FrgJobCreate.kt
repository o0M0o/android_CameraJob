package com.wxm.camerajob.ui.Job.create

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.*
import com.wxm.camerajob.ui.base.EventHelper
import com.wxm.camerajob.ui.base.IAcceptAble
import com.wxm.camerajob.ui.camera.preview.ACCameraPreview
import com.wxm.camerajob.ui.camera.setting.ACCameraSetting
import com.wxm.camerajob.utility.CalendarUtility
import com.wxm.camerajob.utility.CameraJobUtility
import com.wxm.camerajob.utility.DlgUtility
import com.wxm.camerajob.utility.log.TagLog
import kotterknife.bindView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import wxm.androidutil.FrgUtility.FrgSupportBaseAdv
import wxm.androidutil.MoreAdapter.MoreAdapter
import wxm.androidutil.ViewHolder.ViewHolder
import wxm.androidutil.util.UtilFun
import java.util.*
import kotlin.collections.HashMap

/**
 * fragment for create job
 * Created by WangXM on 2016/10/14.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class FrgJobCreate : FrgSupportBaseAdv(), IAcceptAble {
    // for job setting
    private val mETJobName: EditText by bindView(R.id.acet_job_name)
    private val mTVJobStartDate: TextView by bindView(R.id.tv_date_start)
    private val mTVJobEndDate: TextView by bindView(R.id.tv_date_end)
    private val mGVJobType: GridView by bindView(R.id.gv_job_type)
    private val mGVJobPoint: GridView by bindView(R.id.gv_job_point)
    private val mSTSendPic: Switch by bindView(R.id.sw_send_pic)
    private val mVSEmailDetail: ViewSwitcher by bindView(R.id.vs_email_detail)

    // for camera setting
    private val mTVCameraFace: TextView by bindView(R.id.tv_camera_face)
    private val mTVCameraDpi: TextView by bindView(R.id.tv_camera_dpi)
    private val mTVCameraFlash: TextView by bindView(R.id.tv_camera_flash)
    private val mTVCameraFocus: TextView by bindView(R.id.tv_camera_focus)

    // for send pic
    private val mTVEmailSender: TextView by bindView(R.id.tv_email_sender)
    private val mTVEmailSendServerType: TextView by bindView(R.id.tv_email_server_type)
    private val mTVEmailSendType: TextView by bindView(R.id.tv_email_send_type)
    private val mTVEmailReceiver: TextView by bindView(R.id.tv_email_recv_address)

    override fun isUseEventBus(): Boolean = true
    override fun getLayoutID(): Int = R.layout.vw_job_creater

    override fun initUI(savedInstanceState: Bundle?) {
        initJobSetting()
        initCameraSetting()

        EventHelper.setOnClickOperator(view!!,
                intArrayOf(R.id.iv_clock_start, R.id.iv_clock_end, R.id.rl_setting, R.id.rl_preview),
                ::onClick)

        autoScroll(R.id.rl_start_end_date)
    }

    /**
     * for preference event
     *
     * @param event    for preference
     */
    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPreferencesChangeEvent(event: PreferencesChangeEvent) {
        if (GlobalDef.STR_CAMERAPROPERTIES_NAME == event.attrName) {
            initCameraSetting()
        }
    }

    override fun onAccept(): Boolean {
        val jobName = mETJobName.text.toString()
        val jobType = (mGVJobType.adapter as GVJobTypeAdapter).selectJobType
        val jobPoint = if (jobType!!.isEmpty()) ""
        else (mGVJobPoint.adapter as GVJobPointAdapter).selectJobPoint

        val jobStartDate = mTVJobStartDate.text.toString() + ":00"
        val jobEndDate = mTVJobEndDate.text.toString() + ":00"

        if (jobName.isEmpty()) {
            mETJobName.requestFocus()

            DlgUtility.showAlert(activity, R.string.warn, "请输入任务名!")
            return false
        }

        if (jobType.isEmpty()) {
            mGVJobType.requestFocus()

            DlgUtility.showAlert(activity, R.string.warn, "请选择任务类型!")
            return false
        }

        if (jobPoint!!.isEmpty()) {
            mGVJobPoint.requestFocus()

            DlgUtility.showAlert(activity, R.string.warn, "请选择任务激活方式!")
            return false
        }

        val st = UtilFun.StringToTimestamp(jobStartDate)
        val et = UtilFun.StringToTimestamp(jobEndDate)
        if (st >= et) {
            String.format(Locale.CHINA, "任务开始时间(%s)比结束时间(%s)晚",
                    jobStartDate, jobEndDate).apply {
                DlgUtility.showAlert(activity, R.string.warn, this)
            }

            return false
        }

        CameraJob().apply {
            name = jobName
            type = jobType
            point = jobPoint
            starttime = st
            endtime = et
            ts.time = System.currentTimeMillis()
            status.job_status = EJobStatus.RUN.status
        }.let {
            return CameraJobUtility.createCameraJob(it)
        }
    }

    override fun onCancel(): Boolean {
        return true
    }


    /// BEGIN PRIVATE

    /**
     * init job
     */
    private fun initJobSetting() {
        // 任务默认开始时间是“当前时间"
        // 任务默认结束时间是“一周”
        System.currentTimeMillis().let {
            mTVJobStartDate.text = CalendarUtility.YearMonthDayHourMinute.getStr(it)
            mTVJobEndDate.text = CalendarUtility.YearMonthDayHourMinute.getStr(it + 1000 * 3600 * 24 * 7)
        }

        // for job type & job point
        ArrayList<HashMap<String, String>>().apply {
            addAll(resources.getStringArray(R.array.jobType)
                    .map { HashMap<String, String>().apply { put(KEY_JOB_TYPE, it) } })
        }.let {
            mGVJobType.adapter = GVJobTypeAdapter(it,
                    arrayOf(KEY_JOB_TYPE), intArrayOf(R.id.tv_job_type))
        }

        // for send pic
        mSTSendPic.setOnCheckedChangeListener { _, isChecked
            ->
            mVSEmailDetail.displayedChild = if (isChecked) 0 else 1
        }

        mSTSendPic.isChecked = false
        mVSEmailDetail.displayedChild = 1

        mTVEmailSender.text = "请设置邮件发送者"
        mTVEmailReceiver.text = "请设置邮件接收者"
    }


    /**
     * for 'setting' and 'preview'
     *
     * @param v  event sender
     */
    fun onClick(v: View) {
        activity.let {
            when (v.id) {
                R.id.rl_setting -> {
                    it.startActivityForResult(Intent(it, ACCameraSetting::class.java), 1)
                }

                R.id.rl_preview -> {
                    it.startActivityForResult(Intent(it, ACCameraPreview::class.java), 1)
                }

                in intArrayOf(R.id.iv_clock_start, R.id.iv_clock_end) -> {
                    val title = if (R.id.iv_clock_start == v.id) "选择任务启动时间" else "选择任务结束时间"
                    (if (R.id.iv_clock_start == v.id) mTVJobStartDate else mTVJobEndDate).let {
                        val cl = CalendarUtility.YearMonthDayHourMinute.parseStr(it.text)
                        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                            val strDate = String.format(Locale.CHINA, "%04d-%02d-%02d",
                                    year, month + 1, dayOfMonth)

                            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                                it.text = String.format(Locale.CHINA, "%s %02d:%02d",
                                        strDate, hourOfDay, minute)
                                it.requestFocus()
                            }.let {
                                TimePickerDialog(context, it, cl.get(Calendar.HOUR_OF_DAY),
                                        cl.get(Calendar.MINUTE), true)
                                        .apply { setTitle(title) }
                                        .show()
                            }
                        }.let {
                            DatePickerDialog(context, it, cl.get(Calendar.YEAR), cl.get(Calendar.MONTH),
                                    cl.get(Calendar.DAY_OF_MONTH))
                                    .apply { setTitle(title) }
                                    .show()
                        }
                    }
                }
            }

            Unit
        }
    }

    /**
     * auto scroll to view [vw] with margin to top [topMargin]
     */
    private fun autoScroll(vw: Any, topMargin: Int = 60) {
        val vwHome = view!!
        { v: View, hasFocus: Boolean ->
            vwHome.scrollY = if (hasFocus) {
                Rect().apply { vwHome.getGlobalVisibleRect(this) }.top
                        .let {
                            Rect().apply { v.getGlobalVisibleRect(this) }
                                    .top - it - topMargin
                        }
            } else 0
        }.apply{
            when(vw)    {
                is Int -> vwHome.findViewById<View>(vw)!!.setOnFocusChangeListener(this)
                is View -> vw.setOnFocusChangeListener(this)
                else -> throw IllegalStateException("${vw.javaClass.name} not support scroll!")
            }
        }
    }


    /**
     * init camera
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun initCameraSetting() {
        // for camera setting
        view!!.findViewById<RelativeLayout>(R.id.rl_preview)!!.let {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                it.visibility = View.INVISIBLE
            }
        }

        PreferencesUtil.loadCameraParam().let {
            mTVCameraFace.text = getString(if (CameraParam.LENS_FACING_BACK == it.mFace)
                R.string.cn_backcamera
            else R.string.cn_frontcamera)

            mTVCameraDpi.text = it.mPhotoSize.toString()
            mTVCameraFlash.text = getString(if (it.mAutoFlash)
                R.string.cn_autoflash
            else R.string.cn_flash_no)
            mTVCameraFocus.text = getString(if (it.mAutoFocus)
                R.string.cn_autofocus
            else R.string.cn_focus_no)
        }
    }
    /// END PRIVATE


    /**
     * for gridview
     */
    inner class GVJobTypeAdapter(data: List<Map<String, String>>, fromKey: Array<String?>, toId: IntArray)
        : MoreAdapter(context, data, R.layout.gi_job_type, fromKey, toId), View.OnClickListener {
        private val mCLSelected: Int = context.getColor(R.color.linen)
        private val mCLNotSelected: Int = context.getColor(R.color.white)
        private var mLastSelected: Int = GlobalDef.INT_INVALID_ID

        @Suppress("UNCHECKED_CAST")
        val selectJobType: String?
            get() {
                return if (GlobalDef.INT_INVALID_ID == mLastSelected) null
                else (getItem(mLastSelected) as HashMap<String, String>)[KEY_JOB_TYPE]
            }

        override fun loadView(pos: Int, vhHolder: ViewHolder) {
            val handler = this::onClick
            vhHolder.convertView.apply {
                setOnClickListener(handler)
                setBackgroundColor(if (mLastSelected == pos) mCLSelected else mCLNotSelected)
            }
        }

        override fun onClick(v: View) {
            val pos = mGVJobType.getPositionForView(v)
            if (mLastSelected == pos)
                return

            invokeJobPoint(pos)
            mLastSelected = pos
            notifyDataSetChanged()
        }

        private fun invokeJobPoint(pos: Int) {
            val hv = ((getItem(pos) as HashMap<*, *>)[KEY_JOB_TYPE] as String)
            try {
                val et = EJobType.getEJobType(hv) ?: return
                ArrayList<String>().apply {
                    when (et) {
                        EJobType.JOB_MINUTELY -> {
                            add(ETimeGap.GAP_FIVE_SECOND.gapName)
                            add(ETimeGap.GAP_FIFTEEN_SECOND.gapName)
                            add(ETimeGap.GAP_THIRTY_SECOND.gapName)
                        }

                        EJobType.JOB_HOURLY -> {
                            add(ETimeGap.GAP_ONE_MINUTE.gapName)
                            add(ETimeGap.GAP_FIVE_MINUTE.gapName)
                            add(ETimeGap.GAP_TEN_MINUTE.gapName)
                            add(ETimeGap.GAP_THIRTY_MINUTE.gapName)
                        }

                        EJobType.JOB_DAILY -> {
                            add(ETimeGap.GAP_ONE_HOUR.gapName)
                            add(ETimeGap.GAP_TWO_HOUR.gapName)
                            add(ETimeGap.GAP_FOUR_HOUR.gapName)
                        }
                    }
                }.let {
                    if (it.isNotEmpty()) {
                        val jobPoint = ArrayList<Map<String, String>>().apply {
                            addAll(it.map { HashMap<String, String>().apply { put(KEY_JOB_POINT, it) } })
                        }

                        GVJobPointAdapter(jobPoint, arrayOf(KEY_JOB_POINT), intArrayOf(R.id.tv_job_point)).let {
                            mGVJobPoint.adapter = it
                        }
                    }
                }
            } catch (e: Resources.NotFoundException) {
                TagLog.e("Not find string array for '$hv'", e)
            }
        }
    }


    /**
     * for gridview
     */
    inner class GVJobPointAdapter(data: List<Map<String, String>>, from: Array<String?>, to: IntArray)
        : MoreAdapter(context, data, R.layout.gi_job_point, from, to) {
        private val mCLSelected: Int = context.getColor(R.color.linen)
        private val mCLNotSelected: Int = context.getColor(R.color.white)
        private var mLastSelected: Int = GlobalDef.INT_INVALID_ID

        @Suppress("UNCHECKED_CAST")
        val selectJobPoint: String?
            get() {
                return if (GlobalDef.INT_INVALID_ID == mLastSelected) null
                else (getItem(mLastSelected) as HashMap<String, String>)[KEY_JOB_POINT]
            }

        override fun loadView(pos: Int, vhHolder: ViewHolder) {
            val handler = this::onClick
            vhHolder.convertView.apply {
                setOnClickListener(handler)
                setBackgroundColor(if (mLastSelected == pos) mCLSelected else mCLNotSelected)
            }
        }

        fun onClick(v: View) {
            mGVJobPoint.getPositionForView(v).let {
                if (mLastSelected != it) {
                    mLastSelected = it
                    notifyDataSetChanged()
                }
            }
        }

        fun cleanSelected() {
            mLastSelected = GlobalDef.INT_INVALID_ID
        }
    }

    companion object {
        private const val KEY_JOB_TYPE = "job_type"
        private const val KEY_JOB_POINT = "job_point"
    }
}
