package com.wxm.camerajob.silentCamera

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import com.wxm.camerajob.utility.FileLogger
import com.wxm.camerajob.utility.log.TagLog
import wxm.androidutil.ImageUtility.ImageUtil

/**
 * @author      WangXM
 * @version     create：2018/5/16
 */
class CaptureCallback constructor(private val mHome: SilentCameraNew,
                                  private val mReader: ImageReader)
    : CameraCaptureSession.CaptureCallback() {
    companion object {
        private val LOG_TAG = ::CaptureCallback.javaClass.simpleName

        private const val MAX_WAIT_TIMES = 5
        private const val PARTIAL_TAG = 1
        private const val COMPLETE_TAG = 2
    }

    init {
        mReader.setOnImageAvailableListener(
                { reader -> processImage(reader.acquireNextImage()) },
                mHome.mCParam.mSessionHandler)
    }

    private var mWaitCount = 0

    private fun checkAE(aeState: Int?): Boolean {
        return aeState == null
                || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
                || aeState == CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED
                || aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE
    }

    private fun processImage(ig: Image?) {
        if (null == ig) {
            mHome.takePhotoCallBack(false)
        } else {
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
                    mHome.mCameraStatus = if (it) ECameraStatus.TAKE_PHOTO_SUCCESS
                    else ECameraStatus.TAKE_PHOTO_FAILURE
                    mHome.takePhotoCallBack(it)

                    Unit
                }
            } catch (e: Throwable) {
                TagLog.e( "save file failure", e)
                mHome.takePhotoCallBack(false)
            }
        }
    }


    /**
     * if use mCaptureSession.capture, then not need check AE_STATE
     * @param result    para
     * @param tag       for log use
     */
    private fun process(result: CaptureResult, tag: Int) {
        mWaitCount++
        if (MAX_WAIT_TIMES < mWaitCount) {
            TagLog.e("wait too many times")
            //processImage(mReader.acquireLatestImage())
        } else {
            result.get(CaptureResult.CONTROL_AE_STATE).let {
                TagLog.i("tag = $tag ae = ${it?.toString() ?: "null"}, " +
                        "waitCount = $mWaitCount")
                /*
                if(checkAE(result.get(CaptureResult.CONTROL_AE_STATE))) {
                    mReader.acquireLatestImage().let {
                        if(null != it) {
                            processImage(it)    }
                        else {
                            TagLog.w(LOG_TAG, "ImageReader is empty!")
                            try {
                                Thread.sleep(250)
                                //mHome.mCaptureSession!!.capture(mBuilder.build(), this, null)
                            } catch (e: InterruptedException) {
                                TagLog.e(LOG_TAG, "thread interrupted", e)
                                mHome.takePhotoCallBack(false)
                            }
                        }
                    }
                } else  {
                    TagLog.i(LOG_TAG, "wait image")
                    try {
                        Thread.sleep(250)
                        //mHome.mCaptureSession!!.capture(mBuilder.build(), this, null)
                    } catch (e: InterruptedException) {
                        TagLog.e(LOG_TAG, "thread interrupted", e)
                        mHome.takePhotoCallBack(false)
                    }
                }
                */

                Unit
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
            TagLog.d( this)
            FileLogger.getLogger().warning(this)
        }

        mHome.mCameraStatus = ECameraStatus.TAKE_PHOTO_FAILURE
        mHome.takePhotoCallBack(false)
    }
}