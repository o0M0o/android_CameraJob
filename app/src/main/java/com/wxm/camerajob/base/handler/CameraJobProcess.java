package com.wxm.camerajob.base.handler;

import android.util.Log;

import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.CameraJobStatus;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.data.TakePhotoParam;
import com.wxm.camerajob.base.db.DBManager;
import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.base.utility.FileLogger;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 处理job
 * Created by 123 on 2016/6/13.
 */
public class CameraJobProcess {
    private final String TAG = "CameraJobProcess";
    private int                     mInitFlag;
    private LinkedList<CameraJob>   mLsJob;
    private Lock                    mLsJobLock;

    private LinkedList<CameraJobStatus>   mLsJobStatus;
    private Lock                          mLsJobStatusLock;

    public CameraJobProcess() {
        mInitFlag       = 0;
        mLsJob          = new LinkedList<>();
        mLsJobStatus    = new LinkedList<>();

        mLsJobLock          = new ReentrantLock();
        mLsJobStatusLock    = new ReentrantLock();
    }


    /**
     * 初始化函数
     * @param dbm db辅助类
     * @return  初始化成功返回true,否则返回false
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean init(DBManager dbm)  {
        if(1 == mInitFlag)  {
            return true;
        }

        mLsJob.addAll(dbm.mCameraJobHelper.GetJobs());
        mLsJobStatus.addAll(dbm.mCameraJobStatusHelper.GetAllJobStatus());

        mInitFlag = 1;
        return true;
    }


    /**
     * 任务处理器唤醒函数
     */
    public void processorWakeup()   {
        if(1 != mInitFlag)
            return;

        List<CameraJob> ls = GetAllJobs();
        for(CameraJob cj : ls)  {
            jobWakeup(cj);
        }
    }


    /**
     * 添加camera job
     * @param cj   待添加job
     * @return  新camerajob id
     */
    public int addCameraJob(CameraJob cj)  {
        int ret = GlobalDef.INT_INVALID_ID;
        mLsJobLock.lock();

        boolean repeat = false;
        for(CameraJob ij : mLsJob)  {
            if(ij.job_name.equals(cj.job_name)) {
                repeat = true;
                break;
            }
        }

        if(!repeat) {
            DBManager dbm = GlobalContext.getInstance().mDBManager;
            if(dbm.mCameraJobHelper.AddJob(cj)) {
                mLsJob.clear();
                mLsJob.addAll(dbm.mCameraJobHelper.GetJobs());

                int new_id = GlobalDef.INT_INVALID_ID;
                for (CameraJob ij : mLsJob) {
                    if (ij.job_name.equals(cj.job_name)) {
                        new_id = ij._id;
                        break;
                    }
                }
                mLsJobLock.unlock();


                if (GlobalDef.INT_INVALID_ID != new_id) {
                    ret = new_id;

                    CameraJobStatus cjs = new CameraJobStatus();
                    cjs.camerjob_id = new_id;
                    cjs.camerajob_status = GlobalDef.STR_CAMERAJOB_RUN;
                    addCameraJobStatus(cjs);
                }
            }
            else    {
                Log.e(TAG, "添加camera job失败，camerajob = " + cj.toString());
            }
        }
        else    {
            mLsJobLock.unlock();
        }

        return ret;
    }

    /**
     * 添加camera job status
     * @param cj   待添加job
     */
    private void addCameraJobStatus(CameraJobStatus cj)  {
        DBManager dbm = GlobalContext.getInstance().mDBManager;
        if(dbm.mCameraJobStatusHelper.AddJobStatus(cj))   {
            mLsJobStatusLock.lock();
            mLsJobStatus.clear();
            mLsJobStatus.addAll(dbm.mCameraJobStatusHelper.GetAllJobStatus());
            mLsJobStatusLock.unlock();
        }
        else    {
            Log.e(TAG, "添加camera job status失败，camerajobstatus = " + cj.toString());
        }
    }

    /**
     * 根据camerajob找到其status
     * @param jobid camerajob id
     * @return 对应的status或者null
     */
    public CameraJobStatus getCameraJobStatus(int jobid)    {
        CameraJobStatus cjs = null;
        DBManager dbm = GlobalContext.getInstance().mDBManager;

        mLsJobStatusLock.lock();
        mLsJobStatus.clear();
        mLsJobStatus.addAll(dbm.mCameraJobStatusHelper.GetAllJobStatus());

        for(CameraJobStatus i : mLsJobStatus)   {
            if(i.camerjob_id == jobid)  {
                cjs = i.Clone();
                break;
            }
        }
        mLsJobStatusLock.unlock();

        return cjs;
    }

    /**
     * 根据ID找到对应的camerajob
     * @param jobid camerajob 的id
     * @return 对应的camerajob或者null
     */
    public CameraJob getCameraJob(int jobid)    {
        CameraJob cj = null;
        DBManager dbm = GlobalContext.getInstance().mDBManager;

        mLsJobLock.lock();
        mLsJob.clear();
        mLsJob.addAll(dbm.mCameraJobHelper.GetJobs());

        for(CameraJob i : mLsJob)   {
            if(i._id == jobid)  {
                cj = i.Clone();
                break;
            }
        }
        mLsJobLock.unlock();

        return cj;
    }


    /**
     * 修改camera job status
     * @param cj   待更新job status
     */
    public void modifyCameraJobStatus(CameraJobStatus cj)  {
        DBManager dbm = GlobalContext.getInstance().mDBManager;
        if(dbm.mCameraJobStatusHelper.ModifyJobStatus(cj))   {
            mLsJobStatusLock.lock();
            mLsJobStatus.clear();
            mLsJobStatus.addAll(dbm.mCameraJobStatusHelper.GetAllJobStatus());
            mLsJobStatusLock.unlock();
        }
        else    {
            Log.e(TAG, "修改camera job status失败，camerajobstatus = " + cj.toString());
        }
    }

    /**
     * 移除camera job
     * @param jobid 待移除job的id
     */
    public void removeCameraJob(String jobid)   {
        DBManager dbm = GlobalContext.getInstance().mDBManager;
        if(dbm.mCameraJobHelper.RemoveJob(jobid))   {
            mLsJobLock.lock();
            mLsJob.clear();
            mLsJob.addAll(dbm.mCameraJobHelper.GetJobs());
            mLsJobLock.unlock();
        }
        else    {
            Log.e(TAG, "移除camera job失败，jobid = " + jobid);
        }
    }

    /**
     * 移除camera job status
     * @param jobstatusid 待移除jobstatus的id
     */
    public void removeCameraJobStatus(String jobstatusid)   {
        DBManager dbm = GlobalContext.getInstance().mDBManager;
        if(dbm.mCameraJobStatusHelper.RemoveJobStatus(jobstatusid))   {
            mLsJobStatusLock.lock();
            mLsJobStatus.clear();
            mLsJobStatus.addAll(dbm.mCameraJobStatusHelper.GetAllJobStatus());
            mLsJobStatusLock.unlock();
        }
        else    {
            Log.e(TAG, "移除camera jobstatus失败，jobstatusid = " + jobstatusid);
        }
    }

    /**
     * 得到所有camera job
     * @return 所有camera job
     */
    public List<CameraJob> GetAllJobs() {
        DBManager dbm = GlobalContext.getInstance().mDBManager;
        LinkedList<CameraJob> ls_ret = new LinkedList<>();
        mLsJobLock.lock();
        mLsJob.clear();
        mLsJob.addAll(dbm.mCameraJobHelper.GetJobs());

        ls_ret.addAll(mLsJob);
        mLsJobLock.unlock();

        return ls_ret;
    }

    /**
     * 得到所有camera job status
     * @return 所有camera job status
     */
    public List<CameraJobStatus> GetAllJobStatus() {
        DBManager dbm = GlobalContext.getInstance().mDBManager;
        LinkedList<CameraJobStatus> ls_ret = new LinkedList<>();
        mLsJobStatusLock.lock();
        mLsJobStatus.clear();
        mLsJobStatus.addAll(dbm.mCameraJobStatusHelper.GetAllJobStatus());

        ls_ret.addAll(mLsJobStatus);
        mLsJobStatusLock.unlock();

        return ls_ret;
    }

    /**
     * 执行job
     * @param cj 唤醒的任务
     */
    private void jobWakeup(CameraJob cj)    {
        Timestamp cur = new Timestamp(0);
        CameraJobStatus cjs = GlobalContext.getInstance()
                                .mJobProcessor.getCameraJobStatus(cj._id);
        if(null == cjs)
            return;

        if(!cjs.camerajob_status.equals(GlobalDef.STR_CAMERAJOB_RUN))   {
            return;
        }

        cur.setTime(System.currentTimeMillis());
        long curms = cur.getTime();
        long sms = cj.job_starttime.getTime();
        long ems = cj.job_endtime.getTime();
        if((curms >= sms) && (curms < ems)) {
            switch (cj.job_type) {
                case GlobalDef.CNSTR_JOBTYPE_MINUTELY: {
                    process_mintuely_job(cj);
                }
                break;

                case GlobalDef.CNSTR_JOBTYPE_HOURLY: {
                    process_hourly_job(cj);
                }
                break;

                case GlobalDef.CNSTR_JOBTYPE_DAILY: {
                    process_daily_job(cj);
                }
                break;
            }
        }
    }

    private void process_mintuely_job(CameraJob cj) {
        Calendar curCal = Calendar.getInstance();
        int cursec = curCal.get(Calendar.SECOND);
        boolean ik = false;
        switch (cj.job_point)   {
            case GlobalDef.CNSTR_EVERY_TEN_SECOND : {
                if(GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec % 10)
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_TWENTY_SECOND : {
                if(GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec % 20)
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_THIRTY_SECOND: {
                if(GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec % 30)
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
            case GlobalDef.CNSTR_EVERY_ONE_MINUTE: {
                if(GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec)
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_TWO_MINUTE: {
                if((0 == curmin % 2)
                        && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec))
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_FIVE_MINUTE: {
                if((0 == curmin % 5)
                        && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec))
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_TEN_MINUTE: {
                if((0 == curmin % 10)
                    && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec))
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_TWENTY_MINUTE : {
                if((0 == curmin % 20)
                        && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec))
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_THIRTY_MINUTE: {
                if((0 == curmin % 30)
                        && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec))
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
                        && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec))
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_TWO_HOUR : {
                if((0 == curhou % 2)
                        && (0 == curmin)
                        && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec))
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_FOUR_HOUR: {
                if((0 == curhou % 4)
                        && (0 == curmin)
                        && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec))
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_SIX_HOUR: {
                if((0 == curhou % 6)
                        && (0 == curmin)
                        && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec))
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_EIGHT_HOUR: {
                if((0 == curhou % 8)
                        && (0 == curmin)
                        && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec))
                    ik = true;
            }
            break;

            case GlobalDef.CNSTR_EVERY_TWELVE_HOUR: {
                if((0 == curhou % 12)
                        && (0 == curmin)
                        && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec))
                    ik = true;
            }
            break;
        }

        if(ik)
            wakeupDuty(cj);
    }


    @SuppressWarnings("ConstantConditions")
    private void wakeupDuty(CameraJob cj)   {
        Log.i(TAG, "wakeup job : " + cj.toString());
        FileLogger.getLogger().info("wakeup job : " + cj.toString());

        Calendar curCal = Calendar.getInstance();
        String fn = String.format(
                        "%d_%d%02d%02d-%02d%02d%02d.jpg"
                        ,cj._id
                        ,curCal.get(Calendar.YEAR)
                        ,curCal.get(Calendar.MONTH) + 1
                        ,curCal.get(Calendar.DAY_OF_MONTH)
                        ,curCal.get(Calendar.HOUR_OF_DAY)
                        ,curCal.get(Calendar.MINUTE)
                        ,curCal.get(Calendar.SECOND));

        String dirp = ContextUtil.getInstance().getCameraJobPhotoDir(cj._id);
        TakePhotoParam tp = new TakePhotoParam(dirp, fn, Integer.toString(cj._id));
        ContextUtil.getCameraHelper().TakePhoto(tp);
    }
}
