package com.wxm.camerajob.ui.fragment;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.wxm.camerajob.R;

import java.util.Arrays;

/**
 * camera2预览fragment
 * Created by 123 on 2016/6/7.
 */
public class CameraFragment extends Fragment implements TextureView.SurfaceTextureListener {

    private TextureView mPreviewView;
    private Handler mHandler;
    private HandlerThread mThreadHandler;
    private CaptureRequest.Builder mPreviewBuilder;

    private boolean mSurfaceAvliable = false;


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

    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                updatePreview(session);
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

    public boolean ActiveFrontCamera() {
        boolean ret = false;
        if(mSurfaceAvliable)    {
            try {
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

    public boolean ActiveBackCamera() {
        boolean ret = false;
        if(mSurfaceAvliable)    {
            try {
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
    }



    private void updatePreview(CameraCaptureSession session) throws CameraAccessException {
        session.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
    }
}
