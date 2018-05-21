package com.wxm.camerajob.ui.utility.Setting

import wxm.androidutil.FrgUtility.FrgSupportBaseAdv


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
     * 得到ACSetting
     * @return  若成功返回结果，否则返回null
     */
    private val rootActivity: ACSetting
        get() {
            return activity as ACSetting
        }

    /**
     * 切换页面
     * @param idx  新页面的idx
     */
    fun toPageByIdx(idx: Int) {
        rootActivity.changePage(idx)
    }

    /**
     * 保存页面所管理的配置
     */
    abstract fun updateSetting()
}