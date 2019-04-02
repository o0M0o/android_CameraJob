package com.wxm.camerajob.preference

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import com.wxm.camerajob.App
import com.wxm.camerajob.data.define.EProperty
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.data.entity.CameraParam
import org.greenrobot.eventbus.EventBus
import wxm.androidutil.type.MySize

/**
 * for preference manage
 * Created by WangXM on 2016/6/18.
 */
object PreferencesUtil {
    private const val CAMERA_SET = "camera_set"
    private const val CAMERA_SET_FLAG = "camera_set_flag"
    private const val CAMERA_SET_FLAG_IS_SET = "camera_isset"
    private const val CAMERA_SET_FLAG_NO_SET = "camera_noset"

    /**
     * load camera parameter
     * (camera parameter is GLOBAL in app)
     */
    fun loadCameraParam(): CameraParam {
        return CameraParam(null).apply {
            App.self.getSharedPreferences(GlobalDef.STR_CAMERAPROPERTIES_NAME,
                    Context.MODE_PRIVATE).let {
                mFace = it.getInt(EProperty.PROP_CAMERA_FACE.paraName,
                        CameraCharacteristics.LENS_FACING_BACK)

                it.getString(EProperty.PROP_CAMERA_DPI.paraName,
                        MySize(640, 480).toString())!!.let { size ->
                    mPhotoSize = MySize.parseSize(size)
                }
                mAutoFocus = it.getBoolean(EProperty.PROP_CAMERA_AUTO_FOCUS.paraName, true)
                mAutoFlash = it.getBoolean(EProperty.PROP_CAMERA_AUTO_FLASH.paraName, true)

                val tc = it.getInt(EProperty.PROP_CAMERA_CAPTURE_TRY_COUNT.paraName, -1)
                if(-1 != tc)    {
                    mCaptureTryCount = tc
                }

                val sf = it.getInt(EProperty.PROP_CAMERA_CAPTURE_SKIP_FRAME.paraName, -1)
                if(-1 != sf)    {
                    mCaptureSkipFrame = sf
                }
            }
        }
    }

    /**
     * save camera parameter
     * (camera parameter is GLOBAL in app)
     * [cp] is camera parameter
     */
    fun saveCameraParam(cp: CameraParam) {
        App.self.getSharedPreferences(GlobalDef.STR_CAMERAPROPERTIES_NAME,
                Context.MODE_PRIVATE).apply {
            edit().putInt(EProperty.PROP_CAMERA_FACE.paraName,
                    cp.mFace)
                    .putString(EProperty.PROP_CAMERA_DPI.paraName,
                            cp.mPhotoSize.toString())
                    .putBoolean(EProperty.PROP_CAMERA_AUTO_FOCUS.paraName,
                            cp.mAutoFocus)
                    .putBoolean(EProperty.PROP_CAMERA_AUTO_FLASH.paraName,
                            cp.mAutoFlash)
                    .putInt(EProperty.PROP_CAMERA_CAPTURE_TRY_COUNT.paraName,
                            cp.mCaptureTryCount)
                    .putInt(EProperty.PROP_CAMERA_CAPTURE_SKIP_FRAME.paraName,
                            cp.mCaptureSkipFrame).apply()
        }

        setCameraSetFlag(true)

        // send event
        PreferencesChangeEvent(GlobalDef.STR_CAMERAPROPERTIES_NAME).let {
            EventBus.getDefault().post(it)
        }
    }

    /**
     * if camera is set return true
     */
    fun checkCameraIsSet(): Boolean {
        return App.self.getSharedPreferences(CAMERA_SET, Context.MODE_PRIVATE)
                .getString(CAMERA_SET_FLAG, CAMERA_SET_FLAG_NO_SET) == CAMERA_SET_FLAG_IS_SET
    }

    /**
     * [isSet] is camera set flag
     */
    private fun setCameraSetFlag(isSet: Boolean) {
        App.self.getSharedPreferences(CAMERA_SET, Context.MODE_PRIVATE).edit()
                .putString(CAMERA_SET_FLAG, if (isSet) CAMERA_SET_FLAG_IS_SET else CAMERA_SET_FLAG_NO_SET)
                .apply()
    }
}
