package com.wxm.camerajob.ui.Test.Camera

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
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
import com.wxm.camerajob.utility.ContextUtil
import com.wxm.camerajob.hardware.SilentCameraHelper

import java.lang.ref.WeakReference

import wxm.androidutil.util.ImageUtil
import wxm.androidutil.util.UtilFun

class ACSilentCameraTest : AppCompatActivity(), View.OnClickListener {
    private var mBTCapture: Button? = null
    private var mIVPhoto: ImageView? = null
    private var mSelfHandler: ACTestMsgHandler? = null
    private var mTPParam: TakePhotoParam? = null

    private var mCLGrey: Int = 0
    private var mCLBlack: Int = 0

    private val mSCHelper = SilentCameraHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_silent_camera_test)

        mBTCapture = UtilFun.cast_t<Button>(findViewById(R.id.acbt_capture))
        mBTCapture!!.setOnClickListener(this)

        val mBTLeave = UtilFun.cast_t<Button>(findViewById(R.id.acbt_leave))
        mBTLeave.setOnClickListener(this)

        mIVPhoto = UtilFun.cast_t<ImageView>(findViewById(R.id.aciv_photo))
        mIVPhoto!!.scaleType = ImageView.ScaleType.FIT_CENTER

        mSelfHandler = ACTestMsgHandler(this)
        mTPParam = null

        mCLGrey = this.getColor(R.color.gray)
        mCLBlack = this.getColor(R.color.black)

        mSCHelper.setTakePhotoCallBack(
                object : SilentCameraHelper.takePhotoCallBack {
                    override fun onTakePhotoSuccess(tp: TakePhotoParam) {
                        mSelfHandler!!.sendEmptyMessage(SELFMSGWHAT_TAKEPHOTO_SUCCESS)
                    }

                    override fun onTakePhotoFailed(tp: TakePhotoParam) {
                        mSelfHandler!!.sendEmptyMessage(SELFMSGWHAT_TAKEPHOTO_FAILED)
                    }
                }
        )
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onClick(v: View) {
        val id = v.id
        when (id) {
            R.id.acbt_leave -> {
                val data = Intent()
                setResult(GlobalDef.INTRET_NOTCARE, data)
                finish()
            }

            R.id.acbt_capture -> {
                mBTCapture!!.isClickable = false
                mBTCapture!!.setTextColor(mCLGrey)

                val sp = ContextUtil.instance.appPhotoRootDir
                mTPParam = TakePhotoParam(sp!!, "tmp.jpg", "1")

                mSCHelper.TakePhoto(PreferencesUtil.loadCameraParam(), mTPParam)
            }
        }
    }

    private class ACTestMsgHandler internal constructor(acstart: ACSilentCameraTest) : Handler() {
        private val mActivity: WeakReference<ACSilentCameraTest>

        init {
            mActivity = WeakReference(acstart)
        }

        override fun handleMessage(msg: Message) {
            val ac_home = mActivity.get() ?: return

            when (msg.what) {
                SELFMSGWHAT_TAKEPHOTO_SUCCESS -> {
                    Toast.makeText(ac_home,
                            "takephoto ok",
                            Toast.LENGTH_SHORT).show()

                    val rt = Rect()
                    ac_home.mIVPhoto!!.getDrawingRect(rt)
                    //MySize psz = new MySize(rt.width(), rt.height());
                    //Log.i(TAG, "perfence size : " + psz);

                    val fn = ac_home.mTPParam!!.mPhotoFileDir + "/" + ac_home.mTPParam!!.mFileName
                    //Bitmap bm = ImageUtil.getRotatedLocalBitmap(fn, psz);
                    val bm = ImageUtil.getLocalBitmap(fn)
                    if (null != bm) {
                        ac_home.mIVPhoto!!.setImageBitmap(bm)
                    } else {
                        Toast.makeText(ac_home,
                                "load '$fn' failed!",
                                Toast.LENGTH_SHORT).show()
                    }

                    ac_home.mBTCapture!!.isClickable = true
                    ac_home.mBTCapture!!.setTextColor(ac_home.mCLBlack)
                }

                SELFMSGWHAT_TAKEPHOTO_FAILED -> {
                    Toast.makeText(ac_home,
                            "takephoto failed",
                            Toast.LENGTH_SHORT).show()

                    ac_home.mBTCapture!!.isClickable = true
                    ac_home.mBTCapture!!.setTextColor(ac_home.mCLBlack)
                }

                else -> Log.e(TAG, String.format("msg(%s) can not process", msg.toString()))
            }
        }

        companion object {
            private val TAG = "ACTestMsgHandler"
        }
    }

    companion object {

        private val SELFMSGWHAT_TAKEPHOTO_SUCCESS = 1
        private val SELFMSGWHAT_TAKEPHOTO_FAILED = 2
    }
}
