package com.wxm.camerajob.utility

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.util.JsonReader
import android.util.JsonWriter
import android.util.Log
import android.view.WindowManager
import com.wxm.camerajob.alarm.AlarmReceiver
import com.wxm.camerajob.data.db.CameraJobDBUtility
import com.wxm.camerajob.data.db.CameraJobStatusDBUtility
import com.wxm.camerajob.data.db.DBOrmLiteHelper
import com.wxm.camerajob.data.define.CameraJob
import com.wxm.camerajob.data.define.GlobalDef
import wxm.androidutil.util.UtilFun
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.*

/**
 * get global context
 * Created by 123 on 2016/5/7.
 */
class ContextUtil : Application() {
    private val activities = ArrayList<Activity>()
    private lateinit var mAppRootDir: String

    /**
     * get photo root directory
     * @return  photo root directory
     */
    private lateinit var mPhotoRootDir: String

    private lateinit var mMsgHandler: GlobalMsgHandler
    private lateinit var mJobProcessor: CameraJobProcess

    // for db
    private lateinit var mCameraJobUtility: CameraJobDBUtility
    private lateinit var mCameraJobStatusUtility: CameraJobStatusDBUtility

    /**
     * handler for app crash
     */
    private object UncaughtExceptionHandler : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(t: Thread?, e: Throwable?) {
            Log.e(TAG, UtilFun.ThrowableToString(e))
        }
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler)
    }

    override fun onTerminate() {
        Log.i(TAG, "Application onTerminate")

        super.onTerminate()

        activities.forEach { it.finish() }
        System.exit(0)
    }

    /**
     * init app context
     * -- create phone path
     * -- set alarm
     */
    private fun initAppContext() {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val sdcardDir = Environment.getExternalStorageDirectory()
            val path = sdcardDir.path + "/CameraJobPhotos"
            File(path).let {
                if (!it.exists()) {
                    it.mkdirs()
                } else true
            }.let {
                mAppRootDir = sdcardDir.path
                mPhotoRootDir = if (it) path else sdcardDir.path
            }
        } else {
            try {
                val innerPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val rootPath = getExternalFilesDir(null)
                assert(innerPath != null && rootPath != null)

                mAppRootDir = rootPath.path
                mPhotoRootDir = innerPath.path
            } catch (e: NullPointerException) {
            }
        }

        // for db
        DBOrmLiteHelper(this).let {
            mCameraJobUtility = CameraJobDBUtility(it)
            mCameraJobStatusUtility = CameraJobStatusDBUtility(it)
        }

        // for job
        mMsgHandler = GlobalMsgHandler()
        mJobProcessor = CameraJobProcess()

        // 设置闹钟
        PendingIntent.getBroadcast(this, 0,
                Intent(this, AlarmReceiver::class.java), 0).let {
            (getSystemService(Context.ALARM_SERVICE) as AlarmManager).set(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + GlobalDef.INT_GLOBALJOB_PERIOD,
                    it)
        }

        Log.i(TAG, "Application created")
    }

    /**
     * Check if this device has a camera
     */
    private fun checkCameraHardware(context: Context): Boolean {
        // this device has a camera
        // no camera on this device
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }


    /**
     * create photo directory for job
     * @param cj    job
     * @return      photo directory for cj
     */
    private fun createCameraJobPhotoDir(cj: CameraJob): String? {
        File(mPhotoRootDir + "/" + cj._id).let {
            if (!it.exists()) {
                it.mkdirs()
            }

            if (it.exists()) {
                FileWriter(File(it.path, INFO_FN)).use {
                    try {
                        cj.writeToJson(JsonWriter(it))
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    Unit
                }

                return it.path
            }
        }

        return null
    }

    /**
     * use directory path get job data
     * @param path      directory path
     * @return          camera job
     */
    private fun getCameraJobFromPath(path: String): CameraJob? {
        return File(path, INFO_FN).let {
            if (it.exists()) {
                FileReader(it).use {
                    CameraJob.readFromJson(JsonReader(it))
                }
            } else null
        }
    }

    /**
     * get job photo directory according to job id
     * @param cj_id     id for camera job
     * @return          photo directory path for job or ""
     */
    private fun getCameraJobPhotoDir(cj_id: Int): String? {
        return File("$mPhotoRootDir/$cj_id").let {
            if (it.exists()) it.path else null
        }
    }

    companion object {
        private val TAG = ::ContextUtil.javaClass.simpleName
        private const val INFO_FN = "info.json"
        private const val SELF_PACKAGE_NAME = "com.wxm.camerajob"

        private lateinit var instance: ContextUtil

        fun appContext(): ContextUtil = instance

        fun initUtil() {
            instance.initAppContext()
        }

        fun leaveAPP() {
            instance.onTerminate()
        }

        fun getAppRootDir(): String {
            return instance.mAppRootDir
        }

        fun getPhotoRootDir(): String {
            return instance.mPhotoRootDir
        }

        fun addActivity(activity: Activity) {
            instance.apply {
                activities.add(activity)
            }
        }

        fun createJobDir(cj: CameraJob): String? {
            return instance.createCameraJobPhotoDir(cj)
        }

        fun getCameraJobFromDir(path: String): CameraJob? {
            return instance.getCameraJobFromPath(path)
        }

        fun getCameraJobDir(cj_id: Int): String? {
            return instance.getCameraJobPhotoDir(cj_id)
        }


        fun <T> getSystemService(service: String): T? {
            @Suppress("UNCHECKED_CAST")
            return instance.getSystemService(service) as T
        }

        fun getWindowManager(): WindowManager? {
            return getSystemService(Context.WINDOW_SERVICE)
        }

        fun getCameraManager(): CameraManager? {
            return getSystemService(Context.CAMERA_SERVICE)
        }

        fun getMsgHandler(): Handler {
            return UtilFun.cast<Handler>(instance.mMsgHandler)
        }

        fun getJobProcess(): CameraJobProcess {
            return instance.mJobProcessor
        }

        fun getCameraJobUtility(): CameraJobDBUtility {
            return instance.mCameraJobUtility
        }

        fun getCameraJobStatusUtility(): CameraJobStatusDBUtility {
            return instance.mCameraJobStatusUtility
        }

        fun checkPermission(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(instance, permission) ==
                    PackageManager.PERMISSION_GRANTED
        }

        /**
         * check whether use new camera api
         * @return  true if use new camera api else false
         */
        @SuppressLint("ObsoleteSdkInt")
        fun useNewCamera(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        }


        /**
         * get version number for package
         * @param context       for package
         * @return              version number
         */
        fun getVerCode(context: Context): Int {
            var verCode = -1
            try {
                verCode = context.packageManager.getPackageInfo(SELF_PACKAGE_NAME, 0).versionCode
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(TAG, e.message)
            }

            return verCode
        }


        /**
         * get version paraName for package
         * @param context       context for package
         * @return              version paraName
         */
        fun getVerName(context: Context): String {
            var verName = ""
            try {
                verName = context.packageManager.getPackageInfo(SELF_PACKAGE_NAME, 0).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(TAG, e.message)
            }

            return verName
        }
    }
}
