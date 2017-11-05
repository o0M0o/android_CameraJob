package com.wxm.camerajob.utility;

import android.os.Message;
import android.util.Log;

import com.wxm.camerajob.data.define.CameraJob;
import com.wxm.camerajob.data.define.GlobalDef;
import com.wxm.camerajob.data.define.PreferencesUtil;
import com.wxm.camerajob.data.define.TakePhotoParam;
import com.wxm.camerajob.hardware.SilentCameraHelper;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * process camera job
 * Created by 123 on 2016/6/13.
 */
class CameraJobProcess {
    private final String TAG = "CameraJobProcess";
    private int                     mInitFlag;

    CameraJobProcess() {
        mInitFlag       = 0;
    }

    /**
     * init self
     * @return  true if success else false
     */
    @SuppressWarnings("UnusedReturnValue")
    boolean init()  {
        if(1 == mInitFlag)  {
            return true;
        }

        mInitFlag = 1;
        return true;
    }


    /**
     * wakeup to process job
     */
    void processorWakeup(List<CameraJob> ls)   {
        if(1 != mInitFlag)
            return;

        LinkedList<CameraJob>  active_job      = new LinkedList<>();
        for(CameraJob cj : ls)  {
            if(checkJobWakeup(cj))  {
                active_job.add(cj);
            }
        }
        
        for(CameraJob cj : active_job)  {
            wakeupDuty(cj);
        }
    }



    /**
     * check job whether is wakeup
     * @param cj   job need check
     * @return  true if wakeup else false
     */
    private boolean checkJobWakeup(CameraJob cj)    {
        boolean ret = false;
        if(!cj.getStatus().getJob_status().equals(GlobalDef.STR_CAMERAJOB_RUN))   {
            return ret;
        }

        Timestamp cur = new Timestamp(0);
        cur.setTime(System.currentTimeMillis());
        long curms = cur.getTime();
        long sms = cj.getStarttime().getTime();
        long ems = cj.getEndtime().getTime();
        if((curms >= sms) && (curms < ems)) {
            switch (cj.getType()) {
                case GlobalDef.CNSTR_JOBTYPE_MINUTELY: {
                    ret = check_mintuely_job(cj);
                }
                break;

                case GlobalDef.CNSTR_JOBTYPE_HOURLY: {
                    ret = check_hourly_job(cj);
                }
                break;

                case GlobalDef.CNSTR_JOBTYPE_DAILY: {
                    ret = check_daily_job(cj);
                }
                break;
            }
        }

        return ret;
    }

    private boolean check_mintuely_job(CameraJob cj) {
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

        return ik;
    }

    private boolean check_hourly_job(CameraJob cj) {
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

        return ik;
    }


    private boolean check_daily_job(CameraJob cj) {
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

        return ik;
    }


    /**
     * execute job
     * @param cj    job need executed
     */
    @SuppressWarnings("ConstantConditions")
    private void wakeupDuty(CameraJob cj)   {
        Log.i(TAG, "wakeup job : " + cj.toString());
        FileLogger.getLogger().info("wakeup job : " + cj.toString());

        Calendar curCal = Calendar.getInstance();
        String fn = String.format(Locale.CHINA,  "%d_%d%02d%02d-%02d%02d%02d.jpg"
                        ,cj.get_id()
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
                Message m = Message.obtain(ContextUtil.GetMsgHandlder(),
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
