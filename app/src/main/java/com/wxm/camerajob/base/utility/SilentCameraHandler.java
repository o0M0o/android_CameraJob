package com.wxm.camerajob.base.utility;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.wxm.camerajob.base.data.TakePhotoParam;

/**
 * 静默相机句柄
 * Created by 123 on 2016/6/16.
 */
public class SilentCameraHandler {
    private final static String TAG = "SilentCameraHandler";
    private final static int  WAIT_MSECS = 10000;
    private final static int  CAPTURE_NOT_START = 0;
    private final static int  CAPTURE_STARTING = 3;
    private final static int  CAPTURE_FINISHED = 1;
    private final static int  CAPTURE_FAILED = 2;
    private int mCaptureFinishFlag = CAPTURE_NOT_START;

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
            Log.i(TAG, "run start");
            mSHandler.mCamera.TakePhoto(mParam, WAIT_MSECS);
            Log.i(TAG, "run end");

            mSHandler.mCaptureFinishFlag = CAPTURE_FINISHED;
        }

    }

    public SilentCameraHandler()    {
        mCamera = new SilentCamera();
        startBackgroundThread();

        TakePhotoParam para = new TakePhotoParam("fuck.jpg");
        mCamera.openCamera(para, mBackgroundHandler);
    }

    protected void finalize() throws Throwable {
        mCamera.closeCamera();
        stopBackgroundThread();
        super.finalize();
    }


    public void TakePhoto(TakePhotoParam para)  {
        if(CAPTURE_STARTING == mCaptureFinishFlag)
            return;

        mCaptureFinishFlag = CAPTURE_STARTING;
        //mCamera.openCamera(para, mBackgroundHandler);
        mBackgroundHandler.post(new SilentCameraRun(para,
                                            this));
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
