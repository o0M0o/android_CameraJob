package com.wxm.camerajob.data.define

/**
 * for properties
 * Created by WangXM on 2018/2/21.
 */
enum class EProperty private constructor(val paraName: String) {
    PROPERTIES_FILENAME("appConfig.properties"),
    PROPERTIES_CAMERA_FACE("camera_face"),
    PROPERTIES_CAMERA_DPI("camera_dpi"),
    PROPERTIES_CAMERA_AUTO_FLASH("camera_auto_flash"),
    PROPERTIES_CAMERA_AUTO_FOCUS("camera_auto_focus");


    companion object {
        fun getEPropertyName(szPara: String): EProperty? {
            return EProperty.values().find { it.paraName == szPara }
        }
    }
}
