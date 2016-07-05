package com.wxm.camerajob.base.utility;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.wxm.camerajob.base.data.CameraParam;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.data.TakePhotoParam;
import com.wxm.camerajob.base.handler.GlobalContext;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

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

    private Semaphore                   mCameraLock;
    private Semaphore                   mRunnerLock;
    private LinkedList<TakePhotoParam>  mTPList;
    private takePhotoCallBack           mTPCBTakePhoto;

    public interface takePhotoCallBack {
        void onTakePhotoSuccess(TakePhotoParam tp);
        void onTakePhotoFailed(TakePhotoParam tp);
    }

    public SilentCameraHelper(CameraParam cp)    {
        startBackgroundThread();

        mCameraParam = cp;
        mCameraParam.mSessionHandler = mBackgroundHandlerCamera;

        // for camera
        mCameraLock = new Semaphore(1);

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
                        FileLogger.getLogger().warning(
                                "give up takephoto('"  + para.mFileName + "')");
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

// --Commented out by Inspection START (2016/6/27 23:17):
//    public boolean TakePhotoWait(TakePhotoParam para)  {
//        return takePhotoUtil(para, true);
//    }
// --Commented out by Inspection STOP (2016/6/27 23:17)

    public void setTakePhotoCallBack(takePhotoCallBack tcb)  {
        mTPCBTakePhoto = tcb;
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


    public class TakePhotoRunner implements Runnable {
        private SilentCamera        mSCCamera;
        private TakePhotoParam      mSelfTPTakePhoto;
        public boolean              mRunResult;
        public int                  mRunStat;
        public static final int     RUN_INIT = 0;
        public static final int     RUN_START = 1;
        public static final int     RUN_END = 2;

        private SilentCamera.SilentCameraOpenCameraCallBack mOCCOpen    =
                new SilentCamera.SilentCameraOpenCameraCallBack() {
                    @Override
                    public void onOpenSuccess(CameraParam cp) {
                        mSCCamera.setOpenCameraCallBack(null);
                        mSCCamera.takePhoto(mSelfTPTakePhoto);
                    }

                    @Override
                    public void onOpenFailed(CameraParam cp) {
                        mSCCamera.setOpenCameraCallBack(null);
                        mSCCamera.setTakePhotoCallBack(null);
                        mSCCamera.closeCamera();
                        mCameraLock.release();

                        mRunResult = false;
                        mRunStat = RUN_END;
                    }
                };

        private SilentCamera.SilentCameraTakePhotoCallBack mTPCTake  =
                new SilentCamera.SilentCameraTakePhotoCallBack() {
                    @Override
                    public void onTakePhotoSuccess(TakePhotoParam tp) {
                        mSCCamera.setTakePhotoCallBack(null);
                        mSCCamera.closeCamera();
                        mCameraLock.release();

                        mRunResult = true;
                        mRunStat = RUN_END;

                        //send msg
                        Message m = Message.obtain(GlobalContext.getInstance().mMsgHandler,
                                GlobalDef.MSGWHAT_CAMERAJOB_TAKEPHOTO);
                        m.obj = new Object[] {Integer.parseInt(mSelfTPTakePhoto.mTag), 1};
                        m.sendToTarget();

                        if(null != mTPCBTakePhoto)
                            mTPCBTakePhoto.onTakePhotoSuccess(mSelfTPTakePhoto);
                    }

                    @Override
                    public void onTakePhotoFailed(TakePhotoParam tp) {
                        mSCCamera.setTakePhotoCallBack(null);
                        mSCCamera.closeCamera();
                        mCameraLock.release();

                        mRunResult = false;
                        mRunStat = RUN_END;

                        if(null != mTPCBTakePhoto)
                            mTPCBTakePhoto.onTakePhotoFailed(mSelfTPTakePhoto);
                    }
                };



        public TakePhotoRunner(TakePhotoParam tp)    {
            mSelfTPTakePhoto = tp;

            mRunResult = false;
            mRunStat = RUN_INIT;
        }

        @Override
        public void run() {
            mRunStat = RUN_START;
            try {
                mCameraLock.acquire();
                if(ContextUtil.useNewCamera())
                    mSCCamera = new SilentCameraNew();
                else
                    mSCCamera = new SilentCameraOld();

                mSCCamera.setupCamera(mCameraParam);

                mSCCamera.setOpenCameraCallBack(mOCCOpen);
                mSCCamera.setTakePhotoCallBack(mTPCTake);

                mSCCamera.openCamera();
            } catch (Throwable e) {
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
}
