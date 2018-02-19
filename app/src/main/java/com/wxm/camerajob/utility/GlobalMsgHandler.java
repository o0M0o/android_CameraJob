package com.wxm.camerajob.utility;

import android.os.Handler;
import android.os.Message;

import com.wxm.camerajob.data.define.EMsgType;

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
        EMsgType.doMessage(msg.what, msg);
    }
}