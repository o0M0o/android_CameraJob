package com.wxm.camerajob.ui.Camera.CameraPreview;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.wxm.camerajob.R;
import com.wxm.camerajob.data.define.GlobalDef;
import com.wxm.camerajob.utility.utility.ContextUtil;

import cn.wxm.andriodutillib.ExActivity.BaseAppCompatActivity;

/**
 * 相机预览UI
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ACCameraPreview extends BaseAppCompatActivity {

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
        LOG_TAG = "ACCameraPreview";
        mFGHolder = FrgCameraPreview.newInstance();
    }

    /// BEGIN PRIVATE
    /// END PRIVATE
}
