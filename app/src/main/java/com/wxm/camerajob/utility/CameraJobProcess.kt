package com.wxm.camerajob.utility

import android.os.Message
import android.util.Log
import com.wxm.camerajob.data.define.*
import com.wxm.camerajob.silentCamera.SilentCamera
import com.wxm.camerajob.silentCamera.SilentCameraNew
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
        if (lsJob.isNotEmpty()) {
            lsJob.pop().let {
                Log.i(LOG_TAG, "wakeup job : " + it.toString())
                val path = ContextUtil.getCameraJobDir(it._id)
                if (null == path) {
                    wakeupDuty(lsJob)
                } else {
                    val param = TakePhotoParam(path, String.format(Locale.CHINA, "%s.jpg",
                            CALENDAR_STR.format(System.currentTimeMillis())),
                            Integer.toString(it._id))

                    val cbTakePhoto = object : SilentCamera.TakePhotoCallBack {
                        override fun onTakePhotoFailed(tp: TakePhotoParam) {
                            wakeupDuty(lsJob)
                        }

                        override fun onTakePhotoSuccess(tp: TakePhotoParam) {
                            Message.obtain(ContextUtil.getMsgHandler(),
                                    EMsgType.CAMERAJOB_TAKEPHOTO.id,
                                    arrayOf<Any>(Integer.parseInt(tp.mTag), 1)).sendToTarget()
                            wakeupDuty(lsJob)
                        }
                    }

                    SilentCamera.takePhoto(PreferencesUtil.loadCameraParam(), param, cbTakePhoto)
                }
            }
        }
    }

    companion object {
        private val LOG_TAG = ::CameraJobProcess.javaClass.simpleName
        private val CALENDAR_STR = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
    }
}
