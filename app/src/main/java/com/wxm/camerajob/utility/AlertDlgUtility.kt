package com.wxm.camerajob.utility

import android.app.Activity
import android.app.AlertDialog

/**
 * @author      WangXM
 * @version     create：2018/5/14
 */
object AlertDlgUtility {
    fun showAlert(home: Activity, szTitle: String, szMsg: String) {
        AlertDialog.Builder(home)
                .setTitle(szTitle).setMessage(szMsg)
                .create().show()
    }
}