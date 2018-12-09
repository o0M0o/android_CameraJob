package com.wxm.camerajob.ui.job.create

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.preference.PreferencesUtil
import com.wxm.camerajob.ui.camera.setting.ACCameraSetting
import com.wxm.camerajob.utility.AppUtil
import wxm.androidutil.log.TagLog
import wxm.androidutil.ui.activity.ACSwitcherActivity
import wxm.androidutil.ui.dialog.DlgAlert

/**
 * UI for create job
 */
class ACJobCreate : ACSwitcherActivity<FrgJobCreate>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppUtil.addActivity(this)
    }

    override fun leaveActivity() {
        setResult(GlobalDef.INTRET_GIVEUP, Intent())
        super.leaveActivity()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.acm_accpet_giveup, menu)
        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            GlobalDef.INTRET_CS_ACCEPT -> {
            }

            GlobalDef.INTRET_CS_GIVEUP -> {
                if (REQUEST_SET_CAMERA == requestCode) {
                    checkCamera()
                }
            }

            else -> {
                TagLog.i("不处理的 resultCode = $resultCode")
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_accept -> {
                hotFragment.onAccept().let {
                    setResult(GlobalDef.INTRET_SURE, Intent())
                    finish()
                }
            }

            R.id.mi_giveup -> {
                hotFragment.onCancel().let {
                    leaveActivity()
                }
            }

            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    /**
     * check camera
     * must set camera before use it
     */
    private fun checkCamera() {
        if (!PreferencesUtil.checkCameraIsSet()) {
            DlgAlert.showAlert(this, R.string.dlg_warn, "相机未设置，需要先设置相机"
            ) { dlg ->
                dlg.setPositiveButton("确 定") { _, _ ->
                    startActivityForResult(Intent(this, ACCameraSetting::class.java),
                            REQUEST_SET_CAMERA)
                }
            }
        }
    }

    companion object {
        private const val REQUEST_SET_CAMERA = 123
    }
}






