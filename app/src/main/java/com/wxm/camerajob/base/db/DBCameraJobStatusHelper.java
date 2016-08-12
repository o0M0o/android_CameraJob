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
import java.util.Locale;

/**
 * 辅助处理camerajob的状态
 * Created by 123 on 2016/6/16.
 */
public class DBCameraJobStatusHelper {
    private DBOrmLiteHelper mHelper;


    public DBCameraJobStatusHelper(DBOrmLiteHelper helper)  {
        mHelper = helper;
    }

    /**
     * 添加一个camera job status
     * @param cjs    待添加的job status
     * @return 如果添加成功返回true, 否则返回false
     */
    public boolean AddJobStatus(CameraJobStatus cjs) {
        return 1 == mHelper.getCamerJobStatusREDao().create(cjs);
    }


    /**
     *  移除一个camera job status
     * @param jobstatusid     待移除jobstatus的id
     * @return 如果添加成功返回true, 否则返回false
     */
    public boolean RemoveJobStatus(String jobstatusid)  {
        int id = Integer.parseInt(jobstatusid);
        return 1 == mHelper.getCamerJobStatusREDao().deleteById(id);
    }

    /**
     * 修改camera job status
     * @param cj    待修改jobstatus
     * @return  修改成功返回true, 否则返回false
     */
    public boolean ModifyJobStatus(CameraJobStatus cj)  {
        return 1 == mHelper.getCamerJobStatusREDao().update(cj);
    }

    /**
     * 从数据库加载job status
     * @return 数据库中的job status
     */
    public List<CameraJobStatus> GetAllJobStatus()    {
        return mHelper.getCamerJobStatusREDao().queryForAll();
    }

    public CameraJobStatus GetJobStatusById(int id) {
        return mHelper.getCamerJobStatusREDao().queryForId(id);
    }
}
