package com.wxm.camerajob.ui.Camera.CameraSetting

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.ui.Utility.Setting.TFSettingCamera
import com.wxm.camerajob.utility.ContextUtil

import wxm.androidutil.ExActivity.BaseAppCompatActivity

/**
 * UI for camera setting
 */
class ACCameraSetting : BaseAppCompatActivity() {
    protected fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        ContextUtil.instance.addActivity(this)
    }

    protected fun leaveActivity() {
        val data = Intent()
        setResult(GlobalDef.INTRET_USR_LOGOUT, data)
        finish()
    }

    protected fun initFrgHolder() {
        LOG_TAG = "ACCameraSetting"
        mFGSupportHolder = TFSettingCamera()
    }

    fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        val inflater = getMenuInflater()
        inflater.inflate(R.menu.acm_accpet_giveup, menu)
        return true
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_accept -> {
                val data = Intent()
                val mTFCamera = mFGSupportHolder as TFSettingCamera
                if (mTFCamera.isSettingDirty) {
                    val alertDialog = AlertDialog.Builder(this).setTitle("配置已经更改").setMessage("是否保存更改的配置?").setPositiveButton("是") { dialog, which ->
                        mTFCamera.updateSetting()
                        setResult(GlobalDef.INTRET_CS_ACCEPT, data)
                        finish()
                    }.setNegativeButton("否") { dialog, which ->
                        setResult(GlobalDef.INTRET_CS_GIVEUP, data)
                        finish()
                    }.create()
                    alertDialog.show()
                } else {
                    finish()
                }
            }

            R.id.mi_giveup -> {
                val data = Intent()
                setResult(GlobalDef.INTRET_CS_GIVEUP, data)
                finish()
            }

            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }
}
