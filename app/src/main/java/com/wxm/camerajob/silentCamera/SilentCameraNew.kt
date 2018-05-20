package com.wxm.camerajob.silentCamera

import android.Manifest
import android.annotation.TargetApi
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.view.Surface
import com.wxm.camerajob.utility.ContextUtil
import com.wxm.camerajob.utility.FileLogger
import com.wxm.camerajob.utility.log.TagLog


/**
 * silent camera use camera2 api
 * Created by WangXM on 2016/7/4.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class SilentCameraNew internal constructor() : SilentCamera() {
    internal var mCameraDevice: CameraDevice? = null
    internal var mCaptureSession: CameraCaptureSession? = null

    /**
     * camera hardware
     */
    private val mHMCameraHardware = ArrayList<CameraHardWare>().apply {
        val lsCamera = this
        ContextUtil.getCameraManager()?.apply {
            try {
                val manager = this
                manager.cameraIdList.filterNotNull().forEach {
                    val cc = manager.getCameraCharacteristics(it)
                    lsCamera.add(CameraHardWare(it).apply {
                        mFace = cc.get(CameraCharacteristics.LENS_FACING) ?: 0
                        mSensorOrientation = cc.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 90
                        mFlashSupported = cc.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                    })
                }
            } catch (e: CameraAccessException) {
                TagLog.e("get camera info failure", e)
            }
        }
    }

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
                    TagLog.d("Orientation : display = $it, " +
                            "sensor = ${mCamera!!.mSensorOrientation}, ret = $ret")

                    ret
                } else  {
                    it
                }
            }
        }

    private val mCameraDeviceStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            "onOpened".apply {
                TagLog.i(this)
                FileLogger.getLogger().info(this)
            }

            mCameraDevice = camera
            openCameraCallBack(true)
        }

        override fun onDisconnected(camera: CameraDevice) {
            "onDisconnected".apply {
                TagLog.i( this)
                FileLogger.getLogger().info(this)
            }

            mCameraDevice = null
            openCameraCallBack(false)
        }

        override fun onError(camera: CameraDevice, error: Int) {
            "onError, error = $error".apply {
                TagLog.i( this)
                FileLogger.getLogger().info(this)
            }

            mCameraDevice = null
            openCameraCallBack(false)
        }
    }


    override fun openCamera() {
        if (!ContextUtil.checkPermission(Manifest.permission.CAMERA)) {
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
            ContextUtil.getCameraManager()!!
                    .openCamera(mCamera!!.mId, mCameraDeviceStateCallback, mCParam.mSessionHandler)
        } catch (e: Exception) {
            TagLog.e( "open camera failure", e)
            FileLogger.getLogger().severe(e.toString())
            openCameraCallBack(false)
        }
    }

    override fun capturePhoto() {
        TagLog.i( "start capture")
        mCameraStatus = ECameraStatus.TAKE_PHOTO_START

        try {
            ImageReader.newInstance(
                    mCParam.mPhotoSize.width, mCParam.mPhotoSize.height,
                    ImageFormat.JPEG, 2).let {
                /*
                it.setOnImageAvailableListener(
                        {
                            TagLog.i(LOG_TAG, "image already")
                        }, mCParam!!.mSessionHandler)
                        */

                mCameraDevice!!.createCaptureSession(listOf<Surface>(it.surface),
                        CaptureStateCallback(this, it), null)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            takePhotoCallBack(false)
        }

    }

    override fun closeCamera() {
        mCameraDevice?.close()
        mCaptureSession?.close()

        ("camera closed, paraTag = ${mTPParam.mTag}").apply {
            TagLog.i( this)
            FileLogger.getLogger().info(this)
        }

        mCameraStatus = ECameraStatus.NOT_OPEN
    }
}


