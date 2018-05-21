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
