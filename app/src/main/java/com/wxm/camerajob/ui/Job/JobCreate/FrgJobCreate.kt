package com.wxm.camerajob.ui.Job.JobCreate

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.*
import com.wxm.camerajob.ui.Base.EventHelper
import com.wxm.camerajob.ui.Camera.CameraPreview.ACCameraPreview
import com.wxm.camerajob.ui.Camera.CameraSetting.ACCameraSetting
import com.wxm.camerajob.utility.AlertDlgUtility
import com.wxm.camerajob.utility.CalendarUtility
import kotterknife.bindView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import wxm.androidutil.Dialog.DlgDatePicker
import wxm.androidutil.Dialog.DlgOKOrNOBase
import wxm.androidutil.FrgUtility.FrgSupportBaseAdv
import wxm.androidutil.util.UtilFun
import java.util.*
import kotlin.collections.HashMap

/**
 * fragment for create job
 * Created by WangXM on 2016/10/14.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class FrgJobCreate : FrgSupportBaseAdv() {
    // for job setting
    private val mETJobName: EditText by bindView(R.id.acet_job_name)
    private val mTVJobStartDate: TextView by bindView(R.id.tv_date_start)
    private val mTVJobEndDate: TextView by bindView(R.id.tv_date_end)
    private val mGVJobType: GridView by bindView(R.id.gv_job_type)
    private val mGVJobPoint: GridView by bindView(R.id.gv_job_point)
    private val mSTSendPic: Switch by bindView(R.id.sw_send_pic)
    private val mVSEmailDetail: ViewSwitcher by bindView(R.id.vs_email_detail)

    private var mALJobPoint: ArrayList<HashMap<String, String>>? = null
    private var mGAJobPoint: GVJobPointAdapter? = null

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
    }

    /**
     * for preference event
     *
     * @param event    for preference
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPreferencesChangeEvent(event: PreferencesChangeEvent) {
        if (GlobalDef.STR_CAMERAPROPERTIES_NAME == event.attrName) {
            initCameraSetting()
        }
    }

    /**
     * use input parameter create job
     *
     * @return  if parameter legal return job else return null
     */
    fun onAccept(): CameraJob? {
        val job_name = mETJobName.text.toString()
        val job_type = (mGVJobType.adapter as GVJobTypeAdapter).selectJobType
        val job_point = if (job_type!!.isEmpty()) ""
        else (mGVJobPoint.adapter as GVJobPointAdapter).selectJobPoint

        val job_starttime = mTVJobStartDate.text.toString() + ":00"
        val job_endtime = mTVJobEndDate.text.toString() + ":00"

        if (job_name.isEmpty()) {
            Log.i(LOG_TAG, "job name为空")
            mETJobName.requestFocus()

            AlertDlgUtility.showAlert(activity, "警告", "请输入任务名!")
            return null
        }

        if (job_type.isEmpty()) {
            Log.i(LOG_TAG, "job type为空")
            mGVJobType.requestFocus()

            AlertDlgUtility.showAlert(activity, "警告", "请选择任务类型!")
            return null
        }

        if (job_point!!.isEmpty()) {
            Log.i(LOG_TAG, "job point为空")
            mGVJobPoint.requestFocus()

            AlertDlgUtility.showAlert(activity, "警告", "请选择任务激活方式!")
            return null
        }

        val st = UtilFun.StringToTimestamp(job_starttime)
        val et = UtilFun.StringToTimestamp(job_endtime)
        if (st >= et) {
            val show = String.format("任务开始时间(%s)比结束时间(%s)晚", job_starttime, job_endtime)
            Log.w(LOG_TAG, show)

            AlertDlgUtility.showAlert(activity, "警告", show)
            return null
        }

        return CameraJob().apply {
            name = job_name
            type = job_type
            point = job_point
            starttime = st
            endtime = et
            ts.time = System.currentTimeMillis()
            status.job_status = EJobStatus.RUN.status
        }
    }

    /// BEGIN PRIVATE

    /**
     * init job
     */
    private fun initJobSetting() {
        // 任务默认开始时间是“当前时间"
        // 任务默认结束时间是“一周”
        System.currentTimeMillis().let {
            mTVJobStartDate.text = CalendarUtility.getYearMonthDayHourMinuteStr(it)
            mTVJobEndDate.text = CalendarUtility.getYearMonthDayHourMinuteStr(it + 1000 * 3600 * 24 * 7)
        }

        // for job type & job point
        ArrayList<HashMap<String, String>>().apply {
            addAll(resources.getStringArray(R.array.job_type)
                    .map { HashMap<String, String>().apply { put(KEY_JOB_TYPE, it) } })
        }.let {
            GVJobTypeAdapter(context, it,
                    arrayOf(KEY_JOB_TYPE), intArrayOf(R.id.tv_job_type)).let {
                mGVJobType.adapter = it
            }
        }

        mALJobPoint = ArrayList()
        mGAJobPoint = GVJobPointAdapter(context, mALJobPoint!!,
                arrayOf(KEY_JOB_POINT), intArrayOf(R.id.tv_job_point))
        mGVJobPoint.adapter = mGAJobPoint

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
                    it.startActivityForResult(
                            Intent(it, ACCameraPreview::class.java).apply {
                                putExtra(EAction.LOAD_CAMERA_SETTING.actName, PreferencesUtil.loadCameraParam())
                            }, 1)
                }

                in intArrayOf(R.id.iv_clock_start, R.id.iv_clock_end) -> {
                    (if (R.id.iv_clock_start == v.id) mTVJobStartDate else mTVJobEndDate).let {
                        DlgDatePicker().apply {
                            setInitDate(it.text.toString() + ":00")
                            addDialogListener(object : DlgOKOrNOBase.DialogResultListener {
                                override fun onDialogPositiveResult(dialog: DialogFragment) {
                                    val curDate = (dialog as DlgDatePicker).curDate
                                    if (!UtilFun.StringIsNullOrEmpty(curDate))
                                        it.text = curDate.substring(0, 16)

                                    it.requestFocus()
                                }

                                override fun onDialogNegativeResult(dialog: DialogFragment) {
                                    it.requestFocus()
                                }
                            })

                            show(fragmentManager,
                                    if (R.id.iv_clock_start == v.id) "选择任务启动时间" else "选择任务结束时间")
                        }
                    }
                }
            }

            Unit
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
    inner class GVJobTypeAdapter(context: Context, data: List<Map<String, *>>,
                                 from: Array<String>, to: IntArray) : SimpleAdapter(context, data, R.layout.gi_job_type, from, to), View.OnClickListener {
        private val mCLSelected: Int = context.getColor(R.color.linen)
        private val mCLNotSelected: Int = context.getColor(R.color.white)
        private var mLastSelected: Int = GlobalDef.INT_INVALID_ID

        val selectJobType: String?
            get() {
                return if (GlobalDef.INT_INVALID_ID == mLastSelected) ""
                else UtilFun.cast<HashMap<String, String>>(getItem(mLastSelected))[KEY_JOB_TYPE]
            }

        override fun getViewTypeCount(): Int {
            val org_ct = count
            return if (org_ct < 1) 1 else org_ct
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun getView(position: Int, view: View, arg2: ViewGroup): View? {
            val v = super.getView(position, view, arg2)
            if (null != v) {
                v.setOnClickListener(this)
                v.setBackgroundColor(if (mLastSelected == position) mCLSelected else mCLNotSelected)
            }

            return v
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
                        mALJobPoint!!.apply {
                            clear()
                            addAll(it.map { HashMap<String, String>().apply { put(KEY_JOB_POINT, it) } })
                        }

                        mGAJobPoint!!.apply {
                            cleanSelected()
                            notifyDataSetChanged()
                        }
                    }
                }
            } catch (e: Resources.NotFoundException) {
                Log.e(LOG_TAG, "Not find string array for '$hv'")
                e.printStackTrace()
            }
        }
    }


    /**
     * for gridview
     */
    inner class GVJobPointAdapter(context: Context, data: List<Map<String, *>>,
                                  from: Array<String>, to: IntArray) : SimpleAdapter(context, data, R.layout.gi_job_point, from, to), View.OnClickListener {
        private val mCLSelected: Int = context.getColor(R.color.linen)
        private val mCLNotSelected: Int = context.getColor(R.color.white)
        private var mLastSelected: Int = GlobalDef.INT_INVALID_ID

        val selectJobPoint: String?
            get() {
                if (GlobalDef.INT_INVALID_ID == mLastSelected)
                    return ""

                val hmd = UtilFun.cast<HashMap<String, Any>>(getItem(mLastSelected))
                return UtilFun.cast_t<String>(hmd[KEY_JOB_POINT])
            }

        override fun getViewTypeCount(): Int {
            val org_ct = count
            return if (org_ct < 1) 1 else org_ct
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun getView(position: Int, view: View, arg2: ViewGroup): View? {
            val v = super.getView(position, view, arg2)
            if (null != v) {
                v.setOnClickListener(this)
                v.setBackgroundColor(if (mLastSelected == position) mCLSelected else mCLNotSelected)
            }

            return v
        }

        override fun onClick(v: View) {
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
