package com.wxm.camerajob.base.db;

import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.GlobalDef;

import java.util.List;

/**
 * 处理camerajob的数据库类
 * Created by 123 on 2016/6/16.
 */
public class DBCameraJobHelper {
    private DBOrmLiteHelper mHelper;

    public DBCameraJobHelper(DBOrmLiteHelper helper)  {
        mHelper = helper;
    }

    /**
     * 添加一个camera job
     * @param cj    待添加的job
     * @return 如果添加成功返回true, 否则返回false
     */
    public boolean AddJob(CameraJob cj) {
        return 1 == mHelper.getCamerJobREDao().create(cj);
    }

    /**
     * 根据ID获取CameraJob
     * @param id  任务id
     * @return  结果数据
     */
    public CameraJob GetJob(int id) {
        return mHelper.getCamerJobREDao().queryForId(id);
    }

    /**
     *  移除一个camera job
     * @param jobid     待移除job的id
     * @return 如果添加成功返回true, 否则返回false
     */
    public boolean RemoveJob(int jobid)  {
        return  1 == mHelper.getCamerJobREDao().deleteById(jobid);
    }

    /**
     * 修改camera job
     * @param cj    待修改job
     * @return  修改成功返回true, 否则返回false
     */
    public boolean ModifyJob(CameraJob cj)  {
        return 1 == mHelper.getCamerJobREDao().update(cj);
    }

    /**
     * 从数据库加载job
     * @return 数据库中的job
     */
    public List<CameraJob> GetJobs()    {
        return mHelper.getCamerJobREDao().queryForAll();
    }

    /**
     * 获取当前激活状态的任务数
     * @return  目前处于活跃状态的任务数
     */
    public int GetActiveJobCount()  {
        int count = 0;
        List<CameraJob> lj = GetJobs();
        for(CameraJob cj : lj)  {
            if(cj.getStatus().getJob_status().equals(GlobalDef.STR_CAMERAJOB_RUN))
                count++;
        }

        return count;
    }
}

