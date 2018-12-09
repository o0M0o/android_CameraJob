package com.wxm.camerajob.preference

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import com.wxm.camerajob.data.entity.CameraParam
import com.wxm.camerajob.data.define.EProperty
import com.wxm.camerajob.data.define.GlobalDef

import com.wxm.camerajob.utility.AppUtil

import org.greenrobot.eventbus.EventBus

import wxm.androidutil.type.MySize

/**
 * for preference manage
 * Created by WangXM on 2016/6/18.
 */
object PreferencesUtil {
    private const val CAMERA_SET = "camera_set"
    private const val CAMERA_SET_FLAG = "camera_set_flag"
    private const val CAMERA_SET_FLAG_ISSET = "camera_isset"
    private const val CAMERA_SET_FLAG_NOSET = "camera_noset"

    /**
     * load camera parameter
     * (camera parameter is GLOBAL in app)
     * @return  camera parameter
     */
    fun loadCameraParam(): CameraParam {
        return CameraParam(null).apply {
            AppUtil.self.getSharedPreferences(GlobalDef.STR_CAMERAPROPERTIES_NAME,
                    Context.MODE_PRIVATE).let {
                mFace = it.getInt(EProperty.PROPERTIES_CAMERA_FACE.paraName,
                        CameraCharacteristics.LENS_FACING_BACK)

                it.getString(EProperty.PROPERTIES_CAMERA_DPI.paraName,
                        MySize(640, 480).toString())!!.let {
                    mPhotoSize = MySize.parseSize(it)
                }
                mAutoFocus = it.getBoolean(EProperty.PROPERTIES_CAMERA_AUTO_FOCUS.paraName, true)
                mAutoFlash = it.getBoolean(EProperty.PROPERTIES_CAMERA_AUTO_FLASH.paraName, true)
            }
        }
    }

    /**
     * save camera parameter
     * (camera parameter is GLOBAL in app)
     * @param cp    camera parameter
     */
    fun saveCameraParam(cp: CameraParam) {
        AppUtil.self.getSharedPreferences(GlobalDef.STR_CAMERAPROPERTIES_NAME,
                Context.MODE_PRIVATE).apply {
            edit().putInt(EProperty.PROPERTIES_CAMERA_FACE.paraName,
                    cp.mFace).apply()
            edit().putString(EProperty.PROPERTIES_CAMERA_DPI.paraName,
                    cp.mPhotoSize.toString()).apply()
            edit().putBoolean(EProperty.PROPERTIES_CAMERA_AUTO_FOCUS.paraName,
                    cp.mAutoFocus).apply()
            edit().putBoolean(EProperty.PROPERTIES_CAMERA_AUTO_FLASH.paraName,
                    cp.mAutoFlash).apply()
        }

        setCameraSetFlag(true)

        // send event
        PreferencesChangeEvent(GlobalDef.STR_CAMERAPROPERTIES_NAME).let {
            EventBus.getDefault().post(it)
        }
    }

    /**
     * check camera is set or not
     * @return  if camera is set return true
     */
    fun checkCameraIsSet(): Boolean {
        return AppUtil.self.getSharedPreferences(CAMERA_SET, Context.MODE_PRIVATE)
                .getString(CAMERA_SET_FLAG, CAMERA_SET_FLAG_NOSET) == CAMERA_SET_FLAG_ISSET
    }

    /**
     * set camera set flag
     * @param isSet  flag for camera set
     */
    private fun setCameraSetFlag(isSet: Boolean) {
        AppUtil.self.getSharedPreferences(CAMERA_SET, Context.MODE_PRIVATE).edit()
                .putString(CAMERA_SET_FLAG, if (isSet) CAMERA_SET_FLAG_ISSET else CAMERA_SET_FLAG_NOSET)
                .apply()
    }
}
