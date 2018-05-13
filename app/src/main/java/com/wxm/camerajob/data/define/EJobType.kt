package com.wxm.camerajob.data.define

/**
 * job type
 * Created by WangXM on 2018/2/20.
 */
enum class EJobType private constructor(val type: String) {
    JOB_MINUTELY("每分钟"),
    JOB_HOURLY("每小时"),
    JOB_DAILY("每天");


    companion object {
        /**
         * get EJobType from paraName
         * @param ty    paraName for job type
         * @return      EJobType or null
         */
        fun getEJobType(ty: String): EJobType? {
            return EJobType.values().find { it.type == ty }
        }
    }
}
