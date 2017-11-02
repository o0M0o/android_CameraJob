package com.wxm.camerajob.ui.Camera.CameraSetting;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.wxm.camerajob.R;
import com.wxm.camerajob.data.define.GlobalDef;
import com.wxm.camerajob.ui.Utility.Setting.TFSettingCamera;
import com.wxm.camerajob.utility.ContextUtil;

import wxm.androidutil.ExActivity.BaseAppCompatActivity;

/**
 * UI for camera setting
 */
public class ACCameraSetting extends BaseAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContextUtil.getInstance().addActivity(this);
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
        LOG_TAG = "ACCameraSetting";
        mFGSupportHolder = new TFSettingCamera();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.acm_accpet_giveup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_accept: {
                final Intent data = new Intent();
                TFSettingCamera mTFCamera = (TFSettingCamera)mFGSupportHolder;
                if(mTFCamera.isSettingDirty()) {
                    Dialog alertDialog = new AlertDialog.Builder(this).
                            setTitle("配置已经更改").
                            setMessage("是否保存更改的配置?").
                            setPositiveButton("是", (dialog, which) -> {
                                mTFCamera.updateSetting();
                                setResult(GlobalDef.INTRET_CS_ACCEPT, data);
                                finish();
                            }).
                            setNegativeButton("否", (dialog, which) -> {
                                setResult(GlobalDef.INTRET_CS_GIVEUP, data);
                                finish();
                            }).create();
                    alertDialog.show();
                } else {
                    finish();
                }
            }
            break;

            case R.id.mi_giveup: {
                Intent data = new Intent();
                setResult(GlobalDef.INTRET_CS_GIVEUP, data);
                finish();
            }
            break;

            default:
                return super.onOptionsItemSelected(item);

        }

        return true;
    }
}
