package com.wxm.camerajob.utility;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import com.wxm.camerajob.data.db.CameraJobDBUtility;
import com.wxm.camerajob.data.db.CameraJobStatusDBUtility;
import com.wxm.camerajob.data.db.DBOrmLiteHelper;
import com.wxm.camerajob.data.define.CameraJob;
import com.wxm.camerajob.data.define.GlobalDef;
import com.wxm.camerajob.receiver.AlarmReceiver;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import wxm.androidutil.util.UtilFun;

/**
 * get global context
 * Created by 123 on 2016/5/7.
 */
public class ContextUtil extends Application {
    private static final String TAG = "ContextUtil";
    private static final String INFO_FN = "info.json";
    private static final String SELF_PACKAGE_NAME = "com.wxm.camerajob";

    private static ContextUtil instance;

    private List<Activity> activities = new ArrayList<>();

    @SuppressWarnings("FieldCanBeLocal")
    private String mAppRootDir;
    private String mAppPhotoRootDir;

    private GlobalMsgHandler    mMsgHandler;
    private CameraJobProcess     mJobProcessor;

    // for db
    private CameraJobDBUtility          mCameraJobUtility;
    private CameraJobStatusDBUtility    mCameraJobStatusUtility;


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

    /**
     * init app context
     * -- create phone path
     * -- set alarm
     */
    public void initAppContext() {
        String en = Environment.getExternalStorageState();
        if (en.equals(Environment.MEDIA_MOUNTED)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            String path = sdcardDir.getPath() + "/CamerajobPhotos";

            File fpath = new File(path);
            boolean bexist = fpath.exists();
            if (!bexist) {
                bexist = fpath.mkdirs();
            }

            mAppRootDir = sdcardDir.getPath();
            mAppPhotoRootDir = bexist ? path : mAppRootDir;
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

        // for db
        DBOrmLiteHelper helper = new DBOrmLiteHelper(ContextUtil.getInstance());
        mCameraJobUtility = new CameraJobDBUtility(helper);
        mCameraJobStatusUtility = new CameraJobStatusDBUtility(helper);

        // for job
        mMsgHandler = new GlobalMsgHandler();
        mJobProcessor = new CameraJobProcess();
        mJobProcessor.init();

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

        super.onTerminate();

        for (Activity activity : activities) {
            activity.finish();
        }

        System.exit(0);
    }

    public static Handler GetMsgHandlder()   {
        return UtilFun.cast(instance.mMsgHandler);
    }

    public static CameraJobProcess GetJobProcess()   {
        return instance.mJobProcessor;
    }

    public static CameraJobDBUtility GetCameraJobUtility() {
        return instance.mCameraJobUtility;
    }

    public static CameraJobStatusDBUtility GetCameraJobStatusUtility() {
        return instance.mCameraJobStatusUtility;
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
     * check whether use new camera api
     * @return  true if use new camera api else false
     */
    public static boolean useNewCamera() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }


    /**
     * handler for app crash
     */
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = (thread, ex) -> {
        //FileLogger.getLogger().severe("App崩溃");
        //FileLogger.getLogger().severe(UtilFun.ThrowableToString(ex));
        Log.e(TAG, UtilFun.ThrowableToString(ex));
    };


    /**
     * create photo directory for job
     * @param cj    job
     * @return      photo directory for cj
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
     * use directory path get job data
     * @param path      directory path
     * @return          camera job
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
     * get job photo directory according to job id
     * @param cj_id     id for camera job
     * @return          photo directory path for job or ""
     */
    public String getCameraJobPhotoDir(int cj_id) {
        File p = new File(mAppPhotoRootDir + "/" + cj_id);
        if (!p.exists()) {
            return "";
        }

        return p.getPath();
    }

    /**
     * get photo root directory
     * @return  photo root directory
     */
    public String getAppPhotoRootDir() {
        return mAppPhotoRootDir;
    }


    /**
     * get version number for package
     * @param context       for package
     * @return              version number
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
     * get version name for package
     * @param context       context for package
     * @return              version name
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
}
