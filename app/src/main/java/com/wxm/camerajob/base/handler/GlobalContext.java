package com.wxm.camerajob.base.handler;

import android.content.ComponentName;
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
    public  CameraJobProcess     mJobProcessor;
    public  DBManager            mDBManager;

    private static GlobalContext ourInstance = new GlobalContext();
    public static GlobalContext getInstance() {
        return ourInstance;
    }

    public static CameraJobProcess GetJobProcess()   {
        return getInstance().mJobProcessor;
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

                case GlobalDef.MSGWHAT_ASK_CAMERAJOB :
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
            Object[] obj_arr = (Object[])msg.obj;
            String _id = (String) obj_arr[0];
            Handler h = (Handler)obj_arr[1];

            CameraJobStatus cjs = GetJobProcess().getCameraJobStatus(Integer.parseInt(_id));
            if(null != cjs) {
                cjs.camerajob_status = GlobalDef.STR_CAMERAJOB_RUN;
                GetJobProcess().modifyCameraJobStatus(cjs);
            }

            Message reply = Message.obtain(h, GlobalDef.MSGWHAT_CAMERAJOB_UPDATE);
            reply.sendToTarget();
        }

        /**
         * 暂停指定camerajob
         * @param msg 消息
         */
        private void processor_camerajob_topause(Message msg) {
            Object[] obj_arr = (Object[])msg.obj;
            String _id = (String) obj_arr[0];
            Handler h = (Handler)obj_arr[1];

            CameraJobStatus cjs = GetJobProcess().getCameraJobStatus(Integer.parseInt(_id));
            if(null != cjs) {
                cjs.camerajob_status = GlobalDef.STR_CAMERAJOB_PAUSE;
                GetJobProcess().modifyCameraJobStatus(cjs);
            }

            Message reply = Message.obtain(h, GlobalDef.MSGWHAT_CAMERAJOB_UPDATE);
            reply.sendToTarget();
        }

        /**
         * 删除camerajob任务目录
         * @param msg 消息
         */
        private void processor_camerajob_delete(Message msg) {
            Object[] obj_arr = (Object[])msg.obj;
            String _id = (String) obj_arr[0];
            Handler h = (Handler)obj_arr[1];

            String path = ContextUtil.getInstance()
                    .getCameraJobPhotoDir(Integer.parseInt(_id));
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
            Object[] obj_arr = (Object[])msg.obj;
            int camerajob_id = (int)obj_arr[0];
            int photo_count = (int)obj_arr[1];
            //Handler h = (Handler)obj_arr[2];

            //Log.i(TAG, "camerajob_id : " + camerajob_id + ", photo_count : " + photo_count);

            List<CameraJobStatus> js = getInstance().mJobProcessor.GetAllJobStatus();
            CameraJobStatus curjs = null;
            for(CameraJobStatus ji : js)    {
                if(ji.camerjob_id == camerajob_id)  {
                    curjs = ji;
                    break;
                }
            }

            if(null != curjs)   {
                curjs.camerajob_photo_count += photo_count;
                curjs.ts.setTime(Calendar.getInstance().getTimeInMillis());
                getInstance().mJobProcessor.modifyCameraJobStatus(curjs);
                //Log.i(TAG, "CameraJobStatus : " + curjs.toString());
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
            GetJobProcess().removeCameraJob(_id);

            int camerajob_id = Integer.parseInt(_id);
            List<CameraJobStatus> js = GetJobProcess().GetAllJobStatus();
            CameraJobStatus curjs = null;
            for(CameraJobStatus ji : js)    {
                if(ji.camerjob_id == camerajob_id)  {
                    curjs = ji;
                    break;
                }
            }

            if(null != curjs)   {
                GetJobProcess()
                        .removeCameraJobStatus(Integer.toString(curjs._id));
            }

            Message reply = Message.obtain(h, GlobalDef.MSGWHAT_CAMERAJOB_UPDATE);
            reply.sendToTarget();
        }

        /**
         * 查询cameraJob
         * @param msg  输入消息
         */
        private void processor_ask_cameraJob(Message msg)    {
            Handler h = (Handler)msg.obj;

            Message answer = Message.obtain(h, GlobalDef.MSGWHAT_ANSWER_CAMERAJOB);
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
            GetJobProcess().processorWakeup();
        }

        /**
         * 添加拍照任务
         * @param msg  消息
         */
        private void processor_camerajob_add(Message msg)  {
            Object[] obj_arr = (Object[])msg.obj;

            CameraJob cj = (CameraJob)obj_arr[0];
            Handler h = (Handler)obj_arr[1];
            int nid = GetJobProcess().addCameraJob(cj);
            CameraJob nj = GetJobProcess().getCameraJob(nid);

            //create prjdir
            if(null != nj) {
                if(!UtilFun.StringIsNullOrEmpty(
                        ContextUtil.getInstance().createCameraJobPhotoDir(nj))) {
                    Message reply = Message.obtain(h, GlobalDef.MSGWHAT_CAMERAJOB_UPDATE);
                    reply.sendToTarget();
                }
            }
        }
    }
}
