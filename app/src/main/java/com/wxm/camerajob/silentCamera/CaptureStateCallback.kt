package com.wxm.camerajob.silentCamera

import android.graphics.Rect
import android.hardware.camera2.*
import android.hardware.camera2.params.MeteringRectangle
import android.media.ImageReader
import com.wxm.camerajob.utility.ContextUtil
import com.wxm.camerajob.utility.log.TagLog

/**
 * @author      WangXM
 * @version     create：2018/5/16
 */
class CaptureStateCallback constructor(private val mHome: SilentCameraNew,
                                       private val mReader: ImageReader)
    : CameraCaptureSession.StateCallback()  {
    private var mCaptureBuilder: CaptureRequest.Builder? = null

    override fun onConfigured(session: CameraCaptureSession) {
        TagLog.i("onConfigured")
        mHome.mCaptureSession = session

        // Auto focus should be continuous for camera preview.
        try {
            mHome.mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG)!!.let {
                setUpCaptureBuilder(it)
                mCaptureBuilder = it

                doCapture(null)

                Unit
            }
        } catch (e: CameraAccessException) {
            TagLog.e("onConfigured", e)
            mHome.takePhotoCallBack(false)
        }
    }

    fun doCapture(callBack:CaptureCallback? = null) {
        try {
            val cb = callBack ?: CaptureCallback(mHome, mReader, this)
            mHome.mCaptureSession!!.capture(mCaptureBuilder!!.build(), cb, null)
        } catch (e: Throwable)  {
            TagLog.e("doCapture failure", e)
            mHome.takePhotoCallBack(false)
        }
    }

    override fun onConfigureFailed(session: CameraCaptureSession) {
        TagLog.e("onConfigureFailed, session : $session")
        mHome.takePhotoCallBack(false)
    }

    private fun setUpCaptureBuilder(builder:CaptureRequest.Builder) {
        builder.addTarget(mReader.surface)
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

        //设置连续帧
        //设置每秒30帧
        mHome.mCamera!!.mCharacteristics.let {
            it.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)?.let {
                builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, it[it.size - 1])
            }
        }

        ContextUtil.getWindowManager()!!.defaultDisplay.rotation.let {
            builder.set(CaptureRequest.JPEG_ORIENTATION, SilentCameraNew.ORIENTATIONS.get(it))
        }
        builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
        builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
        builder.set(CaptureRequest.CONTROL_AE_MODE,
                if (mHome.mCamera!!.mFlashSupported) CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                else CaptureRequest.CONTROL_AE_MODE_ON)
    }
}