package com.wxm.camerajob.silentCamera

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import com.wxm.camerajob.utility.FileLogger
import com.wxm.camerajob.utility.log.TagLog
import wxm.androidutil.ImageUtility.ImageUtil
import java.util.*

/**
 * @author      WangXM
 * @version     create：2018/5/16
 */
class CaptureCallback constructor(private val mHome: SilentCameraNew,
                                  private val mReader: ImageReader,
                                  private val mDoCapture: CaptureStateCallback)
    : CameraCaptureSession.CaptureCallback() {
    companion object {
        private const val MAX_WAIT_TIMES = 5
    }

    private var mStateOk = false
    private var mWaitCount = 0
    private val mAEArray = intArrayOf(
            CameraMetadata.CONTROL_AE_STATE_LOCKED, CameraMetadata.CONTROL_AE_STATE_CONVERGED,
            CameraMetadata.CONTROL_AE_STATE_PRECAPTURE, CameraMetadata.CONTROL_AE_STATE_FLASH_REQUIRED)

    private val mAFArray = intArrayOf(
            CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED, CameraMetadata.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED)

    init {
        mReader.setOnImageAvailableListener({ reader ->
            TagLog.i("reader is ok")
        }, null)
    }

    override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest,
                                    result: TotalCaptureResult) {
        super.onCaptureCompleted(session, request, result)

        mWaitCount++
        if (MAX_WAIT_TIMES < mWaitCount) {
            TagLog.e("wait too many times")
            mHome.takePhotoCallBack(false)
        } else {
            mStateOk = checkState(result)
            if (mStateOk) {
                TagLog.i("camera status is ok")
                getLastImage(1000).let {
                    if (null != it) {
                        TagLog.i("get image success")
                        it.use { processImage(it) }
                    } else {
                        TagLog.i("get image failure")
                        mHome.takePhotoCallBack(false)
                    }

                    Unit
                }
            } else {
                TagLog.i("camera status not ok")
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

    private fun getLastImage(timeOut: Long): Image? {
        return mReader.acquireLatestImage().let {
            if(null != it) {
                return it
            } else    {
                Thread.sleep(timeOut)
                mReader.acquireLatestImage()
            }
        }
    }


    private fun checkState(result: TotalCaptureResult): Boolean {
        val af = result.get(CaptureResult.CONTROL_AF_STATE)
        val ae = result.get(CaptureResult.CONTROL_AE_STATE)
        TagLog.i("afState = ${af ?: "null"}, aeState = ${ae ?: "null"}")
        return (if (null == ae) false else mAEArray.contains(ae)) ||
                (if (null == af) false else mAFArray.contains(af))
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
            ImageUtil.rotateBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size),
                    mHome.orientation, null)!!.let {
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