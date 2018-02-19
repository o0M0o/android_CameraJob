package com.wxm.camerajob.data.define;


import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wxm.camerajob.utility.ContextUtil;

import java.util.Calendar;
import java.util.List;
import java.util.function.UnaryOperator;

import wxm.androidutil.util.UtilFun;

/**
 * msg type
 * Created by ookoo on 2018/2/19.
 */
public enum EMsgType {
    WAKEUP("wakeup", 1000,
            msg -> {
                List<CameraJob> ls = ContextUtil.GetCameraJobUtility().getAllData();
                if((null != ls) && (1 <= ls.size()))
                    ContextUtil.GetJobProcess().processorWakeup(ls);
                return msg;
            }),

    CAMERAJOB_QUERY("query camera job", 1102,
            msg -> {
                Handler h = (Handler)msg.obj;

                List<CameraJob> ls_ret = ContextUtil.GetCameraJobUtility().getAllData();
                if(null == ls_ret)
                    Log.e(EMsgType.TAG, "get camerajob failed!");

                Message answer = Message.obtain(h, EMsgType.REPLAY.getId());
                answer.arg1 = EMsgType.CAMERAJOB_QUERY.getId();
                answer.obj = ls_ret;
                answer.sendToTarget();
                return msg;
            }),

    CAMERAJOB_MODIFY("modify camera job", 1103,
            msg -> msg),

    CAMERAJOB_TAKEPHOTO("camera job take photo", 1104,
            msg -> {
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

                return msg;
            }),

    JOBSHOW_UPDATE("update job show", 1200,
            msg -> msg),

    REPLAY("replay", 9000,
            msg -> msg);

    private static final String TAG = "EMsgType";
    private String szType;
    private int iId;
    private UnaryOperator<Message> opMessage;

    EMsgType(String type, int id, UnaryOperator<Message> op)    {
        szType = type;
        iId = id;
        opMessage = op;
    }

    /**
     * get type name
     * @return  type name
     */
    public String getType()   {
        return szType;
    }

    /**
     * get type id
     * @return  type id
     */
    public int getId()  {
        return iId;
    }

    /**
     * process message
     * not all message processed in here
     *
     * @param msgId     id for message
     * @param msg       message body
     */
    public static void doMessage(int msgId, Message msg)  {
        boolean bProcess = false;
        for(EMsgType et : EMsgType.values())    {
            if(et.getId() == msgId) {
                et.opMessage.apply(msg);
                bProcess = true;
                break;
            }
        }

        if(!bProcess)
            Log.e(TAG, String.format("msg(%s) can not process", msg.toString()));
    }
}
