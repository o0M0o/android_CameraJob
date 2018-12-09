package com.wxm.camerajob.data.db

import com.j256.ormlite.dao.RuntimeExceptionDao
import com.wxm.camerajob.data.entity.CameraJobStatus

import org.greenrobot.eventbus.EventBus
import wxm.androidutil.db.DBUtilityBase


/**
 * process status for camera job
 * Created by WangXM on 2016/6/16.
 */
class CameraJobStatusDBUtility(private val mHelper: DBOrmLiteHelper) : DBUtilityBase<CameraJobStatus, Int>() {
    override fun onDataModify(list: List<Int>) {
        DBDataChangeEvent(DBDataChangeEvent.DATA_JOB_STATUS,
                DBDataChangeEvent.EVENT_MODIFY).let {
            EventBus.getDefault().post(it)
        }
    }

    override fun onDataCreate(list: List<Int>) {
        //DBDataChangeEvent de = new DBDataChangeEvent(DBDataChangeEvent.DATA_JOB_STATUS,
        //        DBDataChangeEvent.EVENT_CREATE);
        //EventBus.getDefault().post(de);
    }

    override fun onDataRemove(list: List<Int>) {
        //DBDataChangeEvent de = new DBDataChangeEvent(DBDataChangeEvent.DATA_JOB_STATUS,
        //        DBDataChangeEvent.EVENT_REMOVE);
        //EventBus.getDefault().post(de);
    }

    override fun getDBHelper(): RuntimeExceptionDao<CameraJobStatus, Int> {
        return mHelper.cameraJobStatusREDao
    }
}
