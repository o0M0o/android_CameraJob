package com.wxm.camerajob.utility

import android.app.AlertDialog
import android.content.Context

/**
 * for easy use dialog
 * @author      WangXM
 * @version     create：2018/5/14
 */
object DlgUtility {
    /**
     * get text presentation for [obj] in [ct]
     */
    private fun anyToCharSequence(ct:Context, obj: Any): CharSequence   {
        return when(obj)   {
            is CharSequence -> obj
            is Int -> ct.getString(obj)
            else -> obj.toString()
        }
    }

    /**
     * show alert dialog with [title] and [msg]
    fun showAlert(home: Context, title: Any, msg: Any) {
        AlertDialog.Builder(home)
                .setTitle(anyToCharSequence(home, title))
                .setMessage(anyToCharSequence(home, msg))
                .create().show()
    }
    */

    /**
     * get alert dialog instance with [title] and [msg]
     */
    fun showAlert(home: Context, title: Any, msg: Any,
                  oper: (dlg: AlertDialog.Builder) -> Unit = {}) {
        return AlertDialog.Builder(home)
                .setTitle(anyToCharSequence(home, title))
                .setMessage(anyToCharSequence(home, msg))
                .apply { oper(this) }
                .create().show()
    }
}