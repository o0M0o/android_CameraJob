package com.wxm.camerajob.data.db;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.wxm.camerajob.data.define.CameraJobStatus;
import com.wxm.camerajob.data.define.DBDataChangeEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import wxm.androidutil.DBHelper.DBUtilityBase;


/**
 * 辅助处理camerajob的状态
 * Created by 123 on 2016/6/16.
 */
public class CameraJobStatusDBUtility
        extends DBUtilityBase<CameraJobStatus, Integer> {
    private DBOrmLiteHelper mHelper;

    public CameraJobStatusDBUtility(DBOrmLiteHelper helper)  {
        mHelper = helper;
    }


    @Override
    protected void onDataModify(List<Integer> list) {
        DBDataChangeEvent de = new DBDataChangeEvent(DBDataChangeEvent.DATA_JOB_STATUS,
                DBDataChangeEvent.EVENT_MODIFY);
        EventBus.getDefault().post(de);
    }

    @Override
    protected void onDataCreate(List<Integer> list) {
        //DBDataChangeEvent de = new DBDataChangeEvent(DBDataChangeEvent.DATA_JOB_STATUS,
        //        DBDataChangeEvent.EVENT_CREATE);
        //EventBus.getDefault().post(de);
    }

    @Override
    protected void onDataRemove(List<Integer> list) {
        //DBDataChangeEvent de = new DBDataChangeEvent(DBDataChangeEvent.DATA_JOB_STATUS,
        //        DBDataChangeEvent.EVENT_REMOVE);
        //EventBus.getDefault().post(de);
    }

    @Override
    protected RuntimeExceptionDao<CameraJobStatus, Integer> getDBHelper() {
        return mHelper.getCamerJobStatusREDao();
    }
}
