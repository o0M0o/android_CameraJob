package com.wxm.camerajob.utility

import android.os.Message
import android.util.Log
import com.wxm.camerajob.data.define.*
import com.wxm.camerajob.hardware.SilentCameraHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * process camera job
 * Created by 123 on 2016/6/13.
 */
class CameraJobProcess {

    /**
     * wakeup to process job
     */
    fun processorWakeup(ls: List<CameraJob>) {
        wakeupDuty(LinkedList<CameraJob>().apply {
            addAll(ls.filter { checkJobWakeup(it) })
        })
    }

    /**
     * check job whether is wakeup
     * @param cj   job need check
     * @return  true if wakeup else false
     */
    private fun checkJobWakeup(cj: CameraJob): Boolean {
        if (cj.status.job_status == EJobStatus.RUN.status) {
            System.currentTimeMillis().let {
                if (it in cj.starttime.time..(cj.endtime.time - 1)) {
                    ETimeGap.values().find { cj.point == it.gapName }?.let {
                        return it.isArrive(Calendar.getInstance())
                    }
                }
            }
        }

        return false
    }

    /**
     * execute job
     * @param lsJob    job need executed
     */
    private fun wakeupDuty(lsJob: LinkedList<CameraJob>) {
        if(lsJob.isNotEmpty()) {
            lsJob.pop().let {
                Log.i(LOG_TAG, "wakeup job : " + it.toString())
                val param = TakePhotoParam(ContextUtil.instance.getCameraJobPhotoDir(it._id),
                        String.format(Locale.CHINA, "%d_%s.jpg",
                                it._id, CALENDAR_STR.format(System.currentTimeMillis())),
                        Integer.toString(it._id))

                SilentCameraHelper().let {
                    it.setTakePhotoCallBack(object : SilentCameraHelper.takePhotoCallBack {
                        override fun onTakePhotoSuccess(tp: TakePhotoParam) {
                            Log.i(LOG_TAG, "take photo success, tag = " + tp.mTag)

                            Message.obtain(ContextUtil.getMsgHandler(),
                                    EMsgType.CAMERAJOB_TAKEPHOTO.id,
                                    arrayOf<Any>(Integer.parseInt(tp.mTag), 1)).sendToTarget()
                            wakeupDuty(lsJob)
                        }

                        override fun onTakePhotoFailed(tp: TakePhotoParam) {
                            "take photo failure, tag = ${tp.mTag}".let {
                                Log.e(LOG_TAG, it)
                                FileLogger.logger.severe(it)
                            }

                            wakeupDuty(lsJob)
                        }
                    })

                    it.TakePhoto(PreferencesUtil.loadCameraParam(), param)
                }
            }
        }
    }

    companion object {
        private val LOG_TAG = ::CameraJobProcess.javaClass.simpleName

        private val CALENDAR_STR = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
    }
}