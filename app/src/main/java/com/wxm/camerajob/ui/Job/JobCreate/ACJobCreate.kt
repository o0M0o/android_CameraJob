package com.wxm.camerajob.ui.Job.JobCreate

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.EAction
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.data.define.PreferencesUtil
import com.wxm.camerajob.ui.Camera.CameraSetting.ACCameraSetting
import com.wxm.camerajob.utility.ContextUtil
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
        ContextUtil.instance.addActivity(this)
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
                /*
                if(REQUEST_SET_CAMERA == requestCode) {
                    Message m = Message.obtain(GlobalContext.getMsgHandler(),
                            GlobalDef.MSG_TYPE_CAMERA_MODIFY);
                    m.obj = PreferencesUtil.loadCameraParam();
                    m.sendToTarget();
                }
                */
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
                hotFragment.onAccept()?.let {
                    setResult(GlobalDef.INTRET_CAMERAJOB_ACCEPT,
                            Intent().apply { putExtra(EAction.LOAD_JOB.actName, it) })
                    finish()
                }
            }

            R.id.mi_giveup -> {
                leaveActivity()
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
            AlertDialog.Builder(home)
                    .setTitle("相机未设置，需要先设置相机")
                    .setPositiveButton("确 定") { _, _ ->
                        val data = Intent(home, ACCameraSetting::class.java)
                        startActivityForResult(data, REQUEST_SET_CAMERA)
                    }
                    .create().show()
        }
    }

    companion object {
        private const val REQUEST_SET_CAMERA = 123
    }
}






