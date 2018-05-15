package com.wxm.camerajob.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Message
import android.util.Log

import com.wxm.camerajob.data.define.CameraJob
import com.wxm.camerajob.data.define.EJobStatus
import com.wxm.camerajob.data.define.ETimeGap
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.utility.ContextUtil

import java.util.Calendar

import wxm.androidutil.util.UtilFun
import java.lang.Math.min

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
            var ret: Long = 0
            val ls_job = ContextUtil.getCameraJobUtility().allData
            if (!UtilFun.ListIsNullOrEmpty(ls_job)) {
                for (cj in ls_job) {
                    if (cj.status.job_status == EJobStatus.RUN.status) {
                        val et = ETimeGap.getETimeGap(cj.point)
                        if (null != et) {
                            val cur_ms = et.getDelay(Calendar.getInstance())
                            ret = if (0L == ret) cur_ms else min(cur_ms, ret)
                        }
                    }
                }
            }

            if (0L == ret) {
                val curms = System.currentTimeMillis()
                ret = curms - curms % GlobalDef.INT_GLOBALJOB_PERIOD + GlobalDef.INT_GLOBALJOB_PERIOD
            }

            return ret
        }

    /**
     * wakeup in here
     * @param arg0   param
     * @param data   param
     */
    override fun onReceive(arg0: Context, data: Intent) {
        try {
            // wakeup app
            Message.obtain(ContextUtil.getMsgHandler(), WAKEUP.id, WAKEUP)
                    .sendToTarget()

            // set alarm
            val ct = ContextUtil.instance
            val intent = Intent(ct, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(ct, 0, intent, 0)
            val alarmManager = ContextUtil.instance.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager?.set(AlarmManager.RTC_WAKEUP, nextAlarmDelay, pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.toString())
        }

    }

    companion object {
        private val TAG = "AlarmReceiver"
    }
}
