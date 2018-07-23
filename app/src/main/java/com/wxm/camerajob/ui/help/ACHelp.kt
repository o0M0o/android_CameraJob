package com.wxm.camerajob.ui.help

import android.content.Intent
import android.os.Bundle
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.utility.AppUtil
import wxm.androidutil.ui.activity.ACSwitcherActivity

/**
 * UI for help
 */
class ACHelp : ACSwitcherActivity<FrgHelp>() {
    override fun setupFragment(): MutableList<FrgHelp> {
        return  ArrayList<FrgHelp>().apply {
            add(FrgHelp().apply {
                intent.getStringExtra(GlobalDef.STR_HELP_TYPE)?.let {
                    arguments = Bundle().apply { putString(GlobalDef.STR_HELP_TYPE, it) }
                }
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppUtil.addActivity(this)
    }

    override fun leaveActivity() {
        setResult(GlobalDef.INTRET_USR_LOGOUT, Intent())
        super.leaveActivity()
    }
}
