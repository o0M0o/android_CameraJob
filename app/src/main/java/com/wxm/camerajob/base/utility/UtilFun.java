package com.wxm.camerajob.base.utility;

import android.util.Size;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * 工具类
 * Created by 123 on 2016/6/16.
 */
public class UtilFun {

    /**
     * 可抛出类打印字符串
     * @param e 可抛出类
     * @return 字符串
     */
    public static String ThrowableToString(Throwable e) {
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw =  new PrintWriter(sw);
            pw.append(e.getMessage());
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (pw != null) {
                pw.close();
            }
        }

        return sw.toString();
    }

    /**
     * 异常 --> 字符串
     * @param e 异常
     * @return 字符串
     */
    public static String ExceptionToString(Exception e) {
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw =  new PrintWriter(sw);
            //将出错的栈信息输出到printWriter中
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (pw != null) {
                pw.close();
            }
        }

        return sw.toString();
    }


    /**
     * 日历类到字符串
     * @param cl 日历类
     * @return 结果
     */
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

    /**
     * 毫秒数到字符串
     * @param ms 1970年以来的毫秒数
     * @return 结果
     */
    public static String MilliSecsToString(long ms) {
        Calendar cl = Calendar.getInstance();
        cl.setTimeInMillis(ms);
        return CalenderToString(cl);
    }

    /**
     * 时间戳转换到字符串
     * @param ts 时间戳
     * @return 结果
     */
    public static String TimestampToString(Timestamp ts)    {
        Calendar cl = Calendar.getInstance();
        cl.setTimeInMillis(ts.getTime());
        return CalenderToString(cl);
    }


    /**
     * Size转换到字符串
     * @param sz 待转换Size
     * @return 结果
     */
    public static String SizeToString(Size sz) {
        return Integer.toString(sz.getWidth())
                + " X " + Integer.toString(sz.getHeight());
    }

    /**
     * 字符串转换到Size
     * @param str 待转换字符串
     * @return 结果
     */
    public static Size StringToSize(String str)     {
        String[] sz = str.split(" X ");
        if(2 != sz.length)
            return new Size(0, 0);


        return new Size(Integer.parseInt(sz[0]), Integer.parseInt(sz[1]));
    }
}
