package com.wxm.camerajob.alarm

import android.annotation.TargetApi
import android.app.Service
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Message
import android.util.Log

import com.wxm.camerajob.data.define.EMsgType
import com.wxm.camerajob.utility.ContextUtil

import java.util.LinkedList

/**
 * app的jobservice
 * Created by wxm on 2016/6/10.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class CameraJobService : JobService() {
    private val jobParamsMap = LinkedList<JobParameters>()

    /*
    @Override
    public void onCreate() {
        super.onCreate();
        //Log.i(LOG_TAG, "JobService created");
        //Context mCurContext = ContextUtil.Companion.getInstance();
    }
    */

    /**
     * When the app's MainActivity is created, it starts this service. This is so that the
     * activity and this service can communicate back and forth. See "setUiCalback()"
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        /*
        Messenger callback = intent.getParcelableExtra(GlobalDef.STR_MESSENGER);
        Message m = Message.obtain();
        m.what = MainActivity.MSG_SERVICE_OBJ;
        m.obj = this;
        try {
            callback.send(m);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error passing service object back to activity.");
        }
        */
        return Service.START_NOT_STICKY
    }

    override fun onStartJob(params: JobParameters): Boolean {
        // We don't do any real 'work' in this sample app. All we'll
        // do is track which jobs have landed on our service, and
        // update the UI accordingly.
        jobParamsMap.add(params)

        Message.obtain(ContextUtil.getMsgHandler(),
                EMsgType.WAKEUP.id, EMsgType.WAKEUP).sendToTarget()
        return false
    }

    override fun onStopJob(params: JobParameters): Boolean {
        // Stop tracking these job parameters, as we've 'finished' executing.
        jobParamsMap.remove(params)
        //Log.i(LOG_TAG, "on stop job: " + params.getJobId());
        return true
    }

    /** Send job to the JobScheduler.  */
    fun scheduleJob(t: JobInfo) {
        Log.d(LOG_TAG, "Scheduling job")
        ContextUtil.getSystemService<JobScheduler>(Context.JOB_SCHEDULER_SERVICE)!!.schedule(t)
    }

    companion object {
        private val LOG_TAG = ::CameraJobService.javaClass.simpleName
    }
}