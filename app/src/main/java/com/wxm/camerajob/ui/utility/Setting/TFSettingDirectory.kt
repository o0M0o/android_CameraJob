package com.wxm.camerajob.ui.utility.Setting


import android.os.Bundle
import android.widget.TextView
import com.wxm.camerajob.R
import com.wxm.camerajob.utility.context.ContextUtil
import kotterknife.bindView
import java.util.*

/**
 * UI for directory setting
 * Created by WangXM on 2016/10/10.
 */
class TFSettingDirectory : TFSettingBase() {
    private val mTVShow: TextView by bindView(R.id.tv_show)

    override fun isUseEventBus(): Boolean = false
    override fun getLayoutID(): Int = R.layout.frg_setting_directory

    override fun initUI(savedInstanceState: Bundle?) {
        mTVShow.text = String.format(Locale.CHINA, "照片根目录 : %s",
                ContextUtil.getPhotoRootDir())
    }

    override fun updateSetting() {
        if (isSettingDirty) {
            isSettingDirty = false
        }
    }
}
