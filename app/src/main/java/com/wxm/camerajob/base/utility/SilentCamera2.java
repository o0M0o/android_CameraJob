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
import java.util.Collections;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.wxm.camerajob.base.utility.FileLogger.getLogger;

/**
 * 静默相机版本2
 * Created by wxm on 2016/6/23.
 */
class SilentCamera2 {
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
    private final static String CAMERA_NOT_SETUP            = "CAMERA_NOT_SETUP";
    private final static String CAMERA_NOT_OPEN             = "CAMERA_NOT_OPEN";
    private final static String CAMERA_SETUP                = "CAMERA_SETUP";
    private final static String CAMERA_OPEN_FINISHED        = "CAMERA_OPEN_FINISHED";
    private final static String CAMERA_TAKEPHOTO_START      = "CAMERA_TAKEPHOTO_START";
    private final static String CAMERA_TAKEPHOTO_FINISHED   = "CAMERA_TAKEPHOTO_FINISHED";
    private final static String CAMERA_TAKEPHOTO_SAVEED     = "CAMERA_TAKEPHOTO_SAVEED";
    private final static String CAMERA_TAKEPHOTO_FAILED     = "CAMERA_TAKEPHOTO_FAILED";

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
    private int                     mCompletedTime = 0;

    @SuppressWarnings("UnusedParameters")
    public interface SilentCamera2TakePhotoCallBack {
        void onTakePhotoSuccess(TakePhotoParam tp);
        void onTakePhotoFailed(TakePhotoParam tp);
    }

    @SuppressWarnings("UnusedParameters")
    public interface SilentCamera2OpenCameraCallBack {
        void onOpenSuccess(CameraParam cp);
        void onOpenFailed(CameraParam cp);
    }

    private SilentCamera2OpenCameraCallBack     mOCCBOpen;
    private SilentCamera2TakePhotoCallBack      mTPCBTakePhoto;

    /**
     * 构造函数
     */
    public SilentCamera2()  {
        mCameraOpenCloseLock = new Semaphore(1);
    }

    public void setOpenCameraCallBack(SilentCamera2OpenCameraCallBack oc)   {
        mOCCBOpen = oc;
    }

    public void setTakePhotoCallBack(SilentCamera2TakePhotoCallBack oc)   {
        mTPCBTakePhoto = oc;
    }

    private void openCameraCallBack(Boolean ret) {
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

    private void takePhotoCallBack(Boolean ret) {
        try {
            mCaptureSession.abortCaptures();
        } catch (CameraAccessException e) {
            e.printStackTrace();
            FileLogger.getLogger().severe(UtilFun.ExceptionToString(e));
        }

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
     * 设定相机
     * 在使用相机前需要使用此函数
     * @param cm    相机服务
     * @param cp    相机参数
     * @return 如果成功返回true, 否则返回false
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean setupCamera(CameraManager cm, CameraParam cp)    {
        closeCamera();

        mCMCameramanager = cm;
        mCParam = cp;
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            return setUpCameraOutputs(mCParam.mFace);
        } catch (InterruptedException e) {
            e.printStackTrace();
            getLogger().severe(UtilFun.ThrowableToString(e));
            return false;
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * 打开相机
     */
    public void openCamera()     {
        if(mCameraStatus.equals(CAMERA_TAKEPHOTO_START)
                || mCameraStatus.equals(CAMERA_TAKEPHOTO_FINISHED))   {
            Log.w(TAG, "when open camera, status = " + mCameraStatus);

            openCameraCallBack(false);
            return;
        }

        if(mCameraStatus.equals(CAMERA_OPEN_FINISHED)) {
            openCameraCallBack(true);
            return;
        }

        if(!mCameraStatus.equals(CAMERA_NOT_SETUP))
            closeCamera();

        if (ContextCompat.checkSelfPermission(ContextUtil.getInstance(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            //requestCameraPermission();
            Log.i(TAG, "need camera permission");
            openCameraCallBack(false);
            return;
        }

        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            mCMCameramanager.openCamera(mCameraId,
                    mCameraDeviceStateCallback, mCParam.mSessionHandler);
        } catch (InterruptedException | CameraAccessException e){
            e.printStackTrace();
            getLogger().severe(UtilFun.ThrowableToString(e));
        }
    }


    /**
     * 执行一次拍照
     * 此操作只能APP范围内串行
     * @param tp 拍照参数
     * @return 成功返回true, 否则返回false
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean takePhoto(TakePhotoParam tp)  {
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
        mCompletedTime = 0;
        if(captureStillPicture())    {
            ret = true;
        } else  {
            takePhotoCallBack(false);
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
            /*
            mCaptureBuilder = mCameraDevice
                    .createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mCaptureBuilder.addTarget(mImageReader.getSurface());

            mCaptureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            if (mFlashSupported) {
                mCaptureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            }
            */

            // set Orientation
            mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation());
            mCaptureSession.capture(
                    mCaptureBuilder.build(),
                    mCaptureCallback,
                    mCParam.mSessionHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            FileLogger.getLogger().severe(UtilFun.ThrowableToString(e));

            return false;
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
     *  @param face   facing lens or backing lens
     * */
    private boolean setUpCameraOutputs(int face) {
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
                if (map == null)
                    continue;

                Integer or = cc.get(CameraCharacteristics.SENSOR_ORIENTATION);
                if(null != or)
                    mSensorOrientation = or.intValue();
                else
                    mSensorOrientation = 90;

                // Check if the flash is supported.
                Boolean available = cc.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                mCameraId = cameraId;
                mCameraStatus = CAMERA_SETUP;
                return true;
            }
        } catch (NullPointerException | CameraAccessException e) {
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

//            mOCCBOpen = null;
//            mTPCBTakePhoto = null;
            mCaptureBuilder = null;
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
                                ImageFormat.JPEG, /*maxImages*/5);

                        Surface mImageReaderSurface = mImageReader.getSurface();
                        mCaptureBuilder.addTarget(mImageReaderSurface);
                        mCaptureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        if (mFlashSupported) {
                            mCaptureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        }

                        mCameraDevice.createCaptureSession(
                                Collections.singletonList(mImageReaderSurface),
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

                    openCameraCallBack(false);
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.i(TAG, "onError, error = " + error);
                    getLogger().info("onError, error = " + error);

                    mCameraDevice = null;
                    mCameraStatus = CAMERA_NOT_OPEN;
                    mCameraOpenCloseLock.release();
                    camera.close();

                    openCameraCallBack(false);
                }
            };

    private CameraCaptureSession.StateCallback mSessionStateCallback =
            new CameraCaptureSession.StateCallback() {
                private final static String TAG = "SessionSCB";

                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    Log.i(TAG, "onConfigured");
                    mCaptureSession = session;
                    mCameraStatus = CAMERA_OPEN_FINISHED;
                    mCameraOpenCloseLock.release();

                    openCameraCallBack(true);
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e(TAG, "onConfigureFailed, session : " + session.toString());
                    mCameraStatus = CAMERA_NOT_OPEN;
                    mCameraOpenCloseLock.release();

                    openCameraCallBack(false);
                }
            };


    private CameraCaptureSession.CaptureCallback  mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {
        //在有的手机上（note5)，需要若干帧后才能调整好对焦和曝光
        private int     MAX_WAIT_FRAMES = 12;

        /**
         * 保存photo
         */
        private boolean savePhoto()    {
            Image ig = mImageReader.acquireLatestImage();
            if(null == ig) {
                FileLogger.getLogger().severe("can not get image");
                return false;
            }

            boolean ret = false;
            FileOutputStream output = null;
            try {
                ByteBuffer buffer = ig.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);

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

                ret = true;
            } catch (Throwable e) {
                e.printStackTrace();
                FileLogger.getLogger().severe(UtilFun.ThrowableToString(e));
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

            return ret;
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            mCompletedTime += 1;
            //FileLogger.getLogger().info("onCaptureCompleted");
            if(CAMERA_TAKEPHOTO_FINISHED.equals(mCameraStatus))
                return;

            long endms = mStartMSec + mCParam.mWaitMSecs + mTPParam.mWaitMSecs;
            if(checkCaptureResult(result)) {
                mCameraStatus = CAMERA_TAKEPHOTO_FINISHED;
                mCompletedTime = 0;

                takePhotoCallBack(savePhoto());
            }
            else if(endms < System.currentTimeMillis()) {
                takePhotoCallBack(false);
            }
            else if(mCompletedTime >= MAX_WAIT_FRAMES)   {
                takePhotoCallBack(false);
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
            takePhotoCallBack(false);
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
            Log.d(TAG, "onCaptureCompleted, " +
                    "afState = " + (afState == null ? "null" : afState) +
                    ", aeState = " + (aeState == null ? "null" : aeState) +
                    ", mCameraStatus = " + mCameraStatus);


            // 旧驱动没有检查曝光和对焦功能
            if((null == afState)||(null == aeState)) {
                return true;
            }

            boolean afflag = false;
            if(mCParam.mAutoFocus
                    && ( CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                    CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN == afState ||
                    CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED == afState ||
                    CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED == afState))  {
                afflag = true;
            }
            else    {
                if(!mCParam.mAutoFocus)
                    afflag = true;
            }

            if(afflag) {
                // check ae
                boolean aeflag = false;
                if ((CaptureResult.CONTROL_AE_STATE_SEARCHING != aeState)
                        && (CaptureResult.CONTROL_AE_STATE_INACTIVE != aeState)) {
                    aeflag = true;
                }

                return aeflag;
            }

            return false;
        }
    };
}
