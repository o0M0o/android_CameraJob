package com.wxm.camerajob.base.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraCharacteristics;

import com.wxm.camerajob.base.data.CameraParam;
import com.wxm.camerajob.base.data.GlobalDef;

import cn.wxm.andriodutillib.type.MySize;
import cn.wxm.andriodutillib.util.UtilFun;

/**
 * 配置管理类
 * Created by 123 on 2016/6/18.
 */
public class PreferencesUtil {
    private static final String CAMERA_SET = "camera_set";
    private static final String CAMERA_SET_FLAG = "camera_set_flag";
    private static final String CAMERA_SET_FLAG_ISSET = "camera_isset";
    private static final String CAMERA_SET_FLAG_NOSET = "camera_noset";

    private static final String CAMERA_USEOLD = "camera_useold";
    private static final String CAMERA_USEOLD_FLAG = "camera_useold_flag";

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
                                new MySize(1280, 960).toString());
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
                    cp.mPhotoSize.toString()).apply();
        param.edit().putBoolean(GlobalDef.STR_PROPERTIES_CAMERA_AUTOFOCUS,
                    cp.mAutoFocus).apply();
        param.edit().putBoolean(GlobalDef.STR_PROPERTIES_CAMERA_AUTOFLASH,
                    cp.mAutoFlash).apply();

        setCameraSetFlag(true);
    }


    /**
     * 检查相机是否已经设置过
     * @return 设置过相机返回true
     */
    public static boolean checkCameraIsSet()    {
        Context ct = ContextUtil.getInstance();
        SharedPreferences param = ct.getSharedPreferences(
                CAMERA_SET,
                Context.MODE_PRIVATE);

        String fg = param.getString(CAMERA_SET_FLAG, CAMERA_SET_FLAG_NOSET);
        if(fg.equals(CAMERA_SET_FLAG_ISSET))
            return true;
        else
            return false;
    }


    /**
     * 设置相机“设置标志"
     * @param bisset 相机设置过否标志
     */
    public static void setCameraSetFlag(boolean bisset) {
        Context ct = ContextUtil.getInstance();
        SharedPreferences param = ct.getSharedPreferences(
                CAMERA_SET,
                Context.MODE_PRIVATE);

        param.edit().putString(CAMERA_SET_FLAG,
                bisset ? CAMERA_SET_FLAG_ISSET : CAMERA_SET_FLAG_NOSET).apply();
    }


    /**
     * 返回是否使用旧相机驱动
     * @return  如果返回{@code true}则强制使用旧相机驱动
    public static boolean checkUseOldCamera()    {
        Context ct = ContextUtil.getInstance();
        SharedPreferences param = ct.getSharedPreferences(
                CAMERA_USEOLD,
                Context.MODE_PRIVATE);

        String fg = param.getString(CAMERA_USEOLD_FLAG, String.valueOf(false));
        return fg.equals(String.valueOf(true));
    }
     */
}
