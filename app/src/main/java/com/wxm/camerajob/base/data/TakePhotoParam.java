package com.wxm.camerajob.base.data;

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

    public TakePhotoParam(String pp, String fn, String tag) {
        mWaitMSecs = WAIT_MSECS;

        mTag = tag;
        mFileName = fn;
        mPhotoFileDir = pp;
    }
}
