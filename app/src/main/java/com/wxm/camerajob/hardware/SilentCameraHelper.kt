package com.wxm.camerajob.hardware

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log

import com.wxm.camerajob.data.define.CameraParam
import com.wxm.camerajob.data.define.TakePhotoParam
import com.wxm.camerajob.utility.ContextUtil
import com.wxm.camerajob.utility.FileLogger

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

import wxm.androidutil.util.UtilFun

/**
 * helper for silent camera
 * Created by WangXM on 2016/6/16.
 */
class SilentCameraHelper {

    private var mTPCBTakePhoto: takePhotoCallBack? = null

    interface takePhotoCallBack {
        fun onTakePhotoSuccess(tp: TakePhotoParam)
        fun onTakePhotoFailed(tp: TakePhotoParam)
    }

    /**
     * set callback for take photo
     * @param tcb  callback holder
     */
    fun setTakePhotoCallBack(tcb: takePhotoCallBack) {
        mTPCBTakePhoto = tcb
    }

    /**
     * take photo
     * @param cp    for camera
     * @param para  for take photo
     */
    fun TakePhoto(cp: CameraParam, para: TakePhotoParam) {
        //new Thread(new TakePhotoRunner(cp, para), "CameraRunner").run();
        TakePhotoRunner(cp, para).run()
    }


    /**
     * do take photo
     */
    private inner class TakePhotoRunner internal constructor(private val mSelfCameraParam: CameraParam, private val mSelfTPTakePhoto: TakePhotoParam) : Runnable {
        private val mSCSelfCamera: SilentCamera

        private val mTPCTake = object : SilentCamera.SilentCameraTakePhotoCallBack {
            override fun onTakePhotoSuccess(tp: TakePhotoParam) {
                if (null != mTPCBTakePhoto)
                    mTPCBTakePhoto!!.onTakePhotoSuccess(mSelfTPTakePhoto)
            }

            override fun onTakePhotoFailed(tp: TakePhotoParam?) {
                if (null != mTPCBTakePhoto)
                    mTPCBTakePhoto!!.onTakePhotoFailed(mSelfTPTakePhoto)
            }
        }

        init {
            mSCSelfCamera = if (ContextUtil.useNewCamera())
                SilentCameraNew()
            else
                SilentCameraOld()
        }

        override fun run() {
            try {
                mSelfCameraParam.mSessionHandler = Handler()
                mSCSelfCamera.takePhoto(mSelfCameraParam, mSelfTPTakePhoto, mTPCTake)
            } catch (e: Throwable) {
                e.printStackTrace()

                val str_e = UtilFun.ThrowableToString(e)
                Log.d(TAG, str_e)
                FileLogger.logger.severe(str_e)
            }

        }
    }

    companion object {
        private val TAG = "SilentCameraHelper"
    }
}
