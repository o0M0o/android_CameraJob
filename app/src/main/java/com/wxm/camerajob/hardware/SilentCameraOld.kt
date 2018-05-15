package com.wxm.camerajob.hardware

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.os.Handler
import android.os.Message
import android.util.Log

import com.wxm.camerajob.data.define.CameraParam
import com.wxm.camerajob.utility.FileLogger

import java.lang.ref.WeakReference

import wxm.androidutil.util.ImageUtil
import wxm.androidutil.util.UtilFun

/**
 * compatible with old camera api
 * Created by WangXM on 2016/7/4.
 */
class SilentCameraOld internal constructor() : SilentCamera() {

    private var mCamera: Camera? = null
    private var mCameraID: Int = 0
    private val mMHHandler: CameraMsgHandler

    private val mPCJpgProcessor = { data, camera ->
        val ret = savePhotoToFile(data, mTPParam!!.mPhotoFileDir, mTPParam!!.mFileName)

        mCameraStatus = if (ret) ECameraStatus.TAKE_PHOTO_SUCCESS else ECameraStatus.TAKE_PHOTO_FAILURE
        takePhotoCallBack(ret)
    }

    private val mPCRawProcessor = { data, camera ->
        var bm = BitmapFactory.decodeByteArray(data, 0, data.size)
        bm = ImageUtil.rotateBitmap(bm, mSensorOrientation, null)
        val ret = saveBitmapToJPGFile(bm, mTPParam!!.mPhotoFileDir, mTPParam!!.mFileName)

        mCameraStatus = if (ret) ECameraStatus.TAKE_PHOTO_SUCCESS else ECameraStatus.TAKE_PHOTO_FAILURE
        takePhotoCallBack(ret)
    }

    init {
        mMHHandler = CameraMsgHandler(this)
    }

    private fun setupCamera(): Boolean {
        try {
            var selid = -1
            val cc = Camera.getNumberOfCameras()
            val ci = Camera.CameraInfo()
            for (id in 0 until cc) {
                Camera.getCameraInfo(id, ci)

                if (CameraParam.LENS_FACING_BACK == mCParam.mFace) {
                    if (Camera.CameraInfo.CAMERA_FACING_BACK == ci.facing) {
                        selid = id
                    }
                } else {
                    if (Camera.CameraInfo.CAMERA_FACING_FRONT == ci.facing) {
                        selid = id
                    }
                }

                if (-1 != selid) {
                    mSensorOrientation = ci.orientation
                    break
                }
            }

            if (-1 == selid) {
                return false
            }

            mCameraID = selid
        } catch (e: Exception) {
            FileLogger.logger.severe(UtilFun.ExceptionToString(e))
            Log.e(TAG, UtilFun.ExceptionToString(e))
            return false
        }

        return true
    }

    public override fun openCamera() {
        if (!setupCamera()) {
            Log.w(TAG, "setup camera failure")
            return
        }

        var b_ret = false
        try {
            mCamera = Camera.open(mCameraID)
            mCameraStatus = ECameraStatus.OPEN
            b_ret = true
        } catch (e: Exception) {
            e.printStackTrace()
            FileLogger.logger.severe(UtilFun.ThrowableToString(e))
        }

        openCameraCallBack(b_ret)
    }

    internal override fun capturePhoto() {
        Log.i(TAG, "start capture")
        mCameraStatus = ECameraStatus.TAKE_PHOTO_START
        try {
            val cpa = mCamera!!.parameters
            mFlashSupported = null != cpa.flashMode

            if (mFlashSupported && mCParam.mAutoFlash) {
                cpa.flashMode = Camera.Parameters.FLASH_MODE_AUTO
            }

            if (mCParam.mAutoFocus) {
                cpa.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
            }

            cpa.setPictureSize(mCParam.mPhotoSize.width,
                    mCParam.mPhotoSize.height)

            mCamera!!.parameters = cpa
            mCamera!!.setPreviewCallback { data, camera -> Log.i(TAG, "preview being called!") }

            mCamera!!.startPreview()
            mCamera!!.takePicture(null, null, mPCJpgProcessor)
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e(TAG, UtilFun.ThrowableToString(e))
            FileLogger.logger.severe(UtilFun.ThrowableToString(e))

            takePhotoCallBack(false)
        }

        mMHHandler.sendEmptyMessageDelayed(MSG_CAPTURE_TIMEOUT, 5000)
    }

    public override fun closeCamera() {
        if (null != mCamera) {
            mCamera!!.release()
            mCamera = null
        }

        val l = "camera closed, paratag = " + if (mTPParam == null || mTPParam!!.mTag == null) "null" else mTPParam!!.mTag
        Log.i(TAG, l)
        FileLogger.logger.info(l)
        mCameraStatus = ECameraStatus.NOT_OPEN
    }


    /**
     * activity msg handler
     * Created by wxm on 2016/8/13.
     */
    private class CameraMsgHandler internal constructor(h: SilentCameraOld) : Handler() {
        private val mWRHandler: WeakReference<SilentCameraOld>


        init {
            mWRHandler = WeakReference(h)
        }

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_CAPTURE_TIMEOUT -> {
                    val l = "wait capture timeout"
                    Log.e(TAG, l)
                    FileLogger.logger.severe(l)

                    val h = mWRHandler.get()
                    h?.takePhotoCallBack(false)
                }

                else -> Log.e(TAG, String.format("msg(%s) can not process", msg.toString()))
            }
        }

        companion object {
            private val TAG = "CameraMsgHandler"
        }
    }

    companion object {
        private val TAG = "SilentCameraOld"
        private val MSG_CAPTURE_TIMEOUT = 1
    }
}
