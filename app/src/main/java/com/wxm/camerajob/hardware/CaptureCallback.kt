package com.wxm.camerajob.hardware

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.util.Log
import com.wxm.camerajob.utility.FileLogger
import wxm.androidutil.ImageUtility.ImageUtil

/**
 * @author      WangXM
 * @version     create：2018/5/16
 */
class CameraCaptureCallback constructor(private val mHome: SilentCameraNew,
                                        private val mReader: ImageReader)
    : CameraCaptureSession.CaptureCallback() {
    companion object {
        private val LOG_TAG = ::CameraCaptureCallback.javaClass.simpleName

        private const val MAX_WAIT_TIMES = 5
        private const val PARTIAL_TAG = 1
        private const val COMPLETE_TAG = 2
    }

    private var mWaitCount = 0

    /**
     * if use mCaptureSession.capture, then not need check AE_STATE
     * @param result    para
     * @param tag       for log use
     */
    private fun process(result: CaptureResult, tag: Int) {
        mWaitCount++
        if (MAX_WAIT_TIMES < mWaitCount) {
            Log.e(LOG_TAG, "wait too many times")
            val ig = mReader.acquireLatestImage()
            if (null != ig) {
                savePhoto(ig)
            } else {
                mHome.takePhotoCallBack(false)
            }
        } else {
            var ret = true
            val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
            Log.i(LOG_TAG, "tag = $tag ae = ${aeState?.toString() ?: "null"}, " +
                    "waitCount = $mWaitCount")
            if (aeState == null
                    || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
                    || aeState == CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED
                    || aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                val ig = mReader.acquireLatestImage()
                if (null != ig) {
                    Log.i(SilentCameraNew.LOG_TAG, "image ok")
                    ret = false

                    savePhoto(ig)
                }
            }

            if (ret) {
                Log.i(LOG_TAG, "wait image ok")
                try {
                    Thread.sleep(250)
                    //mHome.mCaptureSession!!.capture(mBuilder.build(), this, null)
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                    mHome.takePhotoCallBack(false)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    mHome.takePhotoCallBack(false)
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
        process(result, COMPLETE_TAG)
    }

    @SuppressLint("WrongConstant")
    override fun onCaptureFailed(session: CameraCaptureSession,
                                 request: CaptureRequest,
                                 failure: CaptureFailure) {
        super.onCaptureFailed(session, request, failure)
        ("CaptureFailed, reason = ${failure.reason} ").apply {
            Log.d(LOG_TAG, this)
            FileLogger.logger.warning("$LOG_TAG $this")
        }

        mHome.mCameraStatus = ECameraStatus.TAKE_PHOTO_FAILURE
        mHome.takePhotoCallBack(false)
    }

    private fun savePhoto(ig: Image): Boolean {
        val bytes = ig.use {
            it.planes[0].buffer.let {
                ByteArray(it.remaining()).apply {
                    it.get(this)
                }
            }
        }

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size).apply {
            ImageUtil.rotateBitmap(this, mHome.orientation, null)
        }.let {
            ImageUtil.saveBitmapToJPGFile(it, mHome.mTPParam!!.mPhotoFileDir, mHome.mTPParam!!.mFileName)
        }.let {
            mHome.mCameraStatus = if (it) ECameraStatus.TAKE_PHOTO_SUCCESS else ECameraStatus.TAKE_PHOTO_FAILURE
            mHome.takePhotoCallBack(it)

            it
        }
    }
}