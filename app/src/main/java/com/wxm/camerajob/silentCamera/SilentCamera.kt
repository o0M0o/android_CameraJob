package com.wxm.camerajob.hardware

import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.os.Handler
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import com.wxm.camerajob.data.define.CameraParam
import com.wxm.camerajob.data.define.TakePhotoParam
import com.wxm.camerajob.utility.ContextUtil
import com.wxm.camerajob.utility.FileLogger




/**
 * base class for silent camera
 * silent camera can get photo without sound
 * Created by WangXM on 2016/7/4.
 */
abstract class SilentCamera {
    internal var mCameraStatus = ECameraStatus.NOT_OPEN

    internal var mCamera: CameraHardWare? = null
    internal lateinit var mTPParam: TakePhotoParam
    internal lateinit var mCParam: CameraParam
    private lateinit var mTPCBTakePhoto: TakePhotoCallBack

    interface TakePhotoCallBack {
        fun onTakePhotoSuccess(tp: TakePhotoParam)
        fun onTakePhotoFailed(tp: TakePhotoParam)
    }

    /**
     * hardware for camera
     */
    internal data class CameraHardWare(val mId: String) {
        var mSensorOrientation: Int = 0
        var mFace: Int = 0
        var mFlashSupported: Boolean = false
    }

    /**
     * do take photo
     */
    private inner class TakePhotoRunner internal constructor() : Runnable {
        override fun run() {
            try {
                mCParam.mSessionHandler = Handler()
                openCamera()
            } catch (e: Throwable) {
                Log.e(LOG_TAG, "take photo failure", e)
                FileLogger.getLogger().severe(e.toString())
            }
        }
    }


    /**
     * take photo
     * @param cp        for camera
     * @param tp        for photo
     * @param stp       call back holder
     */
    fun takePhoto(cp: CameraParam, tp: TakePhotoParam, stp: TakePhotoCallBack) {
        mCParam = cp
        mTPParam = tp
        mTPCBTakePhoto = stp

        TakePhotoRunner().run()
    }

    /**
     * callback for open camera
     * @param ret  true if success
     */
    internal fun openCameraCallBack(ret: Boolean) {
        if (ret) {
            "camera opened".apply {
                Log.i(LOG_TAG, this)
                FileLogger.getLogger().info(this)
            }

            capturePhoto()
        } else {
            "camera open failed".apply {
                Log.i(LOG_TAG, this)
                FileLogger.getLogger().info(this)
            }

            takePhotoCallBack(false)
        }
    }

    /**
     * callback for take photo
     * @param ret  true if success
     */
    internal fun takePhotoCallBack(ret: Boolean) {
        val tag = mTPParam.mTag

        if (ret) {
            ("take photo success, tag = $tag, photoFile = ${mTPParam.mFileName}").apply {
                Log.i(LOG_TAG, this)
                FileLogger.getLogger().info(this)
            }

            mTPCBTakePhoto.onTakePhotoSuccess(mTPParam)
        } else {
            ("take photo failed, tag = $tag, camera_status = ${mCameraStatus.description}").apply {
                Log.i(LOG_TAG, this)
                FileLogger.getLogger().info(this)
            }

            mTPCBTakePhoto.onTakePhotoFailed(mTPParam)
        }
        closeCamera()
    }


    /**
     * open camera
     * use callback to get result
     */
    internal abstract fun openCamera()

    /**
     * take photo
     * use callback to get result
     */
    internal abstract fun capturePhoto()

    /**
     * close camera
     */
    internal abstract fun closeCamera()

    companion object {
        private val LOG_TAG = ::SilentCameraNew.javaClass.simpleName
        val ORIENTATIONS = SparseIntArray().apply {
            append(Surface.ROTATION_0, 90)
            append(Surface.ROTATION_90, 0)
            append(Surface.ROTATION_180, 270)
            append(Surface.ROTATION_270, 180)
        }
    }
}

