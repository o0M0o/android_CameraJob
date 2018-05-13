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

import wxm.androidutil.util.ImageUtil;
import wxm.androidutil.util.UtilFun;

/**
 * compatible with old camera api
 * Created by 123 on 2016/7/4.
 */
public class SilentCameraOld extends SilentCamera {
    private final static String TAG = "SilentCameraOld";
    private final static int   MSG_CAPTURE_TIMEOUT = 1;

    private Camera  mCamera;
    private int     mCameraID;
    private CameraMsgHandler    mMHHandler;

    SilentCameraOld()    {
        super();
        mMHHandler = new CameraMsgHandler(this);
    }

    private boolean setupCamera() {
        try {
            int selid = -1;
            int cc = Camera.getNumberOfCameras();
            Camera.CameraInfo ci = new Camera.CameraInfo();
            for(int id = 0; id < cc; id++)  {
                Camera.getCameraInfo(id, ci);

                if(CameraParam.LENS_FACING_BACK == mCParam.getMFace())    {
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
            FileLogger.Companion.getLogger().severe(UtilFun.ExceptionToString(e));
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
        try {
            mCamera = Camera.open(mCameraID);
            mCameraStatus = ECameraStatus.OPEN;
            b_ret = true;
        } catch (Exception e){
            e.printStackTrace();
            FileLogger.Companion.getLogger().severe(UtilFun.ThrowableToString(e));
        }

        openCameraCallBack(b_ret);
    }

    @Override
    void capturePhoto() {
        Log.i(TAG, "start capture");
        mCameraStatus = ECameraStatus.TAKE_PHOTO_START;
        try {
            Camera.Parameters cpa = mCamera.getParameters();
            mFlashSupported = (null != cpa.getFlashMode());

            if(mFlashSupported && mCParam.getMAutoFlash())     {
                cpa.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            }

            if(mCParam.getMAutoFocus())  {
                cpa.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }

            cpa.setPictureSize(mCParam.getMPhotoSize().getWidth(),
                    mCParam.getMPhotoSize().getHeight());

            mCamera.setParameters(cpa);
            mCamera.setPreviewCallback((data, camera) -> Log.i(TAG, "preview being called!"));

            mCamera.startPreview();
            mCamera.takePicture(null, null, mPCJpgProcessor);
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, UtilFun.ThrowableToString(e));
            FileLogger.Companion.getLogger().severe(UtilFun.ThrowableToString(e));

            takePhotoCallBack(false);
        }

        mMHHandler.sendEmptyMessageDelayed(MSG_CAPTURE_TIMEOUT, 5000);
    }

    @Override
    public void closeCamera() {
        if(null != mCamera)     {
            mCamera.release();
            mCamera = null;
        }

        String l = "camera closed, paratag = "
                + ((mTPParam == null) || (mTPParam.getMTag() == null) ? "null" : mTPParam.getMTag());
        Log.i(TAG, l);
        FileLogger.Companion.getLogger().info(l);
        mCameraStatus = ECameraStatus.NOT_OPEN;
    }

    private Camera.PictureCallback mPCJpgProcessor = (data, camera) -> {
        boolean ret = savePhotoToFile(data, mTPParam.getMPhotoFileDir(), mTPParam.getMFileName());

        mCameraStatus = ret ? ECameraStatus.TAKE_PHOTO_SUCCESS : ECameraStatus.TAKE_PHOTO_FAILURE;
        takePhotoCallBack(ret);
    };

    private Camera.PictureCallback mPCRawProcessor = (data, camera) -> {
        Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
        bm = ImageUtil.rotateBitmap(bm, mSensorOrientation, null);
        boolean ret = saveBitmapToJPGFile(bm, mTPParam.getMPhotoFileDir(), mTPParam.getMFileName());

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
                    FileLogger.Companion.getLogger().severe(l);

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
