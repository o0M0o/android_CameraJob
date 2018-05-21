@file:Suppress("unused")

package com.wxm.camerajob.ui.Job.show

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageButton
import android.widget.ListView
import android.widget.RelativeLayout
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.*
import com.wxm.camerajob.ui.Job.slide.ACJobSlide
import com.wxm.camerajob.ui.base.FrgCameraInfoHelper
import com.wxm.camerajob.ui.base.JobGallery
import com.wxm.camerajob.utility.CalendarUtility
import com.wxm.camerajob.utility.job.CameraJobUtility
import com.wxm.camerajob.utility.context.ContextUtil
import kotterknife.bindView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import wxm.androidutil.FrgUtility.FrgSupportBaseAdv
import wxm.androidutil.MoreAdapter.MoreAdapter
import wxm.androidutil.ViewHolder.ViewHolder
import wxm.androidutil.util.FileUtil
import wxm.androidutil.util.UtilFun
import java.util.*


/**
 * fragment for show job
 * Created by WangXM on 2016/10/14.
 */
class FrgJobShow : FrgSupportBaseAdv() {
    private val mLVJobs: ListView by bindView(R.id.aclv_start_jobs)

    override fun isUseEventBus(): Boolean = true
    override fun getLayoutID(): Int = R.layout.vw_job_show

    /**
     * 数据库内数据变化处理器
     * @param event     事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDBDataChangeEvent(event: DBDataChangeEvent) {
        reloadUI()
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

    override fun initUI(savedInstanceState: Bundle?) {
        loadUI(savedInstanceState)
    }

    override fun loadUI(savedInstanceState: Bundle?) {
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
            mLVJobs.adapter = LVJobShowAdapter(it,
                    arrayOf(KEY_JOB_NAME,
                            KEY_JOB_TYPE, KEY_JOB_START_END_DATE,
                            KEY_PHOTO_COUNT, KEY_PHOTO_LAST_TIME),
                    intArrayOf(R.id.tv_job_name,
                            R.id.tv_job_type, R.id.tv_job_date,
                            R.id.tv_phtot_count, R.id.tv_photo_last_time))
        }
    }

    /// BEGIN PRIVATE
    private fun diedCameraJob(jobs: MutableList<HashMap<String, String>>, dir: String) {
        val cj = ContextUtil.getCameraJobFromDir(dir) ?: return
        HashMap<String, String>().let {
            it[KEY_JOB_NAME] = cj.name + "(已移除)"
            it[KEY_JOB_TYPE] = ""
            it[KEY_JOB_START_END_DATE] = ""
            it[KEY_PHOTO_COUNT] = "可查看已拍摄图片"
            it[KEY_PHOTO_LAST_TIME] = "可移除此任务"
            it[KEY_ID] = Integer.toString(cj._id)
            it[KEY_STATUS] = EJobStatus.STOP.status
            it[KEY_TYPE] = DIED_JOB
            jobs.add(it)

            Unit
        }
    }

    private fun aliveCameraJob(jobs: MutableList<HashMap<String, String>>, cj: CameraJob) {
        val at = CalendarUtility.YearMonthDayHourMinute.let {
            context.getString(R.string.fs_start_end_date, it.getStr(cj.starttime), it.getStr(cj.endtime))
        }

        val jobName = (if (cj.status.job_status == EJobStatus.RUN.status) "运行" else "暂停").let {
            "${cj.name}($it)"
        }

        val detail = if (0 != cj.status.job_photo_count)
            context.getString(R.string.fs_photo_last, CalendarUtility.Full.getStr(cj.status.ts))
        else ""


        HashMap<String, String>().let {
            it[KEY_JOB_NAME] = jobName
            it[KEY_JOB_TYPE] = context.getString(R.string.fs_job_type, cj.type, cj.point)
            it[KEY_JOB_START_END_DATE] = at
            it[KEY_PHOTO_COUNT] = context.getString(R.string.fs_photo_count, cj.status.job_photo_count)
            it[KEY_PHOTO_LAST_TIME] = detail
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
    inner class LVJobShowAdapter(data: List<Map<String, String>>, fromKey: Array<String?>, toId: IntArray)
        : MoreAdapter(context, data, R.layout.li_job_show, fromKey, toId), View.OnClickListener {
        private var mRLCameraInfo: Array<RelativeLayout?> = arrayOfNulls(data.size)

        override fun notifyDataSetChanged() {
            super.notifyDataSetChanged()
            mRLCameraInfo = arrayOfNulls(count)
        }

        override fun loadView(pos: Int, vhHolder: ViewHolder) {
            val map = UtilFun.cast_t<HashMap<String, String>>(getItem(pos))
            initButton(vhHolder, map)
            vhHolder.getView<RelativeLayout>(R.id.rl_camera_info)!!.let {
                mRLCameraInfo[pos] = it
                fillCameraInfo(it, map)
            }
        }

        private fun initButton(vhHolder: ViewHolder, map: Map<String, String>) {
            val ibPlay: ImageButton = vhHolder.getView(R.id.ib_job_run_or_pause)
            val ibDelete: ImageButton = vhHolder.getView(R.id.ib_job_stop)

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

            val ibLook: ImageButton = vhHolder.getView(R.id.ib_job_look)
            val ibSlide: ImageButton = vhHolder.getView(R.id.ib_job_slide_look)
            if (0 == FileUtil.getDirFilesCount(ContextUtil.getCameraJobDir(Integer.parseInt(map[KEY_ID])),
                            "jpg", false)) {
                ibLook.visibility = View.INVISIBLE

                ibSlide.visibility = View.INVISIBLE
            } else {
                ibLook.visibility = View.VISIBLE
                ibLook.setOnClickListener(this)

                ibSlide.visibility = View.INVISIBLE
                //ibSlide.visibility = View.VISIBLE
                //ibSlide.setOnClickListener(this)
            }
        }

        private fun fillCameraInfo(rl: RelativeLayout, map: Map<String, String>) {
            map[KEY_JOB_TYPE].isNullOrBlank().let {
                if (it) {
                    rl.visibility = View.GONE
                } else {
                    rl.visibility = View.VISIBLE

                    rl.findViewById<RelativeLayout>(R.id.rl_preview).visibility = View.INVISIBLE
                    rl.findViewById<View>(R.id.rl_setting).visibility = View.INVISIBLE

                    FrgCameraInfoHelper.refillLayout(rl, PreferencesUtil.loadCameraParam())
                }
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

        const val KEY_JOB_TYPE = "job_type"
        const val KEY_JOB_START_END_DATE = "job_start_end_date"

        const val KEY_PHOTO_COUNT = "key_photo_count"
        const val KEY_PHOTO_LAST_TIME = "key_photo_last_time"

        const val KEY_STATUS = "key_status"
        const val KEY_TYPE = "key_type"
        const val KEY_ID = "key_id"
    }
}

