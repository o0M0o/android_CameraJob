package com.wxm.camerajob.data.db;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.wxm.camerajob.data.define.CameraJob;
import com.wxm.camerajob.data.define.DBDataChangeEvent;
import com.wxm.camerajob.data.define.EJobStatus;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import wxm.androidutil.DBHelper.DBUtilityBase;


/**
 * database class for CameraJob
 * Created by 123 on 2016/6/16.
 */
public class CameraJobDBUtility
        extends DBUtilityBase<CameraJob, Integer> {
    private DBOrmLiteHelper mHelper;

    public CameraJobDBUtility(DBOrmLiteHelper helper) {
        mHelper = helper;
    }

    /**
     * get amount for activity jobs
     * @return  amount for live jobs
     */
    public int GetActiveJobCount() {
        int count = 0;
        List<CameraJob> lj = getAllData();
        String sz_run = EJobStatus.RUN.getStatus();
        for (CameraJob cj : lj) {
            if (cj.getStatus().getJob_status().equals(sz_run))
                count++;
        }

        return count;
    }

    @Override
    protected RuntimeExceptionDao<CameraJob, Integer> getDBHelper() {
        return mHelper.getCamerJobREDao();
    }

    @Override
    protected void onDataModify(List<Integer> md) {
        DBDataChangeEvent de = new DBDataChangeEvent(DBDataChangeEvent.DATA_JOB,
                DBDataChangeEvent.EVENT_MODIFY);
        EventBus.getDefault().post(de);
    }

    @Override
    protected void onDataCreate(List<Integer> cd) {
        DBDataChangeEvent de = new DBDataChangeEvent(DBDataChangeEvent.DATA_JOB,
                DBDataChangeEvent.EVENT_CREATE);
        EventBus.getDefault().post(de);
    }

    @Override
    protected void onDataRemove(List<Integer> dd) {
        DBDataChangeEvent de = new DBDataChangeEvent(DBDataChangeEvent.DATA_JOB,
                DBDataChangeEvent.EVENT_REMOVE);
        EventBus.getDefault().post(de);
    }
}

