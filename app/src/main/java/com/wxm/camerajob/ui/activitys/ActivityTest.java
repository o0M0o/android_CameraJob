package com.wxm.camerajob.ui.activitys;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.wxm.camerajob.R;
import com.wxm.camerajob.ui.fragment.CameraFragment;

public class ActivityTest extends Activity {
    private CameraFragment mCamearFrag = CameraFragment.newInstance();
    private Button mBtActiveFrontCamear;
    private Button mBtActiveBackCamear;
    private Button mBtTakePhoto;
    private Button mBtCameraClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        if(null == savedInstanceState)  {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.acfl_test_camera_preview, mCamearFrag);
            transaction.commit();
        }

        mBtActiveFrontCamear = (Button)findViewById(R.id.acbt_test_frontcamera_active);
        mBtActiveBackCamear = (Button)findViewById(R.id.acbt_test_backcamera_active);
        mBtTakePhoto = (Button)findViewById(R.id.acbt_test_takephoto);
        mBtCameraClose = (Button)findViewById(R.id.acbt_test_camera_close);

        activeButton(mBtTakePhoto, false);
        activeButton(mBtCameraClose, false);
        mBtActiveFrontCamear.setTextColor(Color.GRAY);
        mBtActiveBackCamear.setTextColor(Color.GRAY);

        mBtActiveFrontCamear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamearFrag.ActiveFrontCamera();
                activeButton(mBtCameraClose, true);
                activeButton(mBtTakePhoto, true);

                mBtActiveFrontCamear.setTextColor(Color.BLACK);
                mBtActiveBackCamear.setTextColor(Color.GRAY);
            }
        });

        mBtActiveBackCamear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamearFrag.ActiveBackCamera();
                activeButton(mBtCameraClose, true);
                activeButton(mBtTakePhoto, true);

                mBtActiveFrontCamear.setTextColor(Color.GRAY);
                mBtActiveBackCamear.setTextColor(Color.BLACK);
            }
        });

        mBtCameraClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamearFrag.CloseCamera();
                activeButton(mBtTakePhoto, false);

                mBtActiveFrontCamear.setTextColor(Color.GRAY);
                mBtActiveBackCamear.setTextColor(Color.GRAY);
                mBtCameraClose.setTextColor(Color.GRAY);
            }
        });

        mBtTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamearFrag.TakePhoto();
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
     * 设置button的有效状态
     * @param bt        待设定的button
     * @param active    如果为true则button可以使用
     */
    private void activeButton(Button bt, boolean active)    {
        bt.setClickable(active);

        if(!active)
            bt.setTextColor(Color.GRAY);
        else
            bt.setTextColor(Color.BLACK);
    }
}
