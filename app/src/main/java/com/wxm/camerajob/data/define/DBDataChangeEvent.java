package com.wxm.camerajob.data.define;

/**
 * event for data change in db
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
     * get event type
     * @return  event type
     */
    public int getEventType()   {
        return mEventType;
    }

    /**
     * get data type
     * @return  data type
     */
    public int getDataType()   {
        return mDataType;
    }
}
