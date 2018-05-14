package com.wxm.camerajob.ui.Test.Camera

import android.app.FragmentTransaction
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button

import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.GlobalDef

class ACCameraTest : AppCompatActivity() {
    private val mCamearFrag = CameraFragment.newInstance()
    private var mBtActiveFrontCamear: Button? = null
    private var mBtActiveBackCamear: Button? = null
    private var mBtTakePhoto: Button? = null
    private var mBtCameraClose: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_camera_test)

        if (null == savedInstanceState) {
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.acfl_test_camera_preview, mCamearFrag)
            transaction.commit()
        }

        mBtActiveFrontCamear = findViewById<View>(R.id.acbt_test_frontcamera_active)
        mBtActiveBackCamear = findViewById<View>(R.id.acbt_test_backcamera_active)
        mBtTakePhoto = findViewById<View>(R.id.acbt_test_takephoto)
        mBtCameraClose = findViewById<View>(R.id.acbt_test_camera_close)

        activeButton(mBtTakePhoto!!, false)
        activeButton(mBtCameraClose!!, false)
        mBtActiveFrontCamear!!.setTextColor(Color.GRAY)
        mBtActiveBackCamear!!.setTextColor(Color.GRAY)

        mBtActiveFrontCamear!!.setOnClickListener { v ->
            mCamearFrag.ActiveFrontCamera()
            activeButton(mBtCameraClose!!, true)
            activeButton(mBtTakePhoto!!, true)

            mBtActiveFrontCamear!!.setTextColor(Color.BLACK)
            mBtActiveBackCamear!!.setTextColor(Color.GRAY)
        }

        mBtActiveBackCamear!!.setOnClickListener { v ->
            mCamearFrag.ActiveBackCamera()
            activeButton(mBtCameraClose!!, true)
            activeButton(mBtTakePhoto!!, true)

            mBtActiveFrontCamear!!.setTextColor(Color.GRAY)
            mBtActiveBackCamear!!.setTextColor(Color.BLACK)
        }

        mBtCameraClose!!.setOnClickListener { v ->
            mCamearFrag.CloseCamera()
            activeButton(mBtTakePhoto!!, false)

            mBtActiveFrontCamear!!.setTextColor(Color.GRAY)
            mBtActiveBackCamear!!.setTextColor(Color.GRAY)
            mBtCameraClose!!.setTextColor(Color.GRAY)
        }

        mBtTakePhoto!!.setOnClickListener { v -> mCamearFrag.TakePhoto() }
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
        val inflater = menuInflater
        inflater.inflate(R.menu.acm_leave, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_leave -> {
                val data = Intent()
                setResult(GlobalDef.INTRET_CS_GIVEUP, data)
                finish()
            }

            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }
}
