package com.wxm.camerajob.utility;

import com.wxm.camerajob.data.define.CameraJob;

import wxm.androidutil.util.FileUtil;
import wxm.androidutil.util.UtilFun;

import static com.wxm.camerajob.utility.GlobalContext.GetDBManager;

/**
 * 拍照任务辅助类
 * Created by 123 on 2016/11/9.
 */
public class CameraJobUtility {

    /**
     * 创建拍照任务
     * 先创建任务目录，然后创建任务
     * @param cj   待创建任务数据
     * @return    若成功创建任务，返回ture
     */
    public static boolean createCameraJob(CameraJob cj) {
        boolean ret = GetDBManager().getCameraJobUtility().createData(cj);
        if(ret) {
            String dir = ContextUtil.getInstance().createCameraJobPhotoDir(cj);
            ret = !UtilFun.StringIsNullOrEmpty(dir);
        }

        return  ret;
    }


    /**
     * 在数据库中移除指定的拍照任务
     * @param cj_id   待移除任务id
     */
    public static void removeCamerJob(int cj_id) {
        GetDBManager().getCameraJobUtility().removeData(cj_id);
    }

    /**
     * 删除指定的拍照任务所占用的磁盘空间
     * @param cj_id   待移除任务id
     */
    public static void deleteCamerJob(int cj_id) {
        GetDBManager().getCameraJobUtility().removeData(cj_id);
        String path = ContextUtil.getInstance().getCameraJobPhotoDir(cj_id);
        FileUtil.DeleteDirectory(path);
    }
}
