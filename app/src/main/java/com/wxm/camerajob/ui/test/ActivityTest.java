package com.wxm.camerajob.ui.test;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.ui.fragment.test.CameraFragment;

public class ActivityTest extends AppCompatActivity {
    private final CameraFragment mCamearFrag = CameraFragment.newInstance();
    private Button mBtActiveFrontCamear;
    private Button mBtActiveBackCamear;
    private Button mBtTakePhoto;
    private Button mBtCameraClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_test);

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

// --Commented out by Inspection START (2016/6/27 23:14):
//    /**
//     * 检查手机是否存在相机
//     * @param context 上下文
//     * @return 若存在则返回true, 否则返回false
//     */
//    private boolean checkCameraHardware(Context context) {
//        return context.getPackageManager().hasSystemFeature(
//                PackageManager.FEATURE_CAMERA);
//    }
// --Commented out by Inspection STOP (2016/6/27 23:14)

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.acm_leave, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_leave: {
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
