package com.wxm.camerajob.ui.activitys;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.ui.fragment.utility.CameraPreview;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ACCameraPreview extends AppCompatActivity {
    private final static String     TAG = "ACCameraPreview";
    private final CameraPreview     mCameraFrag = CameraPreview.newInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_camera_preview);
        ContextUtil.getInstance().addActivity(this);

        if(null == savedInstanceState)  {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.acfl_test_camera_preview, mCameraFrag);
            transaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.acm_testcamera_actbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.meuitem_testcamera_leave: {
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

    /// BEGIN PRIVATE
    /// END PRIVATE
}
