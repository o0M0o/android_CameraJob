package com.wxm.camerajob.silentCamera.define

/**
 * for camera status
 */
enum class ECameraStatus(val description: String) {
    CLOSE("close"),
    OPEN("open"),
    TAKE_PHOTO_START("take_photo_start"),
    TAKE_PHOTO_SUCCESS("take_photo_success"),
    TAKE_PHOTO_FAILURE("take_photo_failure")
}
