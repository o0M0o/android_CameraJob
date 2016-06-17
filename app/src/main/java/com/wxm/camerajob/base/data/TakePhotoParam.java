package com.wxm.camerajob.base.data;

import android.os.Environment;

import com.wxm.camerajob.base.utility.ContextUtil;

import java.io.File;

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

    public TakePhotoParam(String fn, String tag) {
        mWaitMSecs = WAIT_MSECS;

        mTag = tag;
        String en= Environment.getExternalStorageState();
        if(en.equals(Environment.MEDIA_MOUNTED)){
            File sdcardDir =Environment.getExternalStorageDirectory();
            String path = sdcardDir.getPath()+"/CamerajobPhotos";
            File path1 = new File(path);
            if (!path1.exists()) {
                path1.mkdirs();
            }

            mPhotoFileDir = path;
        }else{
            File innerPath = ContextUtil.getInstance().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            mPhotoFileDir = innerPath.getPath();
        }

        mFileName = fn;
    }
}
