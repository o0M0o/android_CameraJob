package com.wxm.camerajob.ui.Job.create

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.data.define.PreferencesUtil
import com.wxm.camerajob.ui.camera.setting.ACCameraSetting
import com.wxm.camerajob.utility.ContextUtil
import com.wxm.camerajob.utility.DlgUtility
import wxm.androidutil.Switcher.ACSwitcherActivity

/**
 * UI for create job
 */
class ACJobCreate : ACSwitcherActivity<FrgJobCreate>() {
    override fun setupFragment(p0: Bundle?) {
        addFragment(FrgJobCreate())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextUtil.addActivity(this)
    }

    override fun leaveActivity() {
        setResult(GlobalDef.INTRET_USR_LOGOUT, Intent())
        finish()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
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
                Log.i(LOG_TAG, "不处理的 resultCode = $resultCode")
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_accept -> {
                hotFragment.onAccept().let {
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
            val home = this
            DlgUtility.showAlert(this, R.string.warn, "相机未设置，需要先设置相机",
                    { dlg ->
                        dlg.setPositiveButton("确 定") { _, _ ->
                            val data = Intent(home, ACCameraSetting::class.java)
                            startActivityForResult(data, REQUEST_SET_CAMERA)
                        }
                    })
        }
    }

    companion object {
        private const val REQUEST_SET_CAMERA = 123
    }
}






