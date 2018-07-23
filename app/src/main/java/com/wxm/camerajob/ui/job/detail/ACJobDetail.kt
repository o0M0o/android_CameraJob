package com.wxm.camerajob.ui.job.detail

import android.os.Bundle
import com.wxm.camerajob.data.define.GlobalDef
import wxm.androidutil.improve.let1
import wxm.androidutil.ui.activity.ACSwitcherActivity

/**
 * slide show ui for job
 */
class ACJobDetail : ACSwitcherActivity<FrgJobDetail>() {
    override fun setupFragment(): MutableList<FrgJobDetail> {
        val ret = ArrayList<FrgJobDetail>()
        val id = intent!!.getIntExtra(KEY_JOB_ID, GlobalDef.INT_INVALID_ID)
        val path = intent!!.getStringExtra(KEY_JOB_DIR)
        if (!(GlobalDef.INT_INVALID_ID == id && null == path)) {
            FrgJobDetail().apply {
                arguments = Bundle().apply {
                    if (GlobalDef.INT_INVALID_ID != id) {
                        putInt(KEY_JOB_ID, id)
                    } else {
                        putString(KEY_JOB_DIR, path)
                    }
                }
            }.let1 {
                ret.add(it)
            }
        }

        return ret
    }

    companion object {
        const val KEY_JOB_ID = "job_id"
        const val KEY_JOB_DIR = "job_dir"
    }
}


