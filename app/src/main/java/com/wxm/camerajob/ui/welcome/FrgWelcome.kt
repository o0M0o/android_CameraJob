package com.wxm.camerajob.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.wxm.camerajob.R
import com.wxm.camerajob.ui.job.create.ACJobCreate
import com.wxm.camerajob.ui.base.PageBase
import com.wxm.camerajob.ui.welcome.page.PgJobShow
import com.wxm.camerajob.ui.welcome.page.PgSetting
import wxm.androidutil.improve.let1
import wxm.androidutil.log.TagLog
import wxm.androidutil.ui.frg.FrgSupportBaseAdv
import wxm.androidutil.ui.frg.FrgSupportSwitcher
import wxm.androidutil.ui.view.EventHelper
import wxm.uilib.IconButton.IconButton

/**
 * for welcome
 * Created by WangXM on 2016/12/7.
 */
class FrgWelcome : FrgSupportSwitcher<FrgSupportBaseAdv>() {
    // for page
    private val mPGJobShow = PgJobShow()
    private val mPGSetting = PgSetting()

    init {
        setupFrgID(R.layout.ac_welcome, R.id.fl_page)
    }

    override fun setupFragment(savedInstanceState: Bundle?) {
        addChildFrg(mPGJobShow)
        addChildFrg(mPGSetting)
    }


    override fun initUI(savedInstanceState: Bundle?) {
        super.initUI(savedInstanceState)

        EventHelper.setOnClickOperator(view!!,
                intArrayOf(R.id.ib_job_show, R.id.ib_add_job, R.id.ib_setting),
                ::switchToHotPage)
        switchToHotPage(view!!.findViewById(R.id.ib_job_show))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(REQUEST_CODE_JOB_CREATE == requestCode)   {
            setHotPage(R.id.ib_job_show)
            mPGJobShow.reloadUI()
        }
    }

    fun leaveFrg(): Boolean {
        if ((hotPage as PageBase).leavePage()) {
            if (hotPage is PgJobShow) {
                return true
            } else {
                switchToHotPage(view!!.findViewById(R.id.ib_job_show))
            }

        }

        return false
    }

    private fun switchToHotPage(v: View)    {
        (v as IconButton).let1 {
            if (!it.isHot) {
                activity!!.title = it.actName
                setHotPage(v.id)
            }
        }
    }

    private fun setHotPage(vId: Int) {
        val setHot = { id: Int ->
            intArrayOf(R.id.ib_job_show, R.id.ib_add_job, R.id.ib_setting).forEach {
                (view!!.findViewById<IconButton>(it)).setColdOrHot(it == id)
            }
        }

        setHot(vId)
        when (vId) {
            R.id.ib_job_show -> {
                switchToPage(mPGJobShow)
            }

            R.id.ib_add_job -> {
                startActivityForResult(Intent(context, ACJobCreate::class.java), REQUEST_CODE_JOB_CREATE)
            }

            R.id.ib_setting -> {
                switchToPage(mPGSetting)
            }

            else -> {
                TagLog.e("$vId is not support!!")
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_JOB_CREATE = 1
    }
}
