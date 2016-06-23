package com.wxm.camerajob.base.utility;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.handler.GlobalContext;
import com.wxm.camerajob.base.receiver.AlarmReceiver;

import java.io.File;

/**
 * Created by 123 on 2016/5/7.
 * 获取全局context
 */
public class ContextUtil extends Application    {
    private static final String TAG = "ContextUtil";

    public SilentCameraHelper mSCHHandler;
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
        File p = new File(mAppPhotoRootDir + "/" + cj.job_name);
        if(!p.exists()) {
            p.mkdirs();
        }

        return p.getPath();
    }

    /**
     * 得到camerajob的图片路径
     * @param cj 待查camerajob的job_name
     * @return 图片文件夹路径
     */
    public String getCameraJobPhotoDir(String cj)    {
        File p = new File(mAppPhotoRootDir + "/" + cj);
        if(!p.exists()) {
            p.mkdirs();
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
