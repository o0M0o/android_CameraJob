package com.wxm.camerajob.ui.Job.JobCreate

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.GridView
import android.widget.RelativeLayout
import android.widget.SimpleAdapter
import android.widget.Switch
import android.widget.TextView
import android.widget.ViewSwitcher

import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.CameraJob
import com.wxm.camerajob.data.define.CameraParam
import com.wxm.camerajob.data.define.EAction
import com.wxm.camerajob.data.define.EJobType
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.data.define.EJobStatus
import com.wxm.camerajob.data.define.PreferencesChangeEvent
import com.wxm.camerajob.data.define.PreferencesUtil
import com.wxm.camerajob.data.define.ETimeGap
import com.wxm.camerajob.ui.Camera.CameraPreview.ACCameraPreview
import com.wxm.camerajob.ui.Camera.CameraSetting.ACCameraSetting

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.sql.Timestamp
import java.util.ArrayList
import java.util.HashMap

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import wxm.androidutil.Dialog.DlgDatePicker
import wxm.androidutil.Dialog.DlgOKOrNOBase
import wxm.androidutil.FrgUtility.FrgUtilitySupportBase
import wxm.androidutil.util.UtilFun

/**
 * fragment for create job
 * Created by WangXM on 2016/10/14.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class FrgJobCreate : FrgUtilitySupportBase() {

    // for job setting
    @BindView(R.id.acet_job_name)
    internal var mETJobName: EditText? = null

    @BindView(R.id.tv_date_start)
    internal var mTVJobStartDate: TextView? = null

    @BindView(R.id.tv_date_end)
    internal var mTVJobEndDate: TextView? = null

    @BindView(R.id.gv_job_type)
    internal var mGVJobType: GridView? = null

    @BindView(R.id.gv_job_point)
    internal var mGVJobPoint: GridView? = null

    @BindView(R.id.sw_send_pic)
    internal var mSTSendPic: Switch? = null

    @BindView(R.id.vs_email_detail)
    internal var mVSEmailDetail: ViewSwitcher? = null

    //private Switch      mSWSendPicByEmail;

    private var mALJobPoint: ArrayList<HashMap<String, String>>? = null
    private var mGAJobPoint: GVJobPointAdapter? = null

    // for camera setting
    @BindView(R.id.tv_camera_face)
    internal var mTVCameraFace: TextView? = null

    @BindView(R.id.tv_camera_dpi)
    internal var mTVCameraDpi: TextView? = null

    @BindView(R.id.tv_camera_flash)
    internal var mTVCameraFlash: TextView? = null

    @BindView(R.id.tv_camera_focus)
    internal var mTVCameraFocus: TextView? = null

    // for send pic
    @BindView(R.id.tv_email_sender)
    internal var mTVEmailSender: TextView? = null

    @BindView(R.id.tv_email_server_type)
    internal var mTVEmailSendServerType: TextView? = null

    @BindView(R.id.tv_email_send_type)
    internal var mTVEmailSendType: TextView? = null

    @BindView(R.id.tv_email_recv_address)
    internal var mTVEmailReceiver: TextView? = null

    protected fun enterActivity() {
        super.enterActivity()
        EventBus.getDefault().register(this)
    }

    protected fun leaveActivity() {
        EventBus.getDefault().unregister(this)
        super.leaveActivity()
    }

    protected fun inflaterView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle): View {
        LOG_TAG = "FrgJobCreate"
        val rootView = inflater.inflate(R.layout.vw_job_creater, container, false)
        ButterKnife.bind(this, rootView)
        return rootView
    }

    protected fun initUiComponent(view: View) {
        init_job_setting()
        init_camera_setting()
    }

    protected fun loadUI() {}

    /**
     * for preference event
     *
     * @param event    for preference
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPreferencesChangeEvent(event: PreferencesChangeEvent) {
        if (GlobalDef.STR_CAMERAPROPERTIES_NAME == event.attrName) {
            load_camera_setting()
        }
    }

    /**
     * use input parameter create job
     *
     * @return  if parameter legal return job else return null
     */
    fun onAccept(): CameraJob? {
        val job_name = mETJobName!!.text.toString()
        val job_type = (mGVJobType!!.adapter as GVJobTypeAdapter).selectJobType
        val job_point = if (job_type!!.isEmpty())
            ""
        else
            (mGVJobPoint!!.adapter as GVJobPointAdapter).selectJobPoint
        val job_starttime = mTVJobStartDate!!.text.toString() + ":00"
        val job_endtime = mTVJobEndDate!!.text.toString() + ":00"

        val ct = getContext()
        if (job_name.isEmpty()) {
            Log.i(LOG_TAG, "job name为空")
            mETJobName!!.requestFocus()

            val builder = AlertDialog.Builder(ct)
            builder.setMessage("请输入任务名!").setTitle("警告")
            val dlg = builder.create()
            dlg.show()
            return null
        }

        if (job_type.isEmpty()) {
            Log.i(LOG_TAG, "job type为空")
            mGVJobType!!.requestFocus()

            val builder = AlertDialog.Builder(ct)
            builder.setMessage("请选择任务类型!").setTitle("警告")
            val dlg = builder.create()
            dlg.show()
            return null
        }

        if (job_point!!.isEmpty()) {
            Log.i(LOG_TAG, "job point为空")
            mGVJobPoint!!.requestFocus()

            val builder = AlertDialog.Builder(ct)
            builder.setMessage("请选择任务激活方式!").setTitle("警告")
            val dlg = builder.create()
            dlg.show()
            return null
        }

        val st = UtilFun.StringToTimestamp(job_starttime)
        val et = UtilFun.StringToTimestamp(job_endtime)
        if (0 <= st.compareTo(et)) {
            val show = String.format("任务开始时间(%s)比结束时间(%s)晚", job_starttime, job_endtime)
            Log.w(LOG_TAG, show)

            val builder = AlertDialog.Builder(ct)
            builder.setMessage(show).setTitle("警告")
            val dlg = builder.create()
            dlg.show()
            return null
        }

        val cj = CameraJob()
        cj.name = job_name
        cj.type = job_type
        cj.point = job_point
        cj.starttime = st
        cj.endtime = et
        cj.ts.time = System.currentTimeMillis()
        cj.status.job_status = EJobStatus.RUN.status
        return cj
    }

    /// BEGIN PRIVATE

    /**
     * init job
     */
    private fun init_job_setting() {
        // 任务默认开始时间是“当前时间"
        // 任务默认结束时间是“一周”
        mTVJobStartDate!!.text = UtilFun.MilliSecsToString(
                System.currentTimeMillis()).substring(0, 16)
        mTVJobEndDate!!.text = UtilFun.MilliSecsToString(System.currentTimeMillis() + 1000 * 3600 * 24 * 7).substring(0, 16)

        // for job type & job point
        val str_arr = getResources().getStringArray(R.array.job_type)
        val al_hm = ArrayList<HashMap<String, String>>()
        for (i in str_arr) {
            val hm = HashMap<String, String>()
            hm[KEY_JOB_TYPE] = i

            al_hm.add(hm)
        }

        val ga = GVJobTypeAdapter(getContext(), al_hm,
                arrayOf(KEY_JOB_TYPE), intArrayOf(R.id.tv_job_type))
        mGVJobType!!.adapter = ga
        ga.notifyDataSetChanged()

        mALJobPoint = ArrayList()
        mGAJobPoint = GVJobPointAdapter(getContext(), mALJobPoint,
                arrayOf(KEY_JOB_POINT), intArrayOf(R.id.tv_job_point))
        mGVJobPoint!!.adapter = mGAJobPoint
        mGAJobPoint!!.notifyDataSetChanged()

        // for send pic
        mSTSendPic!!.setOnCheckedChangeListener { buttonView, isChecked -> mVSEmailDetail!!.displayedChild = if (isChecked) 0 else 1 }

        mSTSendPic!!.isChecked = false
        mVSEmailDetail!!.displayedChild = 1

        mTVEmailSender!!.text = "请设置邮件发送者"
        mTVEmailReceiver!!.text = "请设置邮件接收者"
    }


    @OnClick(R.id.iv_clock_start, R.id.iv_clock_end)
    fun onClockClick(v: View) {
        val vid = v.id
        val hot_tv = if (R.id.iv_clock_start == vid) mTVJobStartDate else mTVJobEndDate

        val dp = DlgDatePicker()
        dp.setInitDate(hot_tv!!.text.toString() + ":00")
        dp.addDialogListener(object : DlgOKOrNOBase.DialogResultListener {
            override fun onDialogPositiveResult(dialog: DialogFragment) {
                val cur_dp = UtilFun.cast_t<DlgDatePicker>(dialog)
                val cur_date = cur_dp.curDate

                if (!UtilFun.StringIsNullOrEmpty(cur_date))
                    hot_tv.text = cur_date.substring(0, 16)

                hot_tv.requestFocus()
            }

            override fun onDialogNegativeResult(dialog: DialogFragment) {
                hot_tv.requestFocus()
            }
        })

        dp.show(getFragmentManager(),
                if (R.id.iv_clock_start == vid) "选择任务启动时间" else "选择任务结束时间")
    }

    /**
     * for 'setting' and 'preview'
     *
     * @param v  event sender
     */
    @OnClick(R.id.rl_setting, R.id.rl_preview)
    fun onRLClick(v: View) {
        val ac = getActivity()
        val vid = v.id
        when (vid) {
            R.id.rl_setting -> {
                val data = Intent(ac, ACCameraSetting::class.java)
                ac.startActivityForResult(data, 1)
            }

            R.id.rl_preview -> {
                val it = Intent(ac, ACCameraPreview::class.java)
                it.putExtra(EAction.LOAD_CAMERA_SETTING.actName, PreferencesUtil.loadCameraParam())
                ac.startActivityForResult(it, 1)
            }
        }
    }


    /**
     * init camera
     */
    private fun init_camera_setting() {
        // for camera setting
        val vw = getView() ?: return

        val rl = UtilFun.cast_t<RelativeLayout>(vw!!.findViewById(R.id.rl_preview)) ?: return

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            rl.visibility = View.INVISIBLE
        }

        load_camera_setting()
    }

    /**
     * load camera preference
     */
    private fun load_camera_setting() {
        val cp = PreferencesUtil.loadCameraParam()
        mTVCameraFace!!.setText(getString(if (CameraParam.LENS_FACING_BACK == cp.mFace)
            R.string.cn_backcamera
        else
            R.string.cn_frontcamera))

        mTVCameraDpi!!.text = cp.mPhotoSize.toString()
        mTVCameraFlash!!.setText(getString(if (cp.mAutoFlash)
            R.string.cn_autoflash
        else
            R.string.cn_flash_no))
        mTVCameraFocus!!.setText(getString(if (cp.mAutoFocus)
            R.string.cn_autofocus
        else
            R.string.cn_focus_no))
    }
    /// END PRIVATE


    /**
     * for gridview
     */
    inner class GVJobTypeAdapter(context: Context, data: List<Map<String, *>>,
                                 from: Array<String>, to: IntArray) : SimpleAdapter(context, data, R.layout.gi_job_type, from, to), View.OnClickListener {
        private val mCLSelected: Int
        private val mCLNotSelected: Int
        private var mLastSelected: Int = 0

        val selectJobType: String?
            get() {
                if (GlobalDef.INT_INVALID_ID == mLastSelected)
                    return ""

                val hmd = UtilFun.cast<HashMap<String, Any>>(getItem(mLastSelected))
                return UtilFun.cast_t<String>(hmd[KEY_JOB_TYPE])
            }

        init {


            mCLSelected = getResources().getColor(R.color.linen)
            mCLNotSelected = getResources().getColor(R.color.white)

            mLastSelected = GlobalDef.INT_INVALID_ID
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
            val pos = mGVJobType!!.getPositionForView(v)
            if (mLastSelected == pos)
                return

            invoke_job_point(pos)
            mLastSelected = pos
            notifyDataSetChanged()
        }

        private fun invoke_job_point(pos: Int) {
            val hmd = UtilFun.cast<HashMap<String, Any>>(getItem(pos))
            val hv = UtilFun.cast_t<String>(hmd[KEY_JOB_TYPE])
            val et = EJobType.getEJobType(hv) ?: return

            try {
                val str_arr = ArrayList<String>()
                when (et) {
                    EJobType.JOB_MINUTELY -> {
                        str_arr.add(ETimeGap.GAP_FIVE_SECOND.gapName)
                        str_arr.add(ETimeGap.GAP_FIFTEEN_SECOND.gapName)
                        str_arr.add(ETimeGap.GAP_THIRTY_SECOND.gapName)
                    }

                    EJobType.JOB_HOURLY -> {
                        str_arr.add(ETimeGap.GAP_ONE_MINUTE.gapName)
                        str_arr.add(ETimeGap.GAP_FIVE_MINUTE.gapName)
                        str_arr.add(ETimeGap.GAP_TEN_MINUTE.gapName)
                        str_arr.add(ETimeGap.GAP_THIRTY_MINUTE.gapName)
                    }

                    EJobType.JOB_DAILY -> {
                        str_arr.add(ETimeGap.GAP_ONE_HOUR.gapName)
                        str_arr.add(ETimeGap.GAP_TWO_HOUR.gapName)
                        str_arr.add(ETimeGap.GAP_FOUR_HOUR.gapName)
                    }
                }

                if (!str_arr.isEmpty()) {
                    mALJobPoint!!.clear()
                    for (i in str_arr) {
                        val hm = HashMap<String, String>()
                        hm[KEY_JOB_POINT] = i

                        mALJobPoint!!.add(hm)
                    }

                    mGAJobPoint!!.cleanSelected()
                    mGAJobPoint!!.notifyDataSetChanged()
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
        private val mCLSelected: Int
        private val mCLNotSelected: Int
        private var mLastSelected: Int = 0

        val selectJobPoint: String?
            get() {
                if (GlobalDef.INT_INVALID_ID == mLastSelected)
                    return ""

                val hmd = UtilFun.cast<HashMap<String, Any>>(getItem(mLastSelected))
                return UtilFun.cast_t<String>(hmd[KEY_JOB_POINT])
            }

        init {

            mCLSelected = getResources().getColor(R.color.linen)
            mCLNotSelected = getResources().getColor(R.color.white)

            mLastSelected = GlobalDef.INT_INVALID_ID
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
            val pos = mGVJobPoint!!.getPositionForView(v)
            if (mLastSelected == pos)
                return

            mLastSelected = pos
            notifyDataSetChanged()
        }

        fun cleanSelected() {
            mLastSelected = GlobalDef.INT_INVALID_ID
        }
    }

    companion object {

        private val KEY_JOB_TYPE = "job_type"
        private val KEY_JOB_POINT = "job_point"

        fun newInstance(): FrgJobCreate {
            return FrgJobCreate()
        }
    }
}
