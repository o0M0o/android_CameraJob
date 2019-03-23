package com.wxm.camerajob.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Message
import com.wxm.camerajob.data.define.EJobStatus
import com.wxm.camerajob.data.define.EMsgType
import com.wxm.camerajob.data.define.ETimeGap
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.App
import com.wxm.camerajob.utility.log.FileLogger
import wxm.androidutil.app.AppBase
import wxm.androidutil.log.TagLog
import java.util.*

/**
 * wakeup app & set alarm for next wakeup
 * Created by WangXM on 2016/6/15.
 */
class AlarmReceiver : BroadcastReceiver() {
    /**
     * wakeup in here
     * @param arg0   param
     * @param data   param
     */
    override fun onReceive(arg0: Context, data: Intent) {
        try {
            //TagLog.i("receive alarm")
            Message.obtain(App.getMsgHandler(), EMsgType.WAKEUP.id, EMsgType.WAKEUP)
                    .sendToTarget()
        } catch (e: Exception) {
            TagLog.e("receive alarm failure", e)
            FileLogger.getLogger().severe(e.toString())

            triggerAlarm()
        }
    }

    companion object {
        /**
         * get next alarm delay time(ms)
         * if have job run, use job delay.
         * else active in next GlobalDef.INT_GLOBALJOB_PERIOD / 1000 seconds
         * @return  delay time(ms) before next alarm
         */
        private fun getAlarmDelay(): Long {
            var ret = GlobalDef.INT_GLOBALJOB_PERIOD.toLong()
            try {
                ret = LinkedList<Long>().apply {
                    App.getCameraJobUtility().allData
                            .filter { it.status.job_status == EJobStatus.RUN.status }
                            .forEach {
                                ETimeGap.getETimeGap(it.point)?.let { eg ->
                                    add(eg.getDelay(Calendar.getInstance()))
                                }
                            }
                }.min() ?: GlobalDef.INT_GLOBALJOB_PERIOD.toLong()
            } catch (e: Exception) {
                TagLog.e("getAlarmDelay failure", e)
                FileLogger.getLogger().severe(e.toString())
            }

            return ret + System.currentTimeMillis()
        }

        fun triggerAlarm() {
            val app = App.self
            val pi = PendingIntent.getBroadcast(app, 0,
                    Intent(app, AlarmReceiver::class.java), 0)!!

            AppBase.getSystemService<AlarmManager>(Context.ALARM_SERVICE)!!
                    .set(AlarmManager.RTC_WAKEUP, getAlarmDelay(), pi)
        }
    }
}
