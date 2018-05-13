package com.wxm.camerajob.data.define

/**
 * for action
 * Created by WangXM on 2018/2/21.
 */
enum class EAction private constructor(val actName : String) {
    LOAD_JOB("load_job"),
    LOAD_CAMERA_SETTING("load_camera_setting"),
    LOAD_PHOTO_DIR("load_photo_dir");

    companion object {
        /**
         * use paraName to get enum object
         * @param szName   action paraName
         * @return         enum object
         */
        fun getEAction(szName: String): EAction? {
            return EAction.values().find { it.actName == szName }
        }
    }
}
