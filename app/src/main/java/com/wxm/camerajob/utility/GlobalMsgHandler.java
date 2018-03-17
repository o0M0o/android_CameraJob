package com.wxm.camerajob.utility;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wxm.camerajob.data.define.CameraJob;
import com.wxm.camerajob.data.define.CameraJobStatus;
import com.wxm.camerajob.data.define.EMsgType;

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
        EMsgType et = EMsgType.getEMsgType(msg.what);
        if(null != et) {
            switch (et) {
                case WAKEUP:
                    process_wakeup(msg);
                    break;

                case CAMERAJOB_QUERY:
                    process_camera_job_query(msg);
                    break;

                case CAMERAJOB_TAKEPHOTO:
                    process_camera_job_takephoto(msg);
                    break;
            }
        }
    }

    private void process_wakeup(Message msg) {
        List<CameraJob> ls = ContextUtil.GetCameraJobUtility().getAllData();
        if((null != ls) && (1 <= ls.size()))
            ContextUtil.GetJobProcess().processorWakeup(ls);
    }

    private void process_camera_job_query(Message msg)  {
        Handler h = (Handler)msg.obj;

        List<CameraJob> ls_ret = ContextUtil.GetCameraJobUtility().getAllData();
        if(null == ls_ret)
            Log.e(TAG, "get camerajob failed!");

        Message answer = Message.obtain(h, EMsgType.REPLAY.getId(), ls_ret);
        answer.arg1 = EMsgType.CAMERAJOB_QUERY.getId();
        answer.sendToTarget();
    }

    private void process_camera_job_takephoto(Message msg) {
        Object[] obj_arr = UtilFun.cast(msg.obj);
        int camerajob_id = UtilFun.cast(obj_arr[0]);
        int photo_count = UtilFun.cast(obj_arr[1]);

        CameraJob js = ContextUtil.GetCameraJobUtility().getData(camerajob_id);
        if(null != js)   {
            CameraJobStatus cur_js = js.getStatus();
            cur_js.setJob_photo_count(cur_js.getJob_photo_count() + photo_count);
            cur_js.getTs().setTime(Calendar.getInstance().getTimeInMillis());

            ContextUtil.GetCameraJobStatusUtility().modifyData(cur_js);
        }
    }

}