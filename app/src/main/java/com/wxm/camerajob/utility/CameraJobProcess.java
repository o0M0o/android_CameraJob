package com.wxm.camerajob.utility;

import android.os.Message;
import android.util.Log;

import com.wxm.camerajob.data.define.CameraJob;
import com.wxm.camerajob.data.define.EMsgType;
import com.wxm.camerajob.data.define.GlobalDef;
import com.wxm.camerajob.data.define.EJobStatus;
import com.wxm.camerajob.data.define.PreferencesUtil;
import com.wxm.camerajob.data.define.TakePhotoParam;
import com.wxm.camerajob.data.define.ETimeGap;
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
     */
    void init()  {
        mInitFlag = 1;
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
    private boolean checkJobWakeup(CameraJob cj) {
        if (!cj.getStatus().getJob_status().equals(EJobStatus.RUN.getStatus())) {
            return false;
        }

        Timestamp cur = new Timestamp(0);
        cur.setTime(System.currentTimeMillis());
        long curms = cur.getTime();
        long sms = cj.getStarttime().getTime();
        long ems = cj.getEndtime().getTime();
        if((curms >= sms) && (curms < ems)) {
            String cj_pt = cj.getPoint();
            for(ETimeGap tg : ETimeGap.values())  {
                if(cj_pt.equals(tg.getGapName()))   {
                    return tg.isArrive(Calendar.getInstance());
                }
            }
        }

        return false;
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
                        EMsgType.CAMERAJOB_TAKEPHOTO.getId());
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
