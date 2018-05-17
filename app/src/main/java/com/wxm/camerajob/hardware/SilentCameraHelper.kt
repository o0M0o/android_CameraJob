package com.wxm.camerajob.hardware

import android.os.Handler
import android.util.Log

import com.wxm.camerajob.data.define.CameraParam
import com.wxm.camerajob.data.define.TakePhotoParam
import com.wxm.camerajob.utility.FileLogger

import wxm.androidutil.util.UtilFun

/**
 * helper for silent camera
 * Created by WangXM on 2016/6/16.
 */
class SilentCameraHelper {
    private var mTPCBTakePhoto: TakePhotoCallBack? = null

    interface TakePhotoCallBack {
        fun onTakePhotoSuccess(tp: TakePhotoParam)
        fun onTakePhotoFailed(tp: TakePhotoParam)
    }

    /**
     * set callback for take photo
     * @param tcb  callback holder
     */
    fun setTakePhotoCallBack(tcb: TakePhotoCallBack) {
        mTPCBTakePhoto = tcb
    }

    /**
     * take photo
     * @param cp    for camera
     * @param para  for take photo
     */
    fun takePhoto(cp: CameraParam, para: TakePhotoParam) {
        TakePhotoRunner(cp, para).run()
    }

    /**
     * do take photo
     */
    private inner class TakePhotoRunner
        internal constructor(private val mSelfCameraParam: CameraParam, private val mSelfTPTakePhoto: TakePhotoParam)
        : Runnable {
        private val mSCSelfCamera: SilentCamera = SilentCameraNew()

        private val mTPCTake = object : SilentCamera.SilentCameraTakePhotoCallBack {
            override fun onTakePhotoSuccess(tp: TakePhotoParam) {
                mTPCBTakePhoto?.onTakePhotoSuccess(mSelfTPTakePhoto)
            }

            override fun onTakePhotoFailed(tp: TakePhotoParam?) {
                mTPCBTakePhoto?.onTakePhotoFailed(mSelfTPTakePhoto)
            }
        }

        override fun run() {
            try {
                mSelfCameraParam.mSessionHandler = Handler()
                mSCSelfCamera.takePhoto(mSelfCameraParam, mSelfTPTakePhoto, mTPCTake)
            } catch (e: Throwable) {
                UtilFun.ThrowableToString(e).apply {
                    Log.d(LOG_TAG, this)
                    FileLogger.logger.severe(this)
                }
            }
        }
    }

    companion object {
        private val LOG_TAG = ::SilentCameraHelper.javaClass.simpleName
    }
}
