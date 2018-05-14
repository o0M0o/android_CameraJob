package com.wxm.camerajob.ui.Utility.Help

import android.content.Intent
import android.os.Bundle

import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.utility.ContextUtil

import wxm.androidutil.ExActivity.BaseAppCompatActivity
import wxm.androidutil.util.UtilFun

/**
 * UI for help
 */
class ACHelp : BaseAppCompatActivity() {

    protected fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        ContextUtil.instance.addActivity(this)
    }

    protected fun leaveActivity() {
        val ret_data = GlobalDef.INTRET_USR_LOGOUT

        val data = Intent()
        setResult(ret_data, data)
        finish()
    }

    protected fun initFrgHolder() {
        LOG_TAG = "ACJobCreate"
        mFGHolder = FrgHelp()

        // load help html
        val it = getIntent()
        val help_type = it.getStringExtra(GlobalDef.STR_HELP_TYPE)
        if (!UtilFun.StringIsNullOrEmpty(help_type)) {
            val arg = Bundle()
            arg.putString(GlobalDef.STR_HELP_TYPE, help_type)
            mFGHolder.setArguments(arg)
        }
    }
}
