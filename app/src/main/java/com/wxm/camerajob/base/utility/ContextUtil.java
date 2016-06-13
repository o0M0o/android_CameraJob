package com.wxm.camerajob.base.utility;

import android.app.Application;
import android.app.job.JobScheduler;
import android.content.Context;
import android.util.Log;

import com.wxm.camerajob.base.handler.GlobalContext;

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

        instance = this;
        GlobalContext.getInstance().initContext();
    }

    @Override
    public void onTerminate()  {
        Log.i(TAG, "onTerminate");
        JobScheduler tm = (JobScheduler)getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.cancelAll();

        super.onTerminate();
    }
}
