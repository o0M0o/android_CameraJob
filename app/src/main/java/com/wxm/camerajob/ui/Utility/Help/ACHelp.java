package com.wxm.camerajob.ui.Utility.Help;

import android.content.Intent;
import android.os.Bundle;

import com.wxm.camerajob.data.define.GlobalDef;
import com.wxm.camerajob.utility.ContextUtil;

import wxm.androidutil.ExActivity.BaseAppCompatActivity;
import wxm.androidutil.util.UtilFun;

/**
 * UI for help
 */
public class ACHelp
        extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContextUtil.Companion.getInstance().addActivity(this);
    }

    @Override
    protected void leaveActivity() {
        int ret_data = GlobalDef.INTRET_USR_LOGOUT;

        Intent data = new Intent();
        setResult(ret_data, data);
        finish();
    }

    @Override
    protected void initFrgHolder() {
        LOG_TAG = "ACJobCreate";
        mFGHolder = new FrgHelp();

        // load help html
        Intent it = getIntent();
        String help_type = it.getStringExtra(GlobalDef.STR_HELP_TYPE);
        if(!UtilFun.StringIsNullOrEmpty(help_type))   {
            Bundle arg = new Bundle();
            arg.putString(GlobalDef.STR_HELP_TYPE, help_type);
            mFGHolder.setArguments(arg);
        }
    }
}
