package com.wxm.camerajob.ui.test.silentCamera

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.wxm.camerajob.App
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.preference.PreferencesUtil
import com.wxm.camerajob.silentCamera.ITakePhoto
import com.wxm.camerajob.silentCamera.SilentCamera
import com.wxm.camerajob.silentCamera.TakePhotoParam
import kotterknife.bindView
import wxm.androidutil.app.AppBase
import wxm.androidutil.image.ImageUtil
import wxm.androidutil.log.TagLog

class ACTestSilentCamera : AppCompatActivity(), View.OnClickListener {
    private val mBTCapture: Button by bindView(R.id.acbt_capture)
    private val mIVPhoto: ImageView by bindView(R.id.aciv_photo)

    private val mCLBlack: Int = AppBase.getColor(R.color.black)

    private val mCameraParam = PreferencesUtil.loadCameraParam()
    private val mTPParam = TakePhotoParam(App.getPhotoRootDir(), "tmp.jpg", "1")
    private val mCBTakePhoto = object : ITakePhoto {
        override fun onTakePhotoFailed(tp: TakePhotoParam) {
            mBTCapture.isClickable = true
            mBTCapture.setTextColor(mCLBlack)

            onTakePhotoFailed()
        }

        override fun onTakePhotoSuccess(tp: TakePhotoParam) {
            mBTCapture.isClickable = true
            mBTCapture.setTextColor(mCLBlack)

            onTakePhotoSuccess()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_silent_camera_test)

        mIVPhoto.scaleType = ImageView.ScaleType.FIT_CENTER

        findViewById<View>(R.id.acbt_leave).setOnClickListener(this)
        mBTCapture.setOnClickListener(this)

        mBTCapture.isClickable = true
        mBTCapture.setTextColor(mCLBlack)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.acbt_leave -> {
                setResult(GlobalDef.INTRET_NOTCARE, Intent())
                finish()
            }

            R.id.acbt_capture -> {
                mBTCapture.isClickable = true
                mBTCapture.setTextColor(mCLBlack)

                TagLog.i("start capture!")
                SilentCamera.takePhoto(mCameraParam, mTPParam, mCBTakePhoto)
            }
        }
    }

    /// PRIVATE START
    private fun onTakePhotoSuccess() {
        Toast.makeText(this, "takePhoto ok", Toast.LENGTH_SHORT).show()

        mIVPhoto.getDrawingRect(Rect())
        val fn = "${mTPParam.mPhotoFileDir}/${mTPParam.mFileName}"
        //ImageUtil.getRotatedLocalBitmap(fn).let {
        ImageUtil.getLocalBitmap(fn).let {
            if (null != it) {
                mIVPhoto.setImageBitmap(it)
            } else {
                Toast.makeText(this, "load '$fn' failed!",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onTakePhotoFailed() {
        Toast.makeText(this, "takePhoto failed", Toast.LENGTH_SHORT).show()
    }
    /// PRIVATE END
}
