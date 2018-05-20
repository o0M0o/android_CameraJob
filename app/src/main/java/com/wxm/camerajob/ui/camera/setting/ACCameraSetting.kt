package com.wxm.camerajob.ui.camera.setting

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.ui.Utility.Setting.TFSettingCamera
import com.wxm.camerajob.utility.ContextUtil
import com.wxm.camerajob.utility.DlgUtility
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
        when (item.itemId) {
            R.id.mi_accept -> {
                val data = Intent()
                hotFragment.let {
                    if (it.isSettingDirty) {
                        DlgUtility.showAlert(this, R.string.hint, "是否保存更改的配置?",
                                { dlg ->
                                    dlg.setPositiveButton("是") { _, _ ->
                                        it.updateSetting()
                                        setResult(GlobalDef.INTRET_CS_ACCEPT, data)
                                        finish()
                                    }.setNegativeButton("否") { _, _ ->
                                        leaveActivity()
                                    }
                                })
                    } else {
                        finish()
                    }
                }
            }

            R.id.mi_giveup -> {
                leaveActivity()
            }

            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }
}