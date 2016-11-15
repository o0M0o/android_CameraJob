package com.wxm.camerajob.base.utility;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import cn.wxm.andriodutillib.util.ImageUtil;
import cn.wxm.andriodutillib.util.UtilFun;

import static com.wxm.camerajob.base.utility.FileLogger.getLogger;

/**
 * 使用camera2 api
 * Created by 123 on 2016/7/4.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SilentCameraNew extends SilentCamera {
    private final static String TAG = "SilentCameraNew";
    private final static int   MSG_CAPTURE_TIMEOUT = 1;

    private ImageReader     mImageReader;

    private String                  mCameraId;
    private CameraDevice            mCameraDevice = null;
    private CameraCaptureSession    mCaptureSession = null;
    private CaptureRequest.Builder  mCaptureBuilder = null;
    private CameraManager           mCMCameramanager;

    private CameraMsgHandlerNew    mMHHandler;


    public SilentCameraNew()    {
        super();
        mMHHandler = new CameraMsgHandlerNew(this);
    }

    private boolean setupCamera() {
        mCMCameramanager = (CameraManager) ContextUtil.getInstance()
                .getSystemService(Context.CAMERA_SERVICE);

        mCameraId = "";
        boolean b_lock = false;
        try {
            if (!mCameraLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            b_lock = true;
            for (String cameraId : mCMCameramanager.getCameraIdList()) {
                CameraCharacteristics cc
                        = mCMCameramanager.getCameraCharacteristics(cameraId);

                Integer facing = cc.get(CameraCharacteristics.LENS_FACING);
                if ((null != facing) && (mCParam.mFace != facing))
                    continue;

                StreamConfigurationMap map = cc.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null)
                    continue;

                Integer or = cc.get(CameraCharacteristics.SENSOR_ORIENTATION);
                mSensorOrientation = null != or ?  or : 90;

                // Check if the flash is supported.
                Boolean available = cc.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                mCameraId = cameraId;
                break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            getLogger().severe(UtilFun.ThrowableToString(e));
            return false;
        } catch (NullPointerException | CameraAccessException e) {
            e.printStackTrace();
            getLogger().severe(UtilFun.ThrowableToString(e));
            return false;
        } finally {
            if(b_lock)
                mCameraLock.release();
        }

        return !mCameraId.equals("");
    }

    @Override
    public void openCamera() {
        if(!setupCamera())  {
            Log.w(TAG, "setup camera failure");
            openCameraCallBack(false);
            return;
        }

        if (ContextCompat.checkSelfPermission(ContextUtil.getInstance(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "need camera permission");
            openCameraCallBack(false);
            return;
        }

        try {
            mCMCameramanager.openCamera(mCameraId,
                    mCameraDeviceStateCallback, mCParam.mSessionHandler);
        } catch (Exception e){
            openCameraCallBack(false);

            e.printStackTrace();
            getLogger().severe(UtilFun.ThrowableToString(e));
        }
    }

    @Override
    void capturePhoto() {
        Log.i(TAG, "start capture");
        mCameraStatus = CAMERA_TAKEPHOTO_START;
        mStartMSec = System.currentTimeMillis();

        boolean b_ok = true;
        try {
            mImageReader = ImageReader.newInstance(
                    mCParam.mPhotoSize.getWidth(), mCParam.mPhotoSize.getHeight(),
                    ImageFormat.JPEG, 2);
            mCameraDevice.createCaptureSession(
                    Collections.singletonList(mImageReader.getSurface()),
                    mSessionStateCallback, null);
        } catch (CameraAccessException e) {
            b_ok = false;
            e.printStackTrace();

            takePhotoCallBack(false);
        }

        if(b_ok)
            mMHHandler.sendEmptyMessageDelayed(MSG_CAPTURE_TIMEOUT, 5000);
    }

    @Override
    public void closeCamera() {
        boolean b_lock = false;
        try {
            if (!mCameraLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            b_lock = true;
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
            mCameraStatus = CAMERA_NOT_OPEN;
        } catch (InterruptedException e) {
            getLogger().severe(UtilFun.ThrowableToString(e));
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            if(b_lock)
                mCameraLock.release();
        }
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
                    openCameraCallBack(true);
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    Log.i(TAG, "onDisconnected");
                    getLogger().info("camerdevice disconnected");

                    mCameraDevice = null;
                    openCameraCallBack(false);
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.i(TAG, "onError, error = " + error);
                    getLogger().info("onError, error = " + error);

                    mCameraDevice = null;
                    openCameraCallBack(false);
                }
            };

    private CameraCaptureSession.StateCallback mSessionStateCallback =
            new CameraCaptureSession.StateCallback() {
                private final static String TAG = "Capture.StateCallback";



                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    Log.i(TAG, "onConfigured");
                    mCaptureSession = session;

                    // Auto focus should be continuous for camera preview.
                    try {
                        mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                        mCaptureBuilder.addTarget(mImageReader.getSurface());
                        mCaptureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                        // Use the same AE and AF modes as the preview.
                        if (mFlashSupported)
                            mCaptureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                        mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation());
                        mImageReader.setOnImageAvailableListener(
                                new ImageReader.OnImageAvailableListener() {
                                    @Override
                                    public void onImageAvailable(ImageReader reader) {
                                        Log.i(TAG, "image already");
                                        //savePhoto(reader.acquireLatestImage());
                                    }
                                }, mCParam.mSessionHandler);

                        mCaptureSession.capture(mCaptureBuilder.build(), mCaptureCallback, null);
                    } catch (CameraAccessException e) {
                        takePhotoCallBack(false);
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e(TAG, "onConfigureFailed, session : " + session.toString());
                    mCameraStatus = CAMERA_NOT_OPEN;

                    takePhotoCallBack(false);
                }
            };


    private CameraCaptureSession.CaptureCallback  mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

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

            Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            bm = ImageUtil.rotateBitmap(bm, getOrientation(), null);
            boolean ret = saveBitmapToJPGFile(bm, mTPParam.mPhotoFileDir, mTPParam.mFileName);

            mCameraStatus = ret ? CAMERA_TAKEPHOTO_SUCCESS : CAMERA_TAKEPHOTO_FAILURE;
            takePhotoCallBack(ret);
            return ret;
        }

        private void process(CaptureResult result) {
            // CONTROL_AE_STATE can be null on some devices
            Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
            if (aeState == null
                    || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
                    || aeState == CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED
                    || aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                //captureStillPicture();
                // This is the CaptureRequest.Builder that we use to take a picture.
                mMHHandler.removeMessages(MSG_CAPTURE_TIMEOUT);
                savePhoto(mImageReader.acquireLatestImage());
            } else  {
                try {
                    mCaptureSession.capture(mCaptureBuilder.build(), mCaptureCallback, null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                    takePhotoCallBack(false);
                }
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

            mCameraStatus = CAMERA_TAKEPHOTO_FAILURE;
            takePhotoCallBack(false);
        }
    };

    /**
     * activity msg handler
     * Created by wxm on 2016/8/13.
     */
    private static class CameraMsgHandlerNew extends Handler {
        private static final String TAG = "CameraMsgHandlerNew";
        private WeakReference<SilentCameraNew> mWRHandler;


        CameraMsgHandlerNew(SilentCameraNew h) {
            super();
            mWRHandler = new WeakReference<>(h);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CAPTURE_TIMEOUT : {
                    SilentCameraNew h = mWRHandler.get();
                    if(null != h) {
                        if(h.mCameraStatus.equals(CAMERA_TAKEPHOTO_START)) {
                            String l = "wait capture timeout";
                            Log.e(TAG, l);

                            FileLogger.getLogger().severe(l);
                            h.takePhotoCallBack(false);
                        }
                    }
                }
                break;

                default:
                    Log.e(TAG, String.format("msg(%s) can not process", msg.toString()));
                    break;
            }
        }
    }
}
