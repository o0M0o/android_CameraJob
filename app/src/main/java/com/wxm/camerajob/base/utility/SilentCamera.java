package com.wxm.camerajob.base.utility;

import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;

import com.wxm.camerajob.base.data.CameraParam;
import com.wxm.camerajob.base.data.TakePhotoParam;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import cn.wxm.andriodutillib.util.UtilFun;

/**
 * 静默相机基类
 * Created by 123 on 2016/7/4.
 */
public abstract class SilentCamera {
    private final static String TAG = "SilentCamera";

    protected Semaphore                   mCameraLock;
    protected int                         mSensorOrientation;
    protected static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @SuppressWarnings("UnusedParameters")
    public interface SilentCameraTakePhotoCallBack {
        void onTakePhotoSuccess(TakePhotoParam tp);
        void onTakePhotoFailed(TakePhotoParam tp);
    }

    @SuppressWarnings("UnusedParameters")
    public interface SilentCameraOpenCameraCallBack {
        void onOpenSuccess(CameraParam cp);
        void onOpenFailed(CameraParam cp);
    }

    private SilentCameraOpenCameraCallBack     mOCCBOpen;
    private SilentCameraTakePhotoCallBack      mTPCBTakePhoto;

    protected TakePhotoParam        mTPParam;
    protected CameraParam           mCParam;
    protected long                  mStartMSec;
    protected boolean               mFlashSupported;

    protected String mCameraStatus = CAMERA_NOT_OPEN;
    protected final static String CAMERA_NOT_SETUP            = "CAMERA_NOT_SETUP";
    protected final static String CAMERA_NOT_OPEN             = "CAMERA_NOT_OPEN";
    protected final static String CAMERA_SETUP                = "CAMERA_SETUP";
    protected final static String CAMERA_OPEN_FINISHED        = "CAMERA_OPEN_FINISHED";
    protected final static String CAMERA_TAKEPHOTO_START      = "CAMERA_TAKEPHOTO_START";
    protected final static String CAMERA_TAKEPHOTO_FINISHED   = "CAMERA_TAKEPHOTO_FINISHED";
    protected final static String CAMERA_TAKEPHOTO_SAVEED     = "CAMERA_TAKEPHOTO_SAVEED";
    protected final static String CAMERA_TAKEPHOTO_FAILED     = "CAMERA_TAKEPHOTO_FAILED";


    public SilentCamera()   {
        mCameraLock = new Semaphore(1);
    }

    public void setOpenCameraCallBack(SilentCameraOpenCameraCallBack oc)   {
        mOCCBOpen = oc;
    }

    public void setTakePhotoCallBack(SilentCameraTakePhotoCallBack oc)   {
        mTPCBTakePhoto = oc;
    }


    protected void openCameraCallBack(Boolean ret) {
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

    protected void takePhotoCallBack(Boolean ret) {
        String tag = (mTPParam == null ? "null"
                : (mTPParam.mTag == null ? "null" : mTPParam.mTag));
        if(ret) {
            String l = "take photo success, paratag = " + tag;
            Log.i(TAG, l);
            FileLogger.getLogger().info(l);

            if(null != mTPCBTakePhoto)
                mTPCBTakePhoto.onTakePhotoSuccess(mTPParam);
        }
        else {
            String l = "take photo failed, paratag = "
                    + tag + ", camerastatus = " + mCameraStatus;
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
    protected boolean savePhotoToFile(byte[] data, String fileDir, String fileName) {
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
     * 设定相机
     * 在使用相机前需要使用此函数
     * @param cp    相机参数
     * @return  成功返回true, 否则返回false
     */
    public abstract boolean setupCamera(CameraParam cp);


    /**
     * 打开相机
     * 通过回调函数得到操作结果
     */
    public abstract void openCamera();


    /**
     * 执行拍照
     * @param tp 拍照参数
     * @return 成功返回true, 否则返回false
     */
    public abstract boolean takePhoto(TakePhotoParam tp);


    /**
     * 关闭相机
     */
    public abstract void closeCamera();
}
