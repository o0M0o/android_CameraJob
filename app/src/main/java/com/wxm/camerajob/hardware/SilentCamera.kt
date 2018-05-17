package com.wxm.camerajob.hardware

import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import com.wxm.camerajob.data.define.CameraParam
import com.wxm.camerajob.data.define.TakePhotoParam
import com.wxm.camerajob.utility.FileLogger


/**
 * base class for silent camera
 * silent camera can get photo without sound
 * Created by WangXM on 2016/7/4.
 */
abstract class SilentCamera {
    var mCamera: CameraHardWare? = null
    var mCameraStatus = ECameraStatus.NOT_OPEN

    var mTPParam: TakePhotoParam? = null
    var mCParam: CameraParam? = null


    private var mTPCBTakePhoto: SilentCameraTakePhotoCallBack? = null

    interface SilentCameraOpenCameraCallBack {
        fun onOpenSuccess(cp: CameraParam)
        fun onOpenFailed(cp: CameraParam)
    }

    interface SilentCameraTakePhotoCallBack {
        fun onTakePhotoSuccess(tp: TakePhotoParam)
        fun onTakePhotoFailed(tp: TakePhotoParam?)
    }

    /**
     * take photo
     * @param cp        for camera
     * @param tp        for photo
     * @param stp       call back holder
     */
    fun takePhoto(cp: CameraParam, tp: TakePhotoParam, stp: SilentCameraTakePhotoCallBack) {
        mCParam = cp
        mTPParam = tp
        mTPCBTakePhoto = stp

        openCamera()
    }

    /**
     * callback for open camera
     * @param ret  true if success
     */
    fun openCameraCallBack(ret: Boolean?) {
        if (ret!!) {
            "camera opened".apply {
                Log.i(LOG_TAG, this)
                FileLogger.logger.info(this)
            }

            capturePhoto()
        } else {
            "camera open failed".apply {
                Log.i(LOG_TAG, this)
                FileLogger.logger.info(this)
            }

            takePhotoCallBack(false)
        }
    }

    /**
     * callback for take photo
     * @param ret  true if success
     */
    fun takePhotoCallBack(ret: Boolean) {
        val tag = if (mTPParam == null) "null"
        else mTPParam!!.mTag

        closeCamera()
        if (ret) {
            ("take photo success, tag = $tag, photoFile = ${mTPParam!!.mFileName}").apply {
                Log.i(LOG_TAG, this)
                FileLogger.logger.info(this)
            }

            mTPCBTakePhoto?.onTakePhotoSuccess(mTPParam!!)
        } else {
            ("take photo failed, tag = $tag, camera_status = ${mCameraStatus.description}").apply {
                Log.i(LOG_TAG, this)
                FileLogger.logger.info(this)
            }

            mTPCBTakePhoto?.onTakePhotoFailed(mTPParam)
        }
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
        private val LOG_TAG = this.javaClass.simpleName
        val ORIENTATIONS = SparseIntArray().apply {
            append(Surface.ROTATION_0, 90)
            append(Surface.ROTATION_90, 0)
            append(Surface.ROTATION_180, 270)
            append(Surface.ROTATION_270, 180)
        }
    }
}

/**
 * hardware for camera
 */
data class CameraHardWare(val mId: String) {
    var mSensorOrientation: Int = 0
    var mFace: Int = 0
    var mFlashSupported: Boolean = false
}