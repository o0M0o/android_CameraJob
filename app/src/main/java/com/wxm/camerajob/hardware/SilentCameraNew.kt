package com.wxm.camerajob.hardware

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Surface
import com.wxm.camerajob.utility.ContextUtil
import com.wxm.camerajob.utility.FileLogger
import wxm.androidutil.util.UtilFun


/**
 * silent camera use camera2 api
 * Created by WangXM on 2016/7/4.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class SilentCameraNew internal constructor() : SilentCamera() {
    internal var mCameraDevice: CameraDevice? = null
    internal var mCaptureSession: CameraCaptureSession? = null


    // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
    // We have to take that into account and rotate JPEG properly.
    // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
    // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    internal val orientation: Int
        get() {
            return ContextUtil.getWindowManager()!!.defaultDisplay.rotation.let {
                if(null != mCamera) {
                    val ret = (SilentCamera.ORIENTATIONS.get(it) + mCamera!!.mSensorOrientation + 270) % 360
                    Log.d(LOG_TAG, "Orientation : display = $it, " +
                            "sensor = ${mCamera!!.mSensorOrientation}, ret = $ret")

                    ret
                } else  {
                    it
                }
            }
        }

    private val mCameraDeviceStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            "onOpened".let {
                Log.i(LOG_TAG, it)
                FileLogger.logger.info("$LOG_TAG $it")
            }

            mCameraDevice = camera
            openCameraCallBack(true)
        }

        override fun onDisconnected(camera: CameraDevice) {
            "onDisconnected".let {
                Log.i(LOG_TAG, it)
                FileLogger.logger.info("$LOG_TAG $it")
            }

            mCameraDevice = null
            openCameraCallBack(false)
        }

        override fun onError(camera: CameraDevice, error: Int) {
            "onError, error = $error".let {
                Log.i(LOG_TAG, it)
                FileLogger.logger.info("$LOG_TAG $it")
            }

            mCameraDevice = null
            openCameraCallBack(false)
        }
    }


    /**
     * setup camera before use it
     * use camera face to find it's camera id
     * @return      true if find
     */
    private fun setupCamera(): Boolean {
        mCamera = mHMCameraHardware.find { it.mFace ==  mCParam!!.mFace }
        return mCamera == null
    }

    public override fun openCamera() {
        if (!setupCamera()) {
            Log.w(LOG_TAG, "setup camera failure")
            openCameraCallBack(false)
            return
        }

        if (!ContextUtil.checkPermission(Manifest.permission.CAMERA)) {
            Log.i(LOG_TAG, "need camera permission")
            openCameraCallBack(false)
            return
        }

        try {
            ContextUtil.getCameraManager()
                    ?.openCamera(mCamera!!.mId, mCameraDeviceStateCallback, mCParam!!.mSessionHandler)
        } catch (e: Exception) {
            openCameraCallBack(false)

            UtilFun.ThrowableToString(e).apply {
                Log.e(LOG_TAG, this)
                FileLogger.logger.severe("$LOG_TAG $this")
            }
        }
    }

    override fun capturePhoto() {
        Log.i(LOG_TAG, "start capture")
        mCameraStatus = ECameraStatus.TAKE_PHOTO_START

        try {
            ImageReader.newInstance(
                    mCParam!!.mPhotoSize.width, mCParam!!.mPhotoSize.height,
                    ImageFormat.JPEG, 2).let {
                it.setOnImageAvailableListener(
                        {
                            Log.i(LOG_TAG, "image already")
                        }, mCParam!!.mSessionHandler)

                mCameraDevice!!.createCaptureSession(listOf<Surface>(it.surface),
                        CaptureStateCallback(this, it), null)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            takePhotoCallBack(false)
        }

    }

    public override fun closeCamera() {
        mCaptureSession?.close()
        mCameraDevice?.close()

        ("camera closed, paraTag = " + if (mTPParam == null) "null" else mTPParam!!.mTag).let {
            Log.i(LOG_TAG, it)
            FileLogger.logger.info("$LOG_TAG, $it")
        }

        mCameraStatus = ECameraStatus.NOT_OPEN
    }


    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        internal val LOG_TAG = javaClass.simpleName

        /**
         * get camera hardware setting
         */
        private val mHMCameraHardware = ArrayList<CameraHardWare>()
        init {
            ContextUtil.getCameraManager()?.let {
                try {
                    val manager = it
                    it.cameraIdList.filterNotNull().forEach {
                        val cc = manager.getCameraCharacteristics(it)
                        mHMCameraHardware.add(CameraHardWare(it).apply {
                            cc.get(CameraCharacteristics.LENS_FACING)?.let {
                                mFace = it
                            }

                            mSensorOrientation = cc.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 90
                            mFlashSupported = cc.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                        }
                        )
                    }
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
            }
        }
    }
}


