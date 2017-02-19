package com.wxm.camerajob.utility.utility;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wxm.camerajob.data.define.CameraJob;
import com.wxm.camerajob.data.define.CameraJobStatus;
import com.wxm.camerajob.data.define.GlobalDef;
import com.wxm.camerajob.data.db.DBManager;

import java.util.Calendar;
import java.util.List;

import cn.wxm.andriodutillib.util.UtilFun;

/**
 * app context
 * Created by wxm on 2016/6/10.
 */
public class GlobalContext {
    private static final String TAG = "GlobalContext";
    private int initFlag;

    private GlobalMsgHandler    mMsgHandler;
    private CameraJobProcess     mJobProcessor;
    private DBManager            mDBManager;

    private static GlobalContext ourInstance = new GlobalContext();
    public static GlobalContext getInstance() {
        return ourInstance;
    }

    public static Handler getMsgHandlder()   {
        return UtilFun.cast(getInstance().mMsgHandler);
    }

    private static CameraJobProcess GetJobProcess()   {
        return getInstance().mJobProcessor;
    }

    public static DBManager GetDBManager() {
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
    private static class GlobalMsgHandler extends Handler {
        private static final String TAG = "GlobalMsgHandler";

        GlobalMsgHandler()   {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            if(!getInstance().isInited())   {
                Log.e(TAG, "context not inited");
                return;
            }

            switch (msg.what)   {
                case GlobalDef.MSG_TYPE_WAKEUP:
                    processor_wakeup();
                    break;

                case GlobalDef.MSG_TYPE_CAMERAJOB_QUERY:
                    processor_ask_cameraJob(msg);
                    break;

                case GlobalDef.MSG_TYPE_CAMERAJOB_TAKEPHOTO:
                    processor_camerajob_takephoto(msg);
                    break;

                default:
                    Log.e(TAG, String.format("msg(%s) can not process", msg.toString()));
                    break;
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

            CameraJob js = GetDBManager().getCameraJobUtility().GetJob(camerajob_id);
            if(null != js)   {
                CameraJobStatus curjs = js.getStatus();
                curjs.setJob_photo_count(curjs.getJob_photo_count() + photo_count);
                curjs.getTs().setTime(Calendar.getInstance().getTimeInMillis());

                GetDBManager().getCameraJobStatusUtility().ModifyJobStatus(curjs);
            }
        }


        /**
         * 查询cameraJob
         * @param msg  输入消息
         */
        private void processor_ask_cameraJob(Message msg)    {
            Handler h = (Handler)msg.obj;

            List<CameraJob> lsret = GetDBManager().getCameraJobUtility().GetJobs();
            if(null == lsret)
                Log.e(TAG, "get camerajob failed!");

            Message answer = Message.obtain(h, GlobalDef.MSG_TYPE_REPLAY);
            answer.arg1 = GlobalDef.MSG_TYPE_CAMERAJOB_QUERY;
            answer.obj = lsret;
            answer.sendToTarget();
        }


        /**
         * 处理唤醒消息
         */
        private void processor_wakeup() {
            List<CameraJob> ls = GetDBManager().getCameraJobUtility().GetJobs();
            if((null != ls) && (1 <= ls.size()))
                GetJobProcess().processorWakeup(ls);
        }
    }
}
