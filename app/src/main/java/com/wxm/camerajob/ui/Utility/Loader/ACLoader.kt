package com.wxm.camerajob.ui.Utility.Loader

import android.Manifest.permission.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.wxm.camerajob.R
import com.wxm.camerajob.ui.Job.JobShow.ACJobShow
import com.wxm.camerajob.utility.ContextUtil
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
        ContextUtil.initUtil()
        ContextUtil.addActivity(this)

        startActivityForResult(Intent(this, ACJobShow::class.java), 1)
    }

    /**
     * 申请APP需要的权限
     */
    private fun mayRequestPermission() {
        ArrayList<String>().let {
            if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                it.add(WRITE_EXTERNAL_STORAGE)
            }

            if (ContextCompat.checkSelfPermission(this, CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                it.add(CAMERA)
            }

            if (ContextCompat.checkSelfPermission(this, WAKE_LOCK)
                    != PackageManager.PERMISSION_GRANTED) {
                it.add(WAKE_LOCK)
            }

            it
        }.toTypedArray().let {
            if(it.isEmpty()) {
                jumpWorkActivity()
            } else  {
                ActivityCompat.requestPermissions(this, it, REQUEST_ALL)
            }

            Unit
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
                    String.format(Locale.CHINA, "由于缺少必须的权限(%s)，本APP无法运行!",
                            permissions[it]).let {
                        AlertDialog.Builder(this).setTitle("警告").setMessage(it)
                                .setCancelable(false)
                                .setPositiveButton("离开应用")
                                { _, _ -> finish() }
                                .create().show()
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
