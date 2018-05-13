package com.wxm.camerajob.data.define

/**
 * msg type
 * Created by WangXM on 2018/2/19.
 */
enum class EMsgType private constructor(
        /**
         * get type paraName
         * @return  type paraName
         */
        val type: String,
        /**
         * get type id
         * @return  type id
         */
        val id: Int) {
    WAKEUP("wakeup", 1000),

    CAMERAJOB_QUERY("query camera job", 1102),
    CAMERAJOB_MODIFY("modify camera job", 1103),
    CAMERAJOB_TAKEPHOTO("camera job take photo", 1104),

    JOBSHOW_UPDATE("update job show", 1200),
    REPLAY("replay", 9000);


    companion object {

        /**
         * get EMsgType from id
         * @param id    id for msg type
         * @return      EMsgType or null
         */
        fun getEMsgType(id: Int): EMsgType? {
            for (et in EMsgType.values()) {
                if (et.id == id)
                    return et
            }

            return null
        }
    }
}
