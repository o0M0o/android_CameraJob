package com.wxm.camerajob.base.db;

import android.content.Context;

/**
 * 执行sqlite业务逻辑
 * Created by wxm on 2016/6/11.
 */
public class DBManager {
    private CameraJobDBUtility mCameraJobUtility;
    private CameraJobStatusDBUtility mCameraJobStatusUtility;

    public DBManager(Context context) {
        DBOrmLiteHelper helper = new DBOrmLiteHelper(context);

        mCameraJobUtility = new CameraJobDBUtility(helper);
        mCameraJobStatusUtility = new CameraJobStatusDBUtility(helper);
    }

    public CameraJobDBUtility getCameraJobUtility() {
        return mCameraJobUtility;
    }

    public CameraJobStatusDBUtility getCameraJobStatusUtility() {
        return mCameraJobStatusUtility;
    }
}
