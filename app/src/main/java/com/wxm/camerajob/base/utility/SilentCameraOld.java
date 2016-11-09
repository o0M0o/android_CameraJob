package com.wxm.camerajob.base.utility;

import android.hardware.Camera;
import android.util.Log;

import com.wxm.camerajob.base.data.CameraParam;
import com.wxm.camerajob.base.data.TakePhotoParam;

import java.util.concurrent.TimeUnit;

import cn.wxm.andriodutillib.util.UtilFun;

import static com.wxm.camerajob.base.utility.FileLogger.getLogger;

/**
 * 兼容旧camera api
 * Created by 123 on 2016/7/4.
 */
public class SilentCameraOld extends SilentCamera {
    private final static String TAG = "SilentCameraOld";
    private Camera  mCamera;
    private int     mCameraID;

    public SilentCameraOld()    {
        super();
    }

    @Override
    public boolean setupCamera(CameraParam cp) {
        mCParam = cp;
        try {
            int selid = -1;
            int cc = Camera.getNumberOfCameras();
            Camera.CameraInfo ci = new Camera.CameraInfo();
            for(int id = 0; id < cc; id++)  {
                Camera.getCameraInfo(id, ci);

                if(CameraParam.LENS_FACING_BACK == cp.mFace)    {
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
            mCameraStatus = CAMERA_SETUP;
        } catch (Exception e)  {
            getLogger().severe(UtilFun.ExceptionToString(e));
            Log.e(TAG, UtilFun.ExceptionToString(e));
            return false;
        }

        return true;
    }

    @Override
    public void openCamera() {
        Log.i(TAG, "open camera, current status = " + mCameraStatus);
        if(mCameraStatus.equals(CAMERA_TAKEPHOTO_START)
                || mCameraStatus.equals(CAMERA_TAKEPHOTO_FINISHED))   {
            Log.w(TAG, "when open camera, status = " + mCameraStatus);

            openCameraCallBack(false);
            return ;
        }

        if(mCameraStatus.equals(CAMERA_OPEN_FINISHED)) {
            openCameraCallBack(true);
            return ;
        }

        if(!mCameraStatus.equals(CAMERA_NOT_SETUP))
            closeCamera();

        try {
            if (!mCameraLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            mCamera = Camera.open(mCameraID);
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
            mCameraStatus = CAMERA_OPEN_FINISHED;
            openCameraCallBack(true);
        } catch (Exception e){
            e.printStackTrace();
            getLogger().severe(UtilFun.ThrowableToString(e));
            openCameraCallBack(false);
        } finally {
            mCameraLock.release();
        }
    }

    @Override
    public boolean takePhoto(TakePhotoParam tp) {
        Log.i(TAG, "takephoto, current status = " + mCameraStatus);
        if(!mCameraStatus.equals(CAMERA_OPEN_FINISHED)
                && !mCameraStatus.equals(CAMERA_TAKEPHOTO_FAILED)
                && !mCameraStatus.equals(CAMERA_TAKEPHOTO_SAVEED))  {
            takePhotoCallBack(false);
            return false;
        }

        mCameraStatus = CAMERA_OPEN_FINISHED;
        mStartMSec = System.currentTimeMillis();
        mTPParam = tp;

        boolean ret = false;
        if(captureStillPicture())    {
            ret = true;
        } else  {
            takePhotoCallBack(false);
        }

        return ret;
    }

    @Override
    public void closeCamera() {
        try {
            if (!mCameraLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            if(null != mCamera)     {
                mCamera.release();
                mCamera = null;
            }

            String l = "camera closed, paratag = "
                    + ((mTPParam == null) || (mTPParam.mTag == null) ? "null" : mTPParam.mTag);
            Log.i(TAG, l);
            getLogger().info(l);
            mCameraStatus = CAMERA_NOT_SETUP;
        } catch (InterruptedException e) {
            getLogger().severe(UtilFun.ThrowableToString(e));
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraLock.release();
        }
    }


    /**
     * Capture a still picture
     * @return 成功返回true, 否则返回false
     */
    private boolean captureStillPicture() {
        mCameraStatus = CAMERA_TAKEPHOTO_START;
        try {
            mCamera.takePicture(null, null, mPicture);
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, UtilFun.ThrowableToString(e));
            getLogger().severe(UtilFun.ThrowableToString(e));

            return false;
        }

        return true;
    }


    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            boolean ret = savePhotoToFile(data, mTPParam.mPhotoFileDir, mTPParam.mFileName);
            if(ret) {
                String l = "save photo to : " + mTPParam.mFileName;
                Log.i(TAG, l);
                getLogger().info(l);
            }

            mCameraStatus = CAMERA_TAKEPHOTO_SAVEED;
            takePhotoCallBack(ret);
        }
    };
}
