package com.wxm.camerajob.ui.setting

import wxm.androidutil.ui.frg.FrgSupportBaseAdv


/**
 * setting base class
 */
abstract class TFSettingBase : FrgSupportBaseAdv() {
    /**
     * 页面所管理的配置是否更改
     * @return  若配置已经更改则返回true, 否则返回false
     */
    var isSettingDirty = false
        protected set

    /**
     * 保存页面所管理的配置
     */
    abstract fun updateSetting()
}