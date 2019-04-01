@file:Suppress("unused")

package com.wxm.camerajob.ui.job.show

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import com.wxm.camerajob.R
import com.wxm.camerajob.data.db.DBDataChangeEvent
import com.wxm.camerajob.data.define.*
import com.wxm.camerajob.data.entity.CameraJob
import com.wxm.camerajob.preference.PreferencesChangeEvent
import com.wxm.camerajob.ui.job.detail.ACJobDetail
import com.wxm.camerajob.App
import kotterknife.bindView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import wxm.androidutil.improve.let1
import wxm.androidutil.time.CalendarUtility
import wxm.androidutil.ui.frg.FrgSupportBaseAdv
import wxm.androidutil.ui.moreAdapter.MoreAdapter
import wxm.androidutil.ui.view.ViewHolder
import wxm.androidutil.util.FileUtil
import java.util.*


/**
 * fragment for show job
 * Created by WangXM on 2016/10/14.
 */
class FrgJobShow : FrgSupportBaseAdv() {
    private val mLVJobs: ListView by bindView(R.id.lv_job)

    override fun isUseEventBus(): Boolean = true
    override fun getLayoutID(): Int = R.layout.pg_job_show

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDBDataChangeEvent(@Suppress("UNUSED_PARAMETER") event: DBDataChangeEvent) {
        reloadUI()
    }

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
        val dirs = FileUtil.getDirDirs(App.getPhotoRootDir(), false)
        ArrayList<HashMap<String, String>>().apply {
            App.getCameraJobHelper().getAllJob().forEach {
                aliveCameraJob(this, it)
                App.getCameraJobDir(it._id)?.let {path ->
                    dirs.remove(path)
                }
            }

            dirs.sorted().forEach {
                diedCameraJob(this, it)
            }
        }.let {
            mLVJobs.adapter = LVJobShowAdapter(this, it,
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
        val cj = App.getCameraJobFromDir(dir) ?: return
        HashMap<String, String>().let1 {
            it[KEY_JOB_NAME] = cj.name + "(已移除)"
            it[KEY_JOB_TYPE] = ""
            it[KEY_JOB_START_END_DATE] = ""
            it[KEY_PHOTO_COUNT] = "可查看已拍摄图片"
            it[KEY_PHOTO_LAST_TIME] = "可移除此任务"
            it[KEY_ID] = Integer.toString(cj._id)
            it[KEY_STATUS] = EJobStatus.STOP.status
            it[KEY_TYPE] = DIED_JOB
            it[KEY_JOB_DIR] = dir
            jobs.add(it)
        }
    }

    private fun aliveCameraJob(jobs: MutableList<HashMap<String, String>>, cj: CameraJob) {
        val at = CalendarUtility.YearMonthDayHourMinute.let {
            context!!.getString(R.string.fs_start_end_date, it.format(cj.startTime), it.format(cj.endTime))
        }

        val jobName = (if (cj.status == EJobStatus.RUN.status) "运行" else "暂停").let {
            "${cj.name}($it)"
        }

        val detail = if (0 != cj.photoCount)
            context!!.getString(R.string.fs_photo_last, CalendarUtility.Full.format(cj.lastPhotoTime))
        else ""


        HashMap<String, String>().let1 {
            it[KEY_JOB_NAME] = jobName
            it[KEY_JOB_TYPE] = context!!.getString(R.string.fs_job_type, cj.type, cj.point)
            it[KEY_JOB_START_END_DATE] = at
            it[KEY_PHOTO_COUNT] = context!!.getString(R.string.fs_photo_count, cj.photoCount)
            it[KEY_PHOTO_LAST_TIME] = detail
            it[KEY_ID] = Integer.toString(cj._id)
            it[KEY_STATUS] = cj.status
            it[KEY_TYPE] = ALIVE_JOB
            jobs.add(it)
        }
    }
    /// END PRIVATE

    /**
     * adapter for jobs status
     */
    inner class LVJobShowAdapter(private val mFrgHome: FrgJobShow, data: List<Map<String, String>>, fromKey: Array<String?>, toId: IntArray)
        : MoreAdapter(context!!, data, R.layout.li_job_show, fromKey, toId) {
        override fun loadView(pos: Int, vhHolder: ViewHolder) {
            @Suppress("UNCHECKED_CAST")
            val hm = getItem(pos) as Map<String, String>
            vhHolder.convertView.setOnClickListener {
                mFrgHome.startActivityForResult(
                        Intent(mContext, ACJobDetail::class.java).apply {
                            if(ALIVE_JOB == hm[KEY_TYPE])   {
                                putExtra(ACJobDetail.KEY_JOB_ID, Integer.parseInt(hm.getValue(KEY_ID)))
                            } else  {
                                putExtra(ACJobDetail.KEY_JOB_DIR, Integer.parseInt(hm.getValue(KEY_JOB_DIR)))
                            }
                        },
                        1)
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
        const val KEY_JOB_DIR = "job_dir"
    }
}

