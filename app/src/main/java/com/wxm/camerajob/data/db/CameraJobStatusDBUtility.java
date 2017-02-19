package com.wxm.camerajob.data.db;

import com.wxm.camerajob.data.define.CameraJobStatus;
import com.wxm.camerajob.data.define.DBDataChangeEvent;

import java.util.List;

/**
 * 辅助处理camerajob的状态
 * Created by 123 on 2016/6/16.
 */
public class CameraJobStatusDBUtility extends DBUtilityBase {
    private DBOrmLiteHelper mHelper;


    public CameraJobStatusDBUtility(DBOrmLiteHelper helper)  {
        mHelper = helper;
        mDataType = DBDataChangeEvent.DATA_JOB_STATUS;
    }

    /**
     * 添加一个camera job status
     * @param cjs    待添加的job status
     * @return 如果添加成功返回true, 否则返回false
     */
    public boolean AddJobStatus(CameraJobStatus cjs) {
        boolean ret = 1 == mHelper.getCamerJobStatusREDao().create(cjs);
        if(ret)
            onDataCreate();

        return ret;
    }


    /**
     *  移除一个camera job status
     * @param jobstatusid     待移除jobstatus的id
     * @return 如果添加成功返回true, 否则返回false
     */
    public boolean RemoveJobStatus(String jobstatusid)  {
        int id = Integer.parseInt(jobstatusid);
        boolean ret =  1 == mHelper.getCamerJobStatusREDao().deleteById(id);
        if(ret)
            onDataDelete();

        return ret;
    }

    /**
     * 修改camera job status
     * @param cj    待修改jobstatus
     * @return  修改成功返回true, 否则返回false
     */
    public boolean ModifyJobStatus(CameraJobStatus cj)  {
        boolean ret = 1 == mHelper.getCamerJobStatusREDao().update(cj);
        if(ret)
            onDataModify();

        return ret;
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
