package com.wxm.camerajob.data.define


/**
 * status for camera job
 * Created by WangXM on 2018/2/19.
 */
enum class EJobStatus(val status: String) {
    RUN("running"),
    PAUSE("pause"),
    STOP("stop"),
    UNKNOWN("unknown");
}
