package com.wxm.camerajob.ui.loader

import android.Manifest.permission.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import com.wxm.camerajob.R
import com.wxm.camerajob.ui.welcome.ACWelcome
import com.wxm.camerajob.utility.AppUtil
import wxm.androidutil.app.AppBase
import wxm.androidutil.improve.let1
import wxm.androidutil.ui.dialog.DlgAlert
import java.util.*

/**
 * first activity for app
 * apply permission then jump to fist work activity
 */
class ACLoader : AppCompatActivity() {
    /**
     * 如果有权限，则直接初始化
     * 如果无权限，则申请权限后再进行初始化
     * @param savedInstanceState   param
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_loader)

        mayRequestPermission()
    }

    /**
     * 跳转到工作首界面
     */
    private fun jumpWorkActivity() {
        AppUtil.initUtil()
        AppUtil.addActivity(this)

        startActivityForResult(Intent(this, ACWelcome::class.java), 1)
    }

    /**
     * 申请APP需要的权限
     */
    private fun mayRequestPermission() {
        ArrayList<String>().let {
            if (!AppBase.checkPermission(WRITE_EXTERNAL_STORAGE)) {
                it.add(WRITE_EXTERNAL_STORAGE)
            }

            if (!AppBase.checkPermission(CAMERA)) {
                it.add(CAMERA)
            }

            if (!AppBase.checkPermission(WAKE_LOCK)) {
                it.add(WAKE_LOCK)
            }

            it
        }.toTypedArray().let1 {
            if (it.isEmpty()) {
                jumpWorkActivity()
            } else {
                ActivityCompat.requestPermissions(this, it, REQUEST_ALL)
            }
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     * 若权限齐全则初始化activity, 否则弹出提示框然后退出APP
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_ALL) {
            grantResults.indices.forEach {
                if (grantResults[it] != PackageManager.PERMISSION_GRANTED) {
                    DlgAlert.showAlert(this, R.string.warn,
                            "由于缺少必须的权限(${permissions[it]})，本APP无法运行!")
                    { builder ->
                        builder.setCancelable(false)
                        builder.setPositiveButton("离开应用")
                        { _, _ -> finish() }
                    }

                    return
                }
            }

            jumpWorkActivity()
        }
    }

    companion object {
        private const val REQUEST_ALL = 99
    }
}
