package com.wxm.camerajob.base.handler;

import android.util.Log;

import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.db.DBManager;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 处理job
 * Created by 123 on 2016/6/13.
 */
public class jobProcess {
    private final String TAG = "jobProcess";
    private int                     mInitFlag;
    private LinkedList<CameraJob>   mLsJob;
    private Lock                    mLsLock;

    public jobProcess() {
        mInitFlag = 0;
        mLsJob = new LinkedList<>();

        mLsLock = new ReentrantLock();
    }


    /**
     * 初始化函数
     * @param dbm db辅助类
     * @return  初始化成功返回true,否则返回false
     */
    public boolean init(DBManager dbm)  {
        boolean ret;
        if(1 == mInitFlag)  {
            ret = true;
            return ret;
        }

        mLsJob.addAll(dbm.GetJobs());

        mInitFlag = 1;
        ret = true;
        return ret;
    }


    /**
     * 任务处理器唤醒函数
     */
    public void processorWakeup()   {
        if(1 != mInitFlag)
            return;

        mLsLock.lock();
        for(CameraJob cj : mLsJob)  {
            jobWakeup(cj);
        }
        mLsLock.unlock();
    }


    /**
     * 添加camera job
     * @param cj   待添加job
     */
    public void addCameraJob(CameraJob cj)  {
        DBManager dbm = GlobalContext.getInstance().mDBManager;
        if(dbm.AddJob(cj))   {
            mLsLock.lock();
            mLsJob.clear();
            mLsJob.addAll(dbm.GetJobs());
            mLsLock.unlock();
        }
        else    {
            Log.e(TAG, "添加camera job失败，camerajob = " + cj.toString());
        }
    }

    /**
     * 执行job
     * @param cj
     */
    private void jobWakeup(CameraJob cj)    {
        switch (cj.job_type)    {
            case GlobalDef.CNSTR_JOBTYPE_MINUTELY : {
                process_mintuely_job(cj);
            }
            break;

            case GlobalDef.CNSTR_JOBTYPE_HOURLY : {
                process_hourly_job(cj);
            }
            break;

            case GlobalDef.CNSTR_JOBTYPE_DAILY : {
                process_daily_job(cj);
            }
            break;
        }
    }

    private void process_mintuely_job(CameraJob cj) {
        Calendar curCal = Calendar.getInstance();
        int cursec = curCal.get(Calendar.SECOND);
        boolean ik = false;
        switch (cj.job_point)   {
            case GlobalDef.CNSTR_EVERY_TEN_SECOND : {
                if(GlobalDef.INT_GLOBALJOB_CHECKPERIOD >= cursec % 10)
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_TWENTY_SECOND : {
                if(GlobalDef.INT_GLOBALJOB_CHECKPERIOD >= cursec % 20)
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_THIRTY_SECOND: {
                if(GlobalDef.INT_GLOBALJOB_CHECKPERIOD >= cursec % 30)
                    ik = true;
            }
            break;
        }

        if(ik)
            wakeupDuty(cj);
    }

    private void process_hourly_job(CameraJob cj) {
        Calendar curCal = Calendar.getInstance();
        int cursec = curCal.get(Calendar.SECOND);
        int curmin = curCal.get(Calendar.MINUTE);
        boolean ik = false;
        switch (cj.job_point)   {
            case GlobalDef.CNSTR_EVERY_TEN_MINUTE: {
                if((0 == curmin % 10)
                    && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD >= cursec))
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_TWENTY_MINUTE : {
                if((0 == curmin % 20)
                        && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD >= cursec))
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_THIRTY_MINUTE: {
                if((0 == curmin % 30)
                        && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD >= cursec))
                    ik = true;
            }
            break;
        }

        if(ik)
            wakeupDuty(cj);
    }


    private void process_daily_job(CameraJob cj) {
        Calendar curCal = Calendar.getInstance();
        int cursec = curCal.get(Calendar.SECOND);
        int curmin = curCal.get(Calendar.MINUTE);
        int curhou = curCal.get(Calendar.HOUR_OF_DAY);
        boolean ik = false;
        switch (cj.job_point)   {
            case GlobalDef.CNSTR_EVERY_ONE_HOUR: {
                if((0 == curmin)
                        && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD >= cursec))
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_TWO_HOUR : {
                if((0 == curhou % 2)
                        && (0 == curmin)
                        && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD >= cursec))
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_FOUR_HOUR: {
                if((0 == curhou % 4)
                        && (0 == curmin)
                        && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD >= cursec))
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_SIX_HOUR: {
                if((0 == curhou % 6)
                        && (0 == curmin)
                        && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD >= cursec))
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_EIGHT_HOUR: {
                if((0 == curhou % 8)
                        && (0 == curmin)
                        && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD >= cursec))
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_TWELVE_HOUR: {
                if((0 == curhou % 12)
                        && (0 == curmin)
                        && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD >= cursec))
                    ik = true;
            }
            break;
        }

        if(ik)
            wakeupDuty(cj);
    }


    private void wakeupDuty(CameraJob cj)   {
/*        Calendar curCal = Calendar.getInstance();
        Log.i(TAG,
                String.format("job (%s) wakeup at '%d-%02d-%02d %02d:%02d:%02d'",
                        cj.toString()
                        ,curCal.get(Calendar.YEAR)
                        ,curCal.get(Calendar.MONTH) + 1
                        ,curCal.get(Calendar.DAY_OF_MONTH) + 1
                        ,curCal.get(Calendar.HOUR_OF_DAY)
                        ,curCal.get(Calendar.MINUTE)
                        ,curCal.get(Calendar.SECOND)));*/

        Log.i(TAG, "wakeup job : " + cj.toString());
    }
}
