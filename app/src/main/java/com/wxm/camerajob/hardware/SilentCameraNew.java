package com.wxm.camerajob.hardware;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.wxm.camerajob.utility.ContextUtil;
import com.wxm.camerajob.utility.FileLogger;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import wxm.androidutil.util.UtilFun;
import wxm.androidutil.util.ImageUtil;

import static com.wxm.camerajob.utility.FileLogger.getLogger;

/**
 * silent camera use camera2 api
 * Created by 123 on 2016/7/4.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SilentCameraNew extends SilentCamera {
    private final static String TAG = "SilentCameraNew";

    private ImageReader mImageReader;

    private String mCameraId;
    private CameraDevice mCameraDevice = null;
    private CameraCaptureSession mCaptureSession = null;
    private CaptureRequest.Builder mCaptureBuilder = null;

    /**
     * get camera hardware setting
     */
    private final static HashMap<String, CameraHardWare> mHMCameraHardware = new HashMap<>();
    static {
        CameraManager camera_manager = (CameraManager) ContextUtil.getInstance()
                .getSystemService(Context.CAMERA_SERVICE);
        if(null != camera_manager) {
            try {
                for (String camera_id : camera_manager.getCameraIdList()) {
                    CameraCharacteristics cc
                            = camera_manager.getCameraCharacteristics(camera_id);

                    CameraHardWare ch = new CameraHardWare();
                    Integer facing = cc.get(CameraCharacteristics.LENS_FACING);
                    if (null != facing) {
                        ch.mFace = facing;

                        Integer or = cc.get(CameraCharacteristics.SENSOR_ORIENTATION);
                        ch.mSensorOrientation = null != or ? or : 90;

                        // Check if the flash is supported.
                        Boolean available = cc.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                        ch.mFlashSupported = available == null ? false : available;
                    }

                    mHMCameraHardware.put(camera_id, ch);
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }


    SilentCameraNew() {
        super();
    }

    /**
     * setup camera before use it
     * use camera face to find it's camera id
     * @return      true if find
     */
    private boolean setupCamera() {
        mCameraId = "";
        for (String cameraId : mHMCameraHardware.keySet()) {
            CameraHardWare ch = mHMCameraHardware.get(cameraId);
            if (mCParam.mFace != ch.mFace)
                continue;

            mSensorOrientation = ch.mSensorOrientation;
            mFlashSupported = ch.mFlashSupported;
            mCameraId = cameraId;
            break;
        }

        return !mCameraId.equals("");
    }

    @Override
    public void openCamera() {
        if (!setupCamera()) {
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
            CameraManager cm = (CameraManager) ContextUtil.getInstance().getSystemService(Context.CAMERA_SERVICE);
            if(null != cm) {
                cm.openCamera(mCameraId,
                        mCameraDeviceStateCallback, mCParam.mSessionHandler);
            }
        } catch (Exception e) {
            openCameraCallBack(false);

            e.printStackTrace();
            getLogger().severe(UtilFun.ThrowableToString(e));
        }
    }

    @Override
    void capturePhoto() {
        Log.i(TAG, "start capture");
        mCameraStatus = ECameraStatus.TAKE_PHOTO_START;

        try {
            mImageReader = ImageReader.newInstance(
                    mCParam.mPhotoSize.getWidth(), mCParam.mPhotoSize.getHeight(),
                    ImageFormat.JPEG, 2);
            mCameraDevice.createCaptureSession(
                    Collections.singletonList(mImageReader.getSurface()),
                    mSessionStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            takePhotoCallBack(false);
        }
    }

    @Override
    public void closeCamera() {
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
        mCameraStatus = ECameraStatus.NOT_OPEN;
    }


    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private int getOrientation() {
        WindowManager wm = (WindowManager)ContextUtil.getInstance()
                                .getSystemService(Context.WINDOW_SERVICE);
        if(null == wm)
            return -1;

        Display dp = wm.getDefaultDisplay();
        int rotation = dp.getRotation();

        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        int ret = (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
        Log.d(TAG, "Orientation : display = " + rotation
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

    /**
     * save photo
     */
    private void savePhoto(Image ig) {
        ByteBuffer buffer = ig.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        ig.close();

        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        bm = ImageUtil.rotateBitmap(bm, getOrientation(), null);
        boolean ret = saveBitmapToJPGFile(bm, mTPParam.mPhotoFileDir, mTPParam.mFileName);

        mCameraStatus = ret ? ECameraStatus.TAKE_PHOTO_SUCCESS : ECameraStatus.TAKE_PHOTO_FAILURE;
        takePhotoCallBack(ret);
    }

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
                                CaptureRequest.CONTROL_AF_MODE_AUTO);

                        // Use the same AE and AF modes as the preview.
                        mCaptureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                mFlashSupported ?
                                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                                        : CaptureRequest.CONTROL_AE_MODE_ON);

                        mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation());
                        mImageReader.setOnImageAvailableListener(
                                reader -> {
                                    Log.i(TAG, "image already");
                                    //savePhoto(reader.acquireLatestImage());
                                }, mCParam.mSessionHandler);

                        mCaptureSession.capture(mCaptureBuilder.build(), mCaptureCallback, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                        takePhotoCallBack(false);
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e(TAG, "onConfigureFailed, session : " + session.toString());

                    takePhotoCallBack(false);
                }
            };


    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {
        private final static String TAG = "Capture.CaptureCallback";
        private final static int MAX_WAIT_TIMES = 5;
        private int mWaitCount = 0;

        private final static int PARTIAL_TAG = 1;
        private final static int COMPELED_TAG = 2;

        /**
         * if use mCaptureSession.capture, then not need check AE_STATE
         * @param result    para
         * @param tag       for log use
         */
        private void process(CaptureResult result, int tag) {
            mWaitCount++;
            if (MAX_WAIT_TIMES < mWaitCount) {
                Log.e(TAG, "wait too many times");
                Image ig = mImageReader.acquireLatestImage();
                if (null != ig) {
                    savePhoto(ig);
                } else {
                    takePhotoCallBack(false);
                }
            } else {
                boolean r_c = true;
                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                Log.i(TAG, "tag = " + tag + ", ae = "
                        + (aeState == null ? "null" : String.valueOf(aeState))
                        + ", waitcount = " + mWaitCount);
                if (aeState == null
                        || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
                        || aeState == CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED
                        || aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                    Image ig = mImageReader.acquireLatestImage();
                    if (null != ig) {
                        Log.i(TAG, "image ok");
                        r_c = false;

                        savePhoto(ig);
                    }
                }

                if (r_c) {
                    Log.i(TAG, "wait image ok");
                    try {
                        Thread.sleep(250);
                        mCaptureSession.capture(mCaptureBuilder.build(), mCaptureCallback, null);
                    } catch (CameraAccessException | InterruptedException e) {
                        e.printStackTrace();
                        takePhotoCallBack(false);
                    }
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult, PARTIAL_TAG);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result, COMPELED_TAG);
        }

        @SuppressLint("WrongConstant")
        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                    @NonNull CaptureRequest request,
                                    @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            Log.d(TAG, "CaptureFailed, reason = " + failure.getReason());
            FileLogger.getLogger().warning(
                    "CaptureFailed, reason = " + failure.getReason());

            mCameraStatus = ECameraStatus.TAKE_PHOTO_FAILURE;
            takePhotoCallBack(false);
        }
    };
}

/**
 * hardware for camera
 */
class CameraHardWare {
    int mSensorOrientation;
    int mFace;
    boolean mFlashSupported;
}
