package com.wxm.camerajob.data.define;

import android.hardware.camera2.CameraCharacteristics;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

import wxm.androidutil.type.MySize;


/**
 * parameter for camera
 * Created by 123 on 2016/6/17.
 */
public class CameraParam  implements Parcelable, Cloneable {
    public final static int     LENS_FACING_BACK  = 1;
    public final static int     LENS_FACING_FRONT = 0;

    private final static int  WAIT_MSECS = 8000;
    private long     mWaitMSecs;

    public int      mFace;
    public MySize mPhotoSize;
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
        return String.format(Locale.CHINA,
                "face : %s, photosize : %d X %d, %s, %s"
                ,mFace == CameraCharacteristics.LENS_FACING_BACK ? "back" : "front"
                ,mPhotoSize.getWidth()   ,mPhotoSize.getHeight()
                ,mAutoFlash ? "AUTO_FLASH" : "NO_FLASH"
                ,mAutoFocus ? "AUTO_FOCUS" : "NO_AUTOFOCUS");
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

    public CameraParam clone()   {
        CameraParam n = null;
        try {
            n = (CameraParam)super.clone();
            n.mFace               = mFace;
            n.mPhotoSize          = mPhotoSize;
            n.mAutoFocus          = mAutoFocus;
            n.mAutoFlash          = mAutoFlash;
            n.mSessionHandler     = mSessionHandler;
            n.mWaitMSecs          = mWaitMSecs;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return n;
    }
}
