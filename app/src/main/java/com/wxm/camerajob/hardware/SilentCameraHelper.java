package com.wxm.camerajob.hardware;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.wxm.camerajob.data.define.CameraParam;
import com.wxm.camerajob.data.define.TakePhotoParam;
import com.wxm.camerajob.utility.ContextUtil;
import com.wxm.camerajob.utility.FileLogger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import wxm.androidutil.util.UtilFun;

/**
 * helper for silent camera
 * Created by 123 on 2016/6/16.
 */
public class SilentCameraHelper {
    private final static String TAG = "SilentCameraHelper";

    private takePhotoCallBack   mTPCBTakePhoto;
    public interface takePhotoCallBack {
        void onTakePhotoSuccess(TakePhotoParam tp);
        void onTakePhotoFailed(TakePhotoParam tp);
    }

    public SilentCameraHelper()    {
    }

    /**
     *  set callback for take photo
     * @param tcb  callback holder
     */
    public void setTakePhotoCallBack(takePhotoCallBack tcb)  {
        mTPCBTakePhoto = tcb;
    }

    /**
     * take photo
     * @param cp    for camera
     * @param para  for take photo
     */
    public void TakePhoto(CameraParam cp, TakePhotoParam para)  {
        new Thread(new TakePhotoRunner(cp, para), "CameraRunner").run();
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
                mSelfCameraParam.mSessionHandler = new Handler();
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
