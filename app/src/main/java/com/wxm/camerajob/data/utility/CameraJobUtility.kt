package com.wxm.camerajob.data.utility

import com.wxm.camerajob.data.entity.CameraJob
import com.wxm.camerajob.App
import com.wxm.camerajob.data.db.CameraJobDBUtility
import com.wxm.camerajob.data.define.EJobStatus

import wxm.androidutil.util.FileUtil
import wxm.androidutil.util.UtilFun


/**
 * helper for job
 * Created by WangXM on 2016/11/9.
 */
class CameraJobUtility(private val mCameraJobUtility: CameraJobDBUtility) {
    /**
     * create camera job
     * first create job directory, then create job
     * @param cj    job information
     * @return      true if success else false
     */
    fun createCameraJob(cj: CameraJob): Boolean {
        return mCameraJobUtility.createData(cj).let {
            if (it) {
                !UtilFun.StringIsNullOrEmpty(App.createJobDir(cj))
            } else it
        }
    }


    /**
     * remove job from db
     * @param cj_id   id for job
     */
    fun removeCameraJob(cj_id: Int) {
        mCameraJobUtility.removeData(cj_id)
    }

    /**
     * delete files for job
     * @param cj_id   id for job
     */
    fun deleteCameraJob(cj_id: Int) {
        mCameraJobUtility.removeData(cj_id)
        App.getCameraJobDir(cj_id)?.let {
            FileUtil.deleteDirectory(it)
        }
    }

    /**
     * modify camera job
     * [js] is modify data
     */
    fun modifyCameraJob(js: CameraJob) {
        mCameraJobUtility.modifyData(js)
    }

    /**
     * get all run cameraJob
     */
    fun getAllRunJob(): List<CameraJob> {
        return mCameraJobUtility.allData.filterNotNull()
                .filter { it.status == EJobStatus.RUN.status  }
                .sortedBy { it.id }
    }

    /**
     * get all cameraJob
     */
    fun getAllJob(): List<CameraJob>    {
        return mCameraJobUtility.allData.filterNotNull().sortedBy { it.id }
    }

    /**
     * get cameraJob with id [jid]
     */
    fun getCameraJobById(jid: Int): CameraJob?  {
        return mCameraJobUtility.getData(jid)
    }

    /**
     * get amount for activity jobs
     */
    fun getActiveJobCount(): Int {
        return mCameraJobUtility.allData
                .filter { it.status == EJobStatus.RUN.status }
                .count()
    }
}
