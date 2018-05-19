package com.wxm.camerajob.utility.log

import android.util.Log

/**
 * @author      WangXM
 * @version     create：2018/5/19
 */
object LogUtil {
    private fun anyToStr(msg: Any?): String   {
        if(null == msg)
            return "null"

        return  when(msg)   {
            is String -> msg
            else -> msg.toString()
        }
    }

    private fun getTag(): String    {
        val createTag = {se: StackTraceElement ->
            val cn = se.className.let {
                it.substring(it.lastIndexOf("."))
            }
            val mn = se.methodName.let {
                it.substring(it.lastIndexOf("."))
            }

            "$cn-$mn"
        }

        return Thread.currentThread().stackTrace!!.let {
            if(it.size <= 2)    {
                ""
            } else  {
                it[3]!!.let {
                    createTag(it)
                }
            }
        }
    }

    fun i(msg: Any?, ta: Throwable? = null) {
        if(null == ta) {
            Log.i(getTag(), anyToStr(msg))
        } else  {
            Log.i(getTag(), anyToStr(msg), ta)
        }
    }
}