package com.wxm.camerajob.utility;

import com.wxm.camerajob.data.define.CameraJob;

import wxm.androidutil.util.FileUtil;
import wxm.androidutil.util.UtilFun;



/**
 * helper for job
 * Created by 123 on 2016/11/9.
 */
public class CameraJobUtility {

    /**
     * create camera job
     * first create job directory, then create job
     * @param cj    job information
     * @return      true if success else false
     */
    public static boolean createCameraJob(CameraJob cj) {
        boolean ret = ContextUtil.GetCameraJobUtility().createData(cj);
        if(ret) {
            String dir = ContextUtil.getInstance().createCameraJobPhotoDir(cj);
            ret = !UtilFun.StringIsNullOrEmpty(dir);
        }

        return  ret;
    }


    /**
     * remove job from db
     * @param cj_id   id for job
     */
    public static void removeCamerJob(int cj_id) {
        ContextUtil.GetCameraJobUtility().removeData(cj_id);
    }

    /**
     * delete files for job
     * @param cj_id   id for job
     */
    public static void deleteCamerJob(int cj_id) {
        ContextUtil.GetCameraJobUtility().removeData(cj_id);
        String path = ContextUtil.getInstance().getCameraJobPhotoDir(cj_id);
        FileUtil.DeleteDirectory(path);
    }
}
