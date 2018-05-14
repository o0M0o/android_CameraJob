package com.wxm.camerajob.ui.Utility.Setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.wxm.camerajob.R
import com.wxm.camerajob.utility.ContextUtil


import java.util.Locale

import butterknife.BindView
import butterknife.ButterKnife

/**
 * UI for directory setting
 * Created by WangXM on 2016/10/10.
 */
class TFSettingDirectory : TFSettingBase() {
    @BindView(R.id.tv_show)
    internal var mTVShow: TextView? = null

    override fun inflaterView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle): View {
        LOG_TAG = "TFSettingDirectory"
        val rootView = inflater.inflate(R.layout.frg_setting_directory, container, false)
        ButterKnife.bind(this, rootView)
        return rootView
    }

    override fun initUiComponent(view: View) {
        val s = String.format(Locale.CHINA, "照片根目录 : %s",
                ContextUtil.instance.appPhotoRootDir)
        mTVShow!!.text = s
    }

    override fun loadUI() {}

    override fun updateSetting() {
        if (isSettingDirty) {
            isSettingDirty = false
        }
    }
}
