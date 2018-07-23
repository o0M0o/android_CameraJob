package com.wxm.camerajob.utility.job

import com.wxm.camerajob.data.define.CameraJob
import com.wxm.camerajob.utility.AppUtil

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
        return AppUtil.getCameraJobUtility().createData(cj).let {
            if (it) {
                !UtilFun.StringIsNullOrEmpty(AppUtil.createJobDir(cj))
            } else it
        }
    }


    /**
     * remove job from db
     * @param cj_id   id for job
     */
    fun removeCameraJob(cj_id: Int) {
        AppUtil.getCameraJobUtility().removeData(cj_id)
    }

    /**
     * delete files for job
     * @param cj_id   id for job
     */
    fun deleteCameraJob(cj_id: Int) {
        AppUtil.getCameraJobUtility().removeData(cj_id)
        AppUtil.getCameraJobDir(cj_id)?.let {
            FileUtil.deleteDirectory(it)
        }
    }
}
