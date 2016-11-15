package com.wxm.camerajob.base.utility;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.wxm.camerajob.BuildConfig;
import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.handler.GlobalContext;
import com.wxm.camerajob.base.receiver.AlarmReceiver;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.wxm.andriodutillib.util.UtilFun;

/**
 * Created by 123 on 2016/5/7.
 * 获取全局context
 */
public class ContextUtil extends Application {
    private static final String TAG = "ContextUtil";
    private static final String INFO_FN = "info.json";
    private static final String SELF_PACKAGE_NAME = "com.wxm.camerajob";

    private List<Activity> activities = new ArrayList<Activity>();

    private SilentCameraHelper mSCHHandlerNew;
    @SuppressWarnings("FieldCanBeLocal")
    private String mAppRootDir;
    private String mAppPhotoRootDir;

    private static ContextUtil instance;

    public static ContextUtil getInstance() {
        return instance;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);

        instance = this;
        //initAppContext();
    }

    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    public void initAppContext() {
        if (checkCameraHardware(this)) {
            mSCHHandlerNew = new SilentCameraHelper();
        } else {
            mSCHHandlerNew = null;
        }

        // 初始化context
        String en = Environment.getExternalStorageState();
        if (en.equals(Environment.MEDIA_MOUNTED)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            String path = sdcardDir.getPath() + "/CamerajobPhotos";
            File path1 = new File(path);
            if (!path1.exists()) {
                path1.mkdirs();
            }

            mAppRootDir = sdcardDir.getPath();
            mAppPhotoRootDir = path;
        } else {
            try {
                File innerPath = ContextUtil.getInstance().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File rootPath = ContextUtil.getInstance().getExternalFilesDir(null);
                assert innerPath != null && rootPath != null;

                mAppRootDir = rootPath.getPath();
                mAppPhotoRootDir = innerPath.getPath();
            } catch (NullPointerException e) {
                FileLogger.getLogger().severe(UtilFun.ExceptionToString(e));
            }
        }

        GlobalContext.getInstance().initContext();

        // 设置闹钟
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + GlobalDef.INT_GLOBALJOB_PERIOD, pendingIntent);

        Log.i(TAG, "Application created");
        FileLogger.getLogger().info("Application created");
    }

    @Override
    public void onTerminate() {
        Log.i(TAG, "Application onTerminate");
        FileLogger.getLogger().info("Application Terminate");
//        JobScheduler tm = (JobScheduler)getSystemService(Context.JOB_SCHEDULER_SERVICE);
//        tm.cancelAll();

        super.onTerminate();

        for (Activity activity : activities) {
            activity.finish();
        }

        System.exit(0);
    }


    public static SilentCameraHelper getCameraHelper() {
        if (null != instance)
            return instance.mSCHHandlerNew;
        else
            return null;
    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }


    /**
     * 是否使用新相机API
     *
     * @return 如果使用新相机API则返回true
     */
    public static boolean useNewCamera() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }


    /**
     * 捕获错误信息的handler
     */
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            //FileLogger.getLogger().severe("App崩溃");
            //FileLogger.getLogger().severe(UtilFun.ThrowableToString(ex));
            Log.e(TAG, UtilFun.ThrowableToString(ex));
        }
    };


    /**
     * 得到camerajob的图片路径
     *
     * @param cj 待查camerajob
     * @return 图片文件夹路径
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public String createCameraJobPhotoDir(CameraJob cj) {
        File p = new File(mAppPhotoRootDir + "/" + cj.get_id());
        if (!p.exists()) {
            p.mkdirs();
        }

        if (p.exists()) {
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
                if (null != fw) {
                    try {
                        fw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return p.getPath();
        }

        return "";
    }

    /**
     * 从目录路径得到camerajob数据
     *
     * @param path 目录路径
     * @return 此目录对应的camerajob
     */
    public CameraJob getCameraJobFromPath(String path) {
        CameraJob ret = null;
        File p = new File(path, INFO_FN);
        if (p.exists()) {
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
                if (null != fw) {
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
     *
     * @param cj_id 待查camerajob的_id
     * @return 图片文件夹路径或者是空字符串
     */
    public String getCameraJobPhotoDir(int cj_id) {
        File p = new File(mAppPhotoRootDir + "/" + cj_id);
        if (!p.exists()) {
            return "";
        }

        return p.getPath();
    }

    /**
     * 得到app的图片根目录
     *
     * @return 图片根路径
     */
    public String getAppPhotoRootDir() {
        return mAppPhotoRootDir;
    }


    /**
     * 获取包版本号
     *
     * @param context 包上下文
     * @return 包版本号
     */
    public static int getVerCode(Context context) {
        int verCode = -1;
        try {
            verCode = context.getPackageManager().getPackageInfo(SELF_PACKAGE_NAME, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        return verCode;
    }


    /**
     * 获取包版本名
     *
     * @param context 包上下文
     * @return 包版本名
     */
    public static String getVerName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().getPackageInfo(SELF_PACKAGE_NAME, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        return verName;
    }

    /**
     * 在测试版本满足条件后抛出异常
     *
     * @param bThrow 若true则抛出异常
     */
    public static void throwExIf(boolean bThrow) throws AssertionError {
        if (BuildConfig.ThrowDebugException && bThrow) {
            throw new AssertionError("测试版本出现异常");
        }
    }

    /**
     * 设置layout可见性
     * 仅调整可见性，其它设置保持不变
     *
     * @param visible 若为 :
     *                1. {@code View.INVISIBLE}, 不可见
     *                2. {@code View.VISIBLE}, 可见
     */
    public static void setViewGroupVisible(ViewGroup rl, int visible) {
        ViewGroup.LayoutParams param = rl.getLayoutParams();
        param.width = rl.getWidth();
        param.height = View.INVISIBLE != visible ? ViewGroup.LayoutParams.WRAP_CONTENT : 0;
        rl.setLayoutParams(param);
    }
}
