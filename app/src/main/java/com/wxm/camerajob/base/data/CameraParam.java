package com.wxm.camerajob.base.data;

import android.hardware.camera2.CameraCharacteristics;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 相机参数
 * Created by 123 on 2016/6/17.
 */
public class CameraParam  implements Parcelable {
    public final static int     LENS_FACING_BACK = 1;
    public final static int     LENS_FACING_FRONT = 2;

    private final static int  WAIT_MSECS = 8000;
    public long     mWaitMSecs;

    public int      mFace;
    public MySize   mPhotoSize;
    public boolean  mAutoFlash;
    public boolean  mAutoFocus;
    public Handler  mSessionHandler;

    public CameraParam(Handler sessionHandler)    {
        mWaitMSecs = WAIT_MSECS;
        mFace = LENS_FACING_BACK;
        mSessionHandler = sessionHandler;

        mPhotoSize = new MySize(1280, 960);
        mAutoFlash = true;
        mAutoFocus = true;
    }

    private CameraParam(Parcel in) {
        mFace = in.readInt();

        int w = in.readInt();
        int h = in.readInt();
        mPhotoSize = new MySize(w, h);

        mWaitMSecs = in.readLong();

        boolean[] b_arr = new boolean[2];
        in.readBooleanArray(b_arr);
        mAutoFocus = b_arr[0];
        mAutoFlash = b_arr[1];
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

        int w = mPhotoSize.getWidth();
        int h = mPhotoSize.getHeight();
        dest.writeInt(w);
        dest.writeInt(h);

        dest.writeLong(mWaitMSecs);
        dest.writeBooleanArray(new boolean[] {mAutoFocus, mAutoFlash});
    }

    /**
     * 根据cp修改数据
     */
    public void Copy(CameraParam cp) {
        /*
        cp.mFace            = mFace;
        cp.mPhotoSize       = mPhotoSize;
        cp.mAutoFlash       = mAutoFlash;
        cp.mAutoFocus       = mAutoFocus;
        cp.mSessionHandler = mSessionHandler;
        */
        mFace               = cp.mFace;
        mPhotoSize          = cp.mPhotoSize;
        mAutoFocus          = cp.mAutoFocus;
        mAutoFlash          = cp.mAutoFlash;
        mSessionHandler     = cp.mSessionHandler;
        mWaitMSecs          = cp.mWaitMSecs;
    }
}
