package com.wxm.camerajob.base.utility;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.wxm.camerajob.base.data.CameraParam;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.data.TakePhotoParam;
import com.wxm.camerajob.base.handler.GlobalContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 静默相机
 * Created by 123 on 2016/6/16.
 */
public class SilentCamera {
    private final static String TAG = "SilentCamera";

    private int mCameraStatus = CAMERA_NOT_READY;
    public final static int CAMERA_NOT_READY            = 1;
    public final static int CAMERA_IDLE                 = 2;
    public final static int CAMERA_TAKEPHOTO_START      = 3;
    public final static int CAMERA_TAKEPHOTO_FINISHED   = 4;
    public final static int CAMERA_TAKEPHOTO_FAILED     = 5;
    public final static int CAMERA_OPEN_FAILED          = 6;

    private File        mFile;
    private ImageReader mImageReader;
    private boolean     mFlashSupported;

    private CameraManager                           mCameraManager;
    private HashMap<String, CameraCharacteristics>  mHMCameraCharacteristics;

    private Semaphore               mCameraOpenCloseLock;
    private String                  mCameraId;
    private CameraDevice            mCameraDevice = null;
    private CameraCaptureSession    mCaptureSession = null;
    private CaptureRequest.Builder  mCaptureBuilder = null;

    private TakePhotoParam  mTPParam;
    private CameraParam     mCParam;

    private CameraDevice.StateCallback mCameraDeviceStateCallback =
            new CameraDevice.StateCallback() {
                private final static String TAG = "DeviceSCB";

                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    Log.i(TAG, "onOpened");

                    mCameraOpenCloseLock.release();
                    mCameraDevice = camera;

                    // Here, we create a CameraCaptureSession for camera preview.
                    try {
                        mCameraDevice.createCaptureSession(
                                Arrays.asList(mImageReader.getSurface()),
                                mSessionStateCallback, null);

                        mCaptureBuilder = mCameraDevice
                                            .createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                        mCaptureBuilder.addTarget(mImageReader.getSurface());

                        // Use the same AE and AF modes as the preview.
                        mCaptureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        if (mFlashSupported) {
                            mCaptureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        }
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    Log.i(TAG, "onDisconnected");

                    mCameraOpenCloseLock.release();
                    camera.close();
                    mCameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.i(TAG, "onError, error = " + error);

                    mCameraOpenCloseLock.release();
                    camera.close();
                    mCameraDevice = null;
                }
            };

    private CameraCaptureSession.StateCallback mSessionStateCallback =
            new CameraCaptureSession.StateCallback() {
                private final static String TAG = "SessionSCB";

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    Log.i(TAG, "onConfigured");

                    // The camera is already closed
                    if (null == mCameraDevice) {
                        mCameraStatus = CAMERA_NOT_READY;
                        return;
                    }

                    mCaptureSession = session;
                    mCameraStatus = CAMERA_IDLE;
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.e(TAG, "onConfigureFailed, session : "
                            + (null != session ? session.toString() : "null"));
                    mCameraStatus = CAMERA_NOT_READY;
                }
            };


    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image ig = reader.acquireNextImage();
            ByteBuffer buffer = ig.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
                //mCameraStatus = CAMERA_TAKEPHOTO_FINISHED;

                Log.i(TAG, "save photo to : " + mFile.toString());
                FileLogger.getLogger().info("save photo to : " + mFile.toString());
                Message m = Message.obtain(GlobalContext.getInstance().mMsgHandler,
                        GlobalDef.MSGWHAT_CAMERAJOB_TAKEPHOTO);
                m.obj = new Object[] {Integer.parseInt(mTPParam.mTag), 1};
                m.sendToTarget();

                /*
                // give up others
                while(null != reader.acquireNextImage())    {
                }
                */
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                ig.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    public SilentCamera() {
        mCameraOpenCloseLock = new Semaphore(1);
        mCameraManager = (CameraManager) ContextUtil.getInstance()
                                .getSystemService(Context.CAMERA_SERVICE);
        mHMCameraCharacteristics = new HashMap<>();
        try {
            for (String cameraId : mCameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = mCameraManager.getCameraCharacteristics(cameraId);

                mHMCameraCharacteristics.put(cameraId, characteristics);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public boolean TakeOncePhoto(CameraParam cp, TakePhotoParam tp)  {
        waitOpenCamera(cp);
        if(CAMERA_IDLE != mCameraStatus) {
            Log.i(TAG, "in TakeOncePhoto, mCameraStatus = " + mCameraStatus);
            FileLogger.getLogger().info("in TakeOncePhoto, mCameraStatus = " + mCameraStatus);

            // 释放资源
            closeCamera();
            return false;
        }

        mTPParam = tp;
        long endmesc = System.currentTimeMillis() + mTPParam.mWaitMSecs;
        if(captureStillPicture(endmesc))    {
            while((CAMERA_TAKEPHOTO_START == getCameraStatus())
                    && (System.currentTimeMillis() < endmesc))  {
                try {
                    Thread.sleep(300);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        boolean ret = true;
        if(CAMERA_TAKEPHOTO_FINISHED != getCameraStatus())
            ret = false;

        closeCamera();
        return ret;
    }

    /**
     * Capture a still picture
     * @param  endmesc      拍照结束时间(毫秒，UTC时间)
     * @return 成功返回true, 否则返回false
     */
   private boolean captureStillPicture(long endmesc) {
        setupFile();

       int cur_status = getCameraStatus();
       if((CAMERA_TAKEPHOTO_FINISHED == cur_status) || (CAMERA_TAKEPHOTO_FAILED == cur_status))
           mCameraStatus = CAMERA_IDLE;

        // 等待相机状态
        while((CAMERA_IDLE != getCameraStatus())
                && (System.currentTimeMillis() < endmesc))  {
            try {
                Thread.sleep(200);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }

        if(CAMERA_IDLE != getCameraStatus()) {
            Log.e(TAG, "in captureStillPicture, camera is not idle");
            FileLogger.getLogger().info("in captureStillPicture, camera is not idle");
            return false;
        }

        try {
            mCameraStatus = CAMERA_TAKEPHOTO_START;
            /*
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            if (mFlashSupported) {
                captureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            }

            // Orientation
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));
            */
            mCaptureSession.capture(
                    mCaptureBuilder.build(),
                    new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                                       @NonNull CaptureRequest request,
                                                       @NonNull TotalCaptureResult result) {
                            Log.d(TAG, "onCaptureCompleted");
                            FileLogger.getLogger().info("onCaptureCompleted");
                            mCameraStatus = CAMERA_TAKEPHOTO_FINISHED;
                        }

                        public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                                    @NonNull CaptureRequest request,
                                                    @NonNull CaptureFailure failure) {
                            Log.d(TAG, "onCaptureFailed");
                            FileLogger.getLogger().info("onCaptureFailed");
                            mCameraStatus = CAMERA_TAKEPHOTO_FAILED;
                        }
                    },
                    mCParam.mSessionHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * 关闭相机
     */
   private void closeCamera()   {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }

            Log.i(TAG, "Camera closed");
            FileLogger.getLogger().info("Camera closed");
            mCameraStatus = CAMERA_NOT_READY;
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * 返回相机状态
     * @return   相机状态
     */
    public int getCameraStatus()    {
        return mCameraStatus;
    }

    /**
     * 打开相机
     * @param param                 相机参数
     */
    private void openCamera(CameraParam param)   {
        Log.i(TAG, "openCamera");
        FileLogger.getLogger().info("openCamera");
        if (ContextCompat.checkSelfPermission(ContextUtil.getInstance(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            //requestCameraPermission();
            Log.i(TAG, "need camera permission");
            return;
        }

        mCParam = param;
        setUpCameraOutputs(mCParam.mFace,
                mCParam.mPhotoSize.getWidth(), mCParam.mPhotoSize.getHeight());
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mCParam.mSessionHandler);
        } catch (CameraAccessException e) {
            mCameraStatus = CAMERA_OPEN_FAILED;
            FileLogger.getLogger().severe(UtilFun.ExceptionToString(e));
            e.printStackTrace();
        } catch (InterruptedException e) {
            mCameraStatus = CAMERA_OPEN_FAILED;
            FileLogger.getLogger().severe(UtilFun.ExceptionToString(e));
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        } catch (Throwable e)   {
            mCameraStatus = CAMERA_OPEN_FAILED;
            FileLogger.getLogger().severe(UtilFun.ThrowableToString(e));
        }
        finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * 打开相机
     * 并一直等待直到操作成功或者失败或者超时
     * @param cp 相机参数
     */
    private void waitOpenCamera(CameraParam cp) {
        long endms = System.currentTimeMillis() + cp.mWaitMSecs;
        openCamera(cp);
        if(CAMERA_OPEN_FAILED == mCameraStatus)
            return;

        // 等待相机状态
        long lastms = System.currentTimeMillis();
        long curms = System.currentTimeMillis();
        while((CAMERA_NOT_READY == mCameraStatus)
                && (curms < endms))  {
            try {
                Thread.sleep(200);

                curms = System.currentTimeMillis();
                if((curms - lastms) > 1000) {
                    Log.i(TAG, "wait open camera one second...");
                    FileLogger.getLogger().info("wait open camera one second...");
                    lastms = curms;
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param face   facing lens or backing lens
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private void setUpCameraOutputs(int face, int width, int height) {
        for(String id : mHMCameraCharacteristics.keySet())  {
            CameraCharacteristics cc = mHMCameraCharacteristics.get(id);

            Integer facing = cc.get(CameraCharacteristics.LENS_FACING);
            if((null != facing) && (face != facing))
                continue;

            StreamConfigurationMap map = cc.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                continue;
            }

            mImageReader = ImageReader.newInstance(
                    width, height,
                    ImageFormat.JPEG, /*maxImages*/2);

            mImageReader.setOnImageAvailableListener(
                    mOnImageAvailableListener
                    ,mCParam.mSessionHandler);

            // Check if the flash is supported.
            Boolean available = cc.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            mFlashSupported = available == null ? false : available;
            mCameraId = id;
            return;
        }
    }


    private void setupFile()    {
        String realFileName;
        if(mTPParam.mFileName.isEmpty())  {
            Calendar curCal = Calendar.getInstance();
            realFileName= String.format(
                    "%d%02d%02d-%02d%02d%02d.jpg"
                    ,curCal.get(Calendar.YEAR)
                    ,curCal.get(Calendar.MONTH) + 1
                    ,curCal.get(Calendar.DAY_OF_MONTH) + 1
                    ,curCal.get(Calendar.HOUR_OF_DAY)
                    ,curCal.get(Calendar.MINUTE)
                    ,curCal.get(Calendar.SECOND));
        }
        else    {
            realFileName = mTPParam.mFileName;
        }

        mFile = new File(mTPParam.mPhotoFileDir, realFileName);
    }
}
