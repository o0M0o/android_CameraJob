package com.wxm.camerajob.utility

import android.app.AlertDialog
import android.content.Context

/**
 * @author      WangXM
 * @version     create：2018/5/14
 */
object AlertDlgUtility {
    fun showAlert(home: Context, szTitle: String, szMsg: String) {
        AlertDialog.Builder(home)
                .setTitle(szTitle).setMessage(szMsg)
                .create().show()
    }
}