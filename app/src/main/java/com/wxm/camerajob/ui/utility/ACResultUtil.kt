package com.wxm.camerajob.ui.utility

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import wxm.androidutil.improve.let1

/**
 * @author      WangXM
 * @version     create：2018/7/7
 */
@Suppress("unused, MemberVisibilityCanBePrivate")
object ACResultUtil {
    const val RET_ERROR = -1

    const val RET_CANCEL = 0
    const val RET_OK = 1

    private const val RET_TAG = "ret_tag"

    fun putResult(ret: Intent, obj: Parcelable)  {
        ret.putExtra(RET_TAG, obj)
    }

    fun getResult(ret:Intent) : Parcelable? {
        return ret.getParcelableExtra(RET_TAG)
    }

    fun doFinish(ac:Activity, ret:Int)    {
        doFinish(ac, ret, null)
    }

    fun doFinish(ac:Activity, ret:Int, obj:Parcelable?)    {
        Intent().apply {
            if(null != obj) {
                putResult(this, obj)
            }
        }.let1 {
            ac.setResult(ret, it)
        }

        ac.finish()
    }
}