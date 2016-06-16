package com.wxm.camerajob.base.data;

import android.hardware.camera2.CameraCharacteristics;
import android.os.Environment;
import android.util.Size;

import com.wxm.camerajob.base.utility.ContextUtil;

import java.io.File;

/**
 * 拍照参数
 * Created by 123 on 2016/6/13.
 */
public class TakePhotoParam {
    public int      mFace;
    public Size     mPhotoSize;
    public String   mPhotoFileDir;
    public String   mFileName;

    public String   mTag;

    public TakePhotoParam(String fn) {
        mFace = CameraCharacteristics.LENS_FACING_BACK;
        mPhotoSize = new Size(1280, 960);

        mTag = Integer.toString(GlobalDef.INT_INVALID_ID);
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
