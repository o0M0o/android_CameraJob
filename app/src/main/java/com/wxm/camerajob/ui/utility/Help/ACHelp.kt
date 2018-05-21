package com.wxm.camerajob.ui.Utility.Help

import android.content.Intent
import android.os.Bundle
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.utility.ContextUtil
import wxm.androidutil.Switcher.ACSwitcherActivity

/**
 * UI for help
 */
class ACHelp : ACSwitcherActivity<FrgHelp>() {
    override fun setupFragment(p0: Bundle?) {
        addFragment(FrgHelp().apply {
            intent.getStringExtra(GlobalDef.STR_HELP_TYPE)?.let {
                arguments = Bundle().apply { putString(GlobalDef.STR_HELP_TYPE, it) }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextUtil.addActivity(this)
    }

    override fun leaveActivity() {
        setResult(GlobalDef.INTRET_USR_LOGOUT, Intent())
        finish()
    }
}
