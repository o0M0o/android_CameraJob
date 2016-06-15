package com.wxm.camerajob.base.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.handler.GlobalContext;

/**
 * Created by 123 on 2016/6/15.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private final static String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context arg0, Intent data) {
        //Log.i(TAG, "app wakeup");

        Message m = Message.obtain(GlobalContext.getInstance().mMsgHandler,
                GlobalDef.MSGWHAT_WAKEUP);
        m.sendToTarget();
    }
}
