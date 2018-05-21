package com.wxm.camerajob.utility.log

import android.support.design.BuildConfig
import android.util.Log
import com.wxm.camerajob.utility.context.ContextUtil
import wxm.androidutil.util.UtilFun
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
        Log.i(LOG_TAG, "init file logger")

        try {
            FileHandler(
                    "${ContextUtil.getLogRootDir()}/${String.format(Locale.CHINA, LOG_NAME, mLogTag)}",
                    true).let {
                it.formatter = object : SimpleFormatter() {
                    override fun format(record: LogRecord): String {
                        return String.format(Locale.CHINA,
                                "%s|%s|%s-%d|%s:%s|%s",
                                UtilFun.MilliSecsToString(record.millis),
                                record.level.name, mLogTag, record.threadID,
                                record.sourceClassName, record.sourceMethodName,
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

        /**
         * get logger to use
         */
        fun getLogger(): Logger {
            return instance.mLogger
        }
    }
}
