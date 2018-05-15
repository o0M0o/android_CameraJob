package com.wxm.camerajob.hardware

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureFailure
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Display
import android.view.WindowManager

import com.wxm.camerajob.utility.ContextUtil
import com.wxm.camerajob.utility.FileLogger

import java.nio.ByteBuffer
import java.util.Collections
import java.util.HashMap

import wxm.androidutil.util.UtilFun
import wxm.androidutil.util.ImageUtil


/**
 * silent camera use camera2 api
 * Created by WangXM on 2016/7/4.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class SilentCameraNew internal constructor() : SilentCamera() {

    private var mImageReader: ImageReader? = null

    private var mCameraId: String? = null
    private var mCameraDevice: CameraDevice? = null
    private var mCaptureSession: CameraCaptureSession? = null
    private var mCaptureBuilder: CaptureRequest.Builder? = null


    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private// Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
    // We have to take that into account and rotate JPEG properly.
    // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
    // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
    val orientation: Int
        get() {
            val wm = ContextUtil.instance
                    .getSystemService(Context.WINDOW_SERVICE) as WindowManager ?: return -1

            val dp = wm.defaultDisplay
            val rotation = dp.rotation
            val ret = (SilentCamera.ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360
            Log.d(TAG, "Orientation : display = " + rotation
                    + ", sensor = " + mSensorOrientation + ", ret = " + ret)
            return ret
        }


    private val mCameraDeviceStateCallback = object : CameraDevice.StateCallback() {
        private val TAG = "DeviceSCB"

        override fun onOpened(camera: CameraDevice) {
            Log.i(TAG, "onOpened")
            FileLogger.logger.info("get camerdevice")

            mCameraDevice = camera
            openCameraCallBack(true)
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.i(TAG, "onDisconnected")
            FileLogger.logger.info("camerdevice disconnected")

            mCameraDevice = null
            openCameraCallBack(false)
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.i(TAG, "onError, error = $error")
            FileLogger.logger.info("onError, error = $error")

            mCameraDevice = null
            openCameraCallBack(false)
        }
    }

    private val mSessionStateCallback = object : CameraCaptureSession.StateCallback() {
        private val TAG = "Capture.StateCallback"

        override fun onConfigured(session: CameraCaptureSession) {
            Log.i(TAG, "onConfigured")
            mCaptureSession = session

            // Auto focus should be continuous for camera preview.
            try {
                mCaptureBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                mCaptureBuilder!!.addTarget(mImageReader!!.surface)
                mCaptureBuilder!!.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_AUTO)

                // Use the same AE and AF modes as the preview.
                mCaptureBuilder!!.set(CaptureRequest.CONTROL_AE_MODE,
                        if (mFlashSupported)
                            CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                        else
                            CaptureRequest.CONTROL_AE_MODE_ON)

                mCaptureBuilder!!.set(CaptureRequest.JPEG_ORIENTATION, orientation)
                mImageReader!!.setOnImageAvailableListener(
                        { reader ->
                            Log.i(TAG, "image already")
                            //savePhoto(reader.acquireLatestImage());
                        }, mCParam.mSessionHandler)

                mCaptureSession!!.capture(mCaptureBuilder!!.build(), mCaptureCallback, null)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
                takePhotoCallBack(false)
            }

        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            Log.e(TAG, "onConfigureFailed, session : " + session.toString())

            takePhotoCallBack(false)
        }
    }


    private val mCaptureCallback = object : CameraCaptureSession.CaptureCallback() {
        private val TAG = "Capture.CaptureCallback"
        private val MAX_WAIT_TIMES = 5
        private var mWaitCount = 0

        private val PARTIAL_TAG = 1
        private val COMPELED_TAG = 2

        /**
         * if use mCaptureSession.capture, then not need check AE_STATE
         * @param result    para
         * @param tag       for log use
         */
        private fun process(result: CaptureResult, tag: Int) {
            mWaitCount++
            if (MAX_WAIT_TIMES < mWaitCount) {
                Log.e(TAG, "wait too many times")
                val ig = mImageReader!!.acquireLatestImage()
                if (null != ig) {
                    savePhoto(ig)
                } else {
                    takePhotoCallBack(false)
                }
            } else {
                var r_c = true
                val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                Log.i(TAG, "tag = " + tag + ", ae = "
                        + (aeState?.toString() ?: "null")
                        + ", waitcount = " + mWaitCount)
                if (aeState == null
                        || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
                        || aeState == CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED
                        || aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                    val ig = mImageReader!!.acquireLatestImage()
                    if (null != ig) {
                        Log.i(TAG, "image ok")
                        r_c = false

                        savePhoto(ig)
                    }
                }

                if (r_c) {
                    Log.i(TAG, "wait image ok")
                    try {
                        Thread.sleep(250)
                        mCaptureSession!!.capture(mCaptureBuilder!!.build(), this, null)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                        takePhotoCallBack(false)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                        takePhotoCallBack(false)
                    }

                }
            }
        }

        override fun onCaptureProgressed(session: CameraCaptureSession,
                                         request: CaptureRequest,
                                         partialResult: CaptureResult) {
            process(partialResult, PARTIAL_TAG)
        }

        override fun onCaptureCompleted(session: CameraCaptureSession,
                                        request: CaptureRequest,
                                        result: TotalCaptureResult) {
            process(result, COMPELED_TAG)
        }

        @SuppressLint("WrongConstant")
        override fun onCaptureFailed(session: CameraCaptureSession,
                                     request: CaptureRequest,
                                     failure: CaptureFailure) {
            super.onCaptureFailed(session, request, failure)
            Log.d(TAG, "CaptureFailed, reason = " + failure.reason)
            FileLogger.logger.warning(
                    "CaptureFailed, reason = " + failure.reason)

            mCameraStatus = ECameraStatus.TAKE_PHOTO_FAILURE
            takePhotoCallBack(false)
        }
    }

    /**
     * setup camera before use it
     * use camera face to find it's camera id
     * @return      true if find
     */
    private fun setupCamera(): Boolean {
        mCameraId = ""
        for (cameraId in mHMCameraHardware.keys) {
            val ch = mHMCameraHardware[cameraId]
            if (mCParam.mFace != ch.mFace)
                continue

            mSensorOrientation = ch.mSensorOrientation
            mFlashSupported = ch.mFlashSupported
            mCameraId = cameraId
            break
        }

        return mCameraId != ""
    }

    public override fun openCamera() {
        if (!setupCamera()) {
            Log.w(TAG, "setup camera failure")
            openCameraCallBack(false)
            return
        }

        if (ContextCompat.checkSelfPermission(ContextUtil.instance, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "need camera permission")
            openCameraCallBack(false)
            return
        }

        try {
            val cm = ContextUtil.instance.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            cm?.openCamera(mCameraId!!,
                    mCameraDeviceStateCallback, mCParam.mSessionHandler)
        } catch (e: Exception) {
            openCameraCallBack(false)

            e.printStackTrace()
            FileLogger.logger.severe(UtilFun.ThrowableToString(e))
        }

    }

    internal override fun capturePhoto() {
        Log.i(TAG, "start capture")
        mCameraStatus = ECameraStatus.TAKE_PHOTO_START

        try {
            mImageReader = ImageReader.newInstance(
                    mCParam.mPhotoSize.width, mCParam.mPhotoSize.height,
                    ImageFormat.JPEG, 2)
            mCameraDevice!!.createCaptureSession(
                    listOf<Surface>(mImageReader!!.surface),
                    mSessionStateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            takePhotoCallBack(false)
        }

    }

    public override fun closeCamera() {
        mCaptureBuilder = null
        if (null != mCaptureSession) {
            mCaptureSession!!.close()
            mCaptureSession = null
        }
        if (null != mCameraDevice) {
            mCameraDevice!!.close()
            mCameraDevice = null
        }

        if (null != mImageReader) {
            mImageReader!!.close()
            mImageReader = null
        }

        val l = "camera closed, paratag = " + if (mTPParam == null || mTPParam!!.mTag == null) "null" else mTPParam!!.mTag
        Log.i(TAG, l)
        FileLogger.logger.info(l)
        mCameraStatus = ECameraStatus.NOT_OPEN
    }

    /**
     * save photo
     */
    private fun savePhoto(ig: Image) {
        val buffer = ig.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        ig.close()

        var bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        bm = ImageUtil.rotateBitmap(bm, orientation, null)
        val ret = saveBitmapToJPGFile(bm, mTPParam!!.mPhotoFileDir, mTPParam!!.mFileName)

        mCameraStatus = if (ret) ECameraStatus.TAKE_PHOTO_SUCCESS else ECameraStatus.TAKE_PHOTO_FAILURE
        takePhotoCallBack(ret)
    }

    companion object {
        private val TAG = "SilentCameraNew"

        /**
         * get camera hardware setting
         */
        private val mHMCameraHardware = HashMap<String, CameraHardWare>()

        init {
            val camera_manager = ContextUtil.instance
                    .getSystemService(Context.CAMERA_SERVICE) as CameraManager
            if (null != camera_manager) {
                try {
                    for (camera_id in camera_manager.cameraIdList) {
                        val cc = camera_manager.getCameraCharacteristics(camera_id)

                        val ch = CameraHardWare()
                        val facing = cc.get(CameraCharacteristics.LENS_FACING)
                        if (null != facing) {
                            ch.mFace = facing

                            val or = cc.get(CameraCharacteristics.SENSOR_ORIENTATION)
                            ch.mSensorOrientation = or ?: 90

                            // Check if the flash is supported.
                            val available = cc.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                            ch.mFlashSupported = available ?: false
                        }

                        mHMCameraHardware[camera_id] = ch
                    }
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }

            }
        }
    }
}

/**
 * hardware for camera
 */
internal class CameraHardWare {
    var mSensorOrientation: Int = 0
    var mFace: Int = 0
    var mFlashSupported: Boolean = false
}
