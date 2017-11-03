package com.wxm.camerajob.data.db;

import android.content.Context;

/**
 * process sqlite business logic
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