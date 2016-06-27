package com.wxm.camerajob.base.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wxm.camerajob.base.data.CameraJob;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * 处理camerajob的数据库类
 * Created by 123 on 2016/6/16.
 */
public class DBCameraJobHelper {
    private SQLiteDatabase mDb;

    private String mAddJobSqlTemp;
    private String mRemoveJobSqlTemp;
    private String mModifyJobSqlTemp;

    public DBCameraJobHelper(SQLiteDatabase sd)  {
        mDb = sd;

        mAddJobSqlTemp = String.format(
                "INSERT INTO %s (%s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?)",
                DBHelper.TABNAME_JOB,
                DBHelper.COLNAME_JOB_NAME, DBHelper.COLNAME_JOB_TYPE,
                DBHelper.COLNAME_JOB_POINT, DBHelper.COLNAME_JOB_STARTTIME,
                DBHelper.COLNAME_JOB_ENDTIME, DBHelper.COLNAME_JOB_TS);

        mModifyJobSqlTemp = String.format(
                "UPDATE %s " +
                        " SET" +
                        " %s = ?" +
                        " ,%s = ?" +
                        " ,%s = ?" +
                        " ,%s = ?" +
                        " ,%s = ?" +
                        " ,%s = ?" +
                        " WHERE %s = ?",
                DBHelper.TABNAME_JOB,
                DBHelper.COLNAME_JOB_NAME, DBHelper.COLNAME_JOB_TYPE,
                DBHelper.COLNAME_JOB_POINT, DBHelper.COLNAME_JOB_STARTTIME,
                DBHelper.COLNAME_JOB_ENDTIME, DBHelper.COLNAME_JOB_TS,
                DBHelper.COLNAME_JOB_ID);

        mRemoveJobSqlTemp = String.format(
                "DELETE FROM %s WHERE %s = ?",
                DBHelper.TABNAME_JOB,
                DBHelper.COLNAME_JOB_ID);

    }

    /**
     * 添加一个camera job
     * @param cj    待添加的job
     * @return 如果添加成功返回true, 否则返回false
     */
    @SuppressWarnings("ConstantConditions")
    public boolean AddJob(CameraJob cj) {
        boolean ret = false;
        mDb.beginTransaction();
        try {
            mDb.execSQL(
                    mAddJobSqlTemp,
                    new Object[]{cj.job_name, cj.job_type, cj.job_point,
                                 cj.job_starttime, cj.job_endtime, cj.ts});
            mDb.setTransactionSuccessful();
            ret = true;
        } finally {
            mDb.endTransaction();
        }

        return ret;
    }

    /**
     *  移除一个camera job
     * @param jobid     待移除job的id
     * @return 如果添加成功返回true, 否则返回false
     */
    @SuppressWarnings("ConstantConditions")
    public boolean RemoveJob(String jobid)  {
        boolean ret = false;
        mDb.beginTransaction();
        try {
            mDb.execSQL(
                    mRemoveJobSqlTemp,
                    new Object[]{jobid});
            mDb.setTransactionSuccessful();
            ret = true;
        } finally {
            mDb.endTransaction();
        }

        return ret;
    }

    /**
     * 修改camera job
     * @param cj    待修改job
     * @return  修改成功返回true, 否则返回false
     */
    @SuppressWarnings("ConstantConditions")
    public boolean ModifyJob(CameraJob cj)  {
        boolean ret = false;
        mDb.beginTransaction();
        try {
            mDb.execSQL(
                    mModifyJobSqlTemp,
                    new Object[]{cj.job_name, cj.job_type, cj.job_point,
                                 cj.job_starttime, cj.job_endtime, cj.ts, cj._id});
            mDb.setTransactionSuccessful();
            ret = true;
        } finally {
            mDb.endTransaction();
        }

        return ret;
    }

    /**
     * 从数据库加载job
     * @return 数据库中的job
     */
    public List<CameraJob> GetJobs()    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        LinkedList<CameraJob> persons = new LinkedList<>();
        Cursor c = queryJobCursor();
        while (c.moveToNext()) {
            CameraJob ri = new CameraJob();
            ri._id = c.getInt(c.getColumnIndex(DBHelper.COLNAME_JOB_ID));
            ri.job_name = c.getString(c.getColumnIndex(DBHelper.COLNAME_JOB_NAME));
            ri.job_type = c.getString(c.getColumnIndex(DBHelper.COLNAME_JOB_TYPE));
            ri.job_point = c.getString(c.getColumnIndex(DBHelper.COLNAME_JOB_POINT));

            try {
                Date date = format.parse(c.getString(
                                c.getColumnIndex(DBHelper.COLNAME_JOB_STARTTIME)));
                ri.job_starttime.setTime(date.getTime());

                date = format.parse(c.getString(
                                c.getColumnIndex(DBHelper.COLNAME_JOB_ENDTIME)));
                ri.job_endtime.setTime(date.getTime());

                date = format.parse(c.getString(
                                c.getColumnIndex(DBHelper.COLNAME_JOB_TS)));
                ri.ts.setTime(date.getTime());
            }
            catch (ParseException ex)
            {
                ri.ts = new Timestamp(0);
                ri.job_starttime = new Timestamp(System.currentTimeMillis());
                ri.job_endtime = new Timestamp(System.currentTimeMillis());
            }

            persons.add(ri);
        }
        c.close();
        return persons;
    }

    /**
     * 得到job游标
     * @return job游标
     */
    private Cursor queryJobCursor() {
        Cursor c = mDb.rawQuery("SELECT * FROM " + DBHelper.TABNAME_JOB, null);
        return c;
    }
}
