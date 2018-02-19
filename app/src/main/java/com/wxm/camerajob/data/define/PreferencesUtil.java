package com.wxm.camerajob.data.define;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Build;

import com.wxm.camerajob.utility.ContextUtil;

import org.greenrobot.eventbus.EventBus;

import wxm.androidutil.type.MySize;
import wxm.androidutil.util.UtilFun;

/**
 * for preference manage
 * Created by 123 on 2016/6/18.
 */
public class PreferencesUtil {
    private static final String CAMERA_SET = "camera_set";
    private static final String CAMERA_SET_FLAG = "camera_set_flag";
    private static final String CAMERA_SET_FLAG_ISSET = "camera_isset";
    private static final String CAMERA_SET_FLAG_NOSET = "camera_noset";

    private static PreferencesUtil instance = new PreferencesUtil();
    public static PreferencesUtil getInstance() {
        return instance;
    }

    private PreferencesUtil()   {
    }

    /**
     * load camera parameter
     * (camera parameter is GLOBAL in app)
     * @return  camera parameter
     */
    public static CameraParam loadCameraParam() {
        CameraParam cp = new CameraParam(null);
        Context ct = ContextUtil.getInstance();
        SharedPreferences param = ct.getSharedPreferences(
                GlobalDef.STR_CAMERAPROPERTIES_NAME,
                Context.MODE_PRIVATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cp.mFace = param.getInt(GlobalDef.STR_PROPERTIES_CAMERA_FACE,
                    CameraCharacteristics.LENS_FACING_BACK);
        } else {
            cp.mFace = param.getInt(GlobalDef.STR_PROPERTIES_CAMERA_FACE,
                    Camera.CameraInfo.CAMERA_FACING_BACK);
        }

        String sz_str = param.getString(GlobalDef.STR_PROPERTIES_CAMERA_DPI,
                                new MySize(640, 480).toString());
        cp.mPhotoSize = UtilFun.StringToSize(sz_str);
        cp.mAutoFocus = param.getBoolean(GlobalDef.STR_PROPERTIES_CAMERA_AUTOFOCUS, true);
        cp.mAutoFlash = param.getBoolean(GlobalDef.STR_PROPERTIES_CAMERA_AUTOFLASH, true);

        return cp;
    }

    /**
     * save camera parameter
     * (camera parameter is GLOBAL in app)
     * @param cp    camera parameter
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

        // send event
        PreferencesChangeEvent pe = new PreferencesChangeEvent(GlobalDef.STR_CAMERAPROPERTIES_NAME);
        EventBus.getDefault().post(pe);
    }

    /**
     * check camera is set or not
     * @return  if camera is set return true
     */
    public static boolean checkCameraIsSet()    {
        Context ct = ContextUtil.getInstance();
        SharedPreferences param = ct.getSharedPreferences(
                CAMERA_SET,
                Context.MODE_PRIVATE);

        String fg = param.getString(CAMERA_SET_FLAG, CAMERA_SET_FLAG_NOSET);
        return fg.equals(CAMERA_SET_FLAG_ISSET);
    }

    /**
     * set camera set flag
     * @param isSet  flag for camera set
     */
    private static void setCameraSetFlag(boolean isSet) {
        Context ct = ContextUtil.getInstance();
        SharedPreferences param = ct.getSharedPreferences(
                CAMERA_SET,
                Context.MODE_PRIVATE);

        param.edit().putString(CAMERA_SET_FLAG,
                isSet ? CAMERA_SET_FLAG_ISSET : CAMERA_SET_FLAG_NOSET).apply();
    }
}
