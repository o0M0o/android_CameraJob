package com.wxm.camerajob.base.data;

/**
 * 配置变化后调用函数
 * Created by 123 on 2016/11/6.
 */
public interface IPreferenceChangeNotice {
    /**
     *  配置信息变化后调用
     *  @param PreferenceName   配置名
     */
    void onPreferenceChanged(String PreferenceName);
}
