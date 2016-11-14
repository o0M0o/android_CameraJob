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

    //private Semaphore           mCameraLock;
    private TakePhotoRunner     mTPRRunner = new TakePhotoRunner();
    private SilentCamera        mSCCamera;

    private takePhotoCallBack   mTPCBTakePhoto;
    public interface takePhotoCallBack {
        void onTakePhotoSuccess(TakePhotoParam tp);
        void onTakePhotoFailed(TakePhotoParam tp);
    }

    SilentCameraHelper(CameraParam cp)    {
        startBackgroundThread();

        // for camera
        //mCameraLock = new Semaphore(1);
        //mSCCamera = ContextUtil.useNewCamera() ? new SilentCameraNew() : new SilentCameraOld();
        mSCCamera = new SilentCameraOld();
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
        mTPRRunner.setPara(cp, para, mSCCamera);
        mBackgroundHandler.post(mTPRRunner);
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
        private SilentCamera        mSCSelfCamera;

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
                        //mCameraLock.release();
                    }
                };

        private SilentCamera.SilentCameraTakePhotoCallBack mTPCTake  =
                new SilentCamera.SilentCameraTakePhotoCallBack() {
                    @Override
                    public void onTakePhotoSuccess(TakePhotoParam tp) {
                        mSCCamera.setTakePhotoCallBack(null);
                        mSCCamera.closeCamera();
                        //mCameraLock.release();

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
                        //mCameraLock.release();

                        if(null != mTPCBTakePhoto)
                            mTPCBTakePhoto.onTakePhotoFailed(mSelfTPTakePhoto);
                    }
                };

        TakePhotoRunner()    {
            super();
        }

        public void setPara(CameraParam cp, TakePhotoParam tp, SilentCamera sc)  {
            mSelfCameraParam = cp;
            mSelfTPTakePhoto = tp;
            mSCSelfCamera = sc;
        }

        @Override
        public void run() {
            try {
                mSelfCameraParam.mSessionHandler =  mBackgroundHandler;
                if(mSCSelfCamera.setupCamera(mSelfCameraParam)) {
                    mSCSelfCamera.setOpenCameraCallBack(mOCCOpen);
                    mSCSelfCamera.openCamera();
                } else  {
                    String l = "setup camera failure, give up : " + mSelfTPTakePhoto.mFileName;
                    Log.d(TAG, l);
                    FileLogger.getLogger().severe(l);
                }
            } catch (Throwable e) {
                e.printStackTrace();

                String str_e = UtilFun.ThrowableToString(e);
                Log.d(TAG, str_e);
                FileLogger.getLogger().severe(str_e);
            }
        }
    }
}
