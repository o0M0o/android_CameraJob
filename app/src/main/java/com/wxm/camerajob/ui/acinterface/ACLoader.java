package com.wxm.camerajob.ui.acinterface;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.utility.ContextUtil;

import java.util.ArrayList;
import java.util.Locale;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WAKE_LOCK;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * App第一个界面
 * 申请权限，然后跳转到工作首界面
 */
public class ACLoader extends AppCompatActivity {
    private static final int REQUEST_ALL  = 99;

    /**
     * 如果有权限，则直接初始化
     * 如果无权限，则申请权限后再进行初始化
     * @param savedInstanceState   param
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_loader);

        ContextUtil.getInstance().addActivity(this);
        if(mayRequestPermission()) {
            jumpWorkActivity();
        }
    }

    /**
     * 跳转到工作首界面
     */
    private void jumpWorkActivity() {
        ContextUtil.getInstance().initAppContext();
        Intent it = new Intent(this, ACJobShow.class);
        startActivityForResult(it, 1);
    }


    /**
     * 申请APP需要的权限
     * @return  如果APP权限已经足够，返回true, 否则返回false
     */
    private boolean mayRequestPermission() {
        ArrayList<String> ls_str = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ls_str.add(WRITE_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(this, CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ls_str.add(CAMERA);
        }

        if (ContextCompat.checkSelfPermission(this, WAKE_LOCK)
                != PackageManager.PERMISSION_GRANTED) {
            ls_str.add(WAKE_LOCK);
        }


        if(ls_str.isEmpty())
            return true;

        String[] str_arr = ls_str.toArray(new String[ls_str.size()]);
        ActivityCompat.requestPermissions(this, str_arr, REQUEST_ALL);
        return false;
    }


    /**
     * Callback received when a permissions request has been completed.
     * 若权限齐全则初始化activity, 否则弹出提示框然后退出APP
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ALL) {
            boolean ct = true;
            for(int pos = 0; pos < grantResults.length; pos++)      {
                if(grantResults[pos] != PackageManager.PERMISSION_GRANTED)  {
                    ct = false;
                    String msg = String.format(Locale.CHINA,
                            "由于缺少必须的权限(%s)，本APP无法运行!",
                            permissions[pos]);

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(msg)
                            .setTitle("警告")
                            .setCancelable(false)
                            .setPositiveButton("离开应用", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ContextUtil.getInstance().onTerminate();
                                }
                            });

                    AlertDialog dlg = builder.create();
                    dlg.show();
                }
            }

            if(ct) {
                jumpWorkActivity();
            }
        }
    }
}
