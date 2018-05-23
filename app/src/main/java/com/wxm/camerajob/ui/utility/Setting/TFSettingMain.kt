package com.wxm.camerajob.ui.utility.Setting

import android.os.Bundle
import android.view.View

import com.wxm.camerajob.R

import com.wxm.camerajob.ui.base.EventHelper

/**
 * UI for main setting
 * Created by WangXM on 2016/10/10.
 */
class TFSettingMain : TFSettingBase() {
    override fun isUseEventBus(): Boolean = false
    override fun getLayoutID(): Int = R.layout.frg_setting_main

    override fun initUI(savedInstanceState: Bundle?) {
        EventHelper.setOnClickOperator(view!!,
                intArrayOf(R.id.rl_setting_check_version, R.id.rl_setting_directory, R.id.rl_setting_camera),
                ::onRelativeLayoutClick)
    }

    private fun onRelativeLayoutClick(v: View) {
        when (v.id) {
            R.id.rl_setting_check_version -> {
                (activity as ACSetting).switchToPageByType(TFSettingCheckVersion::class.java.name)
            }

            R.id.rl_setting_directory -> {
                (activity as ACSetting).switchToPageByType(TFSettingDirectory::class.java.name)
            }

            R.id.rl_setting_camera -> {
                (activity as ACSetting).switchToPageByType(TFSettingCamera::class.java.name)
            }
        }
    }

    override fun updateSetting() {
        isSettingDirty = false
    }
}
