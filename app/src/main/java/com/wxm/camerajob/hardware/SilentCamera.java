package com.wxm.camerajob.hardware;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;

import com.wxm.camerajob.data.define.CameraParam;
import com.wxm.camerajob.data.define.TakePhotoParam;
import com.wxm.camerajob.utility.FileLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import wxm.androidutil.util.UtilFun;


/**
 * base class for silent camera
 * silent camera can get photo without sound
 * Created by 123 on 2016/7/4.
 */
abstract class SilentCamera {
    private final static String TAG = "SilentCamera";

    Semaphore                   mCameraLock;
    int                         mSensorOrientation;
    static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    ECameraStatus mCameraStatus = ECameraStatus.NOT_OPEN;

    TakePhotoParam        mTPParam;
    CameraParam           mCParam;
    boolean               mFlashSupported;

    interface SilentCameraOpenCameraCallBack {
        void onOpenSuccess(CameraParam cp);
        void onOpenFailed(CameraParam cp);
    }
    private SilentCameraOpenCameraCallBack      mOCCBOpen;

    interface SilentCameraTakePhotoCallBack {
        void onTakePhotoSuccess(TakePhotoParam tp);
        void onTakePhotoFailed(TakePhotoParam tp);
    }
    private SilentCameraTakePhotoCallBack      mTPCBTakePhoto;


    SilentCamera()   {
        mCameraLock = new Semaphore(1);

        mOCCBOpen = new SilentCameraOpenCameraCallBack() {
            @Override
            public void onOpenSuccess(CameraParam cp) {
                capturePhoto();
            }

            @Override
            public void onOpenFailed(CameraParam cp) {
                takePhotoCallBack(false);
            }
        };
    }


    /**
     * take photo
     * @param cp        for camera
     * @param tp        for photo
     * @param stp       call back holder
     */
    public void takePhoto(CameraParam cp, TakePhotoParam tp, SilentCameraTakePhotoCallBack stp)    {
        mCParam = cp;
        mTPParam = tp;
        mTPCBTakePhoto = stp;

        openCamera();
    }


    /**
     * callback for open camera
     * @param ret  true if success
     */
    void openCameraCallBack(Boolean ret) {
        if(ret) {
            String l = "camera opened";
            Log.i(TAG, l);
            FileLogger.getLogger().info(l);

            if(null != mOCCBOpen)
                mOCCBOpen.onOpenSuccess(mCParam);
        }
        else {
            String l = "camera open failed";
            Log.i(TAG, l);
            FileLogger.getLogger().info(l);

            if(null != mOCCBOpen)
                mOCCBOpen.onOpenFailed(mCParam);
        }
    }

    /**
     * callback for take photo
     * @param ret  true if success
     */
    void takePhotoCallBack(Boolean ret) {
        String tag = (mTPParam == null ? "null"
                : (mTPParam.mTag == null ? "null" : mTPParam.mTag));

        closeCamera();
        if(ret) {
            String l = "take photo success, tag = " + tag
                        + ", photofile = " + mTPParam.mFileName;
            Log.i(TAG, l);
            FileLogger.getLogger().info(l);

            if(null != mTPCBTakePhoto)
                mTPCBTakePhoto.onTakePhotoSuccess(mTPParam);
        }
        else {
            String l = "take photo failed, tag = "
                    + tag + ", camera_status = " + mCameraStatus.getDescription();
            Log.i(TAG, l);
            FileLogger.getLogger().info(l);

            if(null != mTPCBTakePhoto)
                mTPCBTakePhoto.onTakePhotoFailed(mTPParam);
        }
    }

    /**
     * save photo data to file
     * @param data          photo data
     * @param fileDir       dir
     * @param fileName      file name
     * @return              true if success
     */
    boolean savePhotoToFile(byte[] data, String fileDir, String fileName) {
        boolean ret = false;
        FileOutputStream output = null;
        File mf = new File(fileDir, fileName);
        try {
            output = new FileOutputStream(mf);
            output.write(data);
            ret = true;
        } catch (IOException e) {
            e.printStackTrace();
            FileLogger.getLogger().severe(UtilFun.ExceptionToString(e));
        } finally {
            if(null != output)  {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    FileLogger.getLogger().severe(UtilFun.ExceptionToString(e));
                }
            }
        }

        return ret;
    }

    /**
     * save bitmap as jpg file
     * @param bm            bitmap data
     * @param fileDir       jpg file dir
     * @param fileName      jpg filename
     * @return              true if success
     */
    boolean saveBitmapToJPGFile(Bitmap bm, String fileDir, String fileName) {
        boolean ret = false;
        FileOutputStream output = null;
        File mf = new File(fileDir, fileName);
        try {
            output = new FileOutputStream(mf);
            ret = bm.compress(Bitmap.CompressFormat.JPEG, 85, output);
        } catch (IOException e) {
            e.printStackTrace();
            FileLogger.getLogger().severe(UtilFun.ExceptionToString(e));
        } finally {
            if(null != output)  {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    FileLogger.getLogger().severe(UtilFun.ExceptionToString(e));
                }
            }
        }

        return ret;
    }

    /**
     * open camera
     * use callback to get result
     */
    abstract void openCamera();

    /**
     * take photo
     * use callback to get result
     */
    abstract void capturePhoto();

    /**
     * close camera
     */
    abstract void closeCamera();
}
