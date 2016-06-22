package com.wxm.camerajob.base.utility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.util.Size;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

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
            //pw.append(e.getMessage());
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
                ,cl.get(Calendar.DAY_OF_MONTH)
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
     * 时间字符串转换到时间戳
     * @param str   待转换时间字符串
     * @return 结果
     */
    public static Timestamp StringToTimestamp(String str)   {
        Timestamp ts = new Timestamp(0);
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = format.parse(str);
            ts.setTime(date.getTime());
        } catch (ParseException ex)     {
            ts = new Timestamp(0);
        }

        return  ts;
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


    /**
     * 加载本地图片
     * @param url  本地图片文件地址
     * @return 结果
     */
    public static Bitmap getLocalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 加载本地图片
     * @param url  本地图片文件地址
     * @return 结果
     */
    public static Bitmap getRotatedLocalBitmap(String url) {
        return rotateBitmap(getLocalBitmap(url), readPictureDegree(url));
    }

    /**
     * 旋转图片，使图片保持正确的方向。
     * @param bitmap 原始图片
     * @param degrees 原始图片的角度
     * @return Bitmap 旋转后的图片
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (degrees == 0 || null == bitmap) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                            bitmap.getHeight(), matrix, true);
        if (null != bitmap) {
            bitmap.recycle();
        }
        return bmp;
    }

    /**
     * 读取图片属性：旋转的角度
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return degree;
        }
        return degree;
    }


    /**
     * 转换本地图片到drawable
     * @param url 本地图片地址
     * @return drawable结果
     */
    public static Drawable getLocalDrawable(String url) {
        Bitmap bm = getLocalBitmap(url);
        if(null == bm)
            return null;

        return new BitmapDrawable(ContextUtil.getInstance().getResources(),
                bm);
    }


    /**
     *  遍历文件夹，搜索指定扩展名的文件
     * @param Path          搜索目录
     * @param Extension     扩展名
     * @param IsIterative   是否进入子文件夹
     * @return  满足条件的文件名
     */
    public static LinkedList<String> getDirFiles(String Path, String Extension, boolean IsIterative)
    {
        LinkedList<String> ret = new LinkedList<>();
        File[] files =new File(Path).listFiles();
        for (int i =0; i < files.length; i++)   {
            File f = files[i];
            if (f.isFile())     {
                if (f.getPath().substring(f.getPath().length() - Extension.length()).equals(Extension))
                    ret.add(f.getPath());

                if (!IsIterative)
                    break;
            }
            else if (f.isDirectory() && f.getPath().indexOf("/.") == -1) {
                //忽略点文件（隐藏文件/文件夹）
                getDirFiles(f.getPath(), Extension, IsIterative);
            }
        }

        return ret;
    }
}
