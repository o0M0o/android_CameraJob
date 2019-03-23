package com.wxm.camerajob.ui.setting


import android.os.Bundle
import android.widget.TextView
import com.wxm.camerajob.R
import com.wxm.camerajob.App
import kotterknife.bindView
import java.util.*

/**
 * UI for directory setting
 * Created by WangXM on 2016/10/10.
 */
class TFSettingDirectory : TFSettingBase() {
    private val mTVShow: TextView by bindView(R.id.tv_show)

    override fun isUseEventBus(): Boolean = false
    override fun getLayoutID(): Int = R.layout.pg_setting_directory

    override fun initUI(savedInstanceState: Bundle?) {
        mTVShow.text = String.format(Locale.CHINA, "照片根目录 : %s",
                App.getPhotoRootDir())
    }

    override fun updateSetting() {
        if (isSettingDirty) {
            isSettingDirty = false
        }
    }
}
