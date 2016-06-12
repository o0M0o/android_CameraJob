package com.wxm.camerajob.base;

/**
 * app的全局设定
 * Created by wxm on 2016/6/10.
 */
public class GlobalDef {
    public final static String STR_MESSENGER            = "messenger";
    public final static String STR_JOB_INVOKE           = "job_invoke";

    public final static String STR_LOAD_JOB             = "load_job";

    public final static String STR_JOBTYPE_MINUTELY    = "minutely_job";
    public final static String STR_JOBTYPE_HOURLY      = "hourly_job";
    public final static String STR_JOBTYPE_DAILY       = "daily_job";
    public final static String CNSTR_JOBTYPE_MINUTELY  = "每分钟";
    public final static String CNSTR_JOBTYPE_HOURLY    = "每小时";
    public final static String CNSTR_JOBTYPE_DAILY     = "每天";

    public final static int MSGWHAT_ADDJOB              = 1000;
    public final static int MSGWHAT_ADDJOB_GLOBAL       = 1001;

    public final static int INTRET_JOB_SAVE             = 2000;
    public final static int INTRET_JOB_GIVEUP           = 2001;

}
