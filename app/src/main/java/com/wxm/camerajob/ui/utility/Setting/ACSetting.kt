package com.wxm.camerajob.ui.utility.Setting

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.utility.context.ContextUtil
import com.wxm.camerajob.ui.utility.dialog.DlgUtility
import wxm.androidutil.Switcher.ACSwitcherActivity

/**
 * for setting
 */
class ACSetting : ACSwitcherActivity<TFSettingBase>() {
    private val mTFMain = TFSettingMain()
    private val mTFDir = TFSettingDirectory()
    private val mTFVer = TFSettingCheckVersion()
    private val mTFCamera = TFSettingCamera()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextUtil.addActivity(this)
    }

    override fun setupFragment(p0: Bundle?) {
        addFragment(mTFMain)
        addFragment(mTFDir)
        addFragment(mTFVer)
        addFragment(mTFCamera)
    }

    override fun leaveActivity() {
        if (mTFMain !== hotFragment) {
            hotFragment.reloadUI()
            switchToFragment(mTFMain)
        } else {
            setResult(GlobalDef.INTRET_GIVEUP, Intent())
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.acm_save_giveup, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_save -> {
                if (mTFMain !== hotFragment) {
                    if (hotFragment.isSettingDirty) {
                        DlgUtility.showAlert(this, R.string.warn, "是否保存更改的配置?",
                                { dlg ->
                                    dlg.setPositiveButton("是") { _, _ ->
                                        hotFragment.updateSetting()
                                        switchToFragment(mTFMain)
                                    }
                                    dlg.setNegativeButton("否") { _, _ ->
                                        hotFragment.reloadUI()
                                        switchToFragment(mTFMain)
                                    }
                                }
                        )
                    } else {
                        switchToFragment(mTFMain)
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

    fun switchToPageByType(pageType: String) {
        when(pageType)  {
            TFSettingMain::class.java.name -> switchToFragment(mTFMain)
            TFSettingCheckVersion::class.java.name -> switchToFragment(mTFVer)
            TFSettingDirectory::class.java.name -> switchToFragment(mTFDir)
            TFSettingCamera::class.java.name -> switchToFragment(mTFCamera)
            else -> switchToFragment(mTFMain)
        }
    }
}
