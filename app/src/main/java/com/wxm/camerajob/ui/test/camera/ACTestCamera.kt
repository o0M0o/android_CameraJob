package com.wxm.camerajob.ui.test.camera

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button

import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.GlobalDef
import kotterknife.bindView

class ACTestCamera : AppCompatActivity() {
    private val mCameraFrag = CameraFragment.newInstance()
    private val mBtActiveFrontCamera: Button by bindView(R.id.acbt_test_frontcamera_active)
    private val mBtActiveBackCamera: Button by bindView(R.id.acbt_test_backcamera_active)
    private val mBtTakePhoto: Button by bindView(R.id.acbt_test_takephoto)
    private val mBtCameraClose: Button by bindView(R.id.acbt_test_camera_close)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_camera_test)

        if (null == savedInstanceState) {
            fragmentManager.beginTransaction().apply {
                replace(R.id.acfl_test_camera_preview, mCameraFrag)
                commit()
            }
        }

        activeButton(mBtTakePhoto, false)
        activeButton(mBtCameraClose, false)
        mBtActiveFrontCamera.setTextColor(Color.GRAY)
        mBtActiveBackCamera.setTextColor(Color.GRAY)

        mBtActiveFrontCamera.setOnClickListener {
            mCameraFrag.activeFrontCamera()
            activeButton(mBtCameraClose, true)
            activeButton(mBtTakePhoto, true)

            mBtActiveFrontCamera.setTextColor(Color.BLACK)
            mBtActiveBackCamera.setTextColor(Color.GRAY)
        }

        mBtActiveBackCamera.setOnClickListener {
            mCameraFrag.activeBackCamera()
            activeButton(mBtCameraClose, true)
            activeButton(mBtTakePhoto, true)

            mBtActiveFrontCamera.setTextColor(Color.GRAY)
            mBtActiveBackCamera.setTextColor(Color.BLACK)
        }

        mBtCameraClose.setOnClickListener {
            mCameraFrag.closeCamera()
            activeButton(mBtTakePhoto, false)

            mBtActiveFrontCamera.setTextColor(Color.GRAY)
            mBtActiveBackCamera.setTextColor(Color.GRAY)
            mBtCameraClose.setTextColor(Color.GRAY)
        }

        mBtTakePhoto.setOnClickListener { mCameraFrag.takePhoto() }
    }

    // --Commented out by Inspection START (2016/6/27 23:14):
    //    /**
    //     * 检查手机是否存在相机
    //     * @param context 上下文
    //     * @return 若存在则返回true, 否则返回false
    //     */
    //    private boolean checkCameraHardware(Context context) {
    //        return context.getPackageManager().hasSystemFeature(
    //                PackageManager.FEATURE_CAMERA);
    //    }
    // --Commented out by Inspection STOP (2016/6/27 23:14)

    /**
     * 设置button的有效状态
     * @param bt        待设定的button
     * @param active    如果为true则button可以使用
     */
    private fun activeButton(bt: Button, active: Boolean) {
        bt.isClickable = active

        if (!active)
            bt.setTextColor(Color.GRAY)
        else
            bt.setTextColor(Color.BLACK)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.acm_leave, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_leave -> {
                setResult(GlobalDef.INTRET_CS_GIVEUP, Intent())
                finish()
            }

            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }
}
