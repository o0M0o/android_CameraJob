package com.wxm.camerajob.ui.fragment.setting;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.wxm.camerajob.R;
import com.wxm.camerajob.ui.acutility.ACSetting;

import cn.wxm.andriodutillib.util.UtilFun;

/**
 * 设置主页面
 * Created by 123 on 2016/10/10.
 */
public class TFSettingMain extends TFSettingBase {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_setting_main, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (null != view) {
            RelativeLayout rl = UtilFun.cast(view.findViewById(R.id.rl_setting_check_version));
            assert null != rl;
            rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toPageByIdx(ACSetting.PAGE_IDX_CHECK_VERSION);
                }
            });

            rl = UtilFun.cast(view.findViewById(R.id.rl_setting_directory));
            assert null != rl;
            rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toPageByIdx(ACSetting.PAGE_IDX_DIRECTORY);
                }
            });

            final Activity ac = getActivity();
            rl = UtilFun.cast(view.findViewById(R.id.rl_setting_camera));
            assert null != rl;
            rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toPageByIdx(ACSetting.PAGE_IDX_CAMERA);
                }
            });
        }
    }

    @Override
    public void updateSetting() {
        if(mBSettingDirty)  {
            mBSettingDirty = false;
        }
    }


    /**
     * 设置layout可见性
     * 仅调整可见性，其它设置保持不变
     * @param visible  若为 :
     *                  1. {@code View.INVISIBLE}, 不可见
     *                  2. {@code View.VISIBLE}, 可见
    private void setLayoutVisible(RelativeLayout rl, int visible)    {
        int w = RelativeLayout.LayoutParams.MATCH_PARENT;
        int h = 0;
        if(View.INVISIBLE != visible)
            h = RelativeLayout.LayoutParams.WRAP_CONTENT;

        ViewGroup.LayoutParams param = rl.getLayoutParams();
        param.width = w;
        param.height = h;
        rl.setLayoutParams(param);
    }
     */
}
