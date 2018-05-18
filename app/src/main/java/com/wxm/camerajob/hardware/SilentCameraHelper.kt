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
class SilentCameraHelper(private val mTPCBTakePhoto: SilentCamera.SilentCameraTakePhotoCallBack) {
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

        override fun run() {
            try {
                mSelfCameraParam.mSessionHandler = Handler()
                mSCSelfCamera.takePhoto(mSelfCameraParam, mSelfTPTakePhoto, mTPCBTakePhoto)
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
