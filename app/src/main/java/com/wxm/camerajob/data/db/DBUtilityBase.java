package com.wxm.camerajob.data.db;

import com.wxm.camerajob.data.define.DBDataChangeEvent;
import com.wxm.camerajob.data.define.GlobalDef;

import org.greenrobot.eventbus.EventBus;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedList;

/**
 * 数据工具基类
 * Created by 123 on 2016/10/31.
 */
public abstract class DBUtilityBase {
    private Timestamp mTSLastModifyData;

    protected int  mDataType;

    DBUtilityBase()    {
        mTSLastModifyData = new Timestamp(Calendar.getInstance().getTimeInMillis());
        mDataType = GlobalDef.INT_INVALID_ID;
    }


    /**
     * 返回数据最后更新时间
     * @return  数据最后更新时间
     */
    public Timestamp getDataLastChangeTime()    {
        return mTSLastModifyData;
    }

    /**
     * 数据有更新时调用
     */
    void onDataModify()   {
        mTSLastModifyData.setTime(Calendar.getInstance().getTimeInMillis());

        DBDataChangeEvent de = new DBDataChangeEvent(mDataType, DBDataChangeEvent.EVENT_MODIFY);
        EventBus.getDefault().post(de);
    }

    /**
     * 新建数据后调用
     */
    void onDataCreate()   {
        mTSLastModifyData.setTime(Calendar.getInstance().getTimeInMillis());

        DBDataChangeEvent de = new DBDataChangeEvent(mDataType, DBDataChangeEvent.EVENT_CREATE);
        EventBus.getDefault().post(de);
    }

    /**
     * 删除数据后调用
     */
    void onDataDelete()   {
        mTSLastModifyData.setTime(Calendar.getInstance().getTimeInMillis());

        DBDataChangeEvent de = new DBDataChangeEvent(mDataType, DBDataChangeEvent.EVENT_REMOVE);
        EventBus.getDefault().post(de);
    }
}
