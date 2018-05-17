@file:Suppress("unused")

package com.wxm.camerajob.ui.Job.JobShow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.SimpleAdapter
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.*
import com.wxm.camerajob.ui.Base.FrgCameraInfoHelper
import com.wxm.camerajob.ui.Base.JobGallery
import com.wxm.camerajob.ui.Job.JobSlide.ACJobSlide
import com.wxm.camerajob.utility.CalendarUtility
import com.wxm.camerajob.utility.CameraJobUtility
import com.wxm.camerajob.utility.ContextUtil
import kotterknife.bindView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import wxm.androidutil.FrgUtility.FrgSupportBaseAdv
import wxm.androidutil.util.FileUtil
import wxm.androidutil.util.UtilFun
import java.util.*


/**
 * fragment for show job
 * Created by WangXM on 2016/10/14.
 */
class FrgJobShow : FrgSupportBaseAdv() {
    private val mLVJobs: ListView by bindView(R.id.aclv_start_jobs)
    private val mTimer: Timer = Timer()

    override fun isUseEventBus(): Boolean = true
    override fun getLayoutID(): Int = R.layout.vw_job_show

    /**
     * 数据库内数据变化处理器
     * @param event     事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDBDataChangeEvent(event: DBDataChangeEvent) {
        (if (DBDataChangeEvent.EVENT_CREATE == event.eventType) 1200 else 0).toLong().let {
            Handler().postDelayed({ this.reloadUI() }, it)
        }
    }

    /**
     * 配置变化处理器
     * @param event     事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPreferencesChangeEvent(event: PreferencesChangeEvent) {
        if (GlobalDef.STR_CAMERAPROPERTIES_NAME == event.attrName) {
            reloadUI()
        }
    }

    override fun onDetach() {
        super.onDetach()
        mTimer.cancel()
    }

    override fun initUI(savedInstanceState: Bundle?) {
        // for listview
        mLVJobs.adapter = LVJobShowAdapter(context, ArrayList<HashMap<String, String>>(),
                arrayOf(KEY_JOB_NAME, KEY_JOB_ACTIVE, KEY_JOB_DETAIL),
                intArrayOf(R.id.tv_job_name, R.id.tv_job_active, R.id.tv_job_detail))

        val h = this
        mTimer.schedule(object : TimerTask() {
            override fun run() = h.activity.runOnUiThread({ h.loadUI(null) })
        }, 100, 3000)

        val dirs = FileUtil.getDirDirs(ContextUtil.getPhotoRootDir(), false)
        ArrayList<HashMap<String, String>>().apply {
            ContextUtil.getCameraJobUtility().allData.filterNotNull().sortedBy { it._id }.forEach {
                aliveCameraJob(this, it)
                ContextUtil.getCameraJobDir(it._id).let {
                    dirs.remove(it)
                }
            }

            dirs.filterNotNull().sorted().forEach {
                diedCameraJob(this, it)
            }
        }.let {
            updateData(it)
        }
    }

    override fun loadUI(savedInstanceState: Bundle?) {
    }

    /**
     * 更新数据
     * @param lsData 新数据
     */
    private fun updateData(lsData: List<HashMap<String, String>>) {
        mLVJobs.adapter = LVJobShowAdapter(context,
                ArrayList<HashMap<String, String>>().apply { addAll(lsData) },
                arrayOf(KEY_JOB_NAME, KEY_JOB_ACTIVE, KEY_JOB_DETAIL),
                intArrayOf(R.id.tv_job_name, R.id.tv_job_active, R.id.tv_job_detail))
    }


    /// BEGIN PRIVATE

    private fun diedCameraJob(jobs: MutableList<HashMap<String, String>>, dir: String) {
        val cj = ContextUtil.getCameraJobFromDir(dir) ?: return
        HashMap<String, String>().let {
            it[KEY_JOB_NAME] = cj.name + "(已移除)"
            it[KEY_JOB_ACTIVE] = ""
            it[KEY_JOB_DETAIL] = "可查看已拍摄图片\n可移除本任务文件"
            it[KEY_ID] = Integer.toString(cj._id)
            it[KEY_STATUS] = EJobStatus.STOP.status
            it[KEY_TYPE] = DIED_JOB
            jobs.add(it)

            Unit
        }
    }

    private fun aliveCameraJob(jobs: MutableList<HashMap<String, String>>, cj: CameraJob) {
        val at = String.format(Locale.CHINA, "%s/%s\n%s -\n%s",
                cj.type, cj.point,
                CalendarUtility.getYearMonthDayHourMinuteStr(cj.starttime.time),
                CalendarUtility.getYearMonthDayHourMinuteStr(cj.endtime.time))

        val status = if (cj.status.job_status == EJobStatus.RUN.status) "运行" else "暂停"
        val jobName = "${cj.name}($status)"

        val detail = if (0 != cj.status.job_photo_count) {
            String.format(Locale.CHINA, "已拍摄 : %d\n%s",
                    cj.status.job_photo_count, UtilFun.TimestampToString(cj.status.ts))
        } else {
            String.format(Locale.CHINA, "已拍摄 : %d",
                    cj.status.job_photo_count)
        }

        HashMap<String, String>().let {
            it[KEY_JOB_NAME] = jobName
            it[KEY_JOB_ACTIVE] = at
            it[KEY_JOB_DETAIL] = detail
            it[KEY_ID] = Integer.toString(cj._id)
            it[KEY_STATUS] = cj.status.job_status!!
            it[KEY_TYPE] = ALIVE_JOB
            jobs.add(it)

            Unit
        }
    }
    /// END PRIVATE

    /**
     * adapter for listview to show jobs status
     * Created by wxm on 2016/8/13.
     */
    inner class LVJobShowAdapter internal constructor(context: Context, data: List<Map<String, *>>,
                                                      from: Array<String>, to: IntArray) : SimpleAdapter(context, data, R.layout.li_job_show, from, to), View.OnClickListener {
        private var mRLCameraInfo: Array<RelativeLayout?> = arrayOfNulls(data.size)

        override fun notifyDataSetChanged() {
            super.notifyDataSetChanged()
            mRLCameraInfo = arrayOfNulls(count)
        }

        override fun getView(position: Int, view: View, arg2: ViewGroup): View? {
            val v = super.getView(position, view, arg2)
            if (null != v) {
                initUI(v, position)

                mRLCameraInfo[position] = v.findViewById(R.id.rl_camera_info)!!
                fillCameraInfo(position)
            }

            return v
        }

        private fun initUI(v: View, position: Int) {
            val map = UtilFun.cast_t<HashMap<String, String>>(getItem(position))

            // for imagebutton
            val ibPlay = v.findViewById<ImageButton>(R.id.ib_job_run_or_pause)
            val ibDelete = v.findViewById<ImageButton>(R.id.ib_job_stop)

            when (map[KEY_STATUS]) {
                EJobStatus.RUN.status -> {
                    ibPlay.visibility = View.VISIBLE

                    ibPlay.setBackgroundResource(R.drawable.ic_pause)
                    ibPlay.isClickable = true
                    ibPlay.setOnClickListener(this)
                }
                EJobStatus.PAUSE.status -> {
                    ibPlay.visibility = View.VISIBLE

                    ibPlay.setBackgroundResource(R.drawable.ic_start)
                    ibPlay.isClickable = true
                    ibPlay.setOnClickListener(this)
                }
                else -> {
                    ibPlay.visibility = View.INVISIBLE
                    ibPlay.isClickable = false
                }
            }

            ibDelete.setOnClickListener(this)

            val ibLook = v.findViewById<ImageButton>(R.id.ib_job_look)
            val ibSlide = v.findViewById<ImageButton>(R.id.ib_job_slide_look)
            val pp = ContextUtil.getCameraJobDir(Integer.parseInt(map[KEY_ID]))
            if (0 == FileUtil.getDirFilesCount(pp, "jpg", false)) {
                ibLook.visibility = View.INVISIBLE

                ibSlide.visibility = View.INVISIBLE
            } else {
                ibLook.visibility = View.VISIBLE
                ibLook.setOnClickListener(this)

                ibSlide.visibility = View.VISIBLE
                ibSlide.setOnClickListener(this)
            }
        }

        private fun fillCameraInfo(pos: Int) {
            mRLCameraInfo[pos]?.let {
                it.findViewById<RelativeLayout>(R.id.rl_preview).visibility = View.INVISIBLE
                it.findViewById<View>(R.id.rl_setting).visibility = View.INVISIBLE

                FrgCameraInfoHelper.refillLayout(it, PreferencesUtil.loadCameraParam())
            }
        }

        override fun onClick(v: View) {
            val pos = mLVJobs.getPositionForView(v)
            val map = UtilFun.cast_t<HashMap<String, String>>(getItem(pos))
            val id = Integer.parseInt(map[KEY_ID])
            when (v.id) {
                R.id.ib_job_stop -> {
                    if (ALIVE_JOB == map[KEY_TYPE]) {
                        CameraJobUtility.removeCameraJob(id)
                    } else {
                        CameraJobUtility.deleteCameraJob(id)
                        reloadUI()
                    }
                }

                R.id.ib_job_run_or_pause -> {
                    val cj = ContextUtil.getCameraJobUtility().getData(id)
                    if (null != cj) {
                        cj.status.let {
                            it.job_status = if (it.job_status == EJobStatus.PAUSE.status) EJobStatus.RUN.status
                            else EJobStatus.PAUSE.status
                            ContextUtil.getCameraJobStatusUtility().modifyData(it)

                            Unit
                        }
                        reloadUI()
                    }
                }

                R.id.ib_job_look -> {
                    ContextUtil.getCameraJobDir(id)?.let {
                        JobGallery().openGallery(activity, it)
                    }
                }

                R.id.ib_job_slide_look -> {
                    Intent(activity, ACJobSlide::class.java).let {
                        it.putExtra(EAction.LOAD_PHOTO_DIR.actName, ContextUtil.getCameraJobDir(id)!!)
                        startActivityForResult(it, 1)
                    }
                }
            }
        }
    }

    companion object {
        const val ALIVE_JOB = "alive"
        const val DIED_JOB = "died"

        const val KEY_JOB_NAME = "job_name"
        const val KEY_JOB_DETAIL = "job_detail"
        const val KEY_JOB_ACTIVE = "job_active"
        const val KEY_STATUS = "key_status"
        const val KEY_TYPE = "key_type"
        const val KEY_ID = "key_id"
    }
}

