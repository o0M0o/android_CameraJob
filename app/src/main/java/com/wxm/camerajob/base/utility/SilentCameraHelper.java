package com.wxm.camerajob.base.utility;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.wxm.camerajob.base.data.CameraParam;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.data.TakePhotoParam;
import com.wxm.camerajob.base.handler.GlobalContext;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import cn.wxm.andriodutillib.util.UtilFun;

/**
 * 静默相机句柄
 * Created by 123 on 2016/6/16.
 */
public class SilentCameraHelper {
    private final static String TAG = "SilentCameraHelper";

    private Handler         mBackgroundHandler;
    private HandlerThread   mBackgroundThread;
    private Handler mBackgroundCameraHandler;
    private HandlerThread mBackgroundCameraThread;
    private CameraParam     mCameraParam;

    private Semaphore                   mCameraLock;
    private takePhotoCallBack           mTPCBTakePhoto;


    public interface takePhotoCallBack {
        void onTakePhotoSuccess(TakePhotoParam tp);
        void onTakePhotoFailed(TakePhotoParam tp);
    }

    SilentCameraHelper(CameraParam cp)    {
        startBackgroundThread();

        mCameraParam = cp;
        mCameraParam.mSessionHandler = mBackgroundCameraHandler;

        // for camera
        mCameraLock = new Semaphore(1);
    }

    protected void finalize() throws Throwable {
        stopBackgroundThread();
        super.finalize();
    }

    /**
     *  设置拍照回调接口
     * @param tcb  回调参数
     */
    public void setTakePhotoCallBack(takePhotoCallBack tcb)  {
        mTPCBTakePhoto = tcb;
    }

    /**
     * 更新拍照参数
     * @param cp  拍照参数
     */
    public void ChangeCamera(CameraParam cp)  {
        mCameraParam = cp;
        mCameraParam.mSessionHandler = mBackgroundCameraHandler;
    }

    /**
     * 拍照
     * @param para  拍照参数
     */
    public void TakePhoto(TakePhotoParam para)  {
        TakePhotoRunner tr = new TakePhotoRunner(para);
        mBackgroundHandler.post(tr);
    }

    /**
     * 启动后台线程
     */
    private void startBackgroundThread()    {
        mBackgroundThread = new HandlerThread("Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());

        mBackgroundCameraThread = new HandlerThread("SilentCameraBackground");
        mBackgroundCameraThread.start();
        mBackgroundCameraHandler = new Handler(mBackgroundCameraThread.getLooper());
    }

    /**
     * 关闭后台线程
     */
    private void stopBackgroundThread() {
        mBackgroundCameraThread.quitSafely();
        try {
            mBackgroundCameraThread.join();
            mBackgroundCameraThread = null;
            mBackgroundCameraHandler = null;
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

    /**
     * 执行拍照任务
     */
    private class TakePhotoRunner implements Runnable {
        private SilentCamera        mSCCamera;
        private TakePhotoParam      mSelfTPTakePhoto;

        private SilentCamera.SilentCameraOpenCameraCallBack mOCCOpen    =
                new SilentCamera.SilentCameraOpenCameraCallBack() {
                    @Override
                    public void onOpenSuccess(CameraParam cp) {
                        mSCCamera.setOpenCameraCallBack(null);
                        mSCCamera.setTakePhotoCallBack(mTPCTake);
                        mSCCamera.takePhoto(mSelfTPTakePhoto);
                    }

                    @Override
                    public void onOpenFailed(CameraParam cp) {
                        mSCCamera.setOpenCameraCallBack(null);
                        mSCCamera.setTakePhotoCallBack(null);
                        mSCCamera.closeCamera();
                        mCameraLock.release();
                    }
                };

        private SilentCamera.SilentCameraTakePhotoCallBack mTPCTake  =
                new SilentCamera.SilentCameraTakePhotoCallBack() {
                    @Override
                    public void onTakePhotoSuccess(TakePhotoParam tp) {
                        mSCCamera.setTakePhotoCallBack(null);
                        mSCCamera.closeCamera();
                        mCameraLock.release();

                        //send msg
                        Message m = Message.obtain(GlobalContext.getMsgHandlder(),
                                GlobalDef.MSG_TYPE_CAMERAJOB_TAKEPHOTO);
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

                        if(null != mTPCBTakePhoto)
                            mTPCBTakePhoto.onTakePhotoFailed(mSelfTPTakePhoto);
                    }
                };



        TakePhotoRunner(TakePhotoParam tp)    {
            mSelfTPTakePhoto = tp;
        }

        @Override
        public void run() {
            boolean block = false;
            try {
                if (mCameraLock.tryAcquire(3, TimeUnit.SECONDS)) {
                    block = true;

                    if (ContextUtil.useNewCamera())
                        mSCCamera = new SilentCameraNew();
                    else
                        mSCCamera = new SilentCameraOld();

                    mSCCamera.setupCamera(mCameraParam);
                    mSCCamera.setOpenCameraCallBack(mOCCOpen);
                    mSCCamera.openCamera();
                } else  {
                    FileLogger.getLogger().severe(
                            "camera busy, give up : " + mSelfTPTakePhoto.mFileName);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                FileLogger.getLogger().severe(UtilFun.ThrowableToString(e));

                if(block)
                    mCameraLock.release();
            }
        }
    }
}
