package com.wxm.camerajob.base.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wxm.camerajob.base.data.CameraJobStatus;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by 123 on 2016/6/16.
 */
public class DBCameraJobStatusHelper {
    private SQLiteDatabase mDb;

    private String mAddJobStatusSqlTemp;
    private String mRemoveJobStatusSqlTemp;
    private String mModifyJobStatusSqlTemp;

    public DBCameraJobStatusHelper(SQLiteDatabase sd)  {
        mDb = sd;

        mAddJobStatusSqlTemp = String.format(
                "INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)",
                DBHelper.TABNAME_JOBSTATUS,
                DBHelper.COLNAME_JS_JOBID, DBHelper.COLNAME_JS_STATUS,
                DBHelper.COLNAME_JS_PHOTOCOUNT, DBHelper.COLNAME_JS_TS);

        mModifyJobStatusSqlTemp = String.format(
                "UPDATE %s " +
                        " SET" +
                        " %s = ?" +
                        " ,%s = ?" +
                        " ,%s = ?" +
                        " ,%s = ?" +
                        " WHERE %s = ?",
                DBHelper.TABNAME_JOBSTATUS,
                DBHelper.COLNAME_JS_JOBID, DBHelper.COLNAME_JS_STATUS,
                DBHelper.COLNAME_JS_PHOTOCOUNT, DBHelper.COLNAME_JS_TS,
                DBHelper.COLNAME_JS_ID);

        mRemoveJobStatusSqlTemp = String.format(
                "DELETE FROM %s WHERE %s = ?",
                DBHelper.TABNAME_JOBSTATUS,
                DBHelper.COLNAME_JS_ID);
    }

    /**
     * 添加一个camera job status
     * @param cjs    待添加的job status
     * @return 如果添加成功返回true, 否则返回false
     */
    public boolean AddJobStatus(CameraJobStatus cjs) {
        boolean ret = false;
        mDb.beginTransaction();
        try {
            mDb.execSQL(
                    mAddJobStatusSqlTemp,
                    new Object[]{cjs.camerjob_id, cjs.camerajob_status,
                                 cjs.camerajob_photo_count, cjs.ts});
            mDb.setTransactionSuccessful();
            ret = true;
        } finally {
            mDb.endTransaction();
        }

        return ret;
    }


    /**
     *  移除一个camera job status
     * @param jobstatusid     待移除jobstatus的id
     * @return 如果添加成功返回true, 否则返回false
     */
    public boolean RemoveJobStatus(String jobstatusid)  {
        boolean ret = false;
        mDb.beginTransaction();
        try {
            mDb.execSQL(
                    mRemoveJobStatusSqlTemp,
                    new Object[]{jobstatusid});
            mDb.setTransactionSuccessful();
            ret = true;
        } finally {
            mDb.endTransaction();
        }

        return ret;
    }

    /**
     * 修改camera job status
     * @param cj    待修改jobstatus
     * @return  修改成功返回true, 否则返回false
     */
    public boolean ModifyJobStatus(CameraJobStatus cj)  {
        boolean ret = false;
        mDb.beginTransaction();
        try {
            mDb.execSQL(
                    mModifyJobStatusSqlTemp,
                    new Object[]{cj.camerjob_id, cj.camerajob_status,
                                    cj.camerajob_photo_count, cj.ts, cj._id});
            mDb.setTransactionSuccessful();
            ret = true;
        } finally {
            mDb.endTransaction();
        }

        return ret;
    }

    /**
     * 从数据库加载job status
     * @return 数据库中的job status
     */
    public List<CameraJobStatus> GetAllJobStatus()    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        LinkedList<CameraJobStatus> persons = new LinkedList<>();
        Cursor c = queryJobCursor();
        while (c.moveToNext()) {
            CameraJobStatus ri = new CameraJobStatus();
            ri._id                      = c.getInt(c.getColumnIndex(DBHelper.COLNAME_JS_ID));
            ri.camerjob_id              = c.getInt(c.getColumnIndex(DBHelper.COLNAME_JS_JOBID));
            ri.camerajob_status         = c.getString(c.getColumnIndex(DBHelper.COLNAME_JS_STATUS));
            ri.camerajob_photo_count    = c.getInt(
                                        c.getColumnIndex(DBHelper.COLNAME_JS_PHOTOCOUNT));

            try {
                Date date = format.parse(c.getString(c.getColumnIndex(DBHelper.COLNAME_JS_TS)));
                ri.ts.setTime(date.getTime());
            }
            catch (ParseException ex)
            {
                ri.ts = new Timestamp(0);
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
        Cursor c = mDb.rawQuery("SELECT * FROM " + DBHelper.TABNAME_JOBSTATUS, null);
        return c;
    }
}
