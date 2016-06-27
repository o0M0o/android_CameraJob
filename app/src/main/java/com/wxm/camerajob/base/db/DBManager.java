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
        DBHelper mHelper = new DBHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        SQLiteDatabase mDb = mHelper.getWritableDatabase();

        mCameraJobHelper = new DBCameraJobHelper(mDb);
        mCameraJobStatusHelper = new DBCameraJobStatusHelper(mDb);
    }
}
