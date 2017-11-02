package com.wxm.camerajob.ui.Utility.Setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wxm.camerajob.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * UI for main setting
 * Created by 123 on 2016/10/10.
 */
public class TFSettingMain extends TFSettingBase {

    @Override
    protected View inflaterView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        LOG_TAG = "TFSettingMain";
        View rootView = inflater.inflate(R.layout.frg_setting_main, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    protected void initUiComponent(View view) {
    }

    @Override
    protected void loadUI() {
    }

    @OnClick({R.id.rl_setting_check_version, R.id.rl_setting_directory, R.id.rl_setting_camera})
    public void onRelativeLayoutClick(View v) {
        int vid = v.getId();
        switch (vid) {
            case R.id.rl_setting_check_version: {
                toPageByIdx(FrgSetting.PAGE_IDX_CHECK_VERSION);
            }
            break;

            case R.id.rl_setting_directory: {
                toPageByIdx(FrgSetting.PAGE_IDX_DIRECTORY);
            }
            break;

            case R.id.rl_setting_camera: {
                toPageByIdx(FrgSetting.PAGE_IDX_CAMERA);
            }
            break;
        }
    }

    @Override
    public void updateSetting() {
        if (mBSettingDirty) {
            mBSettingDirty = false;
        }
    }
}
