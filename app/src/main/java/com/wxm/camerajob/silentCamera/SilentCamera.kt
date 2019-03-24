package com.wxm.camerajob.silentCamera

import android.Manifest
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.media.ImageReader
import android.os.Handler
import android.util.SparseIntArray
import android.view.Surface
import com.wxm.camerajob.data.entity.CameraParam
import com.wxm.camerajob.silentCamera.define.CameraHardWare
import com.wxm.camerajob.silentCamera.define.CaptureStateCallback
import com.wxm.camerajob.silentCamera.define.ECameraStatus
import com.wxm.camerajob.App
import com.wxm.camerajob.utility.log.FileLogger
import wxm.androidutil.app.AppBase
import wxm.androidutil.improve.let1
import wxm.androidutil.log.TagLog
import java.util.*


/**
 * base class for silent camera
 * silent camera can get photo without sound
 * Created by WangXM on 2016/7/4.
 */
class SilentCamera {
    private var mCameraStatus = ECameraStatus.CLOSE

    internal var mCamera: CameraHardWare? = null
    internal lateinit var mTPParam: TakePhotoParam
    internal lateinit var mCParam: CameraParam
    internal lateinit var mTPCBTakePhoto: ITakePhoto

    internal var mCameraDevice: CameraDevice? = null
    internal var mCaptureSession: CameraCaptureSession? = null

    /**
     * camera hardware
     */
    private val mHMCameraHardware : ArrayList<CameraHardWare>

    /**
     * Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
     * We have to take that into account and rotate JPEG properly.
     * For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
     * For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
     * Retrieves the JPEG orientation from the specified screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    internal val orientation: Int
        get() {
            return App.getWindowManager()!!.defaultDisplay.rotation.let {
                if(null != mCamera) {
                    val ret = (SilentCamera.ORIENTATIONS.get(it) + mCamera!!.mSensorOrientation + 270) % 360
                    TagLog.i("Orientation : display = $it, " +
                            "sensor = ${mCamera!!.mSensorOrientation}, ret = $ret")

                    ret
                } else  {
                    TagLog.i("Orientation : display = $it, ret = $it")

                    it
                }
            }
        }

    init {
        mHMCameraHardware = ArrayList<CameraHardWare>().apply {
            val lsCamera = this
            App.getCameraManager()?.apply {
                try {
                    cameraIdList.filterNotNull().forEach {
                        lsCamera.add(CameraHardWare(it, getCameraCharacteristics(it)))
                    }
                } catch (e: CameraAccessException) {
                    TagLog.e("get camera info failure", e)
                }
            }
        }
    }

    /**
     * open camera
     * use callback to get result
     */
    fun openCamera()    {
        if (!AppBase.checkPermission(Manifest.permission.CAMERA)) {
            TagLog.e( "need camera permission")
            openCameraCallBack(false)
            return
        }

        mCamera = mHMCameraHardware.find { it.mFace ==  mCParam.mFace }
        if (null == mCamera) {
            TagLog.e( "setup camera failure")
            openCameraCallBack(false)
            return
        }

        try {
            App.getCameraManager()!!.openCamera(mCamera!!.mId,
                    object : CameraDevice.StateCallback() {
                        override fun onOpened(camera: CameraDevice) {
                            "onOpened".let1 {
                                TagLog.i(it)
                                FileLogger.getLogger().info(it)
                            }

                            mCameraDevice = camera
                            openCameraCallBack(true)
                        }

                        override fun onDisconnected(camera: CameraDevice) {
                            "onDisconnected".let1 {
                                TagLog.i( it)
                                FileLogger.getLogger().info(it)
                            }

                            mCameraDevice = null
                            openCameraCallBack(false)
                        }

                        override fun onError(camera: CameraDevice, error: Int) {
                            "onError, error = $error".let1 {
                                TagLog.i( it)
                                FileLogger.getLogger().info(it)
                            }

                            mCameraDevice = null
                            openCameraCallBack(false)
                        }
                    },
                    mCParam.mSessionHandler)
        } catch (e: Exception) {
            TagLog.e( "open camera failure", e)
            FileLogger.getLogger().severe(e.toString())
            openCameraCallBack(false)
        }
    }

    /**
     * close camera
     */
    fun closeCamera()   {
        val log = "camera closed, paraTag = ${mTPParam.mTag}"
        TagLog.i( log)
        FileLogger.getLogger().info(log)

        mCameraDevice?.close()
        mCaptureSession?.close()
        mCameraStatus = ECameraStatus.CLOSE
    }

    /// PRIVATE START
    private fun capturePhoto() {
        TagLog.i( "start capture")
        mCameraStatus = ECameraStatus.TAKE_PHOTO_START

        try {
            ImageReader.newInstance(mCParam.mPhotoSize.width, mCParam.mPhotoSize.height,
                    ImageFormat.JPEG, 5).let {
                mCameraDevice!!.createCaptureSession(listOf<Surface>(it.surface),
                        CaptureStateCallback(this, it), null)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            takePhotoCallBack(false)
        }
    }

    /**
     * callback for open camera
     * @param ret  true if success
     */
    private fun openCameraCallBack(ret: Boolean) {
        if (ret) {
            "camera opened".apply {
                TagLog.i(this)
                FileLogger.getLogger().info(this)
            }

            capturePhoto()
        } else {
            "camera open failed".apply {
                TagLog.i(this)
                FileLogger.getLogger().info(this)
            }

            takePhotoCallBack(false)
        }
    }

    /**
     * callback after take photo
     * when success, [ret] is true
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
    /// PRIVATE END

    companion object {
        private val instance = SilentCamera()
        private val ORIENTATIONS = SparseIntArray().apply {
            append(Surface.ROTATION_0, 90)
            append(Surface.ROTATION_90, 0)
            append(Surface.ROTATION_180, 270)
            append(Surface.ROTATION_270, 180)
        }

        /**
         * take photo
         * can run it in other thread
         * [cp] is camera parameter
         * [tp] is take photo parameter
         * [stp] is callback for take photo
         */
        fun takePhoto(cp: CameraParam, tp: TakePhotoParam, stp: ITakePhoto) {
            instance.apply {
                mCParam = cp
                mTPParam = tp
                mTPCBTakePhoto = stp
                mCamera = null
            }

            Runnable {
                val mTimer = java.util.Timer()
                try {
                    mTimer.schedule(object : TimerTask()    {
                        override fun run() {
                            instance.closeCamera()
                        }
                    }, 5000)

                    instance.mCParam.mSessionHandler = Handler()
                    instance.openCamera()
                } catch (e: Throwable) {
                    TagLog.e("take photo failure", e)
                    FileLogger.getLogger().severe(e.toString())
                } finally {
                    mTimer.cancel()
                    instance.closeCamera()
                }
            }.run()
        }
    }
}

