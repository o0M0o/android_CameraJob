package com.wxm.camerajob.hardware

/**
 * for camera status
 */
enum class ECameraStatus private constructor(val description: String) {
    NOT_OPEN("not_open"),
    OPEN("open"),
    TAKE_PHOTO_START("take_photo_start"),
    TAKE_PHOTO_SUCCESS("take_photo_success"),
    TAKE_PHOTO_FAILURE("take_photo_failure")
}
