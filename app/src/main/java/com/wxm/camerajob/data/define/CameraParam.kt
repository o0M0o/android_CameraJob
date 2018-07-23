package com.wxm.camerajob.data.define

import android.hardware.camera2.CameraCharacteristics
import android.os.Handler
import android.os.Parcel
import android.os.Parcelable

import java.util.Locale

import wxm.androidutil.type.MySize


/**
 * parameter for camera
 * Created by 123 on 2016/6/17.
 */
class CameraParam : Parcelable, Cloneable {
    private var mWaitMSecs: Long = 0

    var mFace: Int = 0
    var mPhotoSize: MySize
    var mAutoFlash: Boolean = false
    var mAutoFocus: Boolean = false
    var mSessionHandler: Handler? = null

    constructor(sessionHandler: Handler?) {
        mWaitMSecs = WAIT_MSECS.toLong()
        mFace = LENS_FACING_BACK
        mSessionHandler = sessionHandler

        mPhotoSize = MySize(1280, 960)
        mAutoFlash = true
        mAutoFocus = true
    }

    private constructor(inSteam: Parcel) {
        mFace = inSteam.readInt()

        val w = inSteam.readInt()
        val h = inSteam.readInt()
        mPhotoSize = MySize(w, h)

        mWaitMSecs = inSteam.readLong()

        BooleanArray(2).let {
            inSteam.readBooleanArray(it)
            mAutoFocus = it[0]
            mAutoFlash = it[1]
        }
    }

    override fun toString(): String {
        return String.format(Locale.CHINA,
                "face : %s, photosize : %d X %d, %s, %s",
                if (mFace == CameraCharacteristics.LENS_FACING_BACK) "back" else "front",
                mPhotoSize.width, mPhotoSize.height,
                if (mAutoFlash) "AUTO_FLASH" else "NO_FLASH",
                if (mAutoFocus) "AUTO_FOCUS" else "NO_AUTOFOCUS")
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(mFace)

        val w = mPhotoSize.width
        val h = mPhotoSize.height
        dest.writeInt(w)
        dest.writeInt(h)

        dest.writeLong(mWaitMSecs)
        dest.writeBooleanArray(booleanArrayOf(mAutoFocus, mAutoFlash))
    }

    public override fun clone(): CameraParam {
        return (super.clone() as CameraParam).let {
            it.mFace = mFace
            it.mPhotoSize = mPhotoSize
            it.mAutoFocus = mAutoFocus
            it.mAutoFlash = mAutoFlash
            it.mSessionHandler = mSessionHandler
            it.mWaitMSecs = mWaitMSecs

            it
        }
    }

    companion object {
        const val LENS_FACING_BACK = 1
        const val LENS_FACING_FRONT = 0

        private const val WAIT_MSECS = 8000

        val CREATOR: Parcelable.Creator<CameraParam> = object : Parcelable.Creator<CameraParam> {
            override fun createFromParcel(`in`: Parcel): CameraParam {
                return CameraParam(`in`)
            }

            override fun newArray(size: Int): Array<CameraParam> {
                return Array(size) {CameraParam(null)}
            }
        }
    }
}
