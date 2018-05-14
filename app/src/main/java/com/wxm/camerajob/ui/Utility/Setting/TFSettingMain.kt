package com.wxm.camerajob.ui.Utility.Setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.wxm.camerajob.R

import butterknife.ButterKnife
import butterknife.OnClick

/**
 * UI for main setting
 * Created by WangXM on 2016/10/10.
 */
class TFSettingMain : TFSettingBase() {

    override fun inflaterView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle): View {
        LOG_TAG = "TFSettingMain"
        val rootView = inflater.inflate(R.layout.frg_setting_main, container, false)
        ButterKnife.bind(this, rootView)
        return rootView
    }

    override fun initUiComponent(view: View) {}

    override fun loadUI() {}

    @OnClick(R.id.rl_setting_check_version, R.id.rl_setting_directory, R.id.rl_setting_camera)
    fun onRelativeLayoutClick(v: View) {
        val vid = v.id
        when (vid) {
            R.id.rl_setting_check_version -> {
                toPageByIdx(FrgSetting.PAGE_IDX_CHECK_VERSION)
            }

            R.id.rl_setting_directory -> {
                toPageByIdx(FrgSetting.PAGE_IDX_DIRECTORY)
            }

            R.id.rl_setting_camera -> {
                toPageByIdx(FrgSetting.PAGE_IDX_CAMERA)
            }
        }
    }

    override fun updateSetting() {
        if (isSettingDirty) {
            isSettingDirty = false
        }
    }
}
