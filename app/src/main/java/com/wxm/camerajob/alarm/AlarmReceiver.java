package com.wxm.camerajob.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.util.Log;

import com.wxm.camerajob.data.define.CameraJob;
import com.wxm.camerajob.data.define.EJobStatus;
import com.wxm.camerajob.data.define.ETimeGap;
import com.wxm.camerajob.data.define.GlobalDef;
import com.wxm.camerajob.utility.ContextUtil;

import java.util.Calendar;
import java.util.List;

import wxm.androidutil.util.UtilFun;

import static com.wxm.camerajob.data.define.EMsgType.*;
import static java.lang.Math.min;

/**
 * wakeup app & set alarm for next wakeup
 * Created by 123 on 2016/6/15.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private final static String TAG = "AlarmReceiver";

    public AlarmReceiver()  {
        super();
    }

    /**
     * wakeup in here
     * @param arg0   param
     * @param data   param
     */
    @Override
    public void onReceive(Context arg0, Intent data) {
        try {
            // wakeup app
            Message.obtain(ContextUtil.Companion.getMsgHandler(), WAKEUP.getId(), WAKEUP)
                    .sendToTarget();

            // set alarm
            Context ct = ContextUtil.Companion.getInstance();
            Intent intent = new Intent(ct, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(ct, 0, intent, 0);
            AlarmManager alarmManager =
                    (AlarmManager)ContextUtil.Companion.getInstance().getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, getNextAlarmDelay(), pendingIntent);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }

    /**
     * get next alarm delay time(ms)
     * if have job run, use job delay.
     * else active in next GlobalDef.INT_GLOBALJOB_PERIOD / 1000 seconds
     * @return  delay time(ms) before next alarm
     */
    private long getNextAlarmDelay() {
        long ret = 0;
        List<CameraJob> ls_job = ContextUtil.Companion.getCameraJobUtility().getAllData();
        if(!UtilFun.ListIsNullOrEmpty(ls_job))   {
            for (CameraJob cj : ls_job) {
                if(cj.getStatus().getJob_status().equals(EJobStatus.RUN.getStatus()))   {
                    ETimeGap et = ETimeGap.Companion.getETimeGap(cj.getPoint());
                    if(null != et)  {
                        long cur_ms = et.getDelay(Calendar.getInstance());
                        ret = 0 == ret ? cur_ms : min(cur_ms, ret);
                    }
                }
            }
        }

        if(0 == ret) {
            long curms = System.currentTimeMillis();
            ret = (curms - (curms % GlobalDef.INT_GLOBALJOB_PERIOD))
                    + GlobalDef.INT_GLOBALJOB_PERIOD;
        }

        return ret;
    }
}
