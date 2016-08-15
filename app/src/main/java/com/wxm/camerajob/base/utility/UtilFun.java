package com.wxm.camerajob.base.utility;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Build;
import android.util.Size;

import com.wxm.camerajob.base.data.MySize;

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
import java.util.Locale;

/**
 * 工具类
 * Created by 123 on 2016/6/16.
 */
@SuppressWarnings("WeakerAccess")
public class UtilFun {

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        /*
        try {
            return (T) obj;
        }catch (ClassCastException | NullPointerException e)   {
            Log.e(TAG, ToolUtil.ExceptionToString(e));
        }

        return null;
        */
        return (T) obj;
    }

    /**
     * 检查字符串是否空或者null
     * @param cstr  待检查字符串
     * @return   检查结果
     */
    public static boolean StringIsNullOrEmpty(String cstr)      {
        return null == cstr || cstr.isEmpty();
    }

    /**
     * 删除目录
     * @param path  待删除目录路径
     */
    public static void DeleteDirectory(String path)  {
        File f = new File(path);
        if(f.isDirectory()) {
            File[] childFiles = f.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                f.delete();
            } else  {
                for(File ff : childFiles)   {
                    ff.delete();
                }

                f.delete();
            }
        }
    }


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
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
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
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static String SizeToString(Size sz) {
        return Integer.toString(sz.getWidth())
                + " X " + Integer.toString(sz.getHeight());
    }

    /**
     * 字符串转换到Size
     * @param str 待转换字符串
     * @return 结果
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Size StringToSize(String str)     {
        String[] sz = str.split(" X ");
        if(2 != sz.length)
            return new Size(0, 0);


        return new Size(Integer.parseInt(sz[0]), Integer.parseInt(sz[1]));
    }


    /**
     * MySize转换到字符串
     * @param sz 待转换Size
     * @return 结果
     */
    public static String MySizeToString(MySize sz)  {
        return Integer.toString(sz.getWidth())
                + " X " + Integer.toString(sz.getHeight());
    }

    /**
     * 字符串转换到MySize
     * @param str 待转换字符串
     * @return 结果
     */
    public static MySize StringToMySize(String str)     {
        String[] sz = str.split(" X ");
        if(2 != sz.length)
            return new MySize(0, 0);

        return new MySize(Integer.parseInt(sz[0]), Integer.parseInt(sz[1]));
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
     * @param wsz  想要的bitmap尺寸（可以为null)
     * @return 结果
     */
    public static Bitmap getRotatedLocalBitmap(String url, MySize wsz) {
        return rotateBitmap(getLocalBitmap(url), readPictureDegree(url), wsz);
    }

    /**
     * 旋转图片，使图片保持正确的方向。
     * @param bitmap 原始图片
     * @param degrees 原始图片的角度
     * @param wantSZ  想要的bitmap尺寸（可以为null)
     * @return Bitmap 旋转后的图片
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degrees, MySize wantSZ) {
        if (degrees == 0 || null == bitmap) {
            return bitmap;
        }

        Matrix matrix = new Matrix();
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        matrix.setRotate(degrees, w / 2, h / 2);

        if(null != wantSZ)  {
            if((w > wantSZ.getWidth()) && (h > wantSZ.getHeight())) {
                float scaleWidth = ((float) wantSZ.getWidth()) / w;
                float scaleHeight = ((float) wantSZ.getHeight()) / h;
                float gscale = scaleWidth > scaleHeight ? scaleWidth : scaleHeight;
                matrix.postScale(gscale, gscale);
            }
        }

        Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        bitmap.recycle();
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
        for (File f : files) {
            if (f.isFile()) {
                if (f.getPath().substring(f.getPath().length() - Extension.length()).equals(Extension))
                    ret.add(f.getPath());
            } else if (f.isDirectory() && !f.getPath().contains("/.")) {
                //忽略点文件（隐藏文件/文件夹）
                if (IsIterative)
                    getDirFiles(f.getPath(), Extension, true);
            }
        }

        return ret;
    }


    /**
     *  遍历文件夹，搜索指定扩展名的文件
     * @param Path          搜索目录
     * @param Extension     扩展名
     * @param IsIterative   是否进入子文件夹
     * @return  满足条件的文件数量
     */
    public static int getDirFilesCount(String Path, String Extension, boolean IsIterative)
    {
        int ret = 0;
        File[] files =new File(Path).listFiles();
        for (File f : files) {
            if (f.isFile()) {
                if (f.getPath().substring(f.getPath().length() - Extension.length()).equals(Extension))
                    ret++;
            } else if (f.isDirectory() && !f.getPath().contains("/.")) {
                //忽略点文件（隐藏文件/文件夹）
                if (IsIterative)
                    getDirFiles(f.getPath(), Extension, true);
            }
        }

        return ret;
    }

    /**
     * 遍历文件夹，搜索子文件夹
     * @param path              搜索目录
     * @param isInterative      是否进入子路径
     * @return  满足条件的子文件夹
     */
    public static LinkedList<String> getDirDirs(String path, boolean isInterative)  {
        LinkedList<String> ret = new LinkedList<>();
        File[] files =new File(path).listFiles();
        for(File f : files)     {
            if (f.isDirectory())     {
                if(!f.getPath().contains("/."))     {
                    ret.add(f.getPath());

                    if(isInterative)
                        getDirDirs(f.getPath(), true);
                }
            }
        }

        return ret;
    }
}
