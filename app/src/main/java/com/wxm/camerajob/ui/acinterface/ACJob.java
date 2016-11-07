package com.wxm.camerajob.ui.acinterface;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
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
import com.wxm.camerajob.ui.fragment.utility.FrgJobCreate;

public class ACJob
        extends AppCompatActivity   {
    private final static String TAG = "ACJob";
    private final FrgJobCreate mFRGJobCreater = FrgJobCreate.newInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_job);
        ContextUtil.getInstance().addActivity(this);

        /*
        // check camera setting
        // 必须设置相机后才可以继续后面的操作
        final ACJob home = this;
        while(!PreferencesUtil.checkCameraIsSet())     {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("相机未设置，需要先设置相机");
            builder.setPositiveButton("确 定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent data = new Intent(home, ACCameraSetting.class);
                    startActivityForResult(data, 1);
                }
            });

            Dialog dialog = builder.create();
            dialog.show();
        }
        */

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
                Message m = Message.obtain(GlobalContext.getMsgHandlder(),
                        GlobalDef.MSGWHAT_CS_CHANGECAMERA);
                m.obj = PreferencesUtil.loadCameraParam();
                m.sendToTarget();
            }
            break;

            /*
            case GlobalDef.INTRET_CS_GIVEUP :   {
            }
            break;
            */

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
}






