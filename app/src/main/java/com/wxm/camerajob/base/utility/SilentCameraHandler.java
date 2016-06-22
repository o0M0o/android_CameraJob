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
    private Handler         mBackgroundHandlerCamera;
    private HandlerThread   mBackgroundThreadCamera;
    private CameraParam     mCameraParam;


    public class TakePhotoRunner implements Runnable {
        private TakePhotoParam  mSelfTPTakePhoto;
        private CameraParam     mSelfCameraParam;
        public boolean          mRunResult;
        public int             mRunStat;
        public static final int    RUN_INIT = 0;
        public static final int    RUN_START = 1;
        public static final int    RUN_END = 2;

        public TakePhotoRunner(CameraParam cp, TakePhotoParam tp)    {
            mSelfTPTakePhoto = tp;
            mSelfCameraParam = cp;

            mRunResult = false;
            mRunStat = RUN_INIT;
        }

        @Override
        public void run() {
            mRunStat = RUN_START;
            SilentCamera mC = null;
            try {
                CameraManager mCMM = (CameraManager) ContextUtil.getInstance()
                        .getSystemService(Context.CAMERA_SERVICE);
                HashMap<String, CameraCharacteristics> mHM = new HashMap<>();

                for (String cameraId : mCMM.getCameraIdList()) {
                    CameraCharacteristics characteristics
                            = mCMM.getCameraCharacteristics(cameraId);

                    mHM.put(cameraId, characteristics);
                }

                mC = new SilentCamera(mCMM, mHM);
                mRunResult = mC.TakeOncePhoto(mSelfCameraParam, mSelfTPTakePhoto);
            } catch (Throwable e)   {
                e.printStackTrace();
                FileLogger.getLogger().severe(UtilFun.ThrowableToString(e));
            }
            finally {
                if(null != mC) {
                    mC.closeCamera();
                }

                mRunStat = RUN_END;
            }
        }
    }


    public SilentCameraHandler(CameraParam cp)    {
        startBackgroundThread();

        mCameraParam = cp;
        mCameraParam.mSessionHandler = mBackgroundHandlerCamera;
    }

    protected void finalize() throws Throwable {
        stopBackgroundThread();
        super.finalize();
    }

    public void ChangeCamera(CameraParam cp)  {
        mCameraParam = cp;
        mCameraParam.mSessionHandler = mBackgroundHandlerCamera;
    }


    public void TakePhoto(TakePhotoParam para)  {
        TakePhotoRunner tr = new TakePhotoRunner(mCameraParam, para);
        mBackgroundHandler.post(tr);
    }

    public boolean TakePhotoWait(TakePhotoParam para)  {
        long sms = System.currentTimeMillis();
        long ems = sms + para.mWaitMSecs + mCameraParam.mWaitMSecs;
        TakePhotoRunner tr = new TakePhotoRunner(mCameraParam, para);
        mBackgroundHandler.post(tr);

        while((System.currentTimeMillis() < ems)
                && (TakePhotoRunner.RUN_END != tr.mRunStat))    {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return tr.mRunResult;
    }

    private void startBackgroundThread()    {
        mBackgroundThread = new HandlerThread("SilentCameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());

        mBackgroundThreadCamera = new HandlerThread("SilentCameraBackground");
        mBackgroundThreadCamera.start();
        mBackgroundHandlerCamera = new Handler(mBackgroundThreadCamera.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThreadCamera.quitSafely();
        try {
            mBackgroundThreadCamera.join();
            mBackgroundThreadCamera = null;
            mBackgroundHandlerCamera = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
