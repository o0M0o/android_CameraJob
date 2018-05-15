package com.wxm.camerajob.ui.Utility.Setting

import android.os.Bundle
import android.widget.TextView

import com.wxm.camerajob.R
import com.wxm.camerajob.utility.ContextUtil

import java.util.Locale

import kotterknife.bindView

/**
 * UI for check version
 * Created by WangXM on 2016/10/10.
 */
class TFSettingCheckVersion : TFSettingBase() {

    private val mTVShow: TextView by bindView(R.id.tv_show)

    override fun isUseEventBus(): Boolean = false
    override fun getLayoutID(): Int = R.layout.frg_setting_version

    override fun initUI(savedInstanceState: Bundle?) {
          mTVShow.text =  String.format(Locale.CHINA,
                  "当前版本号 : %d\n当前版本名 : %s",
                  ContextUtil.getVerCode(context), ContextUtil.getVerName(context))
    }

    override fun updateSetting() {
        if (isSettingDirty) {
            isSettingDirty = false
        }
    }
}
