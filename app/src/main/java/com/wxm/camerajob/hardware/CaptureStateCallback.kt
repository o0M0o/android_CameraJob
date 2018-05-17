package com.wxm.camerajob.hardware

import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.util.Log

/**
 * @author      WangXM
 * @version     create：2018/5/16
 */
class CaptureStateCallback constructor(private val mHome: SilentCameraNew,
                                       private val mReader: ImageReader)
    : CameraCaptureSession.StateCallback()  {

    override fun onConfigured(session: CameraCaptureSession) {
        Log.i(SilentCameraNew.LOG_TAG, "onConfigured")
        mHome.mCaptureSession = session

        // Auto focus should be continuous for camera preview.
        try {
            mHome.mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)!!.let {
                it.addTarget(mReader.surface)
                it.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)

                // Use the same AE and AF modes as the preview.
                it.set(CaptureRequest.CONTROL_AE_MODE,
                        if (mHome.mCamera!!.mFlashSupported) CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                        else CaptureRequest.CONTROL_AE_MODE_ON)
                it.set(CaptureRequest.JPEG_ORIENTATION, mHome.orientation)

                session.capture(it.build(), CaptureCallback(mHome, mReader), null)

                Unit
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            mHome.takePhotoCallBack(false)
        }
    }

    override fun onConfigureFailed(session: CameraCaptureSession) {
        Log.e(SilentCameraNew.LOG_TAG, "onConfigureFailed, session : $session")
        mHome.takePhotoCallBack(false)
    }
}