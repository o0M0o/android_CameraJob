package com.wxm.camerajob.base.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * sqlite辅助类
 * Created by wxm on 2016/6/11.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "AppLocal.db";
    private static final int DATABASE_VERSION = 1;

    public final static String TABNAME_USER         = "tb_Usr";
    public final static String COLNAME_USER_NAME    = "usr_name";
    public final static String COLNAME_USER_PWD     = "usr_pwd";

    public final static String TABNAME_JOB          = "tb_Job";
    public final static String COLNAME_JOB_ID       = "job_id";
    public final static String COLNAME_JOB_NAME     = "job_name";
    public final static String COLNAME_JOB_TYPE     = "job_type";
    public final static String COLNAME_JOB_POINT    = "job_point";
    public final static String COLNAME_JOB_TS       = "job_ts";

    public DBHelper(Context context) {
        //CursorFactory设置为null,使用默认值
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //数据库第一次被创建时onCreate会被调用
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create table
        String sql_job = String.format(
                "CREATE TABLE IF NOT EXISTS %s " +
                        "(%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "  %s NVARCHAR," +
                        "  %s NVARCHAR," +
                        "  %s NVARCHAR," +
                        "  %s TIMESTAMP)",
                TABNAME_JOB,
                COLNAME_JOB_ID, COLNAME_JOB_NAME,
                COLNAME_JOB_TYPE, COLNAME_JOB_POINT,
                COLNAME_JOB_TS);
        db.execSQL(sql_job);

        // create table
        String sql_usr = String.format(
                "CREATE TABLE IF NOT EXISTS %s " +
                        "( %s NVARCHAR PRIMARY KEY," +
                        "  %s NVARCHAR)",
                TABNAME_USER, COLNAME_USER_NAME, COLNAME_USER_PWD);
        db.execSQL(sql_usr);
    }

    //如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("ALTER TABLE person ADD COLUMN other STRING");
    }
}
