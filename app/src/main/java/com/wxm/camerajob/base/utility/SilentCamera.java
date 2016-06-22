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
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

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
    private static Semaphore        mSLCameraGlobalLock = new Semaphore(1);
    private long    mStartMSec;

    private int                         mSensorOrientation;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private int mCameraStatus = CAMERA_NOT_OPEN;
    public final static int CAMERA_NOT_OPEN             = 1;
    public final static int CAMERA_OPEN_FAILED          = 2;
    public final static int CAMERA_OPEN_FINISHED        = 3;
    public final static int CAMERA_TAKEPHOTO_START      = 4;
    public final static int CAMERA_TAKEPHOTO_FINISHED   = 5;
    public final static int CAMERA_TAKEPHOTO_SAVEED     = 6;
    public final static int CAMERA_TAKEPHOTO_FAILED     = 7;

    private ImageReader mImageReader;
    private boolean     mFlashSupported;
    private int   mCompletedTime = 0;

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
                    FileLogger.getLogger().info("get camerdevice");

                    mCameraOpenCloseLock.release();
                    mCameraDevice = camera;

                    // Here, we create a CameraCaptureSession for camera preview.
                    try {
                        mCaptureBuilder = mCameraDevice
                                .createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                        mCaptureBuilder.addTarget(mImageReader.getSurface());

                        mCaptureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        if (mFlashSupported) {
                            mCaptureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        }

                        mCameraDevice.createCaptureSession(
                                Arrays.asList(mImageReader.getSurface()),
                                mSessionStateCallback, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    Log.i(TAG, "onDisconnected");
                    FileLogger.getLogger().info("camerdevice disconnected");

                    mCameraOpenCloseLock.release();
                    camera.close();

                    mCameraDevice = null;
                    mCameraStatus = CAMERA_OPEN_FAILED;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.i(TAG, "onError, error = " + error);
                    FileLogger.getLogger().info("onError, error = " + error);

                    mCameraOpenCloseLock.release();
                    camera.close();

                    mCameraDevice = null;
                    mCameraStatus = CAMERA_OPEN_FAILED;
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
                        mCameraStatus = CAMERA_NOT_OPEN;
                        return;
                    }

                    mCaptureSession = session;
                    mCameraStatus = CAMERA_OPEN_FINISHED;



                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.e(TAG, "onConfigureFailed, session : "
                            + (null != session ? session.toString() : "null"));
                    mCameraStatus = CAMERA_NOT_OPEN;
                }
            };


    private CameraCaptureSession.CaptureCallback  mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {
        //在有的手机上（note5)，需要若干帧后才能调整好对焦和曝光
        private int   MAX_WAIT_FRAMES = 8;
        private File  mFile;

        /**
         * 设置输出文件
         */
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

        /**
         * 保存photo
         */
        private void savePhoto()    {
            Image ig = mImageReader.acquireLatestImage();
            if(null == ig)
                return;

            ByteBuffer buffer = ig.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                setupFile();
                output = new FileOutputStream(mFile);
                output.write(bytes);

                Message m = Message.obtain(GlobalContext.getInstance().mMsgHandler,
                        GlobalDef.MSGWHAT_CAMERAJOB_TAKEPHOTO);
                m.obj = new Object[] {Integer.parseInt(mTPParam.mTag), 1};
                m.sendToTarget();

                Log.i(TAG, "save photo to : " + mFile.toString());
                FileLogger.getLogger().info("save photo to : " + mFile.toString());
                mCameraStatus = CAMERA_TAKEPHOTO_SAVEED;
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

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            mCompletedTime += 1;
            Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
            Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
            Log.d(TAG, "onCaptureCompleted, afState = " + afState +
                    ", aeState = " + aeState +
                    ", mCameraStatus = " + mCameraStatus);
            //FileLogger.getLogger().info("onCaptureCompleted");
            if(CAMERA_TAKEPHOTO_FINISHED == mCameraStatus)
                return;

            boolean useany = false;
            long endms = mStartMSec + mCParam.mWaitMSecs + mTPParam.mWaitMSecs;
            if(checkCaptureResult(result)) {
                useany = true;
            }
            else if(endms < System.currentTimeMillis()) {
                useany = true;
            }
            else if(mCompletedTime >= MAX_WAIT_FRAMES)   {
                useany = true;
            }

            if(useany)  {
                mCameraStatus = CAMERA_TAKEPHOTO_FINISHED;
                savePhoto();
            }
            else    {
                captureStillPicture();
            }
        }


        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                    @NonNull CaptureRequest request,
                                    @NonNull CaptureFailure failure)    {
            super.onCaptureFailed(session,request, failure);
            Log.d(TAG, "CaptureFailed, reason = "  + failure.getReason());
            FileLogger.getLogger().warning(
                    "CaptureFailed, reason = "  + failure.getReason());

            try {
                session.stopRepeating();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            mCameraStatus = CAMERA_TAKEPHOTO_FAILED;
        }


        /**
         * 检查拍照结果
         * @param tr 拍照结果
         * @return  如果拍照结果可接受返回true
         */
        private boolean checkCaptureResult(TotalCaptureResult tr)   {
            // check focus
            Integer afState = tr.get(CaptureResult.CONTROL_AF_STATE);
            Integer aeState = tr.get(CaptureResult.CONTROL_AE_STATE);
            // 旧驱动没有检查曝光和对焦功能
            if((null == afState)||(null == aeState))
                return true;

            boolean afflag = false;
            if(mCParam.mAutoFocus
                && ((null == afState)
                    || (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                        CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED == afState ||
                        CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED == afState)))  {
                afflag = true;
            }
            else    {
                if(!mCParam.mAutoFocus)
                    afflag = true;
            }

            if(afflag) {
                // check ae
                boolean aeflag = false;
                if ((null == aeState)
                        || ((CaptureResult.CONTROL_AE_STATE_SEARCHING != aeState)
                        && (CaptureResult.CONTROL_AE_STATE_INACTIVE != aeState))) {
                    aeflag = true;
                }

                return  afflag && aeflag;
            }

            return false;
        }
    };


    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {


        @Override
        public void onImageAvailable(ImageReader reader) {
        }
    };


    public SilentCamera(CameraManager cm
            ,HashMap<String, CameraCharacteristics> hm) {
        mCameraOpenCloseLock = new Semaphore(1);
        mCameraManager = cm;
        mHMCameraCharacteristics = hm;
    }

    /**
     * 执行一次拍照
     * 此操作只能APP范围内串行
     * @param cp 相机参数
     * @param tp 拍照参数
     * @return 成功返回true, 否则返回false
     */
    public boolean TakeOncePhoto(CameraParam cp, TakePhotoParam tp)  {
        mStartMSec = System.currentTimeMillis();
        mCParam = cp;
        mTPParam = tp;
        boolean ret = false;
        try {
            if (!mSLCameraGlobalLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera operation.");
            }

            waitOpenCamera();
            if(CAMERA_OPEN_FINISHED != mCameraStatus) {
                Log.i(TAG, "can not open camera, mCameraStatus = " + mCameraStatus);
                FileLogger.getLogger().info("can not open camera, mCameraStatus = "
                                            + mCameraStatus);

                return false;
            }

            if(captureStillPicture())    {
                long endmesc = mStartMSec + mCParam.mWaitMSecs + mTPParam.mWaitMSecs;
                while(((CAMERA_TAKEPHOTO_START ==  mCameraStatus)
                        || (CAMERA_TAKEPHOTO_FINISHED == mCameraStatus))
                        && (System.currentTimeMillis() < endmesc))  {
                    try {
                        Thread.sleep(300);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(CAMERA_TAKEPHOTO_SAVEED == mCameraStatus)
                    ret = true;
            } else {
                ret = false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mSLCameraGlobalLock.release();
        }

        return ret;
    }


    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private int getOrientation() {
        Display dp =  ((WindowManager)
                        ContextUtil.getInstance()
                                .getSystemService(Context.WINDOW_SERVICE))
                                    .getDefaultDisplay();

        int rotation = dp.getRotation();

        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        int ret = (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;

        Log.i(TAG, "Orientation : display = " + rotation
                    + ", sensor = " + mSensorOrientation
                    + ", ret = " + ret);
        return ret;
    }

    /**
     * Capture a still picture
     * @return 成功返回true, 否则返回false
     */
   private boolean captureStillPicture() {
       try {
           mCameraStatus = CAMERA_TAKEPHOTO_START;
           // set Orientation
           mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation());
           mCaptureSession.capture(
                   mCaptureBuilder.build(),
                   mCaptureCallback,
                   mCParam.mSessionHandler);
       } catch (CameraAccessException e) {
           e.printStackTrace();
       }

       return true;
    }

    /**
     * 关闭相机
     */
    public void closeCamera()   {
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

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
            mCameraStatus = CAMERA_NOT_OPEN;
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
     */
    private void openCamera()   {
        Log.i(TAG, "openCamera");
        //FileLogger.getLogger().info("openCamera");
        if (ContextCompat.checkSelfPermission(ContextUtil.getInstance(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            //requestCameraPermission();
            Log.i(TAG, "need camera permission");
            return;
        }

        try {
            setUpCameraOutputs(mCParam.mFace,
                    mCParam.mPhotoSize.getWidth(), mCParam.mPhotoSize.getHeight());

            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            if((null == mCameraId) || mCameraId.isEmpty())   {
                mCameraStatus = CAMERA_OPEN_FAILED;
                return;
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
     */
    private void waitOpenCamera() {
        long endms = System.currentTimeMillis() + mCParam.mWaitMSecs;
        openCamera();
        if(CAMERA_OPEN_FAILED == mCameraStatus)
            return;

        // 等待相机状态
        long lastms = System.currentTimeMillis();
        long curms = System.currentTimeMillis();
        while((CAMERA_NOT_OPEN == mCameraStatus)
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
        //FileLogger.getLogger().info("setUpCameraOutputs");
        mCameraId = "";
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

            mSensorOrientation = cc.get(CameraCharacteristics.SENSOR_ORIENTATION);
            mImageReader = ImageReader.newInstance(
                    width, height,
                    ImageFormat.JPEG, /*maxImages*/2);

            mImageReader.setOnImageAvailableListener(
                    mOnImageAvailableListener
                    ,mCParam.mSessionHandler);

            // Check if the flash is supported.
            Boolean available = cc.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            mFlashSupported = available == null ? false : available;
            if(mFlashSupported && mCParam.mAutoFlash)
                mFlashSupported = true;
            else
                mFlashSupported = false;

            mCameraId = id;
            return;
        }
    }



}
