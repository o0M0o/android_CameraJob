package com.wxm.camerajob.ui.camera.setting

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.ui.setting.TFSettingCamera
import com.wxm.camerajob.utility.AppUtil
import wxm.androidutil.ui.activity.ACSwitcherActivity
import wxm.androidutil.ui.dialog.DlgAlert

/**
 * UI for camera setting
 */
open class ACCameraSetting : ACSwitcherActivity<TFSettingCamera>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppUtil.addActivity(this)
    }

    override fun leaveActivity() {
        setResult(GlobalDef.INTRET_CS_GIVEUP, Intent())
        super.leaveActivity()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.acm_accpet_giveup, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_accept -> {
                val hf = hotFragment!!
                if (hf.isSettingDirty) {
                    DlgAlert.showAlert(this, R.string.dlg_hint, "是否保存更改的配置?"
                    ) { dlg ->
                        dlg.setPositiveButton("是") { _, _ ->
                            hf.updateSetting()
                            leaveActivity()
                        }.setNegativeButton("否") { _, _ ->
                        }
                    }
                } else {
                    leaveActivity()
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
