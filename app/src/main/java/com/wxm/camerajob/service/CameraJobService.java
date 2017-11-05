package com.wxm.camerajob.service;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.util.Log;

import com.wxm.camerajob.data.define.GlobalDef;
import com.wxm.camerajob.utility.ContextUtil;

import java.util.LinkedList;

/**
 * app的jobservice
 * Created by wxm on 2016/6/10.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class CameraJobService extends JobService {
    private static final String TAG = "CameraJobService";
    private final LinkedList<JobParameters> jobParamsMap = new LinkedList<>();

    /*
    @Override
    public void onCreate() {
        super.onCreate();
        //Log.i(TAG, "JobService created");
        //Context mCurContext = ContextUtil.getInstance();
    }
    */

    /**
     * When the app's MainActivity is created, it starts this service. This is so that the
     * activity and this service can communicate back and forth. See "setUiCalback()"
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*
        Messenger callback = intent.getParcelableExtra(GlobalDef.STR_MESSENGER);
        Message m = Message.obtain();
        m.what = MainActivity.MSG_SERVICE_OBJ;
        m.obj = this;
        try {
            callback.send(m);
        } catch (RemoteException e) {
            Log.e(TAG, "Error passing service object back to activity.");
        }
        */
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        // We don't do any real 'work' in this sample app. All we'll
        // do is track which jobs have landed on our service, and
        // update the UI accordingly.
        jobParamsMap.add(params);
        //Log.i(TAG, "on start job: " + params.getJobId() + ", context : " + mCurContext);

        //Log.i(TAG, "on start job");
        Message m = Message.obtain(ContextUtil.GetMsgHandlder(),
                                GlobalDef.MSG_TYPE_WAKEUP);
        m.sendToTarget();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // Stop tracking these job parameters, as we've 'finished' executing.
        jobParamsMap.remove(params);
        //Log.i(TAG, "on stop job: " + params.getJobId());
        return true;
    }

    /** Send job to the JobScheduler. */
    public void scheduleJob(JobInfo t) {
        Log.d(TAG, "Scheduling job");
        JobScheduler tm =
                (JobScheduler) ContextUtil.getInstance().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.schedule(t);
    }

    /**
     * Not currently used, but as an exercise you can hook this
     * up to a button in the UI to finish a job that has landed
     * in onStartJob().
    public boolean callJobFinished() {
        JobParameters params = jobParamsMap.poll();
        if (params == null) {
            return false;
        } else {
            jobFinished(params, false);
            return true;
        }
    }
     */
}
