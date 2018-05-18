package com.wxm.camerajob.ui.Utility.Setting

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.utility.ContextUtil
import com.wxm.camerajob.utility.DlgUtility
import wxm.androidutil.Switcher.ACSwitcherActivity

/**
 * for setting
 */
class ACSetting : ACSwitcherActivity<FrgSetting>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextUtil.addActivity(this)
    }

    override fun setupFragment(p0: Bundle?) {
        addFragment(FrgSetting())
    }

    override fun leaveActivity() {
        setResult(GlobalDef.INTRET_USR_LOGOUT, Intent())
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.acm_save_giveup, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_save -> {
                if (FrgSetting.PAGE_IDX_MAIN != hotFragment.currentItem) {
                    hotFragment.currentPage!!.let {
                        if (it.isSettingDirty) {
                            DlgUtility.showAlert(this, R.string.warn, "是否保存更改的配置?",
                                    { dlg ->
                                        dlg.setPositiveButton("是") { _, _ ->
                                            it.updateSetting()
                                            changePage(FrgSetting.PAGE_IDX_MAIN)
                                        }
                                        dlg.setNegativeButton("否") { _, _ ->
                                            changePage(FrgSetting.PAGE_IDX_MAIN)
                                        }
                                    }
                            )
                        } else {
                            changePage(FrgSetting.PAGE_IDX_MAIN)
                        }
                    }
                } else {
                    setResult(GlobalDef.INTRET_SURE, Intent())
                    finish()
                }
            }

            R.id.mi_giveup -> {
                if (FrgSetting.PAGE_IDX_MAIN != hotFragment.currentItem) {
                    changePage(FrgSetting.PAGE_IDX_MAIN)
                } else {
                    setResult(GlobalDef.INTRET_GIVEUP, Intent())
                    finish()
                }
            }

            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    /**
     * 切换到新页面
     * @param new_page 新页面postion
     */
    fun changePage(new_page: Int) {
        hotFragment.currentItem = new_page
    }
}
