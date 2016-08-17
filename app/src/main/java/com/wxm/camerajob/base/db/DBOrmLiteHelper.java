package com.wxm.camerajob.base.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.CameraJobStatus;

import java.sql.SQLException;

/**
 * APP的sqlite helper
 * Created by wxm on 2016/8/12.
 */
public class DBOrmLiteHelper  extends OrmLiteSqliteOpenHelper {
    private static final String     TAG                 = "DBOrmLiteHelper";
    private static final String     DATABASE_NAME       = "AppLocal.db";
    private static final int        DATABASE_VERSION    = 5;


    private RuntimeExceptionDao<CameraJob, Integer> mRDAOCameraJob = null;
    private RuntimeExceptionDao<CameraJobStatus, Integer> mRDAOCameraJobStatus = null;


    public DBOrmLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        CreateAndInitTable();
    }


    @Override
    public void onUpgrade(SQLiteDatabase database,
                          ConnectionSource connectionSource, int oldVersion, int newVersion) {
        if(5 == newVersion) {
            CreateAndInitTable();
        }
    }

    @Override
    public void close() {
        super.close();
        mRDAOCameraJob       = null;
        mRDAOCameraJobStatus = null;
    }

    public RuntimeExceptionDao<CameraJob, Integer> getCamerJobREDao()   {
        if(!isOpen())
            return null;

        if(null == mRDAOCameraJob)
            mRDAOCameraJob = getRuntimeExceptionDao(CameraJob.class);

        return mRDAOCameraJob;
    }

    public RuntimeExceptionDao<CameraJobStatus, Integer> getCamerJobStatusREDao()   {
        if(!isOpen())
            return null;

        if(null == mRDAOCameraJobStatus)
            mRDAOCameraJobStatus = getRuntimeExceptionDao(CameraJobStatus.class);

        return mRDAOCameraJobStatus;
    }



    private void CreateAndInitTable()   {
        try {
            TableUtils.createTable(connectionSource, CameraJob.class);
            TableUtils.createTable(connectionSource, CameraJobStatus.class);
        } catch (SQLException e) {
            Log.e(TAG, "Can't create database", e);
            throw new RuntimeException(e);
        }
    }
}
