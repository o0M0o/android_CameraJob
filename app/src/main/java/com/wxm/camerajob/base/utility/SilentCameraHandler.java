package com.wxm.camerajob.base.utility;

import android.os.Handler;
import android.os.HandlerThread;

import com.wxm.camerajob.base.data.CameraParam;
import com.wxm.camerajob.base.data.TakePhotoParam;

/**
 * 静默相机句柄
 * Created by 123 on 2016/6/16.
 */
public class SilentCameraHandler {
    private final static String TAG = "SilentCameraHandler";

    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private SilentCamera   mCamera;

    /**
     * 静默相机
     */
    private static class SilentCameraRun implements Runnable {
        private final static String TAG = "SilentCameraHandler";

        private final TakePhotoParam mParam;
        private final SilentCameraHandler mSHandler;

        public SilentCameraRun(TakePhotoParam param,
                               SilentCameraHandler shandler) {
            mParam = param;
            mSHandler = shandler;
        }

        @Override
        public void run() {
            mSHandler.mCamera.TakePhoto(mParam);
        }

    }

    public SilentCameraHandler()    {
        mCamera = new SilentCamera();
        startBackgroundThread();

        //CameraParam para = new CameraParam(mBackgroundHandler);
        //mCamera.openCamera(para);
    }

    protected void finalize() throws Throwable {
        mCamera.closeCamera();
        stopBackgroundThread();
        super.finalize();
    }


    public void ChangeCamera()  {
        int cur_status = mCamera.getCameraStatus();
        if(SilentCamera.CAMERA_NOT_READY != cur_status) {
            mCamera.closeCamera();
        }
    }


    public void TakePhoto(TakePhotoParam para)  {
        int cur_status = mCamera.getCameraStatus();
        if(SilentCamera.CAMERA_NOT_READY == cur_status) {
            CameraParam cp = PreferencesUtil.loadCameraParam();
            cp.mSessionHandler = mBackgroundHandler;
            mCamera.openCamera(cp);
        }

        if((SilentCamera.CAMERA_IDLE == cur_status)
            || (SilentCamera.CAMERA_TAKEPHOTO_FINISHED == cur_status)
            ||(SilentCamera.CAMERA_TAKEPHOTO_FAILED == cur_status)) {
            mBackgroundHandler.post(new SilentCameraRun(para, this));
        }
    }


    private void startBackgroundThread()    {
        mBackgroundThread = new HandlerThread("SilentCameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
