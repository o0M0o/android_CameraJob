package com.wxm.camerajob.utility;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;

import com.wxm.camerajob.data.define.CameraParam;
import com.wxm.camerajob.data.define.TakePhotoParam;

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

    String mCameraStatus = CAMERA_NOT_OPEN;
    final static String CAMERA_NOT_OPEN             = "CAMERA_NOT_OPEN";
    final static String CAMERA_OPENED               = "CAMERA_OPENED";
    final static String CAMERA_TAKEPHOTO_START      = "CAMERA_TAKEPHOTO_START";
    final static String CAMERA_TAKEPHOTO_SUCCESS    = "CAMERA_TAKEPHOTO_SUCCESS";
    final static String CAMERA_TAKEPHOTO_FAILURE    = "CAMERA_TAKEPHOTO_FAILURE";

    TakePhotoParam        mTPParam;
    CameraParam           mCParam;
    long                  mStartMSec;
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
     * 执行拍照任务
     * @param cp        相机设置
     * @param tp        拍照设置
     * @param stp       回调设置
     */
    public void takePhoto(CameraParam cp, TakePhotoParam tp, SilentCameraTakePhotoCallBack stp)    {
        mCParam = cp;
        mTPParam = tp;
        mTPCBTakePhoto = stp;

        openCamera();
    }


    /**
     * 打开相机回调API
     * @param ret  若为true则打开成功
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
     * 拍照回调API
     * @param ret  若为true则拍照成功
     */
    void takePhotoCallBack(Boolean ret) {
        String tag = (mTPParam == null ? "null"
                : (mTPParam.mTag == null ? "null" : mTPParam.mTag));
        String str_status = mCameraStatus;

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
                    + tag + ", camerastatus = " + str_status;
            Log.i(TAG, l);
            FileLogger.getLogger().info(l);

            if(null != mTPCBTakePhoto)
                mTPCBTakePhoto.onTakePhotoFailed(mTPParam);
        }
    }

    /**
     * 保存照片到文件
     * @param data          照片数据
     * @param fileDir       文件所在文件夹
     * @param fileName      文件名
     * @return   执行成功返回{@code true}
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
     * 保存照片到文件
     * @param bm            位图数据
     * @param fileDir       文件所在文件夹
     * @param fileName      文件名
     * @return   执行成功返回{@code true}
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
     * 打开相机
     * 通过回调函数得到结果
     */
    abstract void openCamera();


    /**
     * 进行拍照
     * 通过回调函数得到结果
     */
    abstract void capturePhoto();


    /**
     * 关闭相机
     */
    abstract void closeCamera();
}
