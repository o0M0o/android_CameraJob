package com.wxm.camerajob.utility

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.os.Build
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
import com.wxm.camerajob.utility.job.CameraJobProcess
import com.wxm.camerajob.utility.job.GlobalMsgHandler
import wxm.androidutil.app.AppBase
import wxm.androidutil.improve.doJudge
import wxm.androidutil.improve.let1
import wxm.androidutil.log.TagLog
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
class AppUtil : AppBase() {
    private val activities = ArrayList<Activity>()
    private lateinit var mAppRootDir: String
    private lateinit var mLogDir: String

    /**
     * get photo root directory
     * @return  photo root directory
     */
    private lateinit var mPhotoDir: String

    private lateinit var mMsgHandler: GlobalMsgHandler
    private lateinit var mJobProcessor: CameraJobProcess

    // for db
    private lateinit var mCameraJobUtility: CameraJobDBUtility
    private lateinit var mCameraJobStatusUtility: CameraJobStatusDBUtility

    override fun onTerminate() {
        TagLog.i("Application onTerminate")
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
        // for dir
        mAppRootDir = filesDir.path

        "$mAppRootDir/photo".let1 {
            mPhotoDir = File(it).let {
                it.exists().doJudge(true, it.mkdirs())
            }.doJudge(it, mAppRootDir)
        }

        "$mAppRootDir/runLog".let1 {
            mLogDir = File(it).let {
                it.exists().doJudge(true, it.mkdirs())
            }.doJudge(it, mAppRootDir)
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

        TagLog.i("Application created")
    }

    /**
     * Check if this device has a camera
    private fun checkCameraHardware(context: Context): Boolean {
        // this device has a camera
        // no camera on this device
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }
     */

    companion object {
        private const val INFO_FN = "info.json"

        @Suppress("MemberVisibilityCanBePrivate")
        val self: AppUtil
            get() = (AppBase.appContext() as AppUtil)

        fun initUtil() {
            self.initAppContext()
        }

        fun leaveAPP() {
            self.onTerminate()
        }

        fun getLogRootDir(): String {
            return self.mLogDir
        }

        fun getAppRootDir(): String {
            return self.mAppRootDir
        }

        fun getPhotoRootDir(): String {
            return self.mPhotoDir
        }

        fun addActivity(activity: Activity) {
            self.apply {
                activities.add(activity)
            }
        }

        /**
         * create job directory for job [cj]
         * when success return directory path, else null
         */
        fun createJobDir(cj: CameraJob): String? {
            File(getPhotoRootDir() + "/" + cj._id).let {
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
        fun getCameraJobFromDir(path: String): CameraJob? {
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
        fun getCameraJobDir(cj_id: Int): String? {
            return File("${getPhotoRootDir()}/$cj_id").let {
                if (it.exists()) it.path else null
            }
        }

        fun getWindowManager(): WindowManager? {
            return getSystemService(Context.WINDOW_SERVICE)
        }

        fun getCameraManager(): CameraManager? {
            return getSystemService(Context.CAMERA_SERVICE)
        }

        fun getMsgHandler(): Handler {
            return UtilFun.cast<Handler>(self.mMsgHandler)
        }

        fun getJobProcess(): CameraJobProcess {
            return self.mJobProcessor
        }

        fun getCameraJobUtility(): CameraJobDBUtility {
            return self.mCameraJobUtility
        }

        fun getCameraJobStatusUtility(): CameraJobStatusDBUtility {
            return self.mCameraJobStatusUtility
        }

        /**
         * check whether use new camera api
         * @return  true if use new camera api else false
         */
        @SuppressLint("ObsoleteSdkInt")
        fun useNewCamera(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        }
    }
}
