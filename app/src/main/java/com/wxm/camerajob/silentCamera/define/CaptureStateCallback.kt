package com.wxm.camerajob.silentCamera.define

import android.hardware.camera2.*
import android.media.ImageReader
import com.wxm.camerajob.silentCamera.SilentCamera
import com.wxm.camerajob.App
import wxm.androidutil.log.TagLog

/**
 * @author      WangXM
 * @version     create：2018/5/16
 */
internal class CaptureStateCallback constructor(private val mHome: SilentCamera,
                                                private val mReader: ImageReader)
    : CameraCaptureSession.StateCallback() {
    private var mCaptureBuilder: CaptureRequest.Builder? = null

    override fun onConfigured(session: CameraCaptureSession) {
        TagLog.i("onConfigured")
        mHome.mCaptureSession = session

        // Auto focus should be continuous for camera preview.
        try {
            mCaptureBuilder = mHome.mCameraDevice!!
                    .createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG)
            setUpCaptureBuilder(mCaptureBuilder!!)
            doCapture(null)
        } catch (e: CameraAccessException) {
            TagLog.e("onConfigured", e)
            mHome.takePhotoCallBack(false)
        }
    }

    fun doCapture(callBack: CaptureCallback? = null) {
        try {
            val cb = callBack ?: CaptureCallback(mHome, mReader, this)
            mHome.mCaptureSession!!.capture(mCaptureBuilder!!.build(), cb, null)
        } catch (e: Throwable) {
            TagLog.e("doCapture failure", e)
            mHome.takePhotoCallBack(false)
        }
    }

    override fun onConfigureFailed(session: CameraCaptureSession) {
        TagLog.e("onConfigureFailed, session : $session")
        mHome.takePhotoCallBack(false)
    }

    private fun setUpCaptureBuilder(builder: CaptureRequest.Builder) {
        builder.addTarget(mReader.surface)
        builder.set(CaptureRequest.JPEG_ORIENTATION,
                getJPGOrientation(mHome.mCamera!!.mFace == CameraCharacteristics.LENS_FACING_FRONT,
                        mHome.mCamera!!.mSensorOrientation,
                        App.getWindowManager()!!.defaultDisplay.rotation))

        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

        //设置连续帧
        //设置每秒30帧
        mHome.mCamera!!.mCharacteristics
            .get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
            ?.let {
                builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, it[it.size - 1])
            }

        builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
        builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
        builder.set(CaptureRequest.CONTROL_AE_MODE,
                if (mHome.mCamera!!.mFlashSupported) CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                else CaptureRequest.CONTROL_AE_MODE_ON)
    }

    private fun getJPGOrientation(facingFront: Boolean, sensorOrientation: Int, deviceOrientation: Int): Int {
        if (deviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN)
            return 0

        // Round device orientation to a multiple of 90
        var dOrientation = (deviceOrientation + 45) / 90 * 90

        // Reverse device orientation for front-facing cameras
        if (facingFront) dOrientation = -dOrientation

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        return (sensorOrientation + dOrientation + 360) % 360
    }
}