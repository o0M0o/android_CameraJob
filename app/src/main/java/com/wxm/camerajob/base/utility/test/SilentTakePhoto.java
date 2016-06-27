package com.wxm.camerajob.base.utility.test;

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
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.handler.GlobalContext;
import com.wxm.camerajob.base.utility.ContextUtil;

import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 后台方式获取照片
 * Created by 123 on 2016/6/13.
 */
class SilentTakePhoto {
    private static final String TAG = "SilentTakePhoto";

    private String mCameraId;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private File                    mFile;
    private ImageReader mImageReader;
    private boolean mFlashSupported;

    private CameraDevice mCameraDevice = null;
    private CameraCaptureSession mCaptureSession = null;

    private CameraJob mCurCameraJob;

    private CameraCaptureSession.StateCallback mSessionStateCallback =
            new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    // The camera is already closed
                    if (null == mCameraDevice) {
                        return;
                    }

                    mCaptureSession = session;
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e(TAG, "onConfigureFailed, session : " + session.toString());
                }
            };

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.i(TAG, "save photo : " + mFile.toString());

            Message m = Message.obtain(GlobalContext.getInstance().mMsgHandler,
                    GlobalDef.MSGWHAT_CAMERAJOB_TAKEPHOTO);
            m.obj = new Object[] {mCurCameraJob._id, 1};
            m.sendToTarget();
            /*
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
                m.obj = new Object[] {mCurCameraJob._id, 1};
                m.sendToTarget();
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
            */
        }
    };

    private CameraDevice.StateCallback mCameraDeviceStateCallback =
            new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    mCameraOpenCloseLock.release();
                    mCameraDevice = camera;
                    createCameraSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    mCameraOpenCloseLock.release();
                    camera.close();
                    mCameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    mCameraOpenCloseLock.release();
                    camera.close();
                    mCameraDevice = null;
                }
            };

// --Commented out by Inspection START (2016/6/27 23:17):
//    /**
//     * 构造函数
//     */
//    public SilentTakePhoto()    {
//        int per = ContextCompat.checkSelfPermission(ContextUtil.getInstance(),
//                                        Manifest.permission.CAMERA);
//        if (PackageManager.PERMISSION_GRANTED != per) {
//            return;
//        }
//
//        startBackgroundThread();
//    }
// --Commented out by Inspection STOP (2016/6/27 23:17)

    protected void finalize() throws Throwable {
        stopBackgroundThread();
        super.finalize();
    }


    /**
     * 打开相机
     * @param face      前或者后相机
     * @param width     图宽
     * @param height    图高
     */
    public void openCamera(int face, int width, int height) {
        Log.i(TAG, "openCamera");
        if (ContextCompat.checkSelfPermission(ContextUtil.getInstance(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            //requestCameraPermission();
            Log.i(TAG, "need camera permission");
            return;
        }

        setUpCameraOutputs(face, width, height);

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
     * 关闭相机
     */
    public void closeCamera()   {
        Log.i(TAG, "closeCamera : " + mFile.toString());
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
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Capture a still picture
     */
    public void captureStillPicture(@NonNull String fileName, CameraJob cj) {
        mCurCameraJob = cj;
        setupFile(fileName);

        // 等待2秒钟确认相机状态
        for(int i = 0; (null == mCaptureSession) && (i < 100); ++i)  {
            try {
                Log.i(TAG, "wait 20 ms");
                Thread.sleep(20);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(null == mCaptureSession) {
            Log.e(TAG, "in captureStillPicture, mCaptureSession is null");
        }

        try {
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            setAutoFlash(captureBuilder);

            // Orientation
            /*
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            mCaptureSession.stopRepeating();
            */
            mCaptureSession.capture(
                    captureBuilder.build(),
                    new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                                       @NonNull CaptureRequest request,
                                                       @NonNull TotalCaptureResult result) {
                            Log.d(TAG, "photo file path : " + mFile.toString());
                        }

                        public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                                    @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                            Log.d(TAG, "capturefailed");
                        }
                    },
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void setupFile(@NonNull String fileName)    {
        String realFileName;
        if(fileName.isEmpty())  {
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
            realFileName = fileName;
        }

        //获取SDCard状态,如果SDCard插入了手机且为非写保护状态
        String en= Environment.getExternalStorageState();
        if(en.equals(Environment.MEDIA_MOUNTED)){
            File sdcardDir =Environment.getExternalStorageDirectory();
            String path = sdcardDir.getPath()+"/CamerajobPhotos";
            File path1 = new File(path);
            if (!path1.exists()) {
                path1.mkdirs();
            }

            mFile = new File(path1, realFileName);
        }else{
            File innerPath = ContextUtil.getInstance().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            mFile = new File(innerPath, realFileName);
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
        /*
        class CompareSizesByArea implements Comparator<Size> {
            @Override
            public int compare(Size lhs, Size rhs) {
                // We cast here to ensure the multiplications won't overflow
                return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                        (long) rhs.getWidth() * rhs.getHeight());
            }
        }
        */
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
        } catch (CameraAccessException | NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            e.printStackTrace();
        }
    }




    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (mFlashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
    }


    private void createCameraSession() {
        try {
            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(
                    Collections.singletonList(mImageReader.getSurface()),
                    mSessionStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



}
