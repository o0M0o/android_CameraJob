package com.wxm.camerajob.base;

import com.wxm.camerajob.jobservice.CameraJobService;

/**
 * app context
 * Created by wxm on 2016/6/10.
 */
public class GlobalContext {
    private static final String TAG = "GlobalContext";

    public CameraJobService    mJobService;
    public GlobalMsgHandler    mMsgHandler;

    private static GlobalContext ourInstance = new GlobalContext();
    public static GlobalContext getInstance() {
        return ourInstance;
    }

    private GlobalContext()  {
        mJobService = new CameraJobService();
        mMsgHandler = new GlobalMsgHandler();
    }
}
