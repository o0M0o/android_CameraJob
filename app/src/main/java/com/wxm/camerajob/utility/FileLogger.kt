package com.wxm.camerajob.utility

import android.os.Environment
import android.support.design.BuildConfig
import wxm.androidutil.util.UtilFun
import java.io.File
import java.io.IOException
import java.util.*
import java.util.logging.*

/**
 * log in file
 * Created by 123 on 2016/6/18.
 */
class FileLogger private constructor() {
    private val mLogTag: String = "P" + System.currentTimeMillis() % 100000
    private lateinit var mLogger: Logger

    init {
        val logFileName = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            Environment.getExternalStorageDirectory().path + "/CameraJobLogs".let {
                File(it).let {
                    if (!it.exists()) {
                        it.mkdirs()
                    }
                }

                "$it/$LOG_NAME"
            }
        } else {
            val innerPath = ContextUtil.Companion.instance.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!
            innerPath.path + "/" + LOG_NAME
        }

        try {
            FileHandler(logFileName, true).let {
                it.formatter = object : SimpleFormatter() {
                    override fun format(record: LogRecord): String {
                        return String.format(Locale.CHINA,
                                "%s|%s|%s-%d|%s:%s|%s",
                                UtilFun.MilliSecsToString(record.millis),
                                record.level.name,
                                mLogTag, record.threadID,
                                record.sourceClassName,
                                record.sourceMethodName,
                                formatMessage(record)) + System.lineSeparator()
                    }
                }

                mLogger = Logger.getLogger("camerajob_runlog")
                mLogger.addHandler(it)
            }

            @Suppress("ConstantConditionIf")
            mLogger.level = if (BuildConfig.DEBUG) Level.INFO else Level.WARNING
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    companion object {
        private const val LOG_NAME = "camerajob_run_%g.log"

        var instance: FileLogger = FileLogger()
        var logger: Logger = instance.mLogger
    }
}
