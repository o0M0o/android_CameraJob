package com.wxm.camerajob.ui.Job.JobCreate

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.CameraJob
import com.wxm.camerajob.data.define.EAction
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.data.define.PreferencesUtil
import com.wxm.camerajob.utility.ContextUtil
import com.wxm.camerajob.ui.Camera.CameraSetting.ACCameraSetting

import wxm.androidutil.ExActivity.BaseAppCompatActivity

/**
 * UI for create job
 */
class ACJobCreate : BaseAppCompatActivity() {

    private val mFRGJobCreate = FrgJobCreate.newInstance()


    protected fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        ContextUtil.instance.addActivity(this)
    }

    protected fun leaveActivity() {
        val ret_data = GlobalDef.INTRET_USR_LOGOUT

        val data = Intent()
        setResult(ret_data, data)
        finish()
    }

    protected fun initFrgHolder() {
        LOG_TAG = "ACJobCreate"
        mFGSupportHolder = mFRGJobCreate
    }


    fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        val inflater = getMenuInflater()
        inflater.inflate(R.menu.acm_accpet_giveup, menu)
        return true
    }


    protected fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
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


    fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_accept -> {
                val cj = mFRGJobCreate.onAccept()
                if (null != cj) {
                    val data = Intent()
                    data.putExtra(EAction.LOAD_JOB.actName, cj)
                    setResult(GlobalDef.INTRET_CAMERAJOB_ACCEPT, data)
                    finish()
                }
            }

            R.id.mi_giveup -> {
                val data = Intent()
                setResult(GlobalDef.INTRET_CAMERAJOB_GIVEUP, data)
                finish()
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
        val home = this
        if (!PreferencesUtil.checkCameraIsSet()) {
            val builder = AlertDialog.Builder(home)
            builder.setTitle("相机未设置，需要先设置相机")
            builder.setPositiveButton("确 定") { dialog, which ->
                val data = Intent(home, ACCameraSetting::class.java)
                startActivityForResult(data, REQUEST_SET_CAMERA)
            }

            val dialog = builder.create()
            dialog.show()
        }
    }

    companion object {
        private val REQUEST_SET_CAMERA = 123
    }
}






