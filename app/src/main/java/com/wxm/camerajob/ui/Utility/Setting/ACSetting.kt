package com.wxm.camerajob.ui.Utility.Setting

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.utility.ContextUtil

import wxm.androidutil.ExActivity.BaseAppCompatActivity
import wxm.androidutil.util.UtilFun

/**
 * UI for setting
 */
class ACSetting : BaseAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextUtil.instance.addActivity(this)
    }

    override fun leaveActivity() {
        val data = Intent()
        setResult(GlobalDef.INTRET_USR_LOGOUT, data)
        finish()
    }

    override fun initFrgHolder() {
        LOG_TAG = "ACSetting"
        mFGHolder = FrgSetting()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        val inflater = menuInflater
        inflater.inflate(R.menu.acm_save_giveup, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val fs = UtilFun.cast_t<FrgSetting>(mFGHolder)
        when (item.itemId) {
            R.id.mi_save -> {
                if (FrgSetting.PAGE_IDX_MAIN != fs.currentItem) {
                    val tb = fs.currentPage
                    if (tb!!.isSettingDirty) {
                        val alertDialog = AlertDialog.Builder(this).setTitle("配置已经更改").setMessage("是否保存更改的配置?").setPositiveButton("是") { dialog, which ->
                            tb.updateSetting()
                            changePage(FrgSetting.PAGE_IDX_MAIN)
                        }.setNegativeButton("否") { dialog, which -> changePage(FrgSetting.PAGE_IDX_MAIN) }.create()
                        alertDialog.show()
                    } else {
                        changePage(FrgSetting.PAGE_IDX_MAIN)
                    }
                } else {
                    val ret_data = GlobalDef.INTRET_SURE
                    val data = Intent()
                    setResult(ret_data, data)
                    finish()
                }
            }

            R.id.mi_giveup -> {
                if (FrgSetting.PAGE_IDX_MAIN != fs.currentItem) {
                    changePage(FrgSetting.PAGE_IDX_MAIN)
                } else {
                    val ret_data = GlobalDef.INTRET_GIVEUP
                    val data = Intent()
                    setResult(ret_data, data)
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
        val fs = UtilFun.cast_t<FrgSetting>(mFGHolder)
        fs.currentItem = new_page
    }
}
