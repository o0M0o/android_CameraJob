package com.wxm.camerajob.ui.Utility.Setting;

import android.content.Context;

import cn.wxm.andriodutillib.FrgUtility.FrgUtilitySupportBase;
import cn.wxm.andriodutillib.util.UtilFun;

public abstract class TFSettingBase extends FrgUtilitySupportBase {
    protected boolean   mBSettingDirty = false;

    /**
     * 得到ACSetting
     * @return  若成功返回结果，否则返回null
     */
    public ACSetting getRootActivity()  {
        Context ct = getContext();
        if(ct instanceof ACSetting) {
            return UtilFun.cast(ct);
        }

        return null;
    }

    /**
     * 切换页面
     * @param idx  新页面的idx
     */
    public void toPageByIdx(int idx)    {
        ACSetting acs = getRootActivity();
        if(null != acs) {
            acs.changePage(idx);
        }
    }

    /**
     * 页面所管理的配置是否更改
     * @return  若配置已经更改则返回true, 否则返回false
     */
    public boolean isSettingDirty() {
        return mBSettingDirty;
    }

    /**
     * 保存页面所管理的配置
     */
    public abstract void updateSetting();
}