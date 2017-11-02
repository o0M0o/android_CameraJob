package com.wxm.camerajob.utility;

import android.os.Environment;
import android.support.design.BuildConfig;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import wxm.androidutil.util.UtilFun;

/**
 * 写在文件中的日志
 * Created by 123 on 2016/6/18.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class FileLogger {
    private final static String LOG_NAME = "camerajob_run_%g.log";

    private String mLogTag;
    private Logger mLoger;

    private static FileLogger instance;

    private static FileLogger getInstance() {
        if (null == instance) {
            instance = new FileLogger();
        }

        return instance;
    }

    public static Logger getLogger() {
        return getInstance().mLoger;
    }

    private FileLogger() {
        mLogTag =  ("P" + System.currentTimeMillis() % 100000);

        String logfn;
        String en = Environment.getExternalStorageState();
        if (en.equals(Environment.MEDIA_MOUNTED)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            String path = sdcardDir.getPath() + "/CamerajobLogs";
            File path1 = new File(path);
            if (!path1.exists()) {
                path1.mkdirs();
            }

            logfn = path + "/" + LOG_NAME;

        } else {
            File innerPath = ContextUtil.getInstance()
                    .getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            assert innerPath != null;
            logfn = innerPath.getPath() + "/" + LOG_NAME;
        }

        try {
            FileHandler mLogFH = new FileHandler(logfn, true);
            mLogFH.setFormatter(new SimpleFormatter() {
                @Override
                public String format(LogRecord record) {

                    String ret = String.format(Locale.CHINA, "%s|%s|%s-%d|%s:%s|%s"
                            , UtilFun.MilliSecsToString(record.getMillis())
                            , record.getLevel().getName()
                            , mLogTag ,record.getThreadID()
                            , record.getSourceClassName(), record.getSourceMethodName()
                            , formatMessage(record)) + (System.lineSeparator());
                    return ret;
                }
            });

            mLoger = Logger.getLogger("camerajob_runlog");
            mLoger.addHandler(mLogFH);

            if(BuildConfig.DEBUG)
                mLoger.setLevel(Level.INFO);
            else
                mLoger.setLevel(Level.WARNING);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
