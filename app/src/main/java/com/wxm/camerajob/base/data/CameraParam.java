package com.wxm.camerajob.base.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Size;

import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.base.utility.UtilFun;

/**
 * 相机参数
 * Created by 123 on 2016/6/17.
 */
public class CameraParam  implements Parcelable {
    public int      mFace;
    public Size     mPhotoSize;
    public boolean  mAutoFlash;
    public boolean  mAutoFocus;
    public Handler  mSessionHandler;

    public CameraParam(Handler sessionHandler)    {
        mFace = CameraCharacteristics.LENS_FACING_BACK;
        mSessionHandler = sessionHandler;

        mPhotoSize = new Size(1280, 960);
        mAutoFlash = true;
        mAutoFocus = true;
    }

    protected CameraParam(Parcel in) {
        mFace = in.readInt();
        mPhotoSize = in.readSize();

        boolean[] b_arr = new boolean[2];
        in.readBooleanArray(b_arr);
        mAutoFocus = b_arr[0];
        mAutoFlash = b_arr[1];
    }

    /**
     * 从配置文件加载数据
      */
    public void loadCfg()   {
        Context ct = ContextUtil.getInstance();
        SharedPreferences param = ct.getSharedPreferences(
                                GlobalDef.STR_CAMERAPROPERTIES_NAME,
                                Context.MODE_PRIVATE);

        mFace = param.getInt(GlobalDef.STR_PROPERTIES_CAMERA_FACE,
                                CameraCharacteristics.LENS_FACING_BACK);

        String sz_str = param.getString(GlobalDef.STR_PROPERTIES_CAMERA_DPI,
                                UtilFun.SizeToString(new Size(1280, 960)));
        mPhotoSize = UtilFun.StringToSize(sz_str);
        mAutoFocus = param.getBoolean(GlobalDef.STR_PROPERTIES_CAMERA_AUTOFOCUS, true);
        mAutoFlash = param.getBoolean(GlobalDef.STR_PROPERTIES_CAMERA_AUTOFLASH, true);
    }

    /**
     * 写入数据到配置文件
     */
    public void saveCfg()   {
        Context ct = ContextUtil.getInstance();
        SharedPreferences param = ct.getSharedPreferences(
                                        GlobalDef.STR_CAMERAPROPERTIES_NAME,
                                        Context.MODE_PRIVATE);
        param.edit().putInt(GlobalDef.STR_PROPERTIES_CAMERA_FACE,
                            mFace).apply();
        param.edit().putString(GlobalDef.STR_PROPERTIES_CAMERA_DPI,
                            UtilFun.SizeToString(mPhotoSize)).apply();
        param.edit().putBoolean(GlobalDef.STR_PROPERTIES_CAMERA_AUTOFOCUS,
                            mAutoFocus).apply();
        param.edit().putBoolean(GlobalDef.STR_PROPERTIES_CAMERA_AUTOFLASH,
                            mAutoFlash).apply();
    }

    public static final Creator<CameraParam> CREATOR = new Creator<CameraParam>() {
        @Override
        public CameraParam createFromParcel(Parcel in) {
            return new CameraParam(in);
        }

        @Override
        public CameraParam[] newArray(int size) {
            return new CameraParam[size];
        }
    };

    @Override
    public String toString()
    {
        String ret = String.format(
                "face : %s, photosize : %d X %d, %s, %s"
                ,mFace == CameraCharacteristics.LENS_FACING_BACK ? "back" : "front"
                ,mPhotoSize.getWidth()   ,mPhotoSize.getHeight()
                ,mAutoFlash ? "AUTO_FLASH" : "NO_FLASH"
                ,mAutoFocus ? "AUTO_FOCUS" : "NO_AUTOFOCUS");

        return ret;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mFace);
        dest.writeSize(mPhotoSize);
        dest.writeBooleanArray(new boolean[] {mAutoFocus, mAutoFlash});
    }
}
