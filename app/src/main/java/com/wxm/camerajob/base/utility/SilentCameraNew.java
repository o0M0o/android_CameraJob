package com.wxm.camerajob.base.utility;

import android.Manifest;
import android.annotation.TargetApi;
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
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.wxm.camerajob.base.data.CameraParam;
import com.wxm.camerajob.base.data.TakePhotoParam;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import cn.wxm.andriodutillib.util.UtilFun;

import static com.wxm.camerajob.base.utility.FileLogger.getLogger;

/**
 * 使用camera2 api
 * Created by 123 on 2016/7/4.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SilentCameraNew extends SilentCamera {
    private final static String TAG = "SilentCameraNew";

    private ImageReader     mImageReader;
    private ImageReader     mImageReader1;

    private String                  mCameraId;
    private CameraDevice            mCameraDevice = null;
    private CameraCaptureSession    mCaptureSession = null;
    private CaptureRequest.Builder  mCaptureBuilder = null;

    private CameraManager           mCMCameramanager;

    private int                     mCompletedTime;

    public SilentCameraNew()    {
        super();
        mCompletedTime = 0;
    }

    @Override
    public boolean setupCamera(CameraParam cp) {
        mCParam = cp;
        mCMCameramanager = (CameraManager) ContextUtil.getInstance()
                                .getSystemService(Context.CAMERA_SERVICE);

        try {
            if (!mCameraLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            return setUpCameraOutputs(mCParam.mFace);
        } catch (InterruptedException e) {
            e.printStackTrace();
            getLogger().severe(UtilFun.ThrowableToString(e));
            return false;
        } finally {
            mCameraLock.release();
        }
    }

    @Override
    public void openCamera() {
        if(UtilFun.StringIsNullOrEmpty(mCameraId)) {
            Log.w(TAG, "when open camera, mCameraId not setup");
            return;
        }

        if (ContextCompat.checkSelfPermission(ContextUtil.getInstance(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            //requestCameraPermission();
            Log.i(TAG, "need camera permission");
            openCameraCallBack(false);
            return ;
        }

        try {
            if (!mCameraLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            mCMCameramanager.openCamera(mCameraId,
                    mCameraDeviceStateCallback, mCParam.mSessionHandler);
        } catch (InterruptedException | CameraAccessException e){
            e.printStackTrace();
            getLogger().severe(UtilFun.ThrowableToString(e));
        }
    }

    @Override
    public boolean takePhoto(TakePhotoParam tp) {
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

    @Override
    public void closeCamera() {
        try {
            if (!mCameraLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

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
                    mSensorOrientation = or;
                else
                    mSensorOrientation = 90;

                // Check if the flash is supported.
                Boolean available = cc.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                mCameraId = cameraId;
                mCameraStatus = CAMERA_SETUP;
            }
        } catch (NullPointerException | CameraAccessException e) {
            e.printStackTrace();
            getLogger().severe(UtilFun.ThrowableToString(e));
            return false;
        }

        return !mCameraId.equals("");
    }

    /**
     * Capture a still picture
     * @return 成功返回true, 否则返回false
     */
    private boolean captureStillPicture() {
        mCameraStatus = CAMERA_TAKEPHOTO_START;
        try {
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
        Log.i(TAG, "Orientation : display = " + rotation
                + ", sensor = " + mSensorOrientation + ", ret = " + ret);
        return ret;
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
                        /*
                        mCaptureBuilder = mCameraDevice
                                .createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

                        mImageReader = ImageReader.newInstance(
                                mCParam.mPhotoSize.getWidth(), mCParam.mPhotoSize.getHeight(),
                                ImageFormat.JPEG, 2);

                        mCaptureBuilder.addTarget(mImageReader.getSurface());
                        mCaptureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        if (mFlashSupported) {
                            mCaptureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        }
                        */

                        mImageReader = ImageReader.newInstance(
                                mCParam.mPhotoSize.getWidth(), mCParam.mPhotoSize.getHeight(),
                                ImageFormat.JPEG, 2);
                        mImageReader1 = ImageReader.newInstance(
                                mCParam.mPhotoSize.getWidth(), mCParam.mPhotoSize.getHeight(),
                                ImageFormat.JPEG, 2);
                        mCameraDevice.createCaptureSession(
                                Arrays.asList(mImageReader.getSurface(), mImageReader1.getSurface()),
                                mSessionStateCallback, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                    mCameraLock.release();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    Log.i(TAG, "onDisconnected");
                    getLogger().info("camerdevice disconnected");

                    mCameraDevice = null;
                    mCameraStatus = CAMERA_NOT_OPEN;
                    mCameraLock.release();
                    camera.close();

                    openCameraCallBack(false);
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.i(TAG, "onError, error = " + error);
                    getLogger().info("onError, error = " + error);

                    mCameraDevice = null;
                    mCameraStatus = CAMERA_NOT_OPEN;
                    mCameraLock.release();
                    camera.close();

                    openCameraCallBack(false);
                }
            };

    private CameraCaptureSession.StateCallback mSessionStateCallback =
            new CameraCaptureSession.StateCallback() {
                private final static String TAG = "SessionSCB";
                /**
                 * 保存photo
                 */
                private boolean savePhoto(Image ig) {
                    if (null == ig) {
                        FileLogger.getLogger().severe("can not get image");
                        return false;
                    }

                    ByteBuffer buffer = ig.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);

                    /*
                    Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    bm = ImageUtil.rotateBitmap(bm, getOrientation(), null);
                    boolean ret = saveBitmapToJPGFile(bm, mTPParam.mPhotoFileDir, mTPParam.mFileName);
                    */
                    boolean ret = savePhotoToFile(bytes, mTPParam.mPhotoFileDir, mTPParam.mFileName);
                    if (ret) {
                        String l = "save photo to : " + mTPParam.mFileName;
                        Log.i(TAG, l);
                        getLogger().info(l);
                    }

                    mCameraStatus = CAMERA_TAKEPHOTO_SAVEED;
                    takePhotoCallBack(true);
                    return ret;
                }

                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    Log.i(TAG, "onConfigured");
                    mCaptureSession = session;
                    mCameraStatus = CAMERA_OPEN_FINISHED;
                    mCameraLock.release();

                    // Auto focus should be continuous for camera preview.
                    boolean bret = true;
                    try {
                        mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                        mCaptureBuilder.addTarget(mImageReader1.getSurface());
                        mCaptureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                        // Use the same AE and AF modes as the preview.
                        if(mFlashSupported)
                            mCaptureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                        mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation());
                        mImageReader1.setOnImageAvailableListener(
                                new ImageReader.OnImageAvailableListener() {
                                    @Override
                                    public void onImageAvailable(ImageReader reader) {
                                        savePhoto(reader.acquireLatestImage());
                                    }
                                }, mCParam.mSessionHandler);

                        mCaptureSession.setRepeatingRequest(mCaptureBuilder.build(), mCaptureCallback, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                        bret = false;
                    }

                    openCameraCallBack(bret);
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e(TAG, "onConfigureFailed, session : " + session.toString());
                    mCameraStatus = CAMERA_NOT_OPEN;
                    mCameraLock.release();

                    openCameraCallBack(false);
                }
            };


    private CameraCaptureSession.CaptureCallback  mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {


        private void process(CaptureResult result) {
            // CONTROL_AE_STATE can be null on some devices
            Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
            if (aeState == null
                    || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
                    || aeState == CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED
                    || aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                //captureStillPicture();
                // This is the CaptureRequest.Builder that we use to take a picture.
                /*
                try {
                    mCaptureSession.stopRepeating();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                */
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
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
    };
}
