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

    private Handler         mBackgroundHandler;
    private HandlerThread   mBackgroundThread;
    private SilentCamera    mCamera;
    private CameraParam     mCameraParam;

    /**
     * 静默相机
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
     */

    public SilentCameraHandler(CameraParam cp)    {
        startBackgroundThread();

        mCameraParam = cp;
        mCameraParam.mSessionHandler = mBackgroundHandler;
    }

    protected void finalize() throws Throwable {
        stopBackgroundThread();
        super.finalize();
    }

    public void ChangeCamera(CameraParam cp)  {
        mCameraParam = cp;
        mCameraParam.mSessionHandler = mBackgroundHandler;
    }


    public void TakePhoto(TakePhotoParam para)  {
        mCamera = new SilentCamera();
        /*
        int cur_status = mCamera.getCameraStatus();
        if(SilentCamera.CAMERA_NOT_READY != cur_status) {
            Log.i(TAG, "相机处在使用中，放弃此次操作, mTag = '"
                            + para.mTag + "'");

            FileLogger.getLogger().info("相机处在使用中，放弃此次操作, mTag = '"
                            + para.mTag + "'");
            return;
        }
        */
        mCamera.TakeOncePhoto(mCameraParam, para);
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
