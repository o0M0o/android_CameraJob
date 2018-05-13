package com.wxm.camerajob.ui.Camera.CameraPreview;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

import com.wxm.camerajob.data.define.GlobalDef;
import com.wxm.camerajob.utility.ContextUtil;

import wxm.androidutil.ExActivity.BaseAppCompatActivity;

/**
 * camera preview
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ACCameraPreview extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContextUtil.Companion.getInstance().addActivity(this);
    }

    @Override
    protected void leaveActivity() {
        Intent data = new Intent();
        setResult(GlobalDef.INTRET_USR_LOGOUT, data);
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
