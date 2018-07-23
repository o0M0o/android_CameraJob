package com.wxm.camerajob.ui.job.slide

import android.content.Intent
import com.wxm.camerajob.data.define.EAction
import com.wxm.camerajob.data.define.GlobalDef
import wxm.androidutil.ui.activity.ACSwitcherActivity
import wxm.androidutil.util.FileUtil

/**
 * slide show ui for job
 */
class ACJobSlide : ACSwitcherActivity<FrgJobSlide>() {
    override fun leaveActivity() {
        setResult(GlobalDef.INTRET_USR_LOGOUT, Intent())
        super.leaveActivity()
    }

    override fun setupFragment(): MutableList<FrgJobSlide> {
        val ret = ArrayList<FrgJobSlide>()
        intent?.let {
            it.getStringExtra(EAction.LOAD_PHOTO_DIR.actName)?.let {
                FileUtil.getDirFiles(it, "jpg", false).let {
                    ret.add(FrgJobSlide.newInstance(it))
                }
            }
        }

        return ret
    }
}


