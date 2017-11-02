package com.wxm.camerajob.ui.Utility.Help;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.wxm.camerajob.R;
import com.wxm.camerajob.data.define.GlobalDef;

import butterknife.BindView;
import butterknife.ButterKnife;
import wxm.androidutil.FrgUtility.FrgUtilityBase;
import wxm.androidutil.util.UtilFun;

/**
 * 帮助fragment
 */
public class FrgHelp extends FrgUtilityBase {
    private String HELP_MAIN_FILEPATH = "file:///android_asset/help_main.html";
    private static final String ENCODING = "utf-8";

    @BindView(R.id.ac_help_webvw)
    WebView     mWVHelp;

    @Override
    protected View inflaterView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        LOG_TAG = "FrgHelp";
        View rootView = inflater.inflate(R.layout.frg_help, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    protected void initUiComponent(View view) {
        mWVHelp.getSettings().setDefaultTextEncodingName(ENCODING);

        Bundle arg = getArguments();
        if(null != arg) {
            String h_t = arg.getString(GlobalDef.STR_HELP_TYPE);
            if(!UtilFun.StringIsNullOrEmpty(h_t))   {
                switch (h_t)    {
                    case GlobalDef.STR_HELP_MAIN :  {
                        load_main_help();
                    }
                    break;
                }
            }
        }
    }

    @Override
    protected void loadUI() {
    }


    /**
     * 加载应用主帮助信息
     */
    private void load_main_help()   {
        mWVHelp.loadUrl(HELP_MAIN_FILEPATH);
    }
}
