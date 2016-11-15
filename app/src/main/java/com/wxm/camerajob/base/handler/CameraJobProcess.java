package com.wxm.camerajob.base.handler;

import android.os.Message;
import android.util.Log;

import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.data.PreferencesUtil;
import com.wxm.camerajob.base.data.TakePhotoParam;
import com.wxm.camerajob.base.db.DBManager;
import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.base.utility.FileLogger;
import com.wxm.camerajob.base.utility.SilentCameraHelper;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * 处理job
 * Created by 123 on 2016/6/13.
 */
public class CameraJobProcess {
    private final String TAG = "CameraJobProcess";
    private int                     mInitFlag;
    private LinkedList<CameraJob>   mActiveJob;

    public CameraJobProcess() {
        mInitFlag       = 0;
        mActiveJob      = new LinkedList<>();
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

        mInitFlag = 1;
        mActiveJob.clear();
        return true;
    }


    /**
     * 任务处理器唤醒函数
     */
    public void processorWakeup(List<CameraJob> ls)   {
        if(1 != mInitFlag)
            return;

        mActiveJob.clear();
        for(CameraJob cj : ls)  {
            cheakJobWakeup(cj);
        }
        
        for(CameraJob cj : mActiveJob)  {
            wakeupDuty(cj);
        }
    }



    /**
     * 执行job
     * @param cj 唤醒的任务
     */
    private void cheakJobWakeup(CameraJob cj)    {
        Timestamp cur = new Timestamp(0);

        if(!cj.getStatus().getJob_status().equals(GlobalDef.STR_CAMERAJOB_RUN))   {
            return;
        }

        cur.setTime(System.currentTimeMillis());
        long curms = cur.getTime();
        long sms = cj.getStarttime().getTime();
        long ems = cj.getEndtime().getTime();
        if((curms >= sms) && (curms < ems)) {
            switch (cj.getType()) {
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
        switch (cj.getPoint())   {
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
            mActiveJob.add(cj);
    }

    private void process_hourly_job(CameraJob cj) {
        Calendar curCal = Calendar.getInstance();
        int cursec = curCal.get(Calendar.SECOND);
        int curmin = curCal.get(Calendar.MINUTE);
        boolean ik = false;
        switch (cj.getPoint())   {
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
            mActiveJob.add(cj);
    }


    private void process_daily_job(CameraJob cj) {
        Calendar curCal = Calendar.getInstance();
        int cursec = curCal.get(Calendar.SECOND);
        int curmin = curCal.get(Calendar.MINUTE);
        int curhou = curCal.get(Calendar.HOUR_OF_DAY);
        boolean ik = false;
        switch (cj.getPoint())   {
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
            mActiveJob.add(cj);
    }


    @SuppressWarnings("ConstantConditions")
    private void wakeupDuty(CameraJob cj)   {
        Log.i(TAG, "wakeup job : " + cj.toString());
        FileLogger.getLogger().info("wakeup job : " + cj.toString());

        Calendar curCal = Calendar.getInstance();
        String fn = String.format(
                        "%d_%d%02d%02d-%02d%02d%02d.jpg"
                        , cj.get_id()
                        ,curCal.get(Calendar.YEAR)
                        ,curCal.get(Calendar.MONTH) + 1
                        ,curCal.get(Calendar.DAY_OF_MONTH)
                        ,curCal.get(Calendar.HOUR_OF_DAY)
                        ,curCal.get(Calendar.MINUTE)
                        ,curCal.get(Calendar.SECOND));

        String dirp = ContextUtil.getInstance().getCameraJobPhotoDir(cj.get_id());
        TakePhotoParam tp = new TakePhotoParam(dirp, fn, Integer.toString(cj.get_id()));

        SilentCameraHelper sh = new SilentCameraHelper();
        sh.setTakePhotoCallBack(new SilentCameraHelper.takePhotoCallBack() {
            @Override
            public void onTakePhotoSuccess(TakePhotoParam tp) {
                Log.i(TAG, "take photo success, tag = " + tp.mTag);

                //send msg
                Message m = Message.obtain(GlobalContext.getMsgHandlder(),
                        GlobalDef.MSG_TYPE_CAMERAJOB_TAKEPHOTO);
                m.obj = new Object[] {Integer.parseInt(tp.mTag), 1};
                m.sendToTarget();
            }

            @Override
            public void onTakePhotoFailed(TakePhotoParam tp) {
                String l = "take photo failure, tag = " + tp.mTag;
                Log.e(TAG, l);
                FileLogger.getLogger().severe(l);
            }
        });
        sh.TakePhoto(PreferencesUtil.loadCameraParam(), tp);
    }
}
