package com.wxm.camerajob.utility

import com.wxm.camerajob.data.define.CameraJob

import wxm.androidutil.util.FileUtil
import wxm.androidutil.util.UtilFun


/**
 * helper for job
 * Created by 123 on 2016/11/9.
 */
object CameraJobUtility {
    /**
     * create camera job
     * first create job directory, then create job
     * @param cj    job information
     * @return      true if success else false
     */
    fun createCameraJob(cj: CameraJob): Boolean {
        return ContextUtil.getCameraJobUtility().createData(cj).let {
            if (it) {
                !UtilFun.StringIsNullOrEmpty(ContextUtil.createJobDir(cj))
            } else it
        }
    }


    /**
     * remove job from db
     * @param cj_id   id for job
     */
    fun removeCameraJob(cj_id: Int) {
        ContextUtil.getCameraJobUtility().removeData(cj_id)
    }

    /**
     * delete files for job
     * @param cj_id   id for job
     */
    fun deleteCameraJob(cj_id: Int) {
        ContextUtil.getCameraJobUtility().removeData(cj_id)
        ContextUtil.getCameraJobDir(cj_id)?.let {
            FileUtil.DeleteDirectory(it)
        }
    }
}
