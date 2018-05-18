package com.wxm.camerajob.utility

import java.sql.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author      WangXM
 * @version     create：2018/5/15
 */
object CalendarUtility {
    private val SDF_FULL = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    private val SDF_YEAR_MONTH_DAY_HOUR_MINUTE = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)

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
     * get partly string for timeInMillis [cl]
     * example : result is '2018-05-15 12:00'
     */
    fun getYearMonthDayHourMinuteStr(cl: Any): String    {
        Calendar.getInstance(Locale.CHINA).apply {
            timeInMillis = when (cl) {
                is Long -> cl
                is Calendar -> cl.timeInMillis
                is Timestamp -> cl.time
                else -> 0L
            }
        }.let {
            return doFormat(it, SDF_YEAR_MONTH_DAY_HOUR_MINUTE)
        }
    }

    /**
     * get calendar from string [szCl]
     * example : [szCl] is '2018-05-15 12:00'
     */
    fun parseYearMonthDayHourMinuteStr(szCl: CharSequence): Calendar    {
        return doParse(szCl, SDF_YEAR_MONTH_DAY_HOUR_MINUTE)
    }

    /**
     * get partly string for timeInMillis [cl]
     * example : result is '2018-05-15 12:00'
     */
    fun getFullStr(cl: Any): String    {
        Calendar.getInstance(Locale.CHINA).apply {
            timeInMillis = when (cl) {
                is Long -> cl
                is Calendar -> cl.timeInMillis
                is Timestamp -> cl.time
                else -> 0L
            }
        }.let {
            return doFormat(it, SDF_FULL)
        }
    }

    /**
     * get calendar from string [szCl]
     * example : [szCl] is '2018-05-15 12:00:00'
     */
    fun parseFullStr(szCl: String): Calendar    {
        return doParse(szCl, SDF_FULL)
    }
}