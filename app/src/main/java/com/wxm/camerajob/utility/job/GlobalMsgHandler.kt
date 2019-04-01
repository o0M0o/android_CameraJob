package com.wxm.camerajob.utility.job

import android.os.Handler
import android.os.Message
import com.wxm.camerajob.alarm.AlarmReceiver

import com.wxm.camerajob.data.define.EMsgType
import com.wxm.camerajob.App
import wxm.androidutil.improve.let1
import wxm.androidutil.log.TagLog

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
                    TagLog.e("$it can not process")
                }
            }

            Unit
        }
    }

    private fun onWakeup(msg: Message) {
        App.getCameraJobHelper().getAllJob().let {
            App.getJobProcess().processorWakeup(it)
        }

        AlarmReceiver.triggerAlarm()
    }

    private fun onQueryCameraJob(msg: Message) {
        val h = msg.obj as Handler
        App.getCameraJobHelper().getAllJob().let {
            if(it.isNotEmpty()) {
                Message.obtain(h, EMsgType.REPLAY.id, it).let {msg ->
                    msg.arg1 = EMsgType.CAMERAJOB_QUERY.id
                    msg.sendToTarget()
                }
            }
        }
    }

    private fun onTakePhoto(msg: Message) {
        UtilFun.cast<Array<Any>>(msg.obj)?.let1 {ar ->
            val jobId = UtilFun.cast<Int>(ar[0])
            val photoCount = UtilFun.cast<Int>(ar[1])

            App.getCameraJobHelper().getCameraJobById(jobId)?.let {
                it.photoCount += photoCount
                it.lastPhotoTime.time = Calendar.getInstance().timeInMillis

                App.getCameraJobHelper().modifyCameraJob(it)
            }
        }
   }
}