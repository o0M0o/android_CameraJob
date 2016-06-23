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
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.wxm.camerajob.base.utility.FileLogger.*;

/**
 * 静默相机版本2
 * Created by wxm on 2016/6/23.
 */
public class SilentCamera2 {
    private final static String TAG = "SilentCamera2";

    private int                         mSensorOrientation;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String mCameraStatus = CAMERA_NOT_OPEN;
    public final static String CAMERA_NOT_SETUP            = "CAMERA_NOT_SETUP";
    public final static String CAMERA_NOT_OPEN             = "CAMERA_NOT_OPEN";
    public final static String CAMERA_SETUP                = "CAMERA_SETUP";
    public final static String CAMERA_OPEN_FINISHED        = "CAMERA_OPEN_FINISHED";
    public final static String CAMERA_TAKEPHOTO_START      = "CAMERA_TAKEPHOTO_START";
    public final static String CAMERA_TAKEPHOTO_FINISHED   = "CAMERA_TAKEPHOTO_FINISHED";
    public final static String CAMERA_TAKEPHOTO_SAVEED     = "CAMERA_TAKEPHOTO_SAVEED";
    public final static String CAMERA_TAKEPHOTO_FAILED     = "CAMERA_TAKEPHOTO_FAILED";

    private Semaphore               mCameraOpenCloseLock;
    private ImageReader             mImageReader;
    private String                  mCameraId;
    private CameraDevice            mCameraDevice = null;
    private CameraCaptureSession    mCaptureSession = null;
    private CaptureRequest.Builder  mCaptureBuilder = null;
    private boolean                 mFlashSupported;

    private TakePhotoParam          mTPParam;
    private CameraParam             mCParam;
    private CameraManager           mCMCameramanager;
    private long                    mStartMSec;

    public SilentCamera2()  {
        mCameraOpenCloseLock = new Semaphore(1);
    }

    public boolean setupCamera(CameraManager cm, CameraParam cp)    {
        closeCamera();

        mCMCameramanager = cm;
        mCParam = cp;
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            if(setUpCameraOutputs(mCParam.mFace,
                    mCParam.mPhotoSize.getWidth(), mCParam.mPhotoSize.getHeight()))     {
                return true;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            getLogger().severe(UtilFun.ThrowableToString(e));
        } finally {
            mCameraOpenCloseLock.release();
        }

        return true;
    }

    public boolean openCamera()     {
        long endms = System.currentTimeMillis() + mCParam.mWaitMSecs;
        if(mCameraStatus.equals(CAMERA_TAKEPHOTO_START)
                || mCameraStatus.equals(CAMERA_TAKEPHOTO_FINISHED))   {
            Log.w(TAG, "when open camera, status = " + mCameraStatus);
            return false;
        }

        if(mCameraStatus.equals(CAMERA_OPEN_FINISHED))
            return true;

        if(!mCameraStatus.equals(CAMERA_NOT_SETUP))
            closeCamera();

        if (ContextCompat.checkSelfPermission(ContextUtil.getInstance(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            //requestCameraPermission();
            Log.i(TAG, "need camera permission");
            return false;
        }

        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            mCMCameramanager.openCamera(mCameraId, mCameraDeviceStateCallback, mCParam.mSessionHandler);
        } catch (InterruptedException e) {
            e.printStackTrace();
            getLogger().severe(UtilFun.ThrowableToString(e));
        } catch (CameraAccessException e) {
            e.printStackTrace();
            getLogger().severe(UtilFun.ThrowableToString(e));
        }

        // 等待相机状态
        long lastms = System.currentTimeMillis();
        long curms = System.currentTimeMillis();
        while(((!CAMERA_NOT_OPEN.equals(mCameraStatus))
                    || (!CAMERA_OPEN_FINISHED.equals(mCameraStatus)))
                && (curms < endms))  {
            try {
                Thread.sleep(200);

                curms = System.currentTimeMillis();
                if((curms - lastms) > 1000) {
                    //Log.i(TAG, "wait open camera one second...");
                    //FileLogger.getLogger().info("wait open camera one second...");
                    lastms = curms;
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                getLogger().severe(UtilFun.ThrowableToString(e));
                break;
            }
        }

        return mCameraStatus.equals(CAMERA_OPEN_FINISHED);
    }


    /**
     * 执行一次拍照
     * 此操作只能APP范围内串行
     * @param tp 拍照参数
     * @return 成功返回true, 否则返回false
     */
    public boolean takePhoto(TakePhotoParam tp)  {
        if(!mCameraStatus.equals(CAMERA_OPEN_FINISHED)
                && !mCameraStatus.equals(CAMERA_TAKEPHOTO_FAILED)
                && !mCameraStatus.equals(CAMERA_TAKEPHOTO_SAVEED))  {
            return false;
        }

        mCameraStatus = CAMERA_OPEN_FINISHED;
        mStartMSec = System.currentTimeMillis();
        mTPParam = tp;

        String l = "camera opened, paratag = "
                + ((mTPParam == null || mTPParam.mTag == null) ? "null" : mTPParam.mTag);
        Log.i(TAG, l);
        FileLogger.getLogger().info(l);

        boolean ret = false;
        if(captureStillPicture())    {
            long endmesc = mStartMSec + mCParam.mWaitMSecs + mTPParam.mWaitMSecs;
            while(((CAMERA_TAKEPHOTO_START.equals(mCameraStatus))
                    || (CAMERA_TAKEPHOTO_FINISHED.equals(mCameraStatus)))
                    && (System.currentTimeMillis() < endmesc))  {
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                    getLogger().severe(UtilFun.ThrowableToString(e));
                }
            }

            if(CAMERA_TAKEPHOTO_SAVEED.equals(mCameraStatus))
                ret = true;
        }

        return ret;
    }


    /**
     * Capture a still picture
     * @return 成功返回true, 否则返回false
     */
    private boolean captureStillPicture() {
        mCameraStatus = CAMERA_TAKEPHOTO_START;
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

//        Log.i(TAG, "Orientation : display = " + rotation
//                + ", sensor = " + mSensorOrientation
//                + ", ret = " + ret);
        return ret;
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param face   facing lens or backing lens
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private boolean setUpCameraOutputs(int face, int width, int height) {
        //FileLogger.getLogger().info("setUpCameraOutputs");
        mCameraId = "";
        try {
            for (String cameraId : mCMCameramanager.getCameraIdList()) {
                CameraCharacteristics cc
                        = mCMCameramanager.getCameraCharacteristics(cameraId);

                Integer facing = cc.get(CameraCharacteristics.LENS_FACING);
                if((null != facing) && (face != facing))
                    continue;

                StreamConfigurationMap map = cc.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                mSensorOrientation = cc.get(CameraCharacteristics.SENSOR_ORIENTATION);

                // Check if the flash is supported.
                Boolean available = cc.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                mCameraId = cameraId;
                mCameraStatus = CAMERA_SETUP;
                return true;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            getLogger().severe(UtilFun.ThrowableToString(e));
        }

        return false;
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

            String l = "camera closed, paratag = "
                    + ((mTPParam == null) || (mTPParam.mTag == null) ? "null" : mTPParam.mTag);
            Log.i(TAG, l);
            FileLogger.getLogger().info(l);
            mCameraStatus = CAMERA_NOT_SETUP;
        } catch (InterruptedException e) {
            getLogger().severe(UtilFun.ThrowableToString(e));
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private CameraDevice.StateCallback mCameraDeviceStateCallback =
            new CameraDevice.StateCallback() {
                private final static String TAG = "DeviceSCB";

                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    Log.i(TAG, "onOpened");
                    getLogger().info("get camerdevice");

                    mCameraDevice = camera;
                    try {
                        mCaptureBuilder = mCameraDevice
                                .createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

                        mImageReader = ImageReader.newInstance(
                                mCParam.mPhotoSize.getWidth(), mCParam.mPhotoSize.getHeight(),
                                ImageFormat.JPEG, /*maxImages*/2);
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

                    mCameraOpenCloseLock.release();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    Log.i(TAG, "onDisconnected");
                    getLogger().info("camerdevice disconnected");

                    mCameraDevice = null;
                    mCameraStatus = CAMERA_NOT_OPEN;
                    mCameraOpenCloseLock.release();
                    camera.close();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.i(TAG, "onError, error = " + error);
                    getLogger().info("onError, error = " + error);

                    mCameraDevice = null;
                    mCameraStatus = CAMERA_NOT_OPEN;
                    mCameraOpenCloseLock.release();
                    camera.close();
                }
            };

    private CameraCaptureSession.StateCallback mSessionStateCallback =
            new CameraCaptureSession.StateCallback() {
                private final static String TAG = "SessionSCB";

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    Log.i(TAG, "onConfigured");
                    mCaptureSession = session;
                    mCameraStatus = CAMERA_OPEN_FINISHED;

                    mCameraOpenCloseLock.release();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.e(TAG, "onConfigureFailed, session : "
                            + (null != session ? session.toString() : "null"));
                    mCameraStatus = CAMERA_NOT_OPEN;

                    mCameraOpenCloseLock.release();
                }
            };


    private CameraCaptureSession.CaptureCallback  mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {
        //在有的手机上（note5)，需要若干帧后才能调整好对焦和曝光
        private int   MAX_WAIT_FRAMES = 8;

        /**
         * 保存photo
         */
        private void savePhoto()    {
            Image ig = mImageReader.acquireLatestImage();
            mImageReader.close();
            if(null == ig)
                return;

            ByteBuffer buffer = ig.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                File mf = new File(mTPParam.mPhotoFileDir, mTPParam.mFileName);
                output = new FileOutputStream(mf);
                output.write(bytes);

                Message m = Message.obtain(GlobalContext.getInstance().mMsgHandler,
                        GlobalDef.MSGWHAT_CAMERAJOB_TAKEPHOTO);
                m.obj = new Object[] {Integer.parseInt(mTPParam.mTag), 1};
                m.sendToTarget();

                Log.i(TAG, "save photo to : " + mf.toString());
                FileLogger.getLogger().info("save photo to : " + mf.toString());
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

            int mCompletedTime = 1;
            //FileLogger.getLogger().info("onCaptureCompleted");
            if(CAMERA_TAKEPHOTO_FINISHED.equals(mCameraStatus))
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
            Log.d(TAG, "onCaptureCompleted, afState = " + afState +
                    ", aeState = " + aeState +
                    ", mCameraStatus = " + mCameraStatus);


            // 旧驱动没有检查曝光和对焦功能
            if((null == afState)||(null == aeState))
                return true;

            boolean afflag = false;
            if(mCParam.mAutoFocus
                    && ((null == afState)
                    || (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                    CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN == afState ||
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
}