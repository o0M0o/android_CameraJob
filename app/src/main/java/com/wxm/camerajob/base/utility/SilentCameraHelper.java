package com.wxm.camerajob.base.utility;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.wxm.camerajob.base.data.CameraParam;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.data.TakePhotoParam;
import com.wxm.camerajob.base.handler.GlobalContext;

import cn.wxm.andriodutillib.util.UtilFun;

/**
 * 静默相机辅助类
 * Created by 123 on 2016/6/16.
 */
public class SilentCameraHelper {
    private final static String TAG = "SilentCameraHelper";

    private Handler         mBackgroundHandler;
    private HandlerThread   mBackgroundThread;
    private Handler         mCameraSessionHandler;
    private HandlerThread   mCameraSessionThread;

    //private Semaphore           mCameraLock;

    private takePhotoCallBack   mTPCBTakePhoto;
    public interface takePhotoCallBack {
        void onTakePhotoSuccess(TakePhotoParam tp);
        void onTakePhotoFailed(TakePhotoParam tp);
    }

    public SilentCameraHelper()    {
        startBackgroundThread();

        // for camera
        //mCameraLock = new Semaphore(1);
        //mSCCamera = ContextUtil.useNewCamera() ? new SilentCameraNew() : new SilentCameraOld();
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

        mCameraSessionThread = new HandlerThread("CameraSession");
        mCameraSessionThread.start();
        mCameraSessionHandler = new Handler(mCameraSessionThread.getLooper());
    }

    /**
     * 关闭后台线程
     */
    private void stopBackgroundThread() {
        mBackgroundHandler = null;
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mBackgroundThread = null;

        mCameraSessionHandler = null;
        mCameraSessionThread.quitSafely();
        try {
            mCameraSessionThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mCameraSessionThread = null;
    }


    /**
     * 执行拍照任务
     */
    private class TakePhotoRunner implements Runnable {
        private SilentCamera        mSCSelfCamera;

        private TakePhotoParam      mSelfTPTakePhoto;
        private CameraParam         mSelfCameraParam;

        private SilentCamera.SilentCameraTakePhotoCallBack mTPCTake  =
                new SilentCamera.SilentCameraTakePhotoCallBack() {
                    @Override
                    public void onTakePhotoSuccess(TakePhotoParam tp) {
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
                        if(null != mTPCBTakePhoto)
                            mTPCBTakePhoto.onTakePhotoFailed(mSelfTPTakePhoto);
                    }
                };

        TakePhotoRunner(CameraParam cp, TakePhotoParam tp)    {
            super();
            mSelfCameraParam = cp;
            mSelfTPTakePhoto = tp;
            mSCSelfCamera = ContextUtil.useNewCamera() ?
                    new SilentCameraNew() : new SilentCameraOld();
        }

        @Override
        public void run() {
            try {
                mSelfCameraParam.mSessionHandler =  mCameraSessionHandler;
                mSCSelfCamera.takePhoto(mSelfCameraParam, mSelfTPTakePhoto, mTPCTake);
            } catch (Throwable e) {
                e.printStackTrace();

                String str_e = UtilFun.ThrowableToString(e);
                Log.d(TAG, str_e);
                FileLogger.getLogger().severe(str_e);
            }
        }
    }
}
