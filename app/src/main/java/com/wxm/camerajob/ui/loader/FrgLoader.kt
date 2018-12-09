package com.wxm.camerajob.ui.loader

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.widget.TextView
import com.wxm.camerajob.R
import com.wxm.camerajob.ui.welcome.ACWelcome
import com.wxm.camerajob.utility.AppUtil
import kotterknife.bindView
import okhttp3.internal.Util.contains
import wxm.androidutil.app.AppBase
import wxm.androidutil.improve.doJudge
import wxm.androidutil.improve.let1
import wxm.androidutil.ui.frg.FrgSupportBaseAdv
import java.util.ArrayList

/**
 * slide fragment for job
 * Created by WangXM on 2016/10/14.
 */
class FrgLoader : FrgSupportBaseAdv() {
    private val mTVInfo: TextView by bindView(R.id.tv_info)
    private val mBTRequest: TextView by bindView(R.id.bt_request)

    override fun isUseEventBus(): Boolean = false
    override fun getLayoutID(): Int = R.layout.pg_loader

    override fun initUI(savedInstanceState: Bundle?) {
        mBTRequest.setOnClickListener { _ ->
            mayRequestPermission()
        }

        loadUI(savedInstanceState)
    }

    override fun loadUI(savedInstanceState: Bundle?) {
        super.loadUI(savedInstanceState)

        ArrayList<String>().apply {
            if (!AppBase.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            if (!AppBase.checkPermission(Manifest.permission.CAMERA)) {
                add(Manifest.permission.CAMERA)
            }

            if (!AppBase.checkPermission(Manifest.permission.WAKE_LOCK)) {
                add(Manifest.permission.WAKE_LOCK)
            }
        }.let1 { arrIt ->
            if(arrIt.isNotEmpty())    {
                when {
                    arrIt.first().contains("CAMERA") -> "相机"
                    arrIt.first().contains("STORAGE") -> "读写数据"
                    else ->  arrIt.first()
                }.let1 {
                    mTVInfo.text = getString(R.string.info_permission, it)
                }
            } else  {
                (activity as ACLoader).jumpWorkActivity()
            }
        }
    }


    /// PRIVATE START
    /**
     * request permission for app
     */
    private fun mayRequestPermission() {
        ArrayList<String>().apply {
            if (!AppBase.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            if (!AppBase.checkPermission(Manifest.permission.CAMERA)) {
                add(Manifest.permission.CAMERA)
            }

            if (!AppBase.checkPermission(Manifest.permission.WAKE_LOCK)) {
                add(Manifest.permission.WAKE_LOCK)
            }
        }.let1 {
            if(it.isNotEmpty())    {
                ActivityCompat.requestPermissions(activity!!, it.toTypedArray(),
                        ACLoader.REQUEST_ALL)
            } else  {
                (activity as ACLoader).jumpWorkActivity()
            }
        }
    }
    /// PRIVATE END
}

