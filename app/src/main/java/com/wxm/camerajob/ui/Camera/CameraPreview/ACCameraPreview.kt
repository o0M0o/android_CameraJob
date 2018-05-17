package com.wxm.camerajob.ui.Camera.CameraPreview

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi

import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.utility.ContextUtil

import wxm.androidutil.Switcher.ACSwitcherActivity

/**
 * camera preview
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class ACCameraPreview : ACSwitcherActivity<FrgCameraPreview>() {
    override fun setupFragment(p0: Bundle?) {
        addFragment(FrgCameraPreview.newInstance())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextUtil.addActivity(this)
    }

    override fun leaveActivity() {
        setResult(GlobalDef.INTRET_USR_LOGOUT, Intent())
        finish()
    }
    /// BEGIN PRIVATE
    /// END PRIVATE
}
