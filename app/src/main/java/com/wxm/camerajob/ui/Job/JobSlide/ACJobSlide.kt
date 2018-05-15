package com.wxm.camerajob.ui.Job.JobSlide

import android.content.Intent

import com.wxm.camerajob.data.define.EAction
import com.wxm.camerajob.data.define.GlobalDef

import java.util.LinkedList

import wxm.androidutil.ExActivity.BaseAppCompatActivity
import wxm.androidutil.util.FileUtil
import wxm.androidutil.util.UtilFun

/**
 * slide show ui for job
 */
class ACJobSlide : BaseAppCompatActivity() {

    protected fun leaveActivity() {
        val ret_data = GlobalDef.INTRET_USR_LOGOUT

        val data = Intent()
        setResult(ret_data, data)
        finish()
    }

    protected fun initFrgHolder() {
        LOG_TAG = "ACJobSlide"

        val it = getIntent() ?: return

        val sp = it!!.getStringExtra(EAction.LOAD_PHOTO_DIR.actName)
        if (UtilFun.StringIsNullOrEmpty(sp))
            return

        val ll_photo = FileUtil.getDirFiles(sp, "jpg", false)
        if (UtilFun.ListIsNullOrEmpty(ll_photo))
            return

        mFGHolder = FrgJobSlide.newInstance(ll_photo)
    }
}


