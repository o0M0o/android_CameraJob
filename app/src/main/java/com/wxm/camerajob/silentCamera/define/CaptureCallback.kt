package com.wxm.camerajob.silentCamera.define

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import com.wxm.camerajob.silentCamera.SilentCamera
import com.wxm.camerajob.utility.log.FileLogger
import wxm.androidutil.image.ImageUtil
import wxm.androidutil.log.TagLog

/**
 * @author      WangXM
 * @version     create：2018/5/16
 */
internal class CaptureCallback constructor(private val mHome: SilentCamera,
                                           private val mReader: ImageReader,
                                           private val mDoCapture: CaptureStateCallback)
    : CameraCaptureSession.CaptureCallback() {
    private var mSuccessCount = 0
    private var mWaitCount = 0
    private val mAEArray = intArrayOf(
            CameraMetadata.CONTROL_AE_STATE_INACTIVE,
            CameraMetadata.CONTROL_AE_STATE_LOCKED, CameraMetadata.CONTROL_AE_STATE_CONVERGED,
            CameraMetadata.CONTROL_AE_STATE_PRECAPTURE, CameraMetadata.CONTROL_AE_STATE_FLASH_REQUIRED)

    private val mAFArray = intArrayOf(
            CameraMetadata.CONTROL_AF_STATE_PASSIVE_FOCUSED, CameraMetadata.CONTROL_AF_STATE_INACTIVE,
            CameraMetadata.CONTROL_AF_STATE_PASSIVE_UNFOCUSED, CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED,
            CameraMetadata.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED, CameraMetadata.CONTROL_AF_STATE_PASSIVE_SCAN)

    private val mCaptureTryCount = mHome.mCParam.mCaptureTryCount
    private val mCaptureSkipFrame = mHome.mCParam.mCaptureSkipFrame

    /**
     * 对焦和曝光成功的次数大于[mCaptureTryCount], 保存照片
     * 如果拍照次数大于[mCaptureSkipFrame], 保存照片
     */
    override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest,
                                    result: TotalCaptureResult) {
        super.onCaptureCompleted(session, request, result)

        mWaitCount++
        if (mCaptureTryCount <= mWaitCount) {
            TagLog.e("wait too many times")
            saveImage()
        } else {
            if (checkState(result)) {
                mSuccessCount++
                if(mCaptureSkipFrame > mSuccessCount)  {
                    mDoCapture.doCapture(this)
                } else  {
                    saveImage()
                }
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

    /**
     * if first time fail to acquire image, wait 200ms try again
     */
    private fun saveImage() {
        var ig = mReader.acquireLatestImage()
        if(null == ig)  {
            TagLog.i("get image failure first")

            Thread.sleep(200)
            ig = mReader.acquireLatestImage()
        }

        if (null != ig) {
            TagLog.i("get image success")
            mSuccessCount = 0
            ig.use { processImage(it) }
        } else {
            TagLog.i("get image failure")
            mHome.takePhotoCallBack(false)
        }
    }

    /**
     * check capture result status
     * @return true if capture is ok
     */
    private fun checkState(result: TotalCaptureResult): Boolean {
        val af = result.get(CaptureResult.CONTROL_AF_STATE)
        val ae = result.get(CaptureResult.CONTROL_AE_STATE)
        return ((if (null == ae) false else mAEArray.contains(ae)) &&
                (if (null == af) false else mAFArray.contains(af))).apply {
                    TagLog.i("afState = ${af ?: "null"}, aeState = ${ae ?: "null"}, " +
                            "state = ${ if(this) "ok" else "not_ok" }")
                }
    }


    /**
     * save [ig] to file
     */
    private fun processImage(ig: Image) {
        val bytes = ig.use {
            val bb = it.planes[0].buffer
            ByteArray(bb.remaining()).apply {
                bb.get(this)
            }
        }

        try {
            val ret = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)!!.let {
                ImageUtil.saveBitmapToJPGFile(it, mHome.mTPParam.mPhotoFileDir, mHome.mTPParam.mFileName)
            }

            mHome.takePhotoCallBack(ret)
        } catch (e: Throwable) {
            TagLog.e("save file failure", e)
            mHome.takePhotoCallBack(false)
        }
    }
}