package com.wxm.camerajob.base.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * 执行sqlite业务逻辑
 * Created by wxm on 2016/6/11.
 */
public class DBManager {
    public DBCameraJobHelper        mCameraJobHelper;
    public DBCameraJobStatusHelper  mCameraJobStatusHelper;

    public DBManager(Context context) {
        DBOrmLiteHelper helper = new DBOrmLiteHelper(context);

        mCameraJobHelper = new DBCameraJobHelper(helper);
        mCameraJobStatusHelper = new DBCameraJobStatusHelper(helper);
    }
}
