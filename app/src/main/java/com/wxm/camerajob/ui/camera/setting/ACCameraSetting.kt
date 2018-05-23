package com.wxm.camerajob.ui.camera.setting

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.ui.utility.Setting.TFSettingCamera
import com.wxm.camerajob.ui.utility.dialog.DlgUtility
import com.wxm.camerajob.utility.context.ContextUtil
import wxm.androidutil.Switcher.ACSwitcherActivity

/**
 * UI for camera setting
 */
open class ACCameraSetting : ACSwitcherActivity<TFSettingCamera>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextUtil.addActivity(this)
    }

    override fun setupFragment(p0: Bundle?) {
        addFragment(TFSettingCamera())
    }

    override fun leaveActivity() {
        setResult(GlobalDef.INTRET_CS_GIVEUP, Intent())
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.acm_accpet_giveup, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val home = this
        when (item.itemId) {
            R.id.mi_accept -> {
                val hf = hotFragment!!
                if (hf.isSettingDirty) {
                    DlgUtility.showAlert(this, R.string.hint, "是否保存更改的配置?",
                            { dlg ->
                                dlg.setPositiveButton("是") { _, _ ->
                                    hf.updateSetting()
                                    home.leaveActivity()
                                }.setNegativeButton("否") { _, _ ->
                                }
                            })
                } else {
                    home.leaveActivity()
                }
            }

            R.id.mi_giveup -> {
                home.leaveActivity()
            }

            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }
}
