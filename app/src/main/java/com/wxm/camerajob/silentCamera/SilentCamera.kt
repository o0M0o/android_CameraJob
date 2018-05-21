package com.wxm.camerajob.silentCamera

import android.hardware.camera2.CameraCharacteristics
import android.os.Handler
import com.wxm.camerajob.data.define.CameraParam
import com.wxm.camerajob.data.define.TakePhotoParam
import com.wxm.camerajob.utility.log.FileLogger
import com.wxm.camerajob.utility.log.TagLog
import java.util.*


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
    internal lateinit var mTPCBTakePhoto: TakePhotoCallBack

    interface TakePhotoCallBack {
        fun onTakePhotoSuccess(tp: TakePhotoParam)
        fun onTakePhotoFailed(tp: TakePhotoParam)
    }

    /**
     * hardware for camera
     */
    internal data class CameraHardWare(val mId: String, val mCharacteristics: CameraCharacteristics) {
        val mFace: Int = mCharacteristics.get(CameraCharacteristics.LENS_FACING)
                        ?: CameraCharacteristics.LENS_FACING_EXTERNAL
        val mSensorOrientation: Int = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
        val mFlashSupported: Boolean = mCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
    }

    /**
     * do take photo
     * can run it in other thread
     */
    private inner class TakePhotoRunner internal constructor() : Runnable {
        private val mTimer = java.util.Timer()

        override fun run() {
            try {
                mTimer.schedule(object : TimerTask()    {
                    override fun run() {
                        closeCamera()
                    }
                }, 5000)

                mCParam.mSessionHandler = Handler()
                openCamera()
            } catch (e: Throwable) {
                TagLog.e("take photo failure", e)
                FileLogger.getLogger().severe(e.toString())
            } finally {
                mTimer.cancel()
                closeCamera()
            }
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
                TagLog.i(this)
                FileLogger.getLogger().info(this)
            }

            mCameraStatus = ECameraStatus.TAKE_PHOTO_SUCCESS
            mTPCBTakePhoto.onTakePhotoSuccess(mTPParam)
        } else {

            ("take photo failed, tag = $tag, camera_status = ${mCameraStatus.description}").apply {
                TagLog.i(this)
                FileLogger.getLogger().info(this)
            }

            mCameraStatus = ECameraStatus.TAKE_PHOTO_FAILURE
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
     * close camera
     */
    internal abstract fun closeCamera()

    companion object {
        private val instance = SilentCameraNew()

        /**
         * take photo
         * @param cp        for camera
         * @param tp        for photo
         * @param stp       call back holder
         */
        fun takePhoto(cp: CameraParam, tp: TakePhotoParam, stp: TakePhotoCallBack) {
            instance.apply {
                mCParam = cp
                mTPParam = tp
                mTPCBTakePhoto = stp
                mCamera = null

                TakePhotoRunner().run()
            }
        }
    }
}

