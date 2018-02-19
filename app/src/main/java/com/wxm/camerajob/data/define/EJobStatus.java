package com.wxm.camerajob.data.define;

/**
 * status for camera job
 * Created by ookoo on 2018/2/19.
 */
public enum EJobStatus {
    RUN("running"),
    PAUSE("pause"),
    STOP("stop"),
    UNKNOWN("unknown");

    private String szStatus;

    EJobStatus(String sz)    {
        szStatus = sz;
    }

    /**
     * get status string
     * @return  for status
     */
    public String getStatus()   {
        return szStatus;
    }
}
