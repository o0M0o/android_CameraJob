package com.wxm.camerajob.base.handler;

import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.db.DBManager;
import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.jobservice.CameraJobService;

/**
 * app context
 * Created by wxm on 2016/6/10.
 */
public class GlobalContext {
    private static final String TAG = "GlobalContext";
    private int initFlag;

    public CameraJobService     mJobService;
    public GlobalMsgHandler     mMsgHandler;
    public CameraJobProcess mJobProcessor;
    public DBManager            mDBManager;

    private static GlobalContext ourInstance = new GlobalContext();
    public static GlobalContext getInstance() {
        return ourInstance;
    }

    private GlobalContext()  {
        initFlag = 0;
    }


    public void initContext()   {
        mJobService = new CameraJobService();
        mMsgHandler = new GlobalMsgHandler();
        mJobProcessor = new CameraJobProcess();
        mDBManager = new DBManager(ContextUtil.getInstance());

        mJobProcessor.init(mDBManager);

        initFlag = 1;
    }

    public boolean isInited()   {
        return initFlag == 1 ? true : false;
    }

    /**
     * 全局handler
     * Created by wxm on 2016/6/10.
     */
    public static class GlobalMsgHandler extends Handler {
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
            if(!getInstance().isInited())   {
                Log.e(TAG, "context not inited");
                return;
            }

            switch (msg.what)   {
                case GlobalDef.MSGWHAT_JOB_ADD:
                    processor_addjob(msg);
                    break;

                case GlobalDef.MSGWHAT_JOB_ADD_GLOBAL:
                    processor_addjob_global(msg);
                    break;

                case GlobalDef.MSGWHAT_WAKEUP :
                    processor_wakeup(msg);
                    break;

                case GlobalDef.MSGWHAT_CAMERAJOB_ADD :
                    processor_camerajob_add(msg);
                    break;

                case GlobalDef.MSGWHAT_CAMERAJOB_REMOVE :
                    processor_camerajob_remove(msg);
                    break;

                case GlobalDef.MSGWHAT_ASK_CAMERAJOB :
                    processor_ask_cameraJob(msg);
                    break;

                default:
                    Log.e(TAG, String.format("msg(%s) can not process", msg.toString()));
                    break;
            }
        }

        /**
         * 移除cameraJob
         * @param msg  消息
         */
        private void processor_camerajob_remove(Message msg) {
            Object[] obj_arr = (Object[])msg.obj;
            String _id = (String) obj_arr[0];
            Handler h = (Handler)obj_arr[1];
            getInstance().mJobProcessor.removeCameraJob(_id);

            Message reply = Message.obtain(h, GlobalDef.MSGWHAT_CAMERAJOB_UPDATE);
            reply.obj = getInstance().mJobProcessor.GetAllJobs();
            reply.sendToTarget();
        }

        /**
         * 查询cameraJob
         * @param msg  输入消息
         */
        private void processor_ask_cameraJob(Message msg)    {
            Handler h = (Handler)msg.obj;

            Message answer = Message.obtain(h, GlobalDef.MSGWHAT_ANSWER_CAMERAJOB);
            answer.obj = getInstance().mJobProcessor.GetAllJobs();
            answer.sendToTarget();
        }

        /**
         * 添加job
         * @param msg  输入消息
         */
        private void processor_addjob(Message msg)    {
            JobInfo ji = (JobInfo)msg.obj;
            getInstance().mJobService.scheduleJob(ji);
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
            builder.setPeriodic(GlobalDef.INT_GLOBALJOB_PERIOD);
            builder.setPersisted(true);

            getInstance().mJobService.scheduleJob(builder.build());
        }

        /**
         * 处理唤醒消息
         * @param msg   消息
         */
        private void processor_wakeup(Message msg) {
            getInstance().mJobProcessor.processorWakeup();
        }

        /**
         * 添加拍照任务
         * @param msg  消息
         */
        private void processor_camerajob_add(Message msg)  {
            Object[] obj_arr = (Object[])msg.obj;

            CameraJob cj = (CameraJob)obj_arr[0];
            Handler h = (Handler)obj_arr[1];
            getInstance().mJobProcessor.addCameraJob(cj);

            Message reply = Message.obtain(h, GlobalDef.MSGWHAT_CAMERAJOB_UPDATE);
            reply.obj = getInstance().mJobProcessor.GetAllJobs();
            reply.sendToTarget();
        }
    }
}
