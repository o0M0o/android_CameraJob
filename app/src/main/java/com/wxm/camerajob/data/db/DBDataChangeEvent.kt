package com.wxm.camerajob.data.db

/**
 * event for data change in db
 * Created by User on 2017/2/14.
 */
@Suppress("unused")
class DBDataChangeEvent(val dataType: Int, val eventType: Int) {
    companion object {
        const val EVENT_CREATE = 1
        const val EVENT_MODIFY = 2
        const val EVENT_REMOVE = 3

        const val DATA_JOB = 1
        const val DATA_JOB_STATUS = 2
    }
}
