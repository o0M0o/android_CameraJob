package com.wxm.camerajob.data.define;

/**
 * 数据库中数据变化后通知类
 * Created by User on 2017/2/14.
 */
public class DBDataChangeEvent {
    public final static int   EVENT_CREATE = 1;
    public final static int   EVENT_MODIFY = 2;
    public final static int   EVENT_REMOVE = 3;

    public final static int   DATA_JOB          = 1;
    public final static int   DATA_JOB_STATUS   = 2;

    private int mEventType;
    private int mDataType;

    public DBDataChangeEvent(int data_type, int event_type)  {
        mDataType = data_type;
        mEventType = event_type;
    }

    /**
     * 获取事件类型
     * @return  事件类型
     */
    public int getEventType()   {
        return mEventType;
    }

    /**
     * 获取数据类型
     * @return  数据类型
     */
    public int getDataType()   {
        return mDataType;
    }
}
