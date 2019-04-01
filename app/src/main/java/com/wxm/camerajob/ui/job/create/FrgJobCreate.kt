package com.wxm.camerajob.ui.job.create

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import com.wxm.camerajob.App
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.EJobStatus
import com.wxm.camerajob.data.define.ETimeGap
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.data.entity.CameraJob
import com.wxm.camerajob.data.entity.CameraParam
import com.wxm.camerajob.preference.PreferencesChangeEvent
import com.wxm.camerajob.preference.PreferencesUtil
import com.wxm.camerajob.ui.base.IAcceptAble
import com.wxm.camerajob.ui.camera.preview.ACCameraPreview
import com.wxm.camerajob.ui.camera.setting.ACCameraSetting
import com.wxm.camerajob.data.utility.CameraJobUtility
import kotterknife.bindView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import wxm.androidutil.improve.doJudge
import wxm.androidutil.improve.let1
import wxm.androidutil.time.CalendarUtility
import wxm.androidutil.time.toTimestamp
import wxm.androidutil.ui.dialog.DlgAlert
import wxm.androidutil.ui.frg.FrgSupportBaseAdv
import wxm.androidutil.ui.moreAdapter.MoreAdapter
import wxm.androidutil.ui.view.EventHelper
import wxm.androidutil.ui.view.ViewHolder
import java.util.*
import kotlin.collections.HashMap

/**
 * fragment for create job
 * Created by WangXM on 2016/10/14.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
open class FrgJobCreate : FrgSupportBaseAdv(), IAcceptAble {
    // for job setting
    private val mETJobName: EditText by bindView(R.id.acet_job_name)
    private val mTVJobStartDate: TextView by bindView(R.id.tv_date_start)
    private val mTVJobEndDate: TextView by bindView(R.id.tv_date_end)
    private val mSTSendPic: Switch by bindView(R.id.sw_send_pic)
    private val mVSEmailDetail: ViewSwitcher by bindView(R.id.vs_email_detail)

    // for gap
    private val mRBMinute: RadioButton by bindView(R.id.rb_minute)
    private val mRBHour: RadioButton by bindView(R.id.rb_hour)
    private val mRBDay: RadioButton by bindView(R.id.rb_day)
    private val mGVJobPoint: GridView by bindView(R.id.gv_job_point)

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
    override fun getLayoutID(): Int = R.layout.pg_job_creater

    override fun initUI(savedInstanceState: Bundle?) {
        initJobSetting()
        initCameraSetting()

        EventHelper.setOnClickOperator(view!!,
                intArrayOf(R.id.iv_clock_start, R.id.iv_clock_end, R.id.rl_setting, R.id.rl_preview),
                ::onItemClick)

        EventHelper.setOnClickOperator(view!!,
                intArrayOf(R.id.rb_minute, R.id.rb_hour, R.id.rb_day),
                ::onRBClick)

        loadJobPoint()
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
        if (jobName.isEmpty()) {
            mETJobName.requestFocus()
            DlgAlert.showAlert(context!!, R.string.dlg_warn, "请输入任务名!")
            return false
        }

        val jobPoint = (mGVJobPoint.adapter as GVJobPointAdapter).selectJobPoint
        if (jobPoint!!.isEmpty()) {
            mGVJobPoint.requestFocus()
            DlgAlert.showAlert(context!!, R.string.dlg_warn, "请选择任务激活方式!")
            return false
        }

        val st = mTVJobStartDate.text.toString().toTimestamp()
        val et = mTVJobEndDate.text.toString().toTimestamp()
        if (st >= et) {
            DlgAlert.showAlert(context!!, R.string.dlg_warn,
                    "任务开始时间(${mTVJobStartDate.text})比结束时间(${mTVJobEndDate.text})晚")
            return false
        }

        CameraJob().apply {
            // for job
            name = jobName
            type = getJobGap()
            point = jobPoint
            startTime = st
            endTime = et
            ts.time = System.currentTimeMillis()
            status = EJobStatus.RUN.status

            // for camera
            PreferencesUtil.loadCameraParam().let1 {
                photoSize = it.mPhotoSize.toString()
                autoFlash = it.mAutoFlash
                autoFocus = it.mAutoFocus
                face = it.mFace
            }
        }.let {
            return App.getCameraJobHelper().createCameraJob(it)
        }
    }

    override fun onCancel(): Boolean {
        return true
    }

    /// BEGIN PRIVATE

    private fun loadJobPoint() {
        ArrayList<String>().apply {
            when {
                mRBMinute.isChecked -> {
                    add(ETimeGap.GAP_FIVE_SECOND.gapName)
                    add(ETimeGap.GAP_FIFTEEN_SECOND.gapName)
                    add(ETimeGap.GAP_THIRTY_SECOND.gapName)
                }

                mRBHour.isChecked -> {
                    add(ETimeGap.GAP_ONE_MINUTE.gapName)
                    add(ETimeGap.GAP_FIVE_MINUTE.gapName)
                    add(ETimeGap.GAP_TEN_MINUTE.gapName)
                    add(ETimeGap.GAP_THIRTY_MINUTE.gapName)
                }

                mRBDay.isChecked -> {
                    add(ETimeGap.GAP_ONE_HOUR.gapName)
                    add(ETimeGap.GAP_TWO_HOUR.gapName)
                    add(ETimeGap.GAP_FOUR_HOUR.gapName)
                }
            }
        }.let1 {
            if (it.isNotEmpty()) {
                mGVJobPoint.adapter = GVJobPointAdapter(
                        it.map { jp ->
                            HashMap<String, String>().apply { put(KEY_JOB_POINT, jp) }
                        },
                        arrayOf(KEY_JOB_POINT),
                        intArrayOf(R.id.tv_job_point))
            }
        }
    }

    private fun getJobGap(): String {
        return when {
            mRBMinute.isChecked -> getString(R.string.gap_minute)
            mRBHour.isChecked -> getString(R.string.gap_hour)
            mRBDay.isChecked -> getString(R.string.gap_day)
            else -> getString(R.string.gap_day)
        }
    }

    /**
     * init job
     */
    private fun initJobSetting() {
        // 任务默认开始时间是“当前时间"
        // 任务默认结束时间是“一周”
        System.currentTimeMillis().let {
            mTVJobStartDate.text = CalendarUtility.YearMonthDayHourMinute.format(it)
            mTVJobEndDate.text = CalendarUtility.YearMonthDayHourMinute.format(it + 1000 * 3600 * 24 * 7)
        }

        // for send pic
        mSTSendPic.setOnCheckedChangeListener { _, isChecked ->
            mVSEmailDetail.displayedChild = if (isChecked) 0 else 1
        }

        mSTSendPic.isChecked = false
        mVSEmailDetail.displayedChild = 1

        mTVEmailSender.text = "请设置邮件发送者"
        mTVEmailReceiver.text = "请设置邮件接收者"
    }

    /**
     * radiobutton click for [v]
     */
    private fun onRBClick(v: View) {
        loadJobPoint()
    }

    /**
     * click for [v]
     */
    private fun onItemClick(v: View) {
        activity!!.let1 {
            when (v.id) {
                R.id.rl_setting -> {
                    it.startActivityForResult(Intent(it, ACCameraSetting::class.java), 1)
                }

                R.id.rl_preview -> {
                    it.startActivityForResult(Intent(it, ACCameraPreview::class.java), 1)
                }

                in intArrayOf(R.id.iv_clock_start, R.id.iv_clock_end) -> {
                    val title = if (R.id.iv_clock_start == v.id) "选择任务启动时间" else "选择任务结束时间"
                    (if (R.id.iv_clock_start == v.id) mTVJobStartDate else mTVJobEndDate).let {tv ->
                        val cl = CalendarUtility.YearMonthDayHourMinute.parse(tv.text)
                        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                            val strDate = String.format(Locale.CHINA, "%04d-%02d-%02d",
                                    year, month + 1, dayOfMonth)

                            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                                tv.text = String.format(Locale.CHINA, "%s %02d:%02d",
                                        strDate, hourOfDay, minute)
                                tv.requestFocus()
                            }.let {
                                TimePickerDialog(context, it, cl.get(Calendar.HOUR_OF_DAY),
                                        cl.get(Calendar.MINUTE), true)
                                        .apply { setTitle(title) }
                                        .show()
                            }
                        }.let {ol ->
                            DatePickerDialog(context, ol,
                                    cl.get(Calendar.YEAR), cl.get(Calendar.MONTH), cl.get(Calendar.DAY_OF_MONTH))
                                    .apply { setTitle(title) }
                                    .show()
                        }
                    }
                }
            }
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
        }.apply {
            when (vw) {
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
            mTVCameraFace.text = getString((CameraParam.LENS_FACING_BACK == it.mFace)
                    .doJudge(R.string.cn_backcamera, R.string.cn_frontcamera))

            mTVCameraDpi.text = it.mPhotoSize.toString()
            mTVCameraFlash.text = getString((it.mAutoFlash)
                    .doJudge(R.string.cn_autoflash, R.string.cn_flash_no))
            mTVCameraFocus.text = getString((it.mAutoFocus)
                    .doJudge(R.string.cn_autofocus, R.string.cn_focus_no))
        }
    }
    /// END PRIVATE
    /**
     * for gridview
     */
    inner class GVJobPointAdapter(data: List<Map<String, String>>, from: Array<String?>, to: IntArray)
        : MoreAdapter(context!!, data, R.layout.gi_job_point, from, to) {
        private val mCLSelected: Int = context!!.getColor(R.color.linen)
        private val mCLNotSelected: Int = context!!.getColor(R.color.white)
        private var mLastSelected: Int = GlobalDef.INT_INVALID_ID

        @Suppress("UNCHECKED_CAST")
        val selectJobPoint: String?
            get() {
                return if (GlobalDef.INT_INVALID_ID == mLastSelected) null
                else (getItem(mLastSelected) as HashMap<String, String>)[KEY_JOB_POINT]
            }

        override fun loadView(pos: Int, vhHolder: ViewHolder) {
            vhHolder.convertView!!.let1 {
                it.setOnClickListener { v ->
                    val selected = mGVJobPoint.getPositionForView(v)
                    if (mLastSelected != selected) {
                        mLastSelected = selected
                        notifyDataSetChanged()
                    }
                }
                it.setBackgroundColor((mLastSelected == pos).doJudge(mCLSelected, mCLNotSelected))
            }
        }
    }

    companion object {
        private const val KEY_JOB_POINT = "job_point"
    }
}
