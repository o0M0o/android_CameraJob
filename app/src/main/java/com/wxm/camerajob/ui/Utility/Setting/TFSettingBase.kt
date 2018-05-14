package com.wxm.camerajob.ui.Utility.Setting

import android.content.Context

import wxm.androidutil.FrgUtility.FrgUtilitySupportBase
import wxm.androidutil.util.UtilFun

/**
 * setting base class
 */
abstract class TFSettingBase : FrgUtilitySupportBase() {
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
    val rootActivity: ACSetting?
        get() {
            val ct = context
            return if (ct is ACSetting) {
                UtilFun.cast<ACSetting>(ct)
            } else null

        }

    /**
     * 切换页面
     * @param idx  新页面的idx
     */
    fun toPageByIdx(idx: Int) {
        val acs = rootActivity
        acs?.changePage(idx)
    }

    /**
     * 保存页面所管理的配置
     */
    abstract fun updateSetting()
}