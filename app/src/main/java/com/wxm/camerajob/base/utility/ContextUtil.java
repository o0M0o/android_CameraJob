package com.wxm.camerajob.base.utility;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.handler.GlobalContext;
import com.wxm.camerajob.base.receiver.AlarmReceiver;

/**
 * Created by 123 on 2016/5/7.
 * 获取全局context
 */
public class ContextUtil extends Application    {
    private static final String TAG = "ContextUtil";

    public SilentCameraHandler  mSCHHandler;

    private static ContextUtil instance;
    public static ContextUtil getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
//        JobScheduler tm = (JobScheduler)getSystemService(Context.JOB_SCHEDULER_SERVICE);
//        tm.cancelAll();


        // 初始化context
        mSCHHandler = new SilentCameraHandler();
        instance = this;
        GlobalContext.getInstance().initContext();

        // 设置闹钟
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + GlobalDef.INT_GLOBALJOB_PERIOD, pendingIntent);

        Log.i(TAG, "Application created");
    }

    @Override
    public void onTerminate()  {
        Log.i(TAG, "Application onTerminate");
//        JobScheduler tm = (JobScheduler)getSystemService(Context.JOB_SCHEDULER_SERVICE);
//        tm.cancelAll();

        super.onTerminate();
    }
}
