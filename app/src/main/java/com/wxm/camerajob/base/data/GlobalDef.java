package com.wxm.camerajob.base.data;

/**
 * app的全局设定
 * Created by wxm on 2016/6/10.
 */
public class GlobalDef {
    public final static String STR_MESSENGER            = "messenger";
    public final static String STR_JOB_INVOKE           = "job_invoke";

    public final static String STR_LOAD_JOB             = "load_job";
    public final static String STR_LOAD_CAMERASETTING   = "load_camerasetting";
    public final static String STR_LOAD_PHOTODIR        = "load_photodir";

    public final static String STR_JOBTYPE_MINUTELY    = "minutely_job";
    public final static String STR_JOBTYPE_HOURLY      = "hourly_job";
    public final static String STR_JOBTYPE_DAILY       = "daily_job";
    public final static String CNSTR_JOBTYPE_MINUTELY  = "每分钟";
    public final static String CNSTR_JOBTYPE_HOURLY    = "每小时";
    public final static String CNSTR_JOBTYPE_DAILY     = "每天";

    public final static int MSGWHAT_JOB_ADD             = 1000;
    public final static int MSGWHAT_JOB_ADD_GLOBAL      = 1001;
    public final static int MSGWHAT_WAKEUP              = 1002;
    public final static int MSGWHAT_CAMERAJOB_ADD       = 1100;
    public final static int MSGWHAT_CAMERAJOB_UPDATE    = 1101;
    public final static int MSGWHAT_ASK_CAMERAJOB       = 1102;
    public final static int MSGWHAT_ANSWER_CAMERAJOB    = 1103;
    public final static int MSGWHAT_CAMERAJOB_REMOVE    = 1104;
    public final static int MSGWHAT_CAMERAJOB_TAKEPHOTO = 1105;
    public final static int MSGWHAT_ACSTART_UPDATEJOBS  = 1200;
    public final static int MSGWHAT_CS_CHANGECAMERA     = 1300;

    public final static int INTRET_CAMERAJOB_ACCEPT     = 2000;
    public final static int INTRET_CAMERAJOB_GIVEUP     = 2001;
    public final static int INTRET_CS_ACCEPT            = 2010;
    public final static int INTRET_CS_GIVEUP            = 2011;
    public final static int INTRET_NOTCARE              = 9000;

    public final static int INT_INVALID_ID              = -1;
    public final static int INT_GLOBALJOB_PERIOD        = 5000;
    public final static int INT_GLOBALJOB_CHECKPERIOD   = 5;

    public final static String CNSTR_EVERY_TEN_SECOND         = "每10秒";
    public final static String CNSTR_EVERY_TWENTY_SECOND      = "每20秒";
    public final static String CNSTR_EVERY_THIRTY_SECOND      = "每30秒";

    public final static String CNSTR_EVERY_ONE_MINUTE         = "每1分钟";
    public final static String CNSTR_EVERY_TWO_MINUTE         = "每2分钟";
    public final static String CNSTR_EVERY_FIVE_MINUTE        = "每5分钟";
    public final static String CNSTR_EVERY_TEN_MINUTE         = "每10分钟";
    public final static String CNSTR_EVERY_TWENTY_MINUTE      = "每20分钟";
    public final static String CNSTR_EVERY_THIRTY_MINUTE      = "每30分钟";

    public final static String CNSTR_EVERY_ONE_HOUR           = "每1小时";
    public final static String CNSTR_EVERY_TWO_HOUR           = "每2小时";
    public final static String CNSTR_EVERY_FOUR_HOUR          = "每4小时";
    public final static String CNSTR_EVERY_SIX_HOUR           = "每6小时";
    public final static String CNSTR_EVERY_EIGHT_HOUR         = "每8小时";
    public final static String CNSTR_EVERY_TWELVE_HOUR        = "每12小时";


    public final static String  STR_ITEM_TITLE      = "ITEM_TITLE";
    public final static String  STR_ITEM_TYPE       = "ITEM_TYPE";
    public final static String  STR_ITEM_TEXT       = "ITEM_TEXT";
    public final static String  STR_ITEM_ID         = "ITEM_ID";
    public final static String  STR_ITEM_JOBNAME    = "ITEM_JOBNAME";
    public final static String  STR_ITEM_PHOTOSIZE  = "ITEM_PHOTOSIZE";

    public final static String  STR_CAMERA_DPI      = "CAMERA_DPI";


    public final static String  STR_CAMERAJOB_RUN           = "running";
    public final static String  STR_CAMERAJOB_PAUSE         = "pause";
    public final static String  STR_CAMERAJOB_STOP          = "stopped";
    public final static String  STR_CAMERAJOB_UNKNOWN       = "unknown";

    public final static String  STR_CAMERAPROPERTIES_NAME       = "camera_properties";
    public final static String  STR_PROPERTIES_FILENAME         = "appConfig.properties";
    public final static String  STR_PROPERTIES_CAMERA_FACE      = "camera_face";
    public final static String  STR_PROPERTIES_CAMERA_DPI       = "camera_dpi";
    public final static String  STR_PROPERTIES_CAMERA_AUTOFLASH = "camera_autoflash";
    public final static String  STR_PROPERTIES_CAMERA_AUTOFOCUS = "camera_autofocus";

}


