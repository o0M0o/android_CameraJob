package com.wxm.camerajob.ui.base

import android.app.Activity
import android.content.Intent
import com.wxm.camerajob.data.define.GlobalDef

/**
 * @author      WangXM
 * @version     create：2018/5/28
 */
interface PageBase  {

    /**
     * if return true, means page can leave
     */
    fun leavePage(): Boolean

    @Suppress("unused")
    fun doLogout(ac:Activity) {
        ac.setResult(GlobalDef.INTRET_USR_LOGOUT, Intent())
        ac.finish()
    }
}