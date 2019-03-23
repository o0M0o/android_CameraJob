package com.wxm.camerajob.data.define

/**
 * job type
 * Created by WangXM on 2018/2/20.
 */
enum class EJobType(val type: String) {
    JOB_MINUTELY("每分钟"),
    JOB_HOURLY("每小时"),
    JOB_DAILY("每天");
}
