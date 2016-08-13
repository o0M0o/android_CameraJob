package com.wxm.camerajob.base.handler;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.CameraJobStatus;
import com.wxm.camerajob.base.data.CameraParam;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.db.DBManager;
import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.base.utility.SilentCameraHelper;
import com.wxm.camerajob.base.utility.UtilFun;

import java.util.Calendar;
import java.util.List;

/**
 * app context
 * Created by wxm on 2016/6/10.
 */
public class GlobalContext {
    private static final String TAG = "GlobalContext";
    private int initFlag;

    public  GlobalMsgHandler     mMsgHandler;
    private CameraJobProcess     mJobProcessor;
    private DBManager            mDBManager;

    private static GlobalContext ourInstance = new GlobalContext();
    public static GlobalContext getInstance() {
        return ourInstance;
    }

    private static CameraJobProcess GetJobProcess()   {
        return getInstance().mJobProcessor;
    }

    private static DBManager GetDBManager() {
        return getInstance().mDBManager;
    }

    private GlobalContext()  {
        initFlag = 0;
    }


    public void initContext()   {
        mMsgHandler = new GlobalMsgHandler();
        mJobProcessor = new CameraJobProcess();
        mDBManager = new DBManager(ContextUtil.getInstance());

        mJobProcessor.init(mDBManager);

        initFlag = 1;
    }

    private boolean isInited()   {
        return initFlag == 1;
    }

    /**
     * 全局handler
     * Created by wxm on 2016/6/10.
     */
    public static class GlobalMsgHandler extends Handler {
        private static final String TAG = "GlobalMsgHandler";

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
                    processor_wakeup();
                    break;

                case GlobalDef.MSGWHAT_CAMERAJOB_ADD :
                    processor_camerajob_add(msg);
                    break;

                case GlobalDef.MSGWHAT_CAMERAJOB_REMOVE :
                    processor_camerajob_remove(msg);
                    break;

                case GlobalDef.MSGWHAT_CAMERAJOB_DELETE :
                    processor_camerajob_delete(msg);
                    break;

                case GlobalDef.MSGWHAT_CAMERAJOB_ASKALL:
                    processor_ask_cameraJob(msg);
                    break;

                case GlobalDef.MSGWHAT_CAMERAJOB_TAKEPHOTO :
                    processor_camerajob_takephoto(msg);
                    break;

                case GlobalDef.MSGWHAT_CS_CHANGECAMERA :
                    processor_changecamera(msg);
                    break;

                case GlobalDef.MSGWHAT_CAMERAJOB_TORUN :
                    processor_camerajob_torun(msg);
                    break;

                case GlobalDef.MSGWHAT_CAMERAJOB_RUNPAUSESWITCH :
                    processor_camerajob_runpauseswitch(msg);
                    break;

                case GlobalDef.MSGWHAT_CAMERAJOB_TOPAUSE :
                    processor_camerajob_topause(msg);
                    break;

                default:
                    Log.e(TAG, String.format("msg(%s) can not process", msg.toString()));
                    break;
            }
        }

        /**
         * 运行指定camerajob
         * @param msg 消息
         */
        private void processor_camerajob_torun(Message msg) {
            Object[] obj_arr = UtilFun.cast(msg.obj);
            Handler h = UtilFun.cast(obj_arr[0]);
            int _id = UtilFun.cast(obj_arr[1]);

            CameraJob cj = GetDBManager().mCameraJobHelper.GetJob(_id);
            if(null != cj) {
                CameraJobStatus cjs = cj.getStatus();
                cjs.setJob_status(GlobalDef.STR_CAMERAJOB_RUN);

                GetDBManager().mCameraJobStatusHelper.ModifyJobStatus(cjs);
            }

            Message reply = Message.obtain(h, GlobalDef.MSGWHAT_CAMERAJOB_UPDATE);
            reply.sendToTarget();
        }

        /**
         * 暂停指定camerajob
         * @param msg 消息
         */
        private void processor_camerajob_topause(Message msg) {
            Object[] obj_arr = UtilFun.cast(msg.obj);
            Handler h = UtilFun.cast(obj_arr[0]);
            int _id = UtilFun.cast(obj_arr[1]);

            CameraJob cj = GetDBManager().mCameraJobHelper.GetJob(_id);
            if(null != cj) {
                CameraJobStatus cjs = cj.getStatus();
                cjs.setJob_status(GlobalDef.STR_CAMERAJOB_PAUSE);

                GetDBManager().mCameraJobStatusHelper.ModifyJobStatus(cjs);
            }

            Message reply = Message.obtain(h, GlobalDef.MSGWHAT_CAMERAJOB_UPDATE);
            reply.sendToTarget();
        }

        private void processor_camerajob_runpauseswitch(Message msg)    {
            Object[] obj_arr = UtilFun.cast(msg.obj);
            Handler h = UtilFun.cast(obj_arr[0]);
            int _id = UtilFun.cast(obj_arr[1]);

            CameraJob cj = GetDBManager().mCameraJobHelper.GetJob(_id);
            if(null != cj) {
                CameraJobStatus cjs = cj.getStatus();
                cjs.setJob_status(cjs.getJob_status() == GlobalDef.STR_CAMERAJOB_PAUSE ?
                        GlobalDef.STR_CAMERAJOB_RUN : GlobalDef.STR_CAMERAJOB_PAUSE);
                GetDBManager().mCameraJobStatusHelper.ModifyJobStatus(cjs);

                Message reply = Message.obtain(h, GlobalDef.MSGWHAT_CAMERAJOB_UPDATE);
                reply.sendToTarget();
            }
        }

        /**
         * 删除camerajob任务目录
         * @param msg 消息
         */
        private void processor_camerajob_delete(Message msg) {
            Object[] obj_arr = UtilFun.cast(msg.obj);
            Handler h = UtilFun.cast(obj_arr[0]);
            int _id = UtilFun.cast(obj_arr[1]);

            String path = ContextUtil.getInstance()
                    .getCameraJobPhotoDir(_id);
            UtilFun.DeleteDirectory(path);

            Message reply = Message.obtain(h, GlobalDef.MSGWHAT_CAMERAJOB_UPDATE);
            reply.sendToTarget();
        }

        /**
         * 拍照后消息
         * @param msg 消息
         */
        private void processor_changecamera(Message msg) {
            //noinspection ConstantConditions
            SilentCameraHelper sh = ContextUtil.getCameraHelper();
            if(null != sh) {
                sh.ChangeCamera((CameraParam) msg.obj);
            }
        }


        /**
         * 拍照后消息
         * @param msg 消息
         */
        private void processor_camerajob_takephoto(Message msg) {
            Object[] obj_arr = UtilFun.cast(msg.obj);
            int camerajob_id = UtilFun.cast(obj_arr[0]);
            int photo_count = UtilFun.cast(obj_arr[1]);

            CameraJob js = GetDBManager().mCameraJobHelper.GetJob(camerajob_id);
            if(null != js)   {
                CameraJobStatus curjs = js.getStatus();
                curjs.setJob_photo_count(curjs.getJob_photo_count() + photo_count);
                curjs.getTs().setTime(Calendar.getInstance().getTimeInMillis());

                GetDBManager().mCameraJobHelper.ModifyJob(js);
                //Log.i(TAG, "CameraJobStatus : " + curjs.toString());
            }
        }


        /**
         * 移除cameraJob
         * @param msg  消息
         */
        private void processor_camerajob_remove(Message msg) {
            Object[] obj_arr = UtilFun.cast(msg.obj);
            Handler h = UtilFun.cast(obj_arr[0]);
            int _id = UtilFun.cast(obj_arr[1]);

            GetDBManager().mCameraJobHelper.RemoveJob(_id);
            Message reply = Message.obtain(h, GlobalDef.MSGWHAT_CAMERAJOB_UPDATE);
            reply.sendToTarget();
        }

        /**
         * 查询cameraJob
         * @param msg  输入消息
         */
        private void processor_ask_cameraJob(Message msg)    {
            Handler h = (Handler)msg.obj;

            List<CameraJob> lsret = GetDBManager().mCameraJobHelper.GetJobs();
            if(null == lsret)
                Log.e(TAG, "get camerajob failed!");

            Message answer = Message.obtain(h, GlobalDef.MSGWHAT_REPLAY);
            answer.arg1 = GlobalDef.MSGWHAT_CAMERAJOB_ASKALL;
            answer.obj = lsret;
            answer.sendToTarget();
        }

        /**
         * 添加job
         * @param msg  输入消息
         */
        private void processor_addjob(Message msg)    {
            /*
            JobInfo ji = (JobInfo)msg.obj;
            getInstance().mJobService.scheduleJob(ji);
            */
        }

        /**
         * 添加全局job
         * @param msg  输入消息
         */
        private void processor_addjob_global(Message msg)    {
            /*
            mServiceComponent = new ComponentName((Context)msg.obj,
                                    CameraJobService.class);

            JobInfo.Builder builder = new JobInfo.Builder(kJobId++, mServiceComponent);
            //builder.setMinimumLatency(2000);
            builder.setPeriodic(GlobalDef.INT_GLOBALJOB_PERIOD);
            builder.setPersisted(true);

            GetJobService().scheduleJob(builder.build());
            */
        }

        /**
         * 处理唤醒消息
         */
        private void processor_wakeup() {
            List<CameraJob> ls = GetDBManager().mCameraJobHelper.GetJobs();
            if((null != ls) && (1 <= ls.size()))
                GetJobProcess().processorWakeup(ls);
        }

        /**
         * 添加拍照任务
         * @param msg  消息
         */
        private void processor_camerajob_add(Message msg)  {
            Object[] obj_arr = UtilFun.cast(msg.obj);
            Handler h = UtilFun.cast(obj_arr[0]);
            CameraJob cj = UtilFun.cast(obj_arr[1]);

            //create prjdir
            if(GetDBManager().mCameraJobHelper.AddJob(cj))  {
                if(!UtilFun.StringIsNullOrEmpty(
                        ContextUtil.getInstance().createCameraJobPhotoDir(cj))) {
                    Message reply = Message.obtain(h, GlobalDef.MSGWHAT_CAMERAJOB_UPDATE);
                    reply.sendToTarget();
                }
            }
        }
    }
}
