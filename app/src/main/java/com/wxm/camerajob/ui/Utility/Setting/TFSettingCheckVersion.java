package com.wxm.camerajob.ui.Utility.Setting;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wxm.camerajob.R;
import com.wxm.camerajob.utility.utility.ContextUtil;
import com.wxm.camerajob.ui.Base.TFSettingBase;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 检查版本设置页面
 * Created by 123 on 2016/10/10.
 */
public class TFSettingCheckVersion extends TFSettingBase {
    @BindView(R.id.tv_show)
    TextView mTVShow;

    @Override
    protected View inflaterView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        LOG_TAG = "TFSettingCheckVersion";
        View rootView = inflater.inflate(R.layout.frg_setting_version, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    protected void initUiComponent(View view) {
        String s = String.format(Locale.CHINA
                , "当前版本号 : %d\n当前版本名 : %s"
                , ContextUtil.getVerCode(getContext())
                , ContextUtil.getVerName(getContext()));
        mTVShow.setText(s);
    }

    @Override
    protected void initUiInfo() {
    }

    @Override
    public void updateSetting() {
        if (mBSettingDirty) {
            mBSettingDirty = false;
        }
    }
}
