package com.wxm.camerajob.data.define;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Locale;

/**
 * 拍照参数
 * Created by 123 on 2016/6/13.
 */
public class TakePhotoParam {
    private final static int  WAIT_MSECS = 8000;

    public String   mPhotoFileDir;
    public String   mFileName;

    public String   mTag;
    public long     mWaitMSecs;

    public Timestamp    mTS;

    public TakePhotoParam(String pp, String fn, String tag) {
        mWaitMSecs = WAIT_MSECS;

        mTag = tag;
        mFileName = fn;
        mPhotoFileDir = pp;

        mTS = new Timestamp(Calendar.getInstance(Locale.CANADA).getTimeInMillis());
    }


    public TakePhotoParam() {
        mTS = new Timestamp(Calendar.getInstance(Locale.CANADA).getTimeInMillis());
    }
}
