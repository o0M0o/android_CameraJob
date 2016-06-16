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
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.data.TakePhotoParam;
import com.wxm.camerajob.base.handler.GlobalContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 静默相机
 * Created by 123 on 2016/6/16.
 */
public class SilentCamera {
    private final static String TAG = "SilentCamera";
    private final static int  CAPTURE_NOT_START = 0;
    private final static int  CAPTURE_FINISHED = 1;
    private final static int  CAPTURE_FAILED = 2;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private Handler mBackgroundHandler;

    private String mCameraId;
    private File mFile;
    private ImageReader mImageReader;
    private boolean mFlashSupported;

    private CameraDevice mCameraDevice = null;
    private CameraCaptureSession mCaptureSession = null;
    private int mCaptureFinishFlag = CAPTURE_NOT_START;

    private TakePhotoParam  mTPParam;

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
                    Log.i(TAG, "onError");

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
                        return;
                    }

                    mCaptureSession = session;
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.e(TAG, "onConfigureFailed, session : "
                            + (null != session ? session.toString() : "null"));
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

                Log.i(TAG, "save photo : " + mFile.toString());
                Message m = Message.obtain(GlobalContext.getInstance().mMsgHandler,
                        GlobalDef.MSGWHAT_CAMERAJOB_TAKEPHOTO);
                m.obj = new Object[] {Integer.parseInt(mTPParam.mTag), 1};
                m.sendToTarget();

                // give up others
                while(null != reader.acquireNextImage())    {
                }

                mCaptureFinishFlag = CAPTURE_FINISHED;
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

    public SilentCamera()   {
    }


    /**
     * 执行拍照
     * @param waitMillisecond       等待毫秒数
     * @return 成功返回true, 否则返回false
     */
    public boolean TakePhoto(TakePhotoParam param, long waitMillisecond)  {
        Log.i(TAG, "TakePhoto");

        boolean ret = false;
        mTPParam = param;
        long endmesc = System.currentTimeMillis() + waitMillisecond;
        if(captureStillPicture(endmesc))    {
            // 确认拍照状态
            while((CAPTURE_NOT_START == mCaptureFinishFlag)
                    && (System.currentTimeMillis() < endmesc))  {
                try {
                    Log.i(TAG, "wait captureresult 100 ms");
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(CAPTURE_FINISHED == mCaptureFinishFlag)
                ret = true;
        }

        //closeCamera();
        return ret;
    }


    /**
     * Capture a still picture
     * @param endmesc      超时时间
     * @return 成功返回true, 否则返回false
     */
   private boolean captureStillPicture(long endmesc) {
        setupFile();

        // 确认相机状态
        while((null == mCaptureSession) && (System.currentTimeMillis() < endmesc))  {
            try {
                //Log.i(TAG, "wait 20 ms");
                Thread.sleep(200);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(null == mCaptureSession) {
            Log.e(TAG, "in captureStillPicture, mCaptureSession is null");
            return false;
        }

        try {
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
            /*
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));
            */
            mCaptureSession.capture(
                    captureBuilder.build(),
                    new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                                       @NonNull CaptureRequest request,
                                                       @NonNull TotalCaptureResult result) {
                            Log.d(TAG, "onCaptureCompleted");
                            mCaptureFinishFlag = CAPTURE_FINISHED;
                        }

                        public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                                    @NonNull CaptureRequest request,
                                                    @NonNull CaptureFailure failure) {
                            Log.d(TAG, "onCaptureFailed");
                            mCaptureFinishFlag = CAPTURE_FAILED;
                        }
                    },
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * 关闭相机
     */
    public void closeCamera()   {
        Log.i(TAG, "closeCamera start");
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

            Log.i(TAG, "closeCamera end");
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * 打开相机
     * @param param                 拍照参数
     * @param sessionHandler        动作handler
     */
    public void openCamera(TakePhotoParam param,
                            Handler sessionHandler) {
        Log.i(TAG, "openCamera");
        if (ContextCompat.checkSelfPermission(ContextUtil.getInstance(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            //requestCameraPermission();
            Log.i(TAG, "need camera permission");
            return;
        }

        mTPParam = param;
        //mBackgroundHandler = sessionHandler;
        mBackgroundHandler = null;

        setUpCameraOutputs(param.mFace, param.mPhotoSize.getWidth(), param.mPhotoSize.getHeight());
        CameraManager manager =
                (CameraManager) ContextUtil.getInstance().getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            manager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
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
        CameraManager manager =
                (CameraManager) ContextUtil.getInstance().getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if((null != facing) && (face != facing))
                    continue;

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                /*
                // For still image captures, we use the largest available size.
                mLargestSize = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                        */
                mImageReader = ImageReader.newInstance(
                        width, height,
                        ImageFormat.JPEG, /*maxImages*/2);

                mImageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener
                        ,mBackgroundHandler);

                // Check if the flash is supported.
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            e.printStackTrace();
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
