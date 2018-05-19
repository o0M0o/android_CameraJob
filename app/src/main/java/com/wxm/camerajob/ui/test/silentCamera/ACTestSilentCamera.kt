package com.wxm.camerajob.ui.Test.Camera

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast

import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.data.define.PreferencesUtil
import com.wxm.camerajob.data.define.TakePhotoParam
import com.wxm.camerajob.hardware.SilentCamera
import com.wxm.camerajob.utility.ContextUtil
import com.wxm.camerajob.hardware.SilentCameraHelper
import kotterknife.bindView

import java.lang.ref.WeakReference

import wxm.androidutil.ImageUtility.ImageUtil

class ACSilentCameraTest : AppCompatActivity(), View.OnClickListener {
    private val mBTCapture: Button by bindView(R.id.acbt_capture)
    private val mIVPhoto: ImageView by bindView(R.id.aciv_photo)
    private val mSelfHandler: ACTestMsgHandler = ACTestMsgHandler(this)
    private val mTPParam: TakePhotoParam = TakePhotoParam(ContextUtil.getPhotoRootDir(), "tmp.jpg", "1")

    private val mCLGrey: Int = ContextUtil.appContext().getColor(R.color.gray)
    private val mCLBlack: Int = ContextUtil.appContext().getColor(R.color.black)

    private val mSCHelper = SilentCameraHelper(object : SilentCamera.SilentCameraTakePhotoCallBack {
        override fun onTakePhotoFailed(tp: TakePhotoParam) {
            mBTCapture.apply {
                isClickable = true
                setTextColor(mCLBlack)
            }

            mSelfHandler.sendEmptyMessage(MSG_TAKE_PHOTO_FAILED)
        }

        override fun onTakePhotoSuccess(tp: TakePhotoParam) {
            mBTCapture.apply {
                isClickable = true
                setTextColor(mCLBlack)
            }

            mSelfHandler.sendEmptyMessage(MSG_TAKE_PHOTO_SUCCESS)
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_silent_camera_test)

        mIVPhoto.scaleType = ImageView.ScaleType.FIT_CENTER

        findViewById<View>(R.id.acbt_leave).setOnClickListener(this)

        val home = this
        mBTCapture.apply {
            setOnClickListener(home)
            isClickable = true
            setTextColor(mCLBlack)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.acbt_leave -> {
                val data = Intent()
                setResult(GlobalDef.INTRET_NOTCARE, data)
                finish()
            }

            R.id.acbt_capture -> {
                mBTCapture.apply {
                    isClickable = true
                    setTextColor(mCLBlack)
                }

                mSCHelper.takePhoto(PreferencesUtil.loadCameraParam(), mTPParam)
            }
        }
    }

    private class ACTestMsgHandler internal constructor(ac: ACSilentCameraTest) : Handler() {
        private val mActivity: WeakReference<ACSilentCameraTest> = WeakReference(ac)

        override fun handleMessage(msg: Message) {
            val acHome = mActivity.get() ?: return

            when (msg.what) {
                MSG_TAKE_PHOTO_SUCCESS -> {
                    Toast.makeText(acHome, "takePhoto ok", Toast.LENGTH_SHORT).show()

                    acHome.mIVPhoto.getDrawingRect(Rect())
                    //MySize psz = new MySize(rt.width(), rt.height());
                    //Log.i(LOG_TAG, "perfence size : " + psz);

                    val fn = "${acHome.mTPParam.mPhotoFileDir}/${acHome.mTPParam.mFileName}"
                    ImageUtil.getLocalBitmap(fn).let {
                        if (null != it) {
                            acHome.mIVPhoto.setImageBitmap(it)
                        } else {
                            Toast.makeText(acHome, "load '$fn' failed!",
                                    Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                MSG_TAKE_PHOTO_FAILED -> {
                    Toast.makeText(acHome, "takePhoto failed", Toast.LENGTH_SHORT).show()
                }

                else -> Log.e(LOG_TAG, String.format("msg(%s) can not process", msg.toString()))
            }
        }
    }

    companion object {
        private val LOG_TAG = ::ACSilentCameraTest.javaClass.simpleName

        private const val MSG_TAKE_PHOTO_SUCCESS = 1
        private const val MSG_TAKE_PHOTO_FAILED = 2
    }
}
