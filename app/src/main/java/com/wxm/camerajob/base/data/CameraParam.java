package com.wxm.camerajob.base.data;

import android.hardware.camera2.CameraCharacteristics;
import android.os.Handler;
import android.util.Size;

/**
 * 相机参数
 * Created by 123 on 2016/6/17.
 */
public class CameraParam {
    public int      mFace;
    public Size mPhotoSize;
    public Handler  mSessionHandler;

    public CameraParam(Handler sessionHandler)    {
        mFace = CameraCharacteristics.LENS_FACING_BACK;
        mSessionHandler = sessionHandler;

        mPhotoSize = new Size(1280, 960);
    }
}
