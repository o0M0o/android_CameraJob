package com.wxm.camerajob.base.utility;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.wxm.camerajob.base.data.CameraParam;
import com.wxm.camerajob.base.data.TakePhotoParam;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 静默相机句柄
 * Created by 123 on 2016/6/16.
 */
public class SilentCameraHelper {
    private final static String TAG = "SilentCameraHelper";

    private Handler         mBackgroundHandler;
    private HandlerThread   mBackgroundThread;
    private Handler         mBackgroundHandlerCamera;
    private HandlerThread   mBackgroundThreadCamera;
    private CameraParam     mCameraParam;

    private Semaphore       mCameraLock;
    private SilentCamera2   mSCCamera;

    private Semaphore                   mRunnerLock;
    private LinkedList<TakePhotoParam>  mTPList;



    public class TakePhotoRunner implements Runnable {
        private TakePhotoParam      mSelfTPTakePhoto;
        public boolean              mRunResult;
        public int                  mRunStat;
        public static final int     RUN_INIT = 0;
        public static final int     RUN_START = 1;
        public static final int     RUN_END = 2;

        public TakePhotoRunner(TakePhotoParam tp)    {
            mSelfTPTakePhoto = tp;

            mRunResult = false;
            mRunStat = RUN_INIT;
        }

        @Override
        public void run() {
            mRunStat = RUN_START;
            try {
                if(mCameraLock.tryAcquire(500, TimeUnit.MILLISECONDS)) {
                    try {
                        if (mSCCamera.openCamera()) {
                            mRunResult = mSCCamera.takePhoto(mSelfTPTakePhoto);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        FileLogger.getLogger().severe(UtilFun.ThrowableToString(e));
                    } finally {
                        mSCCamera.closeCamera();

                        mCameraLock.release();
                        mRunStat = RUN_END;
                    }
                }
                else {
                    FileLogger.getLogger().warning("give up takephoto('"
                                                        + mSelfTPTakePhoto.mFileName
                                                        + "')");
                    mRunStat = RUN_END;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                FileLogger.getLogger().severe(UtilFun.ThrowableToString(e));
            }

            try {
                mRunnerLock.acquire();
                mTPList.remove(mSelfTPTakePhoto);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mRunnerLock.release();
            }
        }
    }


    public SilentCameraHelper(CameraParam cp)    {
        startBackgroundThread();

        mCameraParam = cp;
        mCameraParam.mSessionHandler = mBackgroundHandlerCamera;

        // for camera
        mCameraLock = new Semaphore(1);
        mSCCamera = new SilentCamera2();

        CameraManager mCMM = (CameraManager) ContextUtil.getInstance()
                .getSystemService(Context.CAMERA_SERVICE);
        mSCCamera.setupCamera(mCMM, mCameraParam);

        // for runner list
        mRunnerLock = new Semaphore(1);
        mTPList = new LinkedList<>();
    }

    protected void finalize() throws Throwable {
        stopBackgroundThread();
        super.finalize();
    }

    public void ChangeCamera(CameraParam cp)  {
        mCameraParam = cp;
        mCameraParam.mSessionHandler = mBackgroundHandlerCamera;

        CameraManager mCMM = (CameraManager) ContextUtil.getInstance()
                .getSystemService(Context.CAMERA_SERVICE);

        try {
            mCameraLock.acquire();
            mSCCamera.setupCamera(mCMM, mCameraParam);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            mCameraLock.release();
        }
    }

    private boolean takePhotoUtil(TakePhotoParam para, boolean bw)  {
        long sms = System.currentTimeMillis();
        boolean re = false;
        if(!((null == para.mTag) || para.mTag.isEmpty()))  {
            try {
                mRunnerLock.acquire();
                if(!((null == para.mTag) || para.mTag.isEmpty())) {
                    for (TakePhotoParam r : mTPList) {
                        if (!((null == r.mTag) || r.mTag.isEmpty())) {
                            if (r.mTag.equals(para.mTag)) {
                                re = true;
                                break;
                            }
                        }
                    }

                    if(!re) {
                        mTPList.add(para);
                    }
                    else    {
                        FileLogger.getLogger().warning("give up takephoto('"
                                + para.mFileName
                                + "')");
                    }
                } else  {
                    mTPList.add(para);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mRunnerLock.release();
            }
        }

        if(!re) {
            TakePhotoRunner tr = new TakePhotoRunner(para);
            mBackgroundHandler.post(tr);

            if(bw) {
                long ems = sms + para.mWaitMSecs + mCameraParam.mWaitMSecs;
                while ((System.currentTimeMillis() < ems)
                        && (TakePhotoRunner.RUN_END != tr.mRunStat)) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                return tr.mRunResult;
            }

            return true;
        }

        return false;
    }


    public void TakePhoto(TakePhotoParam para)  {
        takePhotoUtil(para, false);
    }

    public boolean TakePhotoWait(TakePhotoParam para)  {
        return takePhotoUtil(para, true);
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
