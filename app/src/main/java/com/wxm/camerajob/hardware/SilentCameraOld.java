package com.wxm.camerajob.hardware;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wxm.camerajob.data.define.CameraParam;
import com.wxm.camerajob.utility.FileLogger;

import java.lang.ref.WeakReference;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import wxm.androidutil.util.ImageUtil;
import wxm.androidutil.util.UtilFun;

import static com.wxm.camerajob.utility.FileLogger.getLogger;

/**
 * compatible with old camera api
 * Created by 123 on 2016/7/4.
 */
public class SilentCameraOld extends SilentCamera {
    private final static String TAG = "SilentCameraOld";
    private final static int   MSG_CAPTURE_TIMEOUT = 1;

    Semaphore mCameraLock;
    private Camera  mCamera;
    private int     mCameraID;
    private CameraMsgHandler    mMHHandler;

    SilentCameraOld()    {
        super();

        mCameraLock = new Semaphore(1);
        mMHHandler = new CameraMsgHandler(this);
    }

    private boolean setupCamera() {
        try {
            int selid = -1;
            int cc = Camera.getNumberOfCameras();
            Camera.CameraInfo ci = new Camera.CameraInfo();
            for(int id = 0; id < cc; id++)  {
                Camera.getCameraInfo(id, ci);

                if(CameraParam.LENS_FACING_BACK == mCParam.mFace)    {
                    if(Camera.CameraInfo.CAMERA_FACING_BACK == ci.facing)   {
                        selid = id;
                    }
                }   else    {
                    if(Camera.CameraInfo.CAMERA_FACING_FRONT == ci.facing)   {
                        selid = id;
                    }
                }

                if(-1 != selid) {
                    mSensorOrientation = ci.orientation;
                    break;
                }
            }

            if(-1 == selid) {
                return false;
            }

            mCameraID = selid;
        } catch (Exception e)  {
            getLogger().severe(UtilFun.ExceptionToString(e));
            Log.e(TAG, UtilFun.ExceptionToString(e));
            return false;
        }

        return true;
    }

    @Override
    public void openCamera() {
        if(!setupCamera())  {
            Log.w(TAG, "setup camera failure");
            return;
        }

        boolean b_ret = false;
        boolean b_lock = false;
        try {
            if (!mCameraLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            b_lock = true;

            mCamera = Camera.open(mCameraID);
            mCameraStatus = ECameraStatus.OPEN;
            b_ret = true;
        } catch (Exception e){
            e.printStackTrace();
            getLogger().severe(UtilFun.ThrowableToString(e));
        } finally {
            if(b_lock)
                mCameraLock.release();
        }

        openCameraCallBack(b_ret);
    }

    @Override
    void capturePhoto() {
        Log.i(TAG, "start capture");

        mCameraStatus = ECameraStatus.TAKE_PHOTO_START;
        boolean b_lock = false;
        try {
            if (!mCameraLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            b_lock = true;

            Camera.Parameters cpa = mCamera.getParameters();
            mFlashSupported = (null != cpa.getFlashMode());

            if(mFlashSupported && mCParam.mAutoFlash)     {
                cpa.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            }

            if(mCParam.mAutoFocus)  {
                cpa.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }

            cpa.setPictureSize(mCParam.mPhotoSize.getWidth(),
                    mCParam.mPhotoSize.getHeight());

            mCamera.setParameters(cpa);
            mCamera.setPreviewCallback((data, camera) -> Log.i(TAG, "preview being called!"));

            mCamera.startPreview();
            mCamera.takePicture(null, null, mPCJpgProcessor);
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, UtilFun.ThrowableToString(e));
            getLogger().severe(UtilFun.ThrowableToString(e));

            takePhotoCallBack(false);
        } finally {
            if(b_lock)
                mCameraLock.release();
        }

        mMHHandler.sendEmptyMessageDelayed(MSG_CAPTURE_TIMEOUT, 5000);
    }

    @Override
    public void closeCamera() {
        boolean b_lock = false;
        try {
            if (!mCameraLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out close camera.");
            }
            b_lock = true;

            if(null != mCamera)     {
                mCamera.release();
                mCamera = null;
            }

            String l = "camera closed, paratag = "
                    + ((mTPParam == null) || (mTPParam.mTag == null) ? "null" : mTPParam.mTag);
            Log.i(TAG, l);
            getLogger().info(l);
            mCameraStatus = ECameraStatus.NOT_OPEN;
        } catch (InterruptedException e) {
            getLogger().severe(UtilFun.ThrowableToString(e));
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            if(b_lock)
                mCameraLock.release();
        }
    }

    private Camera.PictureCallback mPCJpgProcessor = (data, camera) -> {
        boolean ret = savePhotoToFile(data, mTPParam.mPhotoFileDir, mTPParam.mFileName);

        mCameraStatus = ret ? ECameraStatus.TAKE_PHOTO_SUCCESS : ECameraStatus.TAKE_PHOTO_FAILURE;
        takePhotoCallBack(ret);
    };

    private Camera.PictureCallback mPCRawProcessor = (data, camera) -> {
        Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
        bm = ImageUtil.rotateBitmap(bm, mSensorOrientation, null);
        boolean ret = saveBitmapToJPGFile(bm, mTPParam.mPhotoFileDir, mTPParam.mFileName);

        mCameraStatus = ret ? ECameraStatus.TAKE_PHOTO_SUCCESS : ECameraStatus.TAKE_PHOTO_FAILURE;
        takePhotoCallBack(ret);
    };


    /**
     * activity msg handler
     * Created by wxm on 2016/8/13.
     */
    private static class CameraMsgHandler extends Handler {
        private static final String TAG = "CameraMsgHandler";
        private WeakReference<SilentCameraOld>      mWRHandler;


        CameraMsgHandler(SilentCameraOld h) {
            super();
            mWRHandler = new WeakReference<>(h);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CAPTURE_TIMEOUT : {
                    String l = "wait capture timeout";
                    Log.e(TAG, l);
                    FileLogger.getLogger().severe(l);

                    SilentCameraOld h = mWRHandler.get();
                    if(null != h)
                        h.takePhotoCallBack(false);
                }
                break;

                default:
                    Log.e(TAG, String.format("msg(%s) can not process", msg.toString()));
                    break;
            }
        }
    }
}
