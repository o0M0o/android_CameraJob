package com.wxm.camerajob.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Message
import android.util.Log
import com.wxm.camerajob.data.define.*

import com.wxm.camerajob.utility.ContextUtil
import com.wxm.camerajob.utility.FileLogger
import com.wxm.camerajob.utility.log.TagLog

import java.util.*

/**
 * wakeup app & set alarm for next wakeup
 * Created by WangXM on 2016/6/15.
 */
class AlarmReceiver : BroadcastReceiver() {

    /**
     * get next alarm delay time(ms)
     * if have job run, use job delay.
     * else active in next GlobalDef.INT_GLOBALJOB_PERIOD / 1000 seconds
     * @return  delay time(ms) before next alarm
     */
    private val nextAlarmDelay: Long
        get() {
            return LinkedList<Long>().apply {
                ContextUtil.getCameraJobUtility().allData
                        .filter { it.status.job_status == EJobStatus.RUN.status }
                        .forEach {
                            ETimeGap.getETimeGap(it.point)?.let {
                                add(it.getDelay(Calendar.getInstance()))
                            }
                        }
            }.min() ?: GlobalDef.INT_GLOBALJOB_PERIOD.toLong()
        }

    /**
     * wakeup in here
     * @param arg0   param
     * @param data   param
     */
    override fun onReceive(arg0: Context, data: Intent) {
        try {
            // wakeup app
            Message.obtain(ContextUtil.getMsgHandler(), EMsgType.WAKEUP.id, EMsgType.WAKEUP)
                    .sendToTarget()

            // set alarm
            ContextUtil.appContext().let {
                val intent = Intent(it, AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(it, 0, intent, 0)
                ContextUtil.getSystemService<AlarmManager>(Context.ALARM_SERVICE)!!
                        .set(AlarmManager.RTC_WAKEUP, nextAlarmDelay, pendingIntent)
            }
        } catch (e: Exception) {
            TagLog.e("receive alarm failure", e)
            FileLogger.getLogger().severe(e.toString())
        }
    }

    companion object {
        private val LOG_TAG = ::AlarmReceiver.javaClass.simpleName
    }
}
