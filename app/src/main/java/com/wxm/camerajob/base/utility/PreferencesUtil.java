package com.wxm.camerajob.base.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Size;

import com.wxm.camerajob.base.data.CameraParam;
import com.wxm.camerajob.base.data.GlobalDef;

/**
 * 配置管理类
 * Created by 123 on 2016/6/18.
 */
public class PreferencesUtil {
    /**
     * 加载相机参数配置
     * (相机参数配置为app唯一性配置)
     * @return  配置数据
     */
    public static CameraParam loadCameraParam() {
        CameraParam cp = new CameraParam(null);
        Context ct = ContextUtil.getInstance();
        SharedPreferences param = ct.getSharedPreferences(
                GlobalDef.STR_CAMERAPROPERTIES_NAME,
                Context.MODE_PRIVATE);

        cp.mFace = param.getInt(GlobalDef.STR_PROPERTIES_CAMERA_FACE,
                CameraCharacteristics.LENS_FACING_BACK);

        String sz_str = param.getString(GlobalDef.STR_PROPERTIES_CAMERA_DPI,
                UtilFun.SizeToString(new Size(1280, 960)));
        cp.mPhotoSize = UtilFun.StringToSize(sz_str);
        cp.mAutoFocus = param.getBoolean(GlobalDef.STR_PROPERTIES_CAMERA_AUTOFOCUS, true);
        cp.mAutoFlash = param.getBoolean(GlobalDef.STR_PROPERTIES_CAMERA_AUTOFLASH, true);

        return cp;
    }


    /**
     * 保存相机参数配置
     * (相机参数配置为app唯一性配置)
     * @param cp 保存配置
     */
    public static void saveCameraParam(CameraParam cp) {
        Context ct = ContextUtil.getInstance();
        SharedPreferences param = ct.getSharedPreferences(
                GlobalDef.STR_CAMERAPROPERTIES_NAME,
                Context.MODE_PRIVATE);
        param.edit().putInt(GlobalDef.STR_PROPERTIES_CAMERA_FACE,
                cp.mFace).apply();
        param.edit().putString(GlobalDef.STR_PROPERTIES_CAMERA_DPI,
                UtilFun.SizeToString(cp.mPhotoSize)).apply();
        param.edit().putBoolean(GlobalDef.STR_PROPERTIES_CAMERA_AUTOFOCUS,
                cp.mAutoFocus).apply();
        param.edit().putBoolean(GlobalDef.STR_PROPERTIES_CAMERA_AUTOFLASH,
                cp.mAutoFlash).apply();
    }

}
