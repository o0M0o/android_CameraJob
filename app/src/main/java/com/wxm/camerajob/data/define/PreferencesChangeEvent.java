package com.wxm.camerajob.data.define;

/**
 * 数据库中数据变化后通知类
 * Created by User on 2017/2/14.
 */
public class PreferencesChangeEvent {
    private String mPreferencesName;

    public PreferencesChangeEvent(String preference_name)  {
        mPreferencesName = preference_name;
    }

    /**
     * 获取变化配置名
     * @return  配置名
     */
    public String getPreferencesName()   {
        return mPreferencesName;
    }
}
