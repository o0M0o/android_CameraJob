package com.wxm.camerajob.data.db

import com.j256.ormlite.dao.RuntimeExceptionDao
import com.wxm.camerajob.data.define.CameraJob
import com.wxm.camerajob.data.define.DBDataChangeEvent
import com.wxm.camerajob.data.define.EJobStatus

import org.greenrobot.eventbus.EventBus
import wxm.androidutil.DBHelper.DBUtilityBase

/**
 * database class for CameraJob
 * Created by 123 on 2016/6/16.
 */
class CameraJobDBUtility(private val mHelper: DBOrmLiteHelper) : DBUtilityBase<CameraJob, Int>() {
    /**
     * get amount for activity jobs
     * @return  amount for live jobs
     */
    fun getActiveJobCount(): Int {
        return allData.filter { it.status.job_status == EJobStatus.RUN.status }.count()
    }

    override fun getDBHelper(): RuntimeExceptionDao<CameraJob, Int> {
        return mHelper.cameraJobREDao
    }

    override fun onDataModify(md: List<Int>) {
        DBDataChangeEvent(DBDataChangeEvent.DATA_JOB,
                DBDataChangeEvent.EVENT_MODIFY).let {
            EventBus.getDefault().post(it)
        }
    }

    override fun onDataCreate(cd: List<Int>) {
        DBDataChangeEvent(DBDataChangeEvent.DATA_JOB,
                DBDataChangeEvent.EVENT_CREATE).let {
            EventBus.getDefault().post(it)
        }
    }

    override fun onDataRemove(dd: List<Int>) {
        DBDataChangeEvent(DBDataChangeEvent.DATA_JOB,
                DBDataChangeEvent.EVENT_REMOVE).let {
            EventBus.getDefault().post(it)
        }
    }
}

