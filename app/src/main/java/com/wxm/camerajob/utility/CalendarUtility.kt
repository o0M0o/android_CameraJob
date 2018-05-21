package com.wxm.camerajob.utility

import java.sql.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author      WangXM
 * @version     create：2018/5/15
 */
class CalendarUtility(private val mFormatter: SimpleDateFormat) {
    companion object {
        val SDF_FULL = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        val SDF_YEAR_MONTH_DAY_HOUR_MINUTE = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)

        val Full = CalendarUtility(SDF_FULL)
        val YearMonthDayHourMinute = CalendarUtility(SDF_YEAR_MONTH_DAY_HOUR_MINUTE)
    }

    private fun doFormat(cl: Calendar, utility: SimpleDateFormat): String    {
        return utility.format(cl.timeInMillis)
    }

    private fun doParse(szCl: CharSequence, utility: SimpleDateFormat): Calendar    {
        return Calendar.getInstance().apply {
            try {
                timeInMillis = utility.parse(szCl as? String ?: szCl.toString()).time
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * get date string for [cl]
     */
    fun getStr(cl: Any): String    {
        Calendar.getInstance(Locale.CHINA).apply {
            timeInMillis = when (cl) {
                is Long -> cl
                is Calendar -> cl.timeInMillis
                is Timestamp -> cl.time
                is Date -> cl.time
                else -> 0L
            }
        }.let {
            return doFormat(it, mFormatter)
        }
    }

    /**
     * get calendar from string [szCl]
     */
    fun parseStr(szCl: CharSequence): Calendar    {
        return doParse(szCl, mFormatter)
    }
}