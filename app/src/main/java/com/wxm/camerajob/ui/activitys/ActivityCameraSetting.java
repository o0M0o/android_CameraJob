package com.wxm.camerajob.ui.activitys;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.CameraParam;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.base.utility.UtilFun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

public class ActivityCameraSetting
        extends AppCompatActivity
        implements View.OnClickListener {

    private RadioButton  mRBFrontCamera;
    private RadioButton  mRBBackCamera;

    private Button      mBTAccept;
    private Button      mBTGiveup;

    private Switch      mSWAutoFocus;
    private Switch      mSWAutoFlash;

    private Spinner     mSPPhotoSize;

    private HashMap<String, CameraCharacteristics>  mHMCameras;
    private String      mBackCameraID;
    private String      mFrontCameraID;

    private LinkedList<HashMap<String, String>>     mLLDpi;
    private LinkedList<HashMap<String, String>>     mLLBackCameraDpi;
    private LinkedList<HashMap<String, String>>     mLLFrontCameraDpi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_setting);

        Intent data = getIntent();
        CameraParam cp = data.getParcelableExtra(GlobalDef.STR_LOAD_CAMERASETTING);
        mHMCameras = new HashMap<>();
        mLLDpi = new LinkedList<>();
        mLLBackCameraDpi = new LinkedList<>();
        mLLFrontCameraDpi = new LinkedList<>();

        mRBBackCamera = (RadioButton)findViewById(R.id.acrb_cs_backcamera);
        mRBFrontCamera = (RadioButton)findViewById(R.id.acrb_cs_frontcamera);
        mRBBackCamera.setOnClickListener(this);
        mRBFrontCamera.setOnClickListener(this);

        mBTAccept = (Button)findViewById(R.id.acbt_cs_accept);
        mBTGiveup = (Button)findViewById(R.id.acbt_cs_giveup);
        mBTAccept.setOnClickListener(this);
        mBTGiveup.setOnClickListener(this);

        mSWAutoFocus = (Switch)findViewById(R.id.acsw_cs_autofocus);
        mSWAutoFlash = (Switch)findViewById(R.id.acsw_cs_autoflash);
        mSWAutoFlash.setOnClickListener(this);
        mSWAutoFocus.setOnClickListener(this);

        mSPPhotoSize = (Spinner)findViewById(R.id.acsp_cs_dpi);
        load_camerainfo();
        load_cameraparam(cp);
    }


    @Override
    public void onClick(View v) {
        int vid = v.getId();
        switch (vid)    {
            case R.id.acbt_cs_accept:
                if(do_save())
                    finish();
                break;

            case R.id.acbt_cs_giveup:
                do_giveup();
                finish();
                break;
        }
    }


    /**
     * 保存任务并返回前activity
     * @return 如果成功返回true,否则返回false
     */
    private boolean do_save()  {
        Intent data = new Intent();
        CameraParam cp = new CameraParam(null);

        data.putExtra(GlobalDef.STR_LOAD_CAMERASETTING, cp);
        setResult(GlobalDef.INTRET_CS_ACCEPT, data);
        return true;
    }

    /**
     * 放弃当前任务
     */
    private void do_giveup()  {
        Intent data = new Intent();
        setResult(GlobalDef.INTRET_CS_GIVEUP, data);
    }


    /**
     * 加载系统相机信息
     */
    private void load_camerainfo() {
        class CompareSizesByArea implements Comparator<Size> {
            @Override
            public int compare(Size lhs, Size rhs) {
                // We cast here to ensure the multiplications won't overflow
                return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                        (long) rhs.getWidth() * rhs.getHeight());
            }
        }

        mBackCameraID = "";
        mFrontCameraID = "";
        CameraManager manager =
                (CameraManager) ContextUtil.getInstance().getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                mHMCameras.put(cameraId, characteristics);
                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if(null != map) {
                    mLLDpi.clear();

                    Size[] sz_arr = map.getOutputSizes(ImageFormat.JPEG);
                    ArrayList<Size> sz_ls = (ArrayList<Size>) Arrays.asList(sz_arr);
                    Collections.sort(sz_ls, new CompareSizesByArea());
                    for (Size sz : sz_ls)    {
                        HashMap<String, String> hmap = new HashMap<>();
                        hmap.put(GlobalDef.STR_CAMERA_DPI, UtilFun.SizeToString(sz));
                        mLLDpi.add(hmap);
                    }
                }


                // 前后相机只采用第一个
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if(CameraCharacteristics.LENS_FACING_BACK  == facing)   {
                    if(mBackCameraID.isEmpty()) {
                        mBackCameraID = cameraId;
                        mLLBackCameraDpi.addAll(mLLDpi);
                    }
                }

                if(CameraCharacteristics.LENS_FACING_FRONT == facing)   {
                    if(mFrontCameraID.isEmpty()) {
                        mFrontCameraID = cameraId;
                        mLLFrontCameraDpi.addAll(mLLDpi);
                    }
                }

            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    /**
     * 展示设定
     * @param cp 相机设定
     */
    private void load_cameraparam(CameraParam cp)    {
        LinkedList<String>  lsstr_dpi = new LinkedList<>();
        LinkedList<Size>    lssize_dpi = new LinkedList<>();
        if(CameraCharacteristics.LENS_FACING_BACK == cp.mFace)  {
            mRBBackCamera.setChecked(true);
            mRBFrontCamera.setChecked(false);



        }
        else    {
            mRBBackCamera.setChecked(false);
            mRBFrontCamera.setChecked(true);
        }


    }


}
