package com.wxm.camerajob.data.define;

/**
 * app的全局设定
 * Created by wxm on 2016/6/10.
 */
public class GlobalDef {
    public final static String STR_LOAD_JOB             = "load_job";
    public final static String STR_LOAD_CAMERASETTING   = "load_camerasetting";
    public final static String STR_LOAD_PHOTODIR        = "load_photodir";

    public final static String CNSTR_JOBTYPE_MINUTELY  = "每分钟";
    public final static String CNSTR_JOBTYPE_HOURLY    = "每小时";
    public final static String CNSTR_JOBTYPE_DAILY     = "每天";

    public final static int MSG_TYPE_WAKEUP                 = 1000;
    public final static int MSG_TYPE_CAMERAJOB_QUERY        = 1102;
    public final static int MSG_TYPE_CAMERAJOB_TAKEPHOTO    = 1104;
    public final static int MSG_TYPE_JOBSHOW_UPDATE         = 1200;
    public final static int MSG_TYPE_CAMERA_MODIFY          = 1300;
    public final static int MSG_TYPE_REPLAY                 = 9000;

    public final static int INTRET_CAMERAJOB_ACCEPT     = 2000;
    public final static int INTRET_CAMERAJOB_GIVEUP     = 2001;
    public final static int INTRET_CS_ACCEPT            = 2010;
    public final static int INTRET_CS_GIVEUP            = 2011;
    public final static int INTRET_ACCEPT               = 2020;
    public final static int INTRET_GIVEUP               = 2021;

    public final static int INTRET_NOTCARE              = 9000;

    public final static int INT_INVALID_ID              = -1;
    public final static int INT_GLOBALJOB_PERIOD        = 10000;
    public final static int INT_GLOBALJOB_CHECKPERIOD   = 10;

    static public final int INTRET_USR_LOGOUT    = 2001;

    public final static String CNSTR_EVERY_TEN_SECOND         = "10秒";
    public final static String CNSTR_EVERY_TWENTY_SECOND      = "20秒";
    public final static String CNSTR_EVERY_THIRTY_SECOND      = "30秒";

    public final static String CNSTR_EVERY_ONE_MINUTE         = "1分钟";
    public final static String CNSTR_EVERY_TWO_MINUTE         = "2分钟";
    public final static String CNSTR_EVERY_FIVE_MINUTE        = "5分钟";
    public final static String CNSTR_EVERY_TEN_MINUTE         = "10分钟";
    public final static String CNSTR_EVERY_TWENTY_MINUTE      = "20分钟";
    public final static String CNSTR_EVERY_THIRTY_MINUTE      = "30分钟";

    public final static String CNSTR_EVERY_ONE_HOUR           = "1小时";
    public final static String CNSTR_EVERY_TWO_HOUR           = "2小时";
    public final static String CNSTR_EVERY_FOUR_HOUR          = "4小时";
    public final static String CNSTR_EVERY_SIX_HOUR           = "6小时";
    public final static String CNSTR_EVERY_EIGHT_HOUR         = "8小时";
    public final static String CNSTR_EVERY_TWELVE_HOUR        = "12小时";

    static public final String STR_HELP_TYPE            = "HELP_TYPE";
    static public final String STR_HELP_MAIN            = "help_main";

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

    public final static String STR_ERROR_NOFIND_CONTROL = "找不到指定的控件";
}


