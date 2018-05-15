package com.wxm.camerajob.ui.Camera.CameraPreview

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi

import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.utility.ContextUtil

import wxm.androidutil.ExActivity.BaseAppCompatActivity

/**
 * camera preview
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class ACCameraPreview : BaseAppCompatActivity() {

    protected fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        ContextUtil.instance.addActivity(this)
    }

    protected fun leaveActivity() {
        val data = Intent()
        setResult(GlobalDef.INTRET_USR_LOGOUT, data)
        finish()
    }

    protected fun initFrgHolder() {
        LOG_TAG = "ACCameraPreview"
        mFGHolder = FrgCameraPreview.newInstance()
    }

    /// BEGIN PRIVATE
    /// END PRIVATE
}
