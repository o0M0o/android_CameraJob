package com.wxm.camerajob.utility

import android.os.Environment
import android.support.design.BuildConfig
import android.util.Log
import wxm.androidutil.util.UtilFun
import java.io.File
import java.io.IOException
import java.util.*
import java.util.logging.*

/**
 * log in file
 * Created by WangXM on 2016/6/18.
 */
class FileLogger private constructor() {
    private val mLogTag: String = "P" + System.currentTimeMillis() % 100000
    private lateinit var mLogger: Logger

    init {
        val fn = String.format(Locale.CHINA, LOG_NAME, mLogTag)
        val logFileName = "${ContextUtil.getAppRootDir()}/$fn".apply{
            File(this).let {
                    if (!it.exists()) {
                        it.mkdirs()
                    }
                }
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

                mLogger = Logger.getLogger("cameraJobRunLog")
                mLogger.addHandler(it)
            }

            @Suppress("ConstantConditionIf")
            mLogger.level = if (BuildConfig.DEBUG) Level.INFO else Level.WARNING
        } catch (e: IOException) {
            Log.e(LOG_TAG, "create file for log failure", e)
        }
    }

    companion object {
        private val LOG_TAG = ::FileLogger.javaClass.simpleName
        private const val LOG_NAME = "cameraJob_run_%s.log"

        private val instance: FileLogger = FileLogger()
        var logger: Logger = instance.mLogger
    }
}
