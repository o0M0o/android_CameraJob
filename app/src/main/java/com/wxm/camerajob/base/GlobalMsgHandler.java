package com.wxm.camerajob.base;

import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wxm.camerajob.jobservice.CameraJobService;

/**
 * 全局handler
 * Created by wxm on 2016/6/10.
 */
public class GlobalMsgHandler extends Handler {
    private static final String TAG = "GlobalMsgHandler";
    private static int kJobId = 0;
    private ComponentName mServiceComponent;

    /*
    private static GlobalMsgHandler ourInstance = new GlobalMsgHandler();
    public static GlobalMsgHandler getInstance() {
        return ourInstance;
    }
    */

    public GlobalMsgHandler()   {
        super();
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what)   {
            case GlobalDef.MSGWHAT_ADDJOB   :
                processor_addjob(msg);
                break;

            case GlobalDef.MSGWHAT_ADDJOB_GLOBAL  :
                processor_addjob_global(msg);
                break;

            default:
                Log.e(TAG, String.format("msg(%s) can not process", msg.toString()));
                break;
        }
    }

    /**
     * 添加job
     * @param msg  输入消息
     */
    private void processor_addjob(Message msg)    {
        JobInfo ji = (JobInfo)msg.obj;
        GlobalContext.getInstance().mJobService.scheduleJob(ji);
    }

    /**
     * 添加全局job
     * @param msg  输入消息
     */
    private void processor_addjob_global(Message msg)    {
        mServiceComponent = new ComponentName((Context)msg.obj,
                                CameraJobService.class);

        JobInfo.Builder builder = new JobInfo.Builder(kJobId++, mServiceComponent);
        //builder.setMinimumLatency(2000);
        builder.setPeriodic(5000);
        builder.setPersisted(true);

        GlobalContext.getInstance().mJobService.scheduleJob(builder.build());
    }
}
