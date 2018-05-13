package com.wxm.camerajob.data.define

import java.sql.Timestamp
import java.util.Calendar
import java.util.Locale

/**
 * param for take photo
 * Created by 123 on 2016/6/13.
 */
class TakePhotoParam(pp: String, fn: String, tag: String) {
    var mPhotoFileDir: String = pp
    var mFileName: String = fn
    var mTag: String = tag

    var mTS: Timestamp = Timestamp(System.currentTimeMillis())
}
