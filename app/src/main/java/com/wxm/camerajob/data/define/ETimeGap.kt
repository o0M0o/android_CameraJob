package com.wxm.camerajob.data.define

import java.util.*

/**
 * time gap for camera job
 * Created by WangXM on 2018/2/19.
 */
enum class ETimeGap private constructor(
        val gapName: String,
        private val funArrive: (Calendar) -> Boolean,
        private val funDelay: (Calendar) -> Long) {
    GAP_FIVE_SECOND("5秒",
            { cr -> 3 >= cr.get(Calendar.SECOND) % 5 },
            { cr ->
                cr.get(Calendar.SECOND).let {
                    if (it == 5) 5 * GlobalDef.MS_SECOND.toLong()
                    else (5 - it % 5) * GlobalDef.MS_SECOND.toLong()
                }
            }),
    GAP_FIFTEEN_SECOND("15秒",
            { cr -> GlobalDef.GLOBALJOB_CHECKPERIOD > cr.get(Calendar.SECOND) % 15 },
            { cr ->
                cr.get(Calendar.SECOND).let {
                    if (it == 15) 15 * GlobalDef.MS_SECOND.toLong()
                    else (15 - it % 15) * GlobalDef.MS_SECOND.toLong()
                }
            }),
    GAP_THIRTY_SECOND("30秒",
            { cr -> GlobalDef.GLOBALJOB_CHECKPERIOD > cr.get(Calendar.SECOND) % 30 },
            { cr ->
                cr.get(Calendar.SECOND).let {
                    if (30 == it) 30 * GlobalDef.MS_SECOND.toLong()
                    else (30 - it.toLong() % 30) * GlobalDef.MS_SECOND
                }
            }),

    GAP_ONE_MINUTE("1分钟",
            { cr -> GlobalDef.GLOBALJOB_CHECKPERIOD > cr.get(Calendar.SECOND) },
            { cr ->
                (60 - cr.get(Calendar.SECOND) % 60) * GlobalDef.MS_SECOND.toLong()
            }),
    GAP_FIVE_MINUTE("5分钟",
            { cr ->
                val cursec = cr.get(Calendar.SECOND)
                val curmin = cr.get(Calendar.MINUTE)
                0 == curmin % 5 && GlobalDef.GLOBALJOB_CHECKPERIOD > cursec
            },
            { cr ->
                (60 - cr.get(Calendar.SECOND) % 60) * GlobalDef.MS_SECOND.toLong()
            }),
    GAP_TEN_MINUTE("10分钟",
            { cr ->
                val cursec = cr.get(Calendar.SECOND)
                val curmin = cr.get(Calendar.MINUTE)
                0 == curmin % 10 && GlobalDef.GLOBALJOB_CHECKPERIOD > cursec
            },
            { cr ->
                ((60 - cr.get(Calendar.SECOND) % 60) * 1000).toLong()
            }),
    GAP_THIRTY_MINUTE("30分钟",
            { cr ->
                val cursec = cr.get(Calendar.SECOND)
                val curmin = cr.get(Calendar.MINUTE)
                0 == curmin % 30 && GlobalDef.GLOBALJOB_CHECKPERIOD > cursec
            },
            { cr ->
                ((60 - cr.get(Calendar.SECOND) % 60) * 1000).toLong()
            }),

    GAP_ONE_HOUR("1小时",
            { cr ->
                GlobalDef.GLOBALJOB_CHECKPERIOD > cr.get(Calendar.SECOND) % 30
            },
            { cr ->
                ((60 - cr.get(Calendar.MINUTE) % 60) * GlobalDef.MS_MINUTE).toLong()
            }),
    GAP_TWO_HOUR("2小时",
            { cr ->
                GlobalDef.GLOBALJOB_CHECKPERIOD > cr.get(Calendar.SECOND) % 30
            },
            { cr ->
                ((60 - cr.get(Calendar.MINUTE) % 60) * GlobalDef.MS_MINUTE).toLong()
            }),
    GAP_FOUR_HOUR("4小时",
            { cr ->
                GlobalDef.GLOBALJOB_CHECKPERIOD > cr.get(Calendar.SECOND) % 30
            },
            { cr ->
                ((60 - cr.get(Calendar.MINUTE) % 60) * GlobalDef.MS_MINUTE).toLong()
            });

    /**
     * check time gap is arrive
     * @param cr    calendar for current
     * @return  true if arrive;
     */
    fun isArrive(cr: Calendar): Boolean {
        return funArrive(cr)
    }

    /**
     * get delay time(ms) for next arrive
     * @param cr    calendar for current
     * @return      delay time(ms)
     */
    fun getDelay(cr: Calendar): Long {
        return funDelay(cr)
    }

    companion object {

        /**
         * get ETimeGap from paraName
         * @param name  paraName for timeGap
         * @return      ETimeGap or null
         */
        fun getETimeGap(name: String): ETimeGap? {
            return ETimeGap.values().find { it.gapName == name }
        }
    }
}

