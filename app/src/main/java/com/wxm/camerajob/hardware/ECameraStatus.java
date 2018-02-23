package com.wxm.camerajob.hardware;

/**
 * for camera status
 */
public enum ECameraStatus   {
    NOT_OPEN("not_open"),
    OPEN("open"),
    TAKE_PHOTO_START("take_photo_start"),
    TAKE_PHOTO_SUCCESS("take_photo_success"),
    TAKE_PHOTO_FAILURE("take_photo_failure");

    private String szDescription;

    ECameraStatus(String description)   {
        szDescription = description;
    }

    public String getDescription()  {
        return szDescription;
    }
}
