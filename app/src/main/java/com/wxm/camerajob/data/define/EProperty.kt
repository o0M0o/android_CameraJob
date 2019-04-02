package com.wxm.camerajob.data.define

/**
 * for properties
 * Created by WangXM on 2018/2/21.
 */
enum class EProperty(val paraName: String) {
    PROP_FILENAME("appConfig.properties"),
    PROP_CAMERA_FACE("camera_face"),
    PROP_CAMERA_DPI("camera_dpi"),
    PROP_CAMERA_AUTO_FLASH("camera_auto_flash"),
    PROP_CAMERA_AUTO_FOCUS("camera_auto_focus"),

    PROP_CAMERA_CAPTURE_TRY_COUNT("camera_capture_try_count"),
    PROP_CAMERA_CAPTURE_SKIP_FRAME("camera_capture_skip_frame");
}
