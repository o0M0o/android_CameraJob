package com.wxm.camerajob.utility;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wxm.camerajob.data.define.CameraJob;
import com.wxm.camerajob.data.define.CameraJobStatus;
import com.wxm.camerajob.data.define.GlobalDef;

import java.util.Calendar;
import java.util.List;

import wxm.androidutil.util.UtilFun;

/**
 * global msg handler
 * Created by wxm on 2016/6/10.
 */
class GlobalMsgHandler extends Handler {
    private static final String TAG = "GlobalMsgHandler";

    GlobalMsgHandler()   {
        super();
    }

    @Override
    public void handleMessage(Message msg) {
        if(!GlobalContext.isInit())   {
            Log.e(TAG, "context not inited");
            return;
        }


        switch (msg.what)   {
            case GlobalDef.MSG_TYPE_WAKEUP:
                process_wakeup();
                break;

            case GlobalDef.MSG_TYPE_CAMERAJOB_QUERY:
                process_ask_cameraJob(msg);
                break;

            case GlobalDef.MSG_TYPE_CAMERAJOB_TAKEPHOTO:
                process_takephoto(msg);
                break;

            default:
                Log.e(TAG, String.format("msg(%s) can not process", msg.toString()));
                break;
        }
    }

    /**
     * take photo
     * @param msg  message
     */
    private void process_takephoto(Message msg) {
        Object[] obj_arr = UtilFun.cast(msg.obj);
        int camerajob_id = UtilFun.cast(obj_arr[0]);
        int photo_count = UtilFun.cast(obj_arr[1]);

        CameraJob js = GlobalContext.GetCameraJobUtility().getData(camerajob_id);
        if(null != js)   {
            CameraJobStatus cur_js = js.getStatus();
            cur_js.setJob_photo_count(cur_js.getJob_photo_count() + photo_count);
            cur_js.getTs().setTime(Calendar.getInstance().getTimeInMillis());

            GlobalContext.GetCameraJobStatusUtility().modifyData(cur_js);
        }
    }

    /**
     * query camera job
     * @param msg  message
     */
    private void process_ask_cameraJob(Message msg)    {
        Handler h = (Handler)msg.obj;

        List<CameraJob> ls_ret = GlobalContext.GetCameraJobUtility().getAllData();
        if(null == ls_ret)
            Log.e(TAG, "get camerajob failed!");

        Message answer = Message.obtain(h, GlobalDef.MSG_TYPE_REPLAY);
        answer.arg1 = GlobalDef.MSG_TYPE_CAMERAJOB_QUERY;
        answer.obj = ls_ret;
        answer.sendToTarget();
    }

    /**
     * wake up
     */
    private void process_wakeup() {
        List<CameraJob> ls = GlobalContext.GetCameraJobUtility().getAllData();
        if((null != ls) && (1 <= ls.size()))
            GlobalContext.GetJobProcess().processorWakeup(ls);
    }
}