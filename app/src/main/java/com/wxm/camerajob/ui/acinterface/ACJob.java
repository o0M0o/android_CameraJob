package com.wxm.camerajob.ui.acinterface;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
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
import com.wxm.camerajob.base.handler.GlobalContext;
import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.base.utility.PreferencesUtil;
import com.wxm.camerajob.ui.acutility.ACCameraSetting;
import com.wxm.camerajob.ui.fragment.utility.FrgJobCreate;

public class ACJob
        extends AppCompatActivity   {
    private final static String TAG = "ACJob";
    private final static int        REQUEST_SET_CAMERA = 123;

    private final FrgJobCreate mFRGJobCreater = FrgJobCreate.newInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_job);
        ContextUtil.getInstance().addActivity(this);

        if(null == savedInstanceState)  {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_job_creater, mFRGJobCreater);
            transaction.commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.acmeu_camerajob_actbar, menu);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(resultCode)  {
            case GlobalDef.INTRET_CS_ACCEPT:    {
                if(REQUEST_SET_CAMERA == requestCode) {
                    Message m = Message.obtain(GlobalContext.getMsgHandlder(),
                            GlobalDef.MSGWHAT_CS_CHANGECAMERA);
                    m.obj = PreferencesUtil.loadCameraParam();
                    m.sendToTarget();
                }
            }
            break;

            case GlobalDef.INTRET_CS_GIVEUP :   {
                if(REQUEST_SET_CAMERA == requestCode) {
                    checkCamera();
                }
            }
            break;

            default:    {
                Log.i(TAG, "不处理的 resultCode = " + resultCode);
            }
            break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.meuitem_camerajob_accept: {
                CameraJob cj = mFRGJobCreater.onAccept();
                if(null != cj) {
                    Intent data = new Intent();
                    data.putExtra(GlobalDef.STR_LOAD_JOB, cj);
                    setResult(GlobalDef.INTRET_CAMERAJOB_ACCEPT, data);
                    finish();
                }
            }
            break;

            case R.id.meuitem_camerajob_giveup  : {
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
            builder.setPositiveButton("确 定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent data = new Intent(home, ACCameraSetting.class);
                    startActivityForResult(data, REQUEST_SET_CAMERA);
                }
            });

            Dialog dialog = builder.create();
            dialog.show();
        }
    }
}






