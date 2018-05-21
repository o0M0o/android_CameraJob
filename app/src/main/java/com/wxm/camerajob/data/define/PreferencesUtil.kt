package com.wxm.camerajob.data.define

import android.content.Context
import android.hardware.camera2.CameraCharacteristics

import com.wxm.camerajob.utility.context.ContextUtil

import org.greenrobot.eventbus.EventBus

import wxm.androidutil.type.MySize
import wxm.androidutil.util.UtilFun

/**
 * for preference manage
 * Created by 123 on 2016/6/18.
 */
object PreferencesUtil {
    private val CAMERA_SET = "camera_set"
    private val CAMERA_SET_FLAG = "camera_set_flag"
    private val CAMERA_SET_FLAG_ISSET = "camera_isset"
    private val CAMERA_SET_FLAG_NOSET = "camera_noset"

    /**
     * load camera parameter
     * (camera parameter is GLOBAL in app)
     * @return  camera parameter
     */
    fun loadCameraParam(): CameraParam {
        return CameraParam(null).apply {
            ContextUtil.appContext().getSharedPreferences(GlobalDef.STR_CAMERAPROPERTIES_NAME,
                    Context.MODE_PRIVATE).let {
                mFace = it.getInt(EProperty.PROPERTIES_CAMERA_FACE.paraName,
                        CameraCharacteristics.LENS_FACING_BACK)

                it.getString(EProperty.PROPERTIES_CAMERA_DPI.paraName,
                        MySize(640, 480).toString())!!.let {
                    mPhotoSize = UtilFun.StringToSize(it)
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
        ContextUtil.appContext().getSharedPreferences(GlobalDef.STR_CAMERAPROPERTIES_NAME,
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
        return ContextUtil.appContext().getSharedPreferences(CAMERA_SET, Context.MODE_PRIVATE)
                .getString(CAMERA_SET_FLAG, CAMERA_SET_FLAG_NOSET) == CAMERA_SET_FLAG_ISSET
    }

    /**
     * set camera set flag
     * @param isSet  flag for camera set
     */
    private fun setCameraSetFlag(isSet: Boolean) {
        ContextUtil.appContext().getSharedPreferences(CAMERA_SET, Context.MODE_PRIVATE).edit()
                .putString(CAMERA_SET_FLAG, if (isSet) CAMERA_SET_FLAG_ISSET else CAMERA_SET_FLAG_NOSET)
                .apply()
    }
}
