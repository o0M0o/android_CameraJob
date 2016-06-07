package com.wxm.camerajob.ui.activitys;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.wxm.camerajob.R;
import com.wxm.camerajob.ui.fragment.CameraFragment;

public class ActivityTest extends AppCompatActivity {
    private CameraFragment mCamearFrag = CameraFragment.newInstance();
    private Button mBtActiveFrontCamear;
    private Button mBtActiveBackCamear;
    private Button mBtTakePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        if(null == savedInstanceState)  {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.acfl_test_camera_preview, mCamearFrag);
            transaction.commit();
        }

        mBtActiveFrontCamear = (Button)findViewById(R.id.acbt_test_frontcamera_active);
        mBtActiveBackCamear = (Button)findViewById(R.id.acbt_test_backcamera_active);
        mBtTakePhoto = (Button)findViewById(R.id.acbt_test_takephoto);

        activeTakePhoto(false);

        mBtActiveFrontCamear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamearFrag.ActiveFrontCamera();
            }
        });

        mBtActiveBackCamear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamearFrag.ActiveBackCamera();
            }
        });
    }

    /**
     * 检查手机是否存在相机
     * @param context 上下文
     * @return 若存在则返回true, 否则返回false
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 修改是否能"take photo"
     * @param active 如果为true则可以执行"take photo"操作
     */
    private void activeTakePhoto(boolean active)  {
        mBtTakePhoto.setClickable(active);

        if(!active)
            mBtTakePhoto.setTextColor(Color.GRAY);
        else
            mBtTakePhoto.setTextColor(Color.BLACK);
    }
}
