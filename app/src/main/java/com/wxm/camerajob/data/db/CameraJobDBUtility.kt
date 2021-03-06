package com.wxm.camerajob.data.db

import com.j256.ormlite.dao.RuntimeExceptionDao
import com.wxm.camerajob.data.entity.CameraJob

import org.greenrobot.eventbus.EventBus
import wxm.androidutil.db.DBUtilityBase

/**
 * database class for CameraJob
 * Created by WangXM on 2016/6/16.
 */
class CameraJobDBUtility(private val mHelper: DBOrmLiteHelper) : DBUtilityBase<CameraJob, Int>() {


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

