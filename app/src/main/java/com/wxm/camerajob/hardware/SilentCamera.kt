package com.wxm.camerajob.hardware

import android.graphics.Bitmap
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface

import com.wxm.camerajob.data.define.CameraParam
import com.wxm.camerajob.data.define.TakePhotoParam
import com.wxm.camerajob.utility.FileLogger

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.Semaphore

import wxm.androidutil.util.UtilFun


/**
 * base class for silent camera
 * silent camera can get photo without sound
 * Created by WangXM on 2016/7/4.
 */
internal abstract class SilentCamera {

    var mSensorOrientation: Int = 0

    var mCameraStatus = ECameraStatus.NOT_OPEN

    var mTPParam: TakePhotoParam? = null
    var mCParam: CameraParam
    var mFlashSupported: Boolean = false
    private val mOCCBOpen: SilentCameraOpenCameraCallBack?
    private var mTPCBTakePhoto: SilentCameraTakePhotoCallBack? = null

    internal interface SilentCameraOpenCameraCallBack {
        fun onOpenSuccess(cp: CameraParam)
        fun onOpenFailed(cp: CameraParam)
    }

    internal interface SilentCameraTakePhotoCallBack {
        fun onTakePhotoSuccess(tp: TakePhotoParam)
        fun onTakePhotoFailed(tp: TakePhotoParam?)
    }

    init {
        mOCCBOpen = object : SilentCameraOpenCameraCallBack {
            override fun onOpenSuccess(cp: CameraParam) {
                capturePhoto()
            }

            override fun onOpenFailed(cp: CameraParam) {
                takePhotoCallBack(false)
            }
        }
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
            val l = "camera opened"
            Log.i(TAG, l)
            FileLogger.logger.info(l)

            mOCCBOpen?.onOpenSuccess(mCParam)
        } else {
            val l = "camera open failed"
            Log.i(TAG, l)
            FileLogger.logger.info(l)

            mOCCBOpen?.onOpenFailed(mCParam)
        }
    }

    /**
     * callback for take photo
     * @param ret  true if success
     */
    fun takePhotoCallBack(ret: Boolean?) {
        val tag = if (mTPParam == null)
            "null"
        else
            if (mTPParam!!.mTag == null) "null" else mTPParam!!.mTag

        closeCamera()
        if (ret!!) {
            val l = ("take photo success, tag = " + tag
                    + ", photofile = " + mTPParam!!.mFileName)
            Log.i(TAG, l)
            FileLogger.logger.info(l)

            if (null != mTPCBTakePhoto)
                mTPCBTakePhoto!!.onTakePhotoSuccess(mTPParam)
        } else {
            val l = ("take photo failed, tag = "
                    + tag + ", camera_status = " + mCameraStatus.description)
            Log.i(TAG, l)
            FileLogger.logger.info(l)

            if (null != mTPCBTakePhoto)
                mTPCBTakePhoto!!.onTakePhotoFailed(mTPParam)
        }
    }

    /**
     * save photo data to file
     * @param data          photo data
     * @param fileDir       dir
     * @param fileName      file paraName
     * @return              true if success
     */
    fun savePhotoToFile(data: ByteArray, fileDir: String, fileName: String): Boolean {
        var ret = false
        var output: FileOutputStream? = null
        val mf = File(fileDir, fileName)
        try {
            output = FileOutputStream(mf)
            output.write(data)
            ret = true
        } catch (e: IOException) {
            e.printStackTrace()
            FileLogger.logger.severe(UtilFun.ExceptionToString(e))
        } finally {
            if (null != output) {
                try {
                    output.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                    FileLogger.logger.severe(UtilFun.ExceptionToString(e))
                }

            }
        }

        return ret
    }

    /**
     * save bitmap as jpg file
     * @param bm            bitmap data
     * @param fileDir       jpg file dir
     * @param fileName      jpg filename
     * @return              true if success
     */
    fun saveBitmapToJPGFile(bm: Bitmap, fileDir: String, fileName: String): Boolean {
        var ret = false
        var output: FileOutputStream? = null
        val mf = File(fileDir, fileName)
        try {
            output = FileOutputStream(mf)
            ret = bm.compress(Bitmap.CompressFormat.JPEG, 85, output)
        } catch (e: IOException) {
            e.printStackTrace()
            FileLogger.logger.severe(UtilFun.ExceptionToString(e))
        } finally {
            if (null != output) {
                try {
                    output.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                    FileLogger.logger.severe(UtilFun.ExceptionToString(e))
                }

            }
        }

        return ret
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
        private val TAG = "SilentCamera"
        val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
    }
}
