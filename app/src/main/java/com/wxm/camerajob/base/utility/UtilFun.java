package com.wxm.camerajob.base.utility;

import java.sql.Timestamp;
import java.util.Calendar;

/**
 * 工具类
 * Created by 123 on 2016/6/16.
 */
public class UtilFun {
    public static String CalenderToString(Calendar cl)  {
        String ret = String.format(
                "%d-%02d-%02d %02d:%02d:%02d"
                ,cl.get(Calendar.YEAR)
                ,cl.get(Calendar.MONTH) + 1
                ,cl.get(Calendar.DAY_OF_MONTH) + 1
                ,cl.get(Calendar.HOUR_OF_DAY)
                ,cl.get(Calendar.MINUTE)
                ,cl.get(Calendar.SECOND));
        return ret;
    }

    public static String TimestampToString(Timestamp ts)    {
        Calendar cl = Calendar.getInstance();
        cl.setTimeInMillis(ts.getTime());
        return CalenderToString(cl);
    }
}
