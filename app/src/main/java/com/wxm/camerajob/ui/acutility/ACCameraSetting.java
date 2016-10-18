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
        inflater.inflate(R.menu.acmeu_cameraset_actbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.meuitem_cameraset_accept : {
                Intent data = new Intent();
                setResult(GlobalDef.INTRET_CS_ACCEPT, data);

                if(mTFCamera.isSettingDirty()) {
                    Dialog alertDialog = new AlertDialog.Builder(this).
                            setTitle("配置已经更改").
                            setMessage("是否保存更改的配置?").
                            setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mTFCamera.updateSetting();
                                    finish();
                                }
                            }).
                            setNegativeButton("否", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            }).
                            create();
                    alertDialog.show();
                } else {
                    mTFCamera.updateSetting();
                    finish();
                }
            }
            break;

            case R.id.meuitem_cameraset_giveup : {
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
