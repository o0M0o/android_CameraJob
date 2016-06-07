package com.wxm.camerajob.ui.fragment;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.wxm.camerajob.R;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * camera2预览fragment
 * Created by 123 on 2016/6/7.
 */
public class CameraFragment extends Fragment implements TextureView.SurfaceTextureListener {

    private TextureView             mPreviewView;
    private Handler                 mHandler;
    private HandlerThread           mThreadHandler;
    private CaptureRequest.Builder  mPreviewBuilder;
    private CameraDevice            mCurCamer = null;
    private CameraCaptureSession    mCameraSession = null;
    private ImageReader             imageReader = null;

    private boolean                 mSurfaceAvliable = false;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static
    {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }


    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            try {
                startPreview(camera);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
        }

        @Override
        public void onError(CameraDevice camera, int error) {
        }
    };

    private CameraCaptureSession.StateCallback mSessionStateCallback =
            new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        updatePreview(session);
                        mCameraSession = session;
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            };

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    /**
     * 激活前置相机
     * @return 如果成功，返回true
     */
    public boolean ActiveFrontCamera() {
        boolean ret = false;
        if(mSurfaceAvliable)    {
            try {
                if(null != mCurCamer)
                    mCurCamer.close();

                //获得CameraManager
                CameraManager cameraManager =
                        (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
                //打开相机
                cameraManager.openCamera("0", mCameraDeviceStateCallback, mHandler);
                ret = true;
            }
            catch (CameraAccessException | SecurityException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * 激活后置相机
     * @return  如果成功，返回true
     */
    public boolean ActiveBackCamera() {
        boolean ret = false;
        if(mSurfaceAvliable)    {
            try {
                if(null != mCurCamer)
                    mCurCamer.close();

                //获得CameraManager
                CameraManager cameraManager =
                        (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
                //打开相机
                cameraManager.openCamera("1", mCameraDeviceStateCallback, mHandler);
                ret = true;
            }
            catch (CameraAccessException | SecurityException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * 关闭相机
     */
    public void CloseCamera()   {
        if(mSurfaceAvliable)    {
            if(null != mCurCamer)
                mCurCamer.close();
        }
    }

    public void TakePhoto() {
        if(!mSurfaceAvliable)
            return;

        if((null == mCurCamer) || (null == mCameraSession))
            return;


        if(null == imageReader)
            setImageReader();


        try
        {
            // 创建作为拍照的CaptureRequest.Builder
            final CaptureRequest.Builder captureRequestBuilder = mCurCamer
                    .createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            // 将imageReader的surface作为CaptureRequest.Builder的目标
            captureRequestBuilder.addTarget(imageReader.getSurface());
            // 设置自动对焦模式
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 设置自动曝光模式
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // 获取设备方向
            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            // 根据设备方向计算设置照片的方向
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION,
                    ORIENTATIONS.get(rotation));
            // 停止连续取景
            mCameraSession.stopRepeating();
            // 捕获静态图像
            mCameraSession.capture(captureRequestBuilder.build()
                    , new CameraCaptureSession.CaptureCallback()
                    {
                        // 拍照完成时激发该方法
                        @Override
                        public void onCaptureCompleted(CameraCaptureSession session
                                , CaptureRequest request, TotalCaptureResult result)
                        {
                            try
                            {
                                // 重设自动对焦模式
                                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                                        CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
                                // 设置自动曝光模式
                                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                                // 打开连续取景模式
                                mCameraSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
                            }
                            catch (CameraAccessException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }, null);
        }
        catch (CameraAccessException e)
        {
            e.printStackTrace();
        }
    }

    // 为Size定义一个比较器Comparator
    static class CompareSizesByArea implements Comparator<Size>
    {
        @Override
        public int compare(Size lhs, Size rhs)
        {
            // 强转为long保证不会发生溢出
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    private void setImageReader()   {
        if(null == mCurCamer)
            return;

        CameraManager manager =
                (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);

        try {
            // 获取指定摄像头的特性
            CameraCharacteristics characteristics
                    = manager.getCameraCharacteristics(mCurCamer.getId());
            // 获取摄像头支持的配置属性
            StreamConfigurationMap map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            // 获取摄像头支持的最大尺寸
            Size largest = Collections.max(
                    Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                    new CompareSizesByArea());

            // 创建一个ImageReader对象，用于获取摄像头的图像数据
            imageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                    ImageFormat.JPEG, 2);
            imageReader.setOnImageAvailableListener(
                    new ImageReader.OnImageAvailableListener() {
                        // 当照片数据可用时激发该方法
                        @Override
                        public void onImageAvailable(ImageReader reader) {
                            // 获取捕获的照片数据
                            Image image = reader.acquireNextImage();
                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            byte[] bytes = new byte[buffer.remaining()];
                            // 使用IO流将照片写入指定文件
                            File file = new File(getContext().getExternalFilesDir(null), "pic.jpg");
                            buffer.get(bytes);
                            try (
                                    FileOutputStream output = new FileOutputStream(file)) {
                                output.write(bytes);
                                Toast.makeText(getActivity(), "保存: "
                                        + file, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                image.close();
                            }
                        }
                    }, null);
        }
        catch(CameraAccessException e) {
            e.printStackTrace();
        }
    }


    @SuppressWarnings("ResourceType")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_camera, null);
        initLooper();
        initUIAndListener(v);
        return v;
    }

    //很多过程都变成了异步的了，所以这里需要一个子线程的looper
    private void initLooper() {
        mThreadHandler = new HandlerThread("CAMERA2");
        mThreadHandler.start();
        mHandler = new Handler(mThreadHandler.getLooper());
    }

    //可以通过TextureView或者SurfaceView
    private void initUIAndListener(View v) {
        mPreviewView = (TextureView) v.findViewById(R.id.frag_camera_textureview);
        mPreviewView.setSurfaceTextureListener(this);
    }

    @SuppressWarnings("ResourceType")
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
/*        try {
            //获得CameraManager
            CameraManager cameraManager =
                    (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            //打开相机
            cameraManager.openCamera("0", mCameraDeviceStateCallback, mHandler);
            mSurfaceAvliable = true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }*/

        mSurfaceAvliable = true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mSurfaceAvliable = false;
        return false;
    }

    //TextureView.SurfaceTextureListener
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }


    //开始预览，主要是camera.createCaptureSession这段代码很重要，创建会话
    private void startPreview(CameraDevice camera) throws CameraAccessException {
        SurfaceTexture texture = mPreviewView.getSurfaceTexture();

        CameraManager cameraManager =
                (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics =
                cameraManager.getCameraCharacteristics(camera.getId());
        //支持的STREAM CONFIGURATION
        StreamConfigurationMap map =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        //显示的size
        Size previewSize = map.getOutputSizes(SurfaceTexture.class)[0];


        texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        Surface surface = new Surface(texture);
        try {
            mPreviewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mPreviewBuilder.addTarget(surface);
        camera.createCaptureSession(Arrays.asList(surface), mSessionStateCallback, mHandler);

        mCurCamer = camera;
    }



    private void updatePreview(CameraCaptureSession session) throws CameraAccessException {
        session.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
    }
}
