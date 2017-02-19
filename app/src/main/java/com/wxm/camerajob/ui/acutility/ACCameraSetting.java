package com.wxm.camerajob.ui.acutility;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.ui.fragment.setting.TFSettingCamera;

public class ACCameraSetting extends AppCompatActivity   {
    private TFSettingCamera  mTFCamera = new TFSettingCamera();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_camera_setting);
        ContextUtil.getInstance().addActivity(this);

        if(null == savedInstanceState)  {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_camera_setting, mTFCamera);
            transaction.commit();
        }
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
