package com.wxm.camerajob.base.utility;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.wxm.camerajob.base.data.CameraParam;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.data.TakePhotoParam;
import com.wxm.camerajob.base.handler.GlobalContext;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import cn.wxm.andriodutillib.util.UtilFun;

/**
 * 静默相机辅助类
 * Created by 123 on 2016/6/16.
 */
public class SilentCameraHelper {
    private final static String TAG = "SilentCameraHelper";

    private Handler         mBackgroundHandler;
    private HandlerThread   mBackgroundThread;

    private Semaphore           mCameraLock;
    private takePhotoCallBack   mTPCBTakePhoto;


    public interface takePhotoCallBack {
        void onTakePhotoSuccess(TakePhotoParam tp);
        void onTakePhotoFailed(TakePhotoParam tp);
    }

    SilentCameraHelper(CameraParam cp)    {
        startBackgroundThread();

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
     * 拍照
     *
     * @param cp    相机设置
     * @param para  拍照参数
     */
    public void TakePhoto(CameraParam cp, TakePhotoParam para)  {
        mBackgroundHandler.post(new TakePhotoRunner(cp, para));
    }

    /**
     * 启动后台线程
     */
    private void startBackgroundThread()    {
        mBackgroundThread = new HandlerThread("Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * 关闭后台线程
     */
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


    /**
     * 执行拍照任务
     */
    private class TakePhotoRunner implements Runnable {
        private SilentCamera        mSCCamera;

        private TakePhotoParam      mSelfTPTakePhoto;
        private CameraParam         mSelfCameraParam;

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



        TakePhotoRunner(CameraParam cp, TakePhotoParam tp)    {
            mSelfCameraParam = cp;
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

                    mSelfCameraParam.mSessionHandler =  mBackgroundHandler;
                    mSCCamera.setupCamera(mSelfCameraParam);
                    mSCCamera.setOpenCameraCallBack(mOCCOpen);
                    mSCCamera.openCamera();
                } else  {
                    String l = "camera busy, give up : " + mSelfTPTakePhoto.mFileName;
                    Log.d(TAG, l);
                    FileLogger.getLogger().severe(l);
                }
            } catch (Throwable e) {
                e.printStackTrace();

                String str_e = UtilFun.ThrowableToString(e);
                Log.d(TAG, str_e);
                FileLogger.getLogger().severe(str_e);
            } finally {
                if(block)
                    mCameraLock.release();
            }
        }
    }
}
