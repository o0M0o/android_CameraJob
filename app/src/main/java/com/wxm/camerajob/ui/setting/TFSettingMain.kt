package com.wxm.camerajob.ui.setting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.View

import com.wxm.camerajob.R
import com.wxm.camerajob.event.ChangePage
import com.wxm.camerajob.ui.dialog.DlgUsrMessage
import org.greenrobot.eventbus.EventBus
import wxm.androidutil.improve.let1
import wxm.androidutil.ui.dialog.DlgAlert
import wxm.androidutil.ui.dialog.DlgOKOrNOBase
import wxm.androidutil.ui.view.EventHelper


/**
 * UI for main setting
 * Created by WangXM on 2016/10/10.
 */
class TFSettingMain : TFSettingBase() {
    override fun isUseEventBus(): Boolean = false
    override fun getLayoutID(): Int = R.layout.pg_setting_main

    override fun initUI(savedInstanceState: Bundle?) {
        EventHelper.setOnClickOperator(view!!,
                intArrayOf(R.id.rl_setting_check_version, R.id.rl_setting_directory,
                        R.id.rl_setting_camera, R.id.rl_setting_suggestion),
                ::onRelativeLayoutClick)
    }

    private fun onRelativeLayoutClick(v: View) {
        when (v.id) {
            R.id.rl_setting_check_version -> {
                EventBus.getDefault().post(ChangePage(TFSettingCheckVersion::class.java.name))
            }

            R.id.rl_setting_directory -> {
                EventBus.getDefault().post(ChangePage(TFSettingDirectory::class.java.name))
            }

            R.id.rl_setting_camera -> {
                EventBus.getDefault().post(ChangePage(TFSettingCamera::class.java.name))
            }

            R.id.rl_setting_email -> {
                DlgAlert.showAlert(context!!, R.string.dlg_info,
                        "作者邮箱 : ${getString(R.string.contact_email)}"
                ) { b ->
                    b.setPositiveButton("直接发起邮件") {_, _ ->
                        sendEmailTOAuthor()
                    }
                    b.setNegativeButton(getString(R.string.cn_cancel)) {_, _ ->
                    }
                }
            }

            R.id.rl_setting_suggestion -> {
                DlgUsrMessage().let {
                    it.addDialogListener(object : DlgOKOrNOBase.DialogResultListener {
                        override fun onDialogPositiveResult(dialogFragment: DialogFragment) {}
                        override fun onDialogNegativeResult(dialogFragment: DialogFragment) {}
                    })

                    it.show(activity!!.supportFragmentManager, "send message")
                    Unit
                }
            }
        }
    }

    override fun updateSetting() {
        isSettingDirty = false
    }

    private fun sendEmailTOAuthor() {
        Intent(Intent.ACTION_SENDTO).let1 {
            it.data = Uri.parse("mailto:${getString(R.string.contact_email)}")
            it.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.contact_title))
            startActivity(it)
        }
    }
}
