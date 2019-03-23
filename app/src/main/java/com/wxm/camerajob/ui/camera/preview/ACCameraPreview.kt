package com.wxm.camerajob.ui.camera.preview

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi

import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.App
import wxm.androidutil.ui.activity.ACSwitcherActivity


/**
 * camera preview
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class ACCameraPreview : ACSwitcherActivity<FrgCameraPreview>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.addActivity(this)
    }

    override fun leaveActivity() {
        setResult(GlobalDef.INTRET_USR_LOGOUT, Intent())
        super.leaveActivity()
    }
    /// BEGIN PRIVATE
    /// END PRIVATE
}
