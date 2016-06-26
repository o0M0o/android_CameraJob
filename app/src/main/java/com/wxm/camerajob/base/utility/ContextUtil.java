package com.wxm.camerajob.base.utility;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Environment;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.handler.GlobalContext;
import com.wxm.camerajob.base.receiver.AlarmReceiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by 123 on 2016/5/7.
 * 获取全局context
 */
public class ContextUtil extends Application    {
    private static final String TAG = "ContextUtil";
    private static final String INFO_FN = "info.json";

    public SilentCameraHelper   mSCHHandler;
    private String              mAppRootDir;
    private String              mAppPhotoRootDir;

    private static ContextUtil instance;
    public static ContextUtil getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);

        instance = this;
        mSCHHandler = new SilentCameraHelper(PreferencesUtil.loadCameraParam());

        // 初始化context
        String en= Environment.getExternalStorageState();
        if(en.equals(Environment.MEDIA_MOUNTED)){
            File sdcardDir =Environment.getExternalStorageDirectory();
            String path = sdcardDir.getPath()+"/CamerajobPhotos";
            File path1 = new File(path);
            if (!path1.exists()) {
                path1.mkdirs();
            }

            mAppRootDir = sdcardDir.getPath();
            mAppPhotoRootDir = path;
        }else{
            File innerPath = ContextUtil.getInstance().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            mAppRootDir = ContextUtil.getInstance().getExternalFilesDir(null).getPath();
            mAppPhotoRootDir = innerPath.getPath();
        }

        GlobalContext.getInstance().initContext();

        // 设置闹钟
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + GlobalDef.INT_GLOBALJOB_PERIOD, pendingIntent);

        Log.i(TAG, "Application created");
        FileLogger.getLogger().info("Application created");
    }

    @Override
    public void onTerminate()  {
        Log.i(TAG, "Application onTerminate");
        FileLogger.getLogger().info("Application Terminate");
//        JobScheduler tm = (JobScheduler)getSystemService(Context.JOB_SCHEDULER_SERVICE);
//        tm.cancelAll();

        super.onTerminate();
    }


    /**
     * 捕获错误信息的handler
     */
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            //FileLogger.getLogger().severe("App崩溃");
            FileLogger.getLogger().severe(UtilFun.ThrowableToString(ex));
        }
    };


    /**
     * 得到camerajob的图片路径
     * @param cj 待查camerajob
     * @return 图片文件夹路径
     */
    public String getCameraJobPhotoDir(CameraJob cj)    {
        File p = new File(mAppPhotoRootDir + "/" + cj._id);
        if(!p.exists()) {
            p.mkdirs();
            FileWriter fw = null;
            if(p.exists())  {
                try {
                    fw = new FileWriter(new File(p.getPath(), INFO_FN));
                    JsonWriter jw = new JsonWriter(fw);
                    cj.writeToJson(jw);
                } catch (IOException e) {
                    e.printStackTrace();
                    FileLogger.getLogger().severe("write camerajob("
                                                + cj.toString() + ") to file failed");
                } finally {
                    if(null != fw)  {
                        try {
                            fw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else  {
            FileWriter fw = null;
            try {
                fw = new FileWriter(new File(p.getPath(), INFO_FN));
                JsonWriter jw = new JsonWriter(fw);
                cj.writeToJson(jw);
            } catch (IOException e) {
                e.printStackTrace();
                FileLogger.getLogger().severe("write camerajob("
                        + cj.toString() + ") to file failed");
            } finally {
                if(null != fw)  {
                    try {
                        fw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return p.getPath();
    }

    /**
     * 从目录路径得到camerajob数据
     * @param path  目录路径
     * @return      此目录对应的camerajob
     */
    public CameraJob getCameraJobFromPath(String path)  {
        CameraJob ret = null;
        File p = new File(path, INFO_FN);
        if(p.exists()) {
            FileReader fw = null;
            try {
                fw = new FileReader(p);
                JsonReader jw = new JsonReader(fw);
                ret = CameraJob.readFromJson(jw);
            } catch (IOException e) {
                e.printStackTrace();
                FileLogger.getLogger().severe("read camerajob form '"
                        + path + "' failed");
            } finally {
                if(null != fw)  {
                    try {
                        fw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return ret;
    }

    /**
     * 得到camerajob的图片路径
     * 此路径必须是已经存在的
     * @param cj_id 待查camerajob的_id
     * @return 图片文件夹路径或者是空字符串
     */
    public String getCameraJobPhotoDir(int cj_id)    {
        File p = new File(mAppPhotoRootDir + "/" + cj_id);
        if(!p.exists()) {
            return "";
        }

        return p.getPath();
    }

    /**
     * 得到app的图片根目录
     * @return 图片根路径
     */
    public String getAppPhotoRootDir()  {
        return mAppPhotoRootDir;
    }
}
