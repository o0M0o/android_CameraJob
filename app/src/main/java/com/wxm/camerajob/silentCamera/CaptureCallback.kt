package com.wxm.camerajob.silentCamera

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import com.wxm.camerajob.utility.log.FileLogger
import com.wxm.camerajob.utility.log.TagLog
import wxm.androidutil.ImageUtility.ImageUtil

/**
 * @author      WangXM
 * @version     create：2018/5/16
 */
class CaptureCallback constructor(private val mHome: SilentCameraNew,
                                  private val mReader: ImageReader,
                                  private val mDoCapture: CaptureStateCallback)
    : CameraCaptureSession.CaptureCallback() {
    companion object {
        private const val MAX_WAIT_TIMES = 10
        private const val MAX_SUCCESS_TIMES = 4
    }

    private var mSuccessCount = 0
    private var mWaitCount = 0
    private val mAEArray = intArrayOf(
            CameraMetadata.CONTROL_AE_STATE_LOCKED, CameraMetadata.CONTROL_AE_STATE_CONVERGED,
            CameraMetadata.CONTROL_AE_STATE_PRECAPTURE, CameraMetadata.CONTROL_AE_STATE_FLASH_REQUIRED)

    private val mAFArray = intArrayOf(
            CameraMetadata.CONTROL_AF_STATE_PASSIVE_UNFOCUSED, CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED,
            CameraMetadata.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED, CameraMetadata.CONTROL_AF_STATE_PASSIVE_SCAN)

    override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest,
                                    result: TotalCaptureResult) {
        super.onCaptureCompleted(session, request, result)

        mWaitCount++
        if (MAX_WAIT_TIMES < mWaitCount) {
            TagLog.e("wait too many times")
            saveImage()
        } else {
            if (checkState(result)) {
                if(MAX_SUCCESS_TIMES > mSuccessCount)  {
                    mDoCapture.doCapture(this)
                } else  {
                    saveImage()
                }

                mSuccessCount++
            } else {
                mDoCapture.doCapture(this)
            }
        }
    }

    @SuppressLint("WrongConstant")
    override fun onCaptureFailed(session: CameraCaptureSession,
                                 request: CaptureRequest,
                                 failure: CaptureFailure) {
        super.onCaptureFailed(session, request, failure)
        ("CaptureFailed, reason = ${failure.reason} ").apply {
            TagLog.d(this)
            FileLogger.getLogger().warning(this)
        }

        mHome.takePhotoCallBack(false)
    }

    private fun saveImage() {
        mReader.acquireLatestImage().let {
            if(null != it) {
                it
            } else    {
                TagLog.i("first get image failure")
                Thread.sleep(200)
                mReader.acquireLatestImage()
            }
        }.let {
            if (null != it) {
                mSuccessCount = 0
                TagLog.i("get image success")
                it.use { processImage(it) }
            } else {
                TagLog.i("get image failure")
                mHome.takePhotoCallBack(false)
            }

            Unit
        }
    }

    private fun checkState(result: TotalCaptureResult): Boolean {
        val af = result.get(CaptureResult.CONTROL_AF_STATE)
        val ae = result.get(CaptureResult.CONTROL_AE_STATE)
        return ((if (null == ae) false else mAEArray.contains(ae)) &&
                (if (null == af) false else mAFArray.contains(af))).apply {
                    TagLog.i("afState = ${af ?: "null"}, aeState = ${ae ?: "null"}, " +
                            "state = ${ if(this) "ok" else "not_ok" }")
                }
    }


    private fun processImage(ig: Image) {
        val bytes = ig.use {
            it.planes[0].buffer.let {
                ByteArray(it.remaining()).apply {
                    it.get(this)
                }
            }
        }

        try {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)!!.let {
                ImageUtil.saveBitmapToJPGFile(it, mHome.mTPParam.mPhotoFileDir, mHome.mTPParam.mFileName)
            }.let {
                mHome.takePhotoCallBack(it)

                Unit
            }
        } catch (e: Throwable) {
            TagLog.e("save file failure", e)
            mHome.takePhotoCallBack(false)
        }
    }
}