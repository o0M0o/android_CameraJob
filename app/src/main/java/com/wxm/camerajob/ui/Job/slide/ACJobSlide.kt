package com.wxm.camerajob.ui.Job.slide

import android.content.Intent
import android.os.Bundle
import com.wxm.camerajob.data.define.EAction
import com.wxm.camerajob.data.define.GlobalDef
import wxm.androidutil.Switcher.ACSwitcherActivity
import wxm.androidutil.util.FileUtil

/**
 * slide show ui for job
 */
class ACJobSlide : ACSwitcherActivity<FrgJobSlide>() {
    override fun leaveActivity() {
        setResult(GlobalDef.INTRET_USR_LOGOUT, Intent())
        finish()
    }

    override fun setupFragment(p0: Bundle?) {
        intent?.let {
            it.getStringExtra(EAction.LOAD_PHOTO_DIR.actName)?.let {
                FileUtil.getDirFiles(it, "jpg", false)?.let {
                    addFragment(FrgJobSlide.newInstance(it))
                }
            }
        }
    }
}


