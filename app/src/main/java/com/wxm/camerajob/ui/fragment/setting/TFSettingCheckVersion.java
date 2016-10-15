package com.wxm.camerajob.ui.fragment.setting;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.BuildConfig;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.utility.ContextUtil;

import java.util.Locale;

import cn.wxm.andriodutillib.util.UtilFun;

/**
 * 检查版本设置页面
 * Created by 123 on 2016/10/10.
 */
public class TFSettingCheckVersion extends TFSettingBase {
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_setting_version, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (null != view) {
            String s = String.format(Locale.CHINA
                    ,"当前版本号 : %d\n当前版本名 : %s"
                    , ContextUtil.getVerCode(getContext())
                    ,ContextUtil.getVerName(getContext()));
            TextView tv = UtilFun.cast(view.findViewById(R.id.tv_show));
            if(BuildConfig.DEBUG && null == tv) {
                throw new AssertionError("获取控件失败");
            }
            tv.setText(s);
        }
    }

    @Override
    public void updateSetting() {
        if(mBSettingDirty)  {
            mBSettingDirty = false;
        }
    }
}
