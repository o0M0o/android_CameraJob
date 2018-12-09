package com.wxm.camerajob.silentCamera.define

import android.hardware.camera2.CameraCharacteristics

/**
 * hardware for camera
 * @author      WangXM
 * @version     create：2018/12/9
 */

internal class CameraHardWare(val mId: String, val mCharacteristics: CameraCharacteristics) {
    val mFace: Int = mCharacteristics.get(CameraCharacteristics.LENS_FACING)
            ?: CameraCharacteristics.LENS_FACING_EXTERNAL
    val mSensorOrientation: Int = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
    val mFlashSupported: Boolean = mCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
}