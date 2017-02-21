package com.wxm.camerajob.ui.Utility.Setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wxm.camerajob.R;
import com.wxm.camerajob.utility.ContextUtil;


import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 检查版本设置页面
 * Created by 123 on 2016/10/10.
 */
public class TFSettingDirectory extends TFSettingBase {
    @BindView(R.id.tv_show)
    TextView mTVShow;


    @Override
    protected View inflaterView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        LOG_TAG = "TFSettingDirectory";
        View rootView = inflater.inflate(R.layout.frg_setting_directory, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    protected void initUiComponent(View view) {
        String s = String.format(Locale.CHINA, "当前APP根目录 : %s",
                ContextUtil.getInstance().getAppPhotoRootDir());
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
