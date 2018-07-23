@file:Suppress("unused")

package com.wxm.camerajob.ui.job.detail

import android.os.Bundle
import android.widget.ImageView
import android.widget.ListView
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.*
import com.wxm.camerajob.utility.AppUtil
import kotterknife.bindView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import wxm.androidutil.improve.let1
import wxm.androidutil.improve.setImagePath
import wxm.androidutil.time.CalendarUtility
import wxm.androidutil.ui.frg.FrgSupportBaseAdv
import wxm.androidutil.ui.moreAdapter.MoreAdapter
import wxm.androidutil.ui.view.ViewHelper
import wxm.androidutil.ui.view.ViewHolder
import wxm.androidutil.util.FileUtil
import java.util.*


/**
 * fragment for show job
 * Created by WangXM on 2016/10/14.
 */
class FrgJobDetail : FrgSupportBaseAdv() {
    private val mLVJobs: ListView by bindView(R.id.gv_pic)

    override fun isUseEventBus(): Boolean = true
    override fun getLayoutID(): Int = R.layout.pg_job_detail

    private var mJobID = GlobalDef.INT_INVALID_ID
    private var mJobPath: String? = ""
    private val mJobPics = LinkedList<String>()

    /**
     * 数据库内数据变化处理器
     * @param event     事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDBDataChangeEvent(event: DBDataChangeEvent) {
        reloadUI()
    }

    override fun initUI(savedInstanceState: Bundle?) {
        arguments!!.let1 {
            mJobID = it.getInt(ACJobDetail.KEY_JOB_ID)
            mJobPath = it.getString(ACJobDetail.KEY_JOB_DIR)

            assert(GlobalDef.INT_INVALID_ID != mJobID || !mJobPath.isNullOrEmpty())
        }

        loadUI(savedInstanceState)
    }

    override fun loadUI(savedInstanceState: Bundle?) {
        if(GlobalDef.INT_INVALID_ID != mJobID)  {
            mJobPath = AppUtil.getCameraJobDir(mJobID)!!
            AppUtil.getCameraJobUtility().getData(mJobID)!!.let1 {
                aliveCameraJob(it)
            }
        } else  {
            diedCameraJob(mJobPath!!)
        }

        mJobPics.addAll(FileUtil.getDirFiles(mJobPath!!, "jpg", false))
    }

    /// BEGIN PRIVATE
    private fun diedCameraJob(dir: String) {
        val cj = AppUtil.getCameraJobFromDir(dir) ?: return
        HashMap<String, String>().let1 {
            it[KEY_JOB_NAME] = cj.name + "(已移除)"
            it[KEY_JOB_TYPE] = ""
            it[KEY_JOB_START_END_DATE] = ""
            it[KEY_PHOTO_COUNT] = "可查看已拍摄图片"
            it[KEY_PHOTO_LAST_TIME] = "可移除此任务"
            it[KEY_ID] = Integer.toString(cj._id)
            it[KEY_STATUS] = EJobStatus.STOP.status
            it[KEY_TYPE] = DIED_JOB

            doShow(it)
        }
    }

    private fun aliveCameraJob(cj: CameraJob) {
        val at = CalendarUtility.YearMonthDayHourMinute.let {
            context!!.getString(R.string.fs_start_end_date, it.format(cj.starttime), it.format(cj.endtime))
        }

        val jobName = (if (cj.status.job_status == EJobStatus.RUN.status) "运行" else "暂停").let {
            "${cj.name}($it)"
        }

        val detail = if (0 != cj.status.job_photo_count)
            context!!.getString(R.string.fs_photo_last, CalendarUtility.Full.format(cj.status.ts))
        else ""

        HashMap<String, String>().let1 {
            it[KEY_JOB_NAME] = jobName
            it[KEY_JOB_TYPE] = context!!.getString(R.string.fs_job_type, cj.type, cj.point)
            it[KEY_JOB_START_END_DATE] = at
            it[KEY_PHOTO_COUNT] = context!!.getString(R.string.fs_photo_count, cj.status.job_photo_count)
            it[KEY_PHOTO_LAST_TIME] = detail
            it[KEY_ID] = Integer.toString(cj._id)
            it[KEY_STATUS] = cj.status.job_status!!
            it[KEY_TYPE] = ALIVE_JOB

            doShow(it)
        }
    }

    private fun doShow(hm: Map<String, String>) {
        ViewHelper(view!!).let1 {
            it.setText(R.id.tv_job_name, hm[KEY_JOB_NAME]!!)
            it.setText(R.id.tv_job_type, hm[KEY_JOB_TYPE]!!)
            it.setText(R.id.tv_job_date, hm[KEY_JOB_START_END_DATE]!!)
            it.setText(R.id.tv_phtot_count, hm[KEY_PHOTO_COUNT]!!)
            it.setText(R.id.tv_photo_last_time, hm[KEY_PHOTO_LAST_TIME]!!)
        }
    }
    /// END PRIVATE

    /**
     * adapter for listview to show jobs status
     * Created by wxm on 2016/8/13.
     */
    inner class LVJobShowAdapter(data: List<Map<String, String>>)
        : MoreAdapter(context!!, data, R.layout.gi_pic) {
        override fun loadView(pos: Int, vhHolder: ViewHolder) {
            @Suppress("UNCHECKED_CAST")
            val hm = (getItem(pos) as Map<String, String>)[KEY_PIC_PATH]!!
            vhHolder.getView<ImageView>(R.id.iv_pic)!!.setImagePath(hm)
        }
    }

    companion object {
        private const val KEY_PIC_PATH = "pic_path"

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

