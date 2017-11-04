package com.wxm.camerajob.utility;

import android.os.Handler;

import com.wxm.camerajob.data.db.CameraJobDBUtility;
import com.wxm.camerajob.data.db.CameraJobStatusDBUtility;
import com.wxm.camerajob.data.db.DBOrmLiteHelper;

import wxm.androidutil.util.UtilFun;

/**
 * app context
 * Created by wxm on 2016/6/10.
 */
public class GlobalContext {
    private static final String TAG = "GlobalContext";
    private int initFlag;

    private GlobalMsgHandler    mMsgHandler;
    private CameraJobProcess     mJobProcessor;

    // for db
    private CameraJobDBUtility          mCameraJobUtility;
    private CameraJobStatusDBUtility    mCameraJobStatusUtility;

    private static GlobalContext ourInstance = new GlobalContext();
    public static Handler getMsgHandlder()   {
        return UtilFun.cast(ourInstance.mMsgHandler);
    }

    static CameraJobProcess GetJobProcess()   {
        return ourInstance.mJobProcessor;
    }

    public static CameraJobDBUtility GetCameraJobUtility() {
        return ourInstance.mCameraJobUtility;
    }
    public static CameraJobStatusDBUtility GetCameraJobStatusUtility() {
        return ourInstance.mCameraJobStatusUtility;
    }

    private GlobalContext()  {
        initFlag = 0;
    }

    /**
     * init self
     * must invoke it before use other function
     */
    static void init()   {
        if(isInit())
            return;

        // for db
        DBOrmLiteHelper helper = new DBOrmLiteHelper(ContextUtil.getInstance());
        ourInstance.mCameraJobUtility = new CameraJobDBUtility(helper);
        ourInstance.mCameraJobStatusUtility = new CameraJobStatusDBUtility(helper);

        // for job
        ourInstance.mMsgHandler = new GlobalMsgHandler();
        ourInstance.mJobProcessor = new CameraJobProcess();
        ourInstance.mJobProcessor.init();

        // for self
        ourInstance.initFlag = 1;
    }

    /**
     * check whether finished init
     * @return  if already init return true else false
     */
    static boolean isInit()   {
        return ourInstance.initFlag == 1;
    }
}
