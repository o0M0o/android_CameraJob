package com.wxm.camerajob.ui.acinterface;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.data.PreferencesUtil;
import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.ui.acutility.ACCameraSetting;
import com.wxm.camerajob.ui.fragment.utility.FrgJobCreate;
import com.wxm.camerajob.ui.fragment.utility.FrgJobShow;

import cn.wxm.andriodutillib.ExActivity.BaseAppCompatActivity;

public class ACJobCreate
        extends BaseAppCompatActivity {
    private final static int        REQUEST_SET_CAMERA = 123;

    private final FrgJobCreate mFRGJobCreat = FrgJobCreate.newInstance();


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
        LOG_TAG = "ACJobCreate";
        mFGSupportHolder = mFRGJobCreat;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.acm_accpet_giveup, menu);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(resultCode)  {
            case GlobalDef.INTRET_CS_ACCEPT:    {
                /*
                if(REQUEST_SET_CAMERA == requestCode) {
                    Message m = Message.obtain(GlobalContext.getMsgHandlder(),
                            GlobalDef.MSG_TYPE_CAMERA_MODIFY);
                    m.obj = PreferencesUtil.loadCameraParam();
                    m.sendToTarget();
                }
                */
            }
            break;

            case GlobalDef.INTRET_CS_GIVEUP :   {
                if(REQUEST_SET_CAMERA == requestCode) {
                    checkCamera();
                }
            }
            break;

            default:    {
                Log.i(LOG_TAG, "不处理的 resultCode = " + resultCode);
            }
            break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_accept: {
                CameraJob cj = mFRGJobCreat.onAccept();
                if(null != cj) {
                    Intent data = new Intent();
                    data.putExtra(GlobalDef.STR_LOAD_JOB, cj);
                    setResult(GlobalDef.INTRET_CAMERAJOB_ACCEPT, data);
                    finish();
                }
            }
            break;

            case R.id.mi_giveup: {
                Intent data = new Intent();
                setResult(GlobalDef.INTRET_CAMERAJOB_GIVEUP, data);
                finish();
            }
            break;

            default:
                return super.onOptionsItemSelected(item);

        }

        return true;
    }

    /**
     * 检查相机设置
     * 必须设置相机后才可以继续后面的操作
     */
    private void checkCamera() {
        final Activity home = this;
        if(!PreferencesUtil.checkCameraIsSet()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(home);
            builder.setTitle("相机未设置，需要先设置相机");
            builder.setPositiveButton("确 定", (dialog, which) -> {
                Intent data = new Intent(home, ACCameraSetting.class);
                startActivityForResult(data, REQUEST_SET_CAMERA);
            });

            Dialog dialog = builder.create();
            dialog.show();
        }
    }
}





