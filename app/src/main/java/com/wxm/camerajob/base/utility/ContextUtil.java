package com.wxm.camerajob.base.utility;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.handler.GlobalContext;
import com.wxm.camerajob.base.receiver.AlarmReceiver;

import java.util.Calendar;

/**
 * Created by 123 on 2016/5/7.
 * 获取全局context
 */
public class ContextUtil extends Application    {
    private static final String TAG = "ContextUtil";

    private static ContextUtil instance;
    public static ContextUtil getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        JobScheduler tm = (JobScheduler)getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.cancelAll();

        // 取消旧闹钟
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        // 初始化context
        instance = this;
        GlobalContext.getInstance().initContext();

        // 设置闹钟
        Calendar cl = Calendar.getInstance();
        alarmManager.set(AlarmManager.RTC_WAKEUP, cl.getTimeInMillis(), pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                cl.getTimeInMillis(),
                GlobalDef.INT_GLOBALJOB_PERIOD, pendingIntent);

        Log.i(TAG, "Application created");
    }

    @Override
    public void onTerminate()  {
        Log.i(TAG, "Application onTerminate");
        JobScheduler tm = (JobScheduler)getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.cancelAll();

        super.onTerminate();
    }
}
