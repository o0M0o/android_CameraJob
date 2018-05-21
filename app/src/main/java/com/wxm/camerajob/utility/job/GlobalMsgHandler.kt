package com.wxm.camerajob.utility.job

import android.os.Handler
import android.os.Message
import android.util.Log
import com.wxm.camerajob.alarm.AlarmReceiver

import com.wxm.camerajob.data.define.EMsgType
import com.wxm.camerajob.utility.context.ContextUtil

import java.util.Calendar

import wxm.androidutil.util.UtilFun

/**
 * global msg handler
 * Created by wxm on 2016/6/10.
 */
@Suppress("UNUSED_PARAMETER")
internal class GlobalMsgHandler : Handler() {
    override fun handleMessage(msg: Message) {
        EMsgType.getEMsgType(msg.what)?.let {
            when (it) {
                EMsgType.WAKEUP -> onWakeup(msg)
                EMsgType.CAMERAJOB_QUERY -> onQueryCameraJob(msg)
                EMsgType.CAMERAJOB_TAKEPHOTO -> onTakePhoto(msg)
                else -> {
                    Log.e(LOG_TAG, "$it can not process")
                }
            }

            Unit
        }
    }

    private fun onWakeup(msg: Message) {
        ContextUtil.getCameraJobUtility().allData.let {
            ContextUtil.getJobProcess().processorWakeup(it)
        }

        AlarmReceiver.triggerAlarm()
    }

    private fun onQueryCameraJob(msg: Message) {
        val h = msg.obj as Handler
        ContextUtil.getCameraJobUtility().allData?.let {
            if(it.isNotEmpty()) {
                Message.obtain(h, EMsgType.REPLAY.id, it).let {
                    it.arg1 = EMsgType.CAMERAJOB_QUERY.id
                    it.sendToTarget()
                }
            }
        }
    }

    private fun onTakePhoto(msg: Message) {
        UtilFun.cast<Array<Any>>(msg.obj)?.let {
            val jobId = UtilFun.cast<Int>(it[0])
            val photoCount = UtilFun.cast<Int>(it[1])

            ContextUtil.getCameraJobUtility().getData(jobId)?.let {
                it.status.let {
                    it.job_photo_count = it.job_photo_count + photoCount
                    it.ts.time = Calendar.getInstance().timeInMillis

                    ContextUtil.getCameraJobStatusUtility().modifyData(it)
                }
            }

            Unit
        }
   }

    companion object {
        private val LOG_TAG = ::GlobalMsgHandler.javaClass.simpleName
    }

}