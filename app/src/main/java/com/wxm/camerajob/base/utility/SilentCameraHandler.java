package com.wxm.camerajob.base.utility;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.HandlerThread;

import com.wxm.camerajob.base.data.CameraParam;
import com.wxm.camerajob.base.data.TakePhotoParam;

import java.util.HashMap;

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

    private CameraManager   mCMManager;
    private HashMap<String, CameraCharacteristics> mHMCameraCharacteristics;

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

        /*
        mCMManager = (CameraManager) ContextUtil.getInstance()
                        .getSystemService(Context.CAMERA_SERVICE);
        mHMCameraCharacteristics = new HashMap<>();
        try {
            for (String cameraId : mCMManager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = mCMManager.getCameraCharacteristics(cameraId);

                mHMCameraCharacteristics.put(cameraId, characteristics);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        */
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
        try {
            mCMManager = (CameraManager) ContextUtil.getInstance()
                    .getSystemService(Context.CAMERA_SERVICE);
            mHMCameraCharacteristics = new HashMap<>();

            for (String cameraId : mCMManager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = mCMManager.getCameraCharacteristics(cameraId);

                mHMCameraCharacteristics.put(cameraId, characteristics);
            }

            mCamera = new SilentCamera(mCMManager, mHMCameraCharacteristics);
            mCamera.TakeOncePhoto(mCameraParam, para);
        } catch (Throwable e)   {
            e.printStackTrace();
            FileLogger.getLogger().severe(UtilFun.ThrowableToString(e));
        }
        finally {
            if(null != mCamera) {
                mCamera.closeCamera();
                mCamera = null;
            }
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
