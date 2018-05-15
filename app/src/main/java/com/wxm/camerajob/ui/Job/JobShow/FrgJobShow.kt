package com.wxm.camerajob.ui.Job.JobShow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.SimpleAdapter

import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.CameraJob
import com.wxm.camerajob.data.define.CameraJobStatus
import com.wxm.camerajob.data.define.DBDataChangeEvent
import com.wxm.camerajob.data.define.EAction
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.data.define.EJobStatus
import com.wxm.camerajob.data.define.PreferencesChangeEvent
import com.wxm.camerajob.data.define.PreferencesUtil
import com.wxm.camerajob.utility.CameraJobUtility
import com.wxm.camerajob.utility.ContextUtil
import com.wxm.camerajob.ui.Base.JobGallery
import com.wxm.camerajob.ui.Job.JobSlide.ACJobSlide
import com.wxm.camerajob.ui.Base.FrgCameraInfoHelper

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.util.ArrayList
import java.util.Comparator
import java.util.HashMap
import java.util.LinkedList
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

import butterknife.BindView
import butterknife.ButterKnife
import wxm.androidutil.FrgUtility.FrgUtilitySupportBase
import wxm.androidutil.util.FileUtil
import wxm.androidutil.util.UtilFun


/**
 * fragment for show job
 * Created by WangXM on 2016/10/14.
 */
class FrgJobShow : FrgUtilitySupportBase() {

    @BindView(R.id.aclv_start_jobs)
    internal var mLVJobs: ListView? = null

    private var mTimer: Timer? = null

    protected fun inflaterView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle): View {
        LOG_TAG = "FrgJobShow"
        val rootView = inflater.inflate(R.layout.vw_job_show, container, false)
        ButterKnife.bind(this, rootView)
        return rootView
    }

    /**
     * 数据库内数据变化处理器
     * @param event     事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDBDataChangeEvent(event: DBDataChangeEvent) {
        val ms = if (DBDataChangeEvent.EVENT_CREATE == event.eventType) 1200 else 0

        //new Handler((Handler.Callback) this.getActivity()).postDelayed(this::refreshFrg, ms);
        Handler().postDelayed({ this.refreshFrg() }, ms.toLong())
    }

    /**
     * 配置变化处理器
     * @param event     事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPreferencesChangeEvent(event: PreferencesChangeEvent) {
        if (GlobalDef.STR_CAMERAPROPERTIES_NAME == event.attrName) {
            refreshFrg()
        }
    }

    protected fun enterActivity() {
        Log.d(LOG_TAG, "in enterActivity")
        super.enterActivity()

        EventBus.getDefault().register(this)

        // update jobs info every 3 seconds
        val h = this
        mTimer = Timer()
        mTimer!!.schedule(object : TimerTask() {
            override fun run() {
                h.getActivity().runOnUiThread(???({ h.refreshCameraJobs() }))
            }
        }, 100, 3000)
    }

    protected fun leaveActivity() {
        Log.d(LOG_TAG, "in leaveActivity")

        mTimer!!.cancel()
        EventBus.getDefault().unregister(this)

        super.leaveActivity()
    }

    protected fun initUiComponent(view: View) {}

    protected fun loadUI() {
        // for listview
        val mLVAdapter = LVJobShowAdapter(getContext(),
                ArrayList<HashMap<String, String>>(),
                arrayOf(KEY_JOB_NAME, KEY_JOB_ACTIVE, KEY_JOB_DETAIL),
                intArrayOf(R.id.tv_job_name, R.id.tv_job_active, R.id.tv_job_detail))
        mLVJobs!!.adapter = mLVAdapter
    }


    /**
     * 更新数据
     * @param lsdata 新数据
     */
    fun updateData(lsdata: List<HashMap<String, String>>) {
        val al_data = ArrayList<HashMap<String, String>>()
        al_data.addAll(lsdata)
        val mLVAdapter = LVJobShowAdapter(getContext(),
                al_data,
                arrayOf(KEY_JOB_NAME, KEY_JOB_ACTIVE, KEY_JOB_DETAIL),
                intArrayOf(R.id.tv_job_name, R.id.tv_job_active, R.id.tv_job_detail))
        mLVJobs!!.adapter = mLVAdapter
    }


    /**
     * update ui
     */
    fun refreshFrg() {
        refreshCameraJobs()
    }

    /// BEGIN PRIVATE
    /**
     * update jobs status in UI
     */
    private fun refreshCameraJobs() {
        val lshm_jobs = ArrayList<HashMap<String, String>>()

        val dirs = FileUtil.getDirDirs(
                ContextUtil.instance.appPhotoRootDir!!, false)
        val ls_job = ContextUtil.getCameraJobUtility().allData
        if (!UtilFun.ListIsNullOrEmpty(ls_job)) {
            ls_job.sort(Comparator.comparingInt(ToIntFunction<CameraJob> { it.get_id() }))
            for (cj in ls_job) {
                alive_camerjob(lshm_jobs, cj)

                val dir = ContextUtil.instance.getCameraJobPhotoDir(cj._id)
                dirs.remove(dir)
            }
        }

        if (!UtilFun.ListIsNullOrEmpty(dirs)) {
            dirs.sort(Comparator { obj, anotherString -> obj.compareTo(anotherString) })

            for (dir in dirs) {
                died_camerajob(lshm_jobs, dir)
            }
        }

        updateData(lshm_jobs)
    }

    private fun died_camerajob(jobs: MutableList<HashMap<String, String>>, dir: String) {
        val cj = ContextUtil.instance.getCameraJobFromPath(dir) ?: return

        val jobname = cj.name + "(已移除)"
        val show = "可查看已拍摄图片\n可移除本任务文件"

        val map = HashMap<String, String>()
        map[KEY_JOB_NAME] = jobname
        map[KEY_JOB_ACTIVE] = ""
        map[KEY_JOB_DETAIL] = show
        map[KEY_ID] = Integer.toString(cj._id)
        map[KEY_STATUS] = EJobStatus.STOP.status
        map[KEY_TYPE] = DIED_JOB
        jobs.add(map)
    }

    private fun alive_camerjob(jobs: MutableList<HashMap<String, String>>, cj: CameraJob) {
        val at = String.format(Locale.CHINA, "%s/%s\n%s -\n%s", cj.type, cj.point, UtilFun.TimestampToString(cj.starttime).substring(0, 16), UtilFun.TimestampToString(cj.endtime).substring(0, 16))

        var jobname = cj.name
        val status = if (cj.status.job_status == EJobStatus.RUN.status)
            "运行"
        else
            "暂停"
        jobname = "$jobname($status)"

        val detail: String
        if (0 != cj.status.job_photo_count) {
            detail = String.format(Locale.CHINA, "已拍摄 : %d\n%s", cj.status.job_photo_count, UtilFun.TimestampToString(cj.status.ts))
        } else {
            detail = String.format(Locale.CHINA, "已拍摄 : %d", cj.status.job_photo_count)
        }

        val map = HashMap<String, String>()
        map[KEY_JOB_NAME] = jobname
        map[KEY_JOB_ACTIVE] = at
        map[KEY_JOB_DETAIL] = detail
        map[KEY_ID] = Integer.toString(cj._id)
        map[KEY_STATUS] = cj.status.job_status
        map[KEY_TYPE] = ALIVE_JOB
        jobs.add(map)
    }
    /// END PRIVATE

    /**
     * adapter for listview to show jobs status
     * Created by wxm on 2016/8/13.
     */
    inner class LVJobShowAdapter internal constructor(context: Context, data: List<Map<String, *>>,
                                                      from: Array<String>, to: IntArray) : SimpleAdapter(context, data, R.layout.li_job_show, from, to), View.OnClickListener {
        private var mRLCameraInfo: Array<RelativeLayout>? = null

        init {
            mRLCameraInfo = arrayOfNulls(data.size)
        }


        override fun notifyDataSetChanged() {
            super.notifyDataSetChanged()
            mRLCameraInfo = arrayOfNulls(count)
        }

        override fun getView(position: Int, view: View, arg2: ViewGroup): View? {
            val v = super.getView(position, view, arg2)
            if (null != v) {
                init_ui(v, position)

                mRLCameraInfo[position] = UtilFun.cast_t(v.findViewById(R.id.rl_camera_info))
                fill_camera_info(position)
            }

            return v
        }

        internal fun init_ui(v: View, position: Int) {
            val map = UtilFun.cast_t<HashMap<String, String>>(getItem(position))

            // for imagebutton
            val ib_play = v.findViewById<View>(R.id.ib_job_run_or_pause) as ImageButton
            val ib_delete = v.findViewById<View>(R.id.ib_job_stop) as ImageButton

            val status = map[KEY_STATUS]
            if (status == EJobStatus.RUN.status) {
                ib_play.visibility = View.VISIBLE

                ib_play.setBackgroundResource(R.drawable.ic_pause)
                ib_play.isClickable = true
                ib_play.setOnClickListener(this)
            } else if (status == EJobStatus.PAUSE.status) {
                ib_play.visibility = View.VISIBLE

                ib_play.setBackgroundResource(R.drawable.ic_start)
                ib_play.isClickable = true
                ib_play.setOnClickListener(this)
            } else {
                ib_play.visibility = View.INVISIBLE
                ib_play.isClickable = false
            }

            ib_delete.setOnClickListener(this)

            val ib_look = v.findViewById<View>(R.id.ib_job_look) as ImageButton
            val ib_slide = UtilFun.cast_t<ImageButton>(v.findViewById(R.id.ib_job_slide_look))
            val pp = ContextUtil.instance.getCameraJobPhotoDir(
                    Integer.parseInt(map[KEY_ID]))
            if (0 == FileUtil.getDirFilesCount(pp, "jpg", false)) {
                ib_look.visibility = View.INVISIBLE

                ib_slide.visibility = View.INVISIBLE
            } else {
                ib_look.visibility = View.VISIBLE
                ib_look.setOnClickListener(this)

                ib_slide.visibility = View.VISIBLE
                ib_slide.setOnClickListener(this)
            }
        }

        internal fun fill_camera_info(pos: Int) {
            val rl_hot = mRLCameraInfo!![pos]
            var rl = UtilFun.cast_t<RelativeLayout>(rl_hot.findViewById(R.id.rl_preview))
            rl.visibility = View.INVISIBLE
            rl = UtilFun.cast_t(rl_hot.findViewById(R.id.rl_setting))
            rl.visibility = View.INVISIBLE

            FrgCameraInfoHelper.refillLayout(rl_hot, PreferencesUtil.loadCameraParam())
        }

        override fun onClick(v: View) {
            val pos = mLVJobs!!.getPositionForView(v)
            val map = UtilFun.cast_t<HashMap<String, String>>(getItem(pos))

            when (v.id) {
                R.id.ib_job_stop -> {
                    val id = Integer.parseInt(map[KEY_ID])
                    val type = map[KEY_TYPE]
                    if (ALIVE_JOB == type) {
                        CameraJobUtility.removeCameraJob(id)
                    } else {
                        CameraJobUtility.deleteCameraJob(id)
                        refreshFrg()
                    }
                }

                R.id.ib_job_run_or_pause -> {
                    val id = Integer.parseInt(map[KEY_ID])
                    val cj = ContextUtil.getCameraJobUtility().getData(id)
                    if (null != cj) {
                        val cjs = cj.status
                        val sz_run = EJobStatus.RUN.status
                        val sz_pause = EJobStatus.PAUSE.status
                        cjs.job_status = if (cjs.job_status == sz_pause)
                            sz_run
                        else
                            sz_pause
                        ContextUtil.getCameraJobStatusUtility().modifyData(cjs)

                        refreshFrg()
                    }
                }

                R.id.ib_job_look -> {
                    val pp = ContextUtil.instance
                            .getCameraJobPhotoDir(
                                    Integer.parseInt(map[KEY_ID]))

                    val jg = JobGallery()
                    jg.openGallery(getActivity(), pp)
                }

                R.id.ib_job_slide_look -> {
                    val pp = ContextUtil.instance
                            .getCameraJobPhotoDir(
                                    Integer.parseInt(map[KEY_ID]))

                    val it = Intent(getActivity(), ACJobSlide::class.java)
                    it.putExtra(EAction.LOAD_PHOTO_DIR.actName, pp)
                    startActivityForResult(it, 1)
                }
            }
        }
    }

    companion object {
        val ALIVE_JOB = "alive"
        val DIED_JOB = "died"

        val KEY_JOB_NAME = "job_name"
        val KEY_JOB_DETAIL = "job_detail"
        val KEY_JOB_ACTIVE = "job_active"
        val KEY_STATUS = "key_status"
        val KEY_TYPE = "key_type"
        val KEY_ID = "key_id"
    }
}

