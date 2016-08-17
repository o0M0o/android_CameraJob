package com.wxm.camerajob.ui.activitys;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.CameraParam;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.utility.ContextUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import cn.wxm.andriodutillib.type.MySize;
import cn.wxm.andriodutillib.util.UtilFun;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class ACCameraSetting
        extends AppCompatActivity
        implements View.OnClickListener {
    private RadioButton  mRBFrontCamera;
    private RadioButton  mRBBackCamera;

    private Switch      mSWAutoFocus;
    private Switch      mSWAutoFlash;

    private Spinner                     mSPPhotoSize;
    private ArrayAdapter<String>        mAAPhotoSize;

    private HashMap<String, CameraCharacteristics>  mHMCameras;

    private LinkedList<HashMap<String, String>>     mLLDpi;
    private LinkedList<HashMap<String, String>>     mLLBackCameraDpi;
    private LinkedList<HashMap<String, String>>     mLLFrontCameraDpi;

    private CameraParam         mCPBack;
    private CameraParam         mCPFront;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_camera_setting);
        ContextUtil.getInstance().addActivity(this);

        Intent data = getIntent();
        CameraParam cp = data.getParcelableExtra(GlobalDef.STR_LOAD_CAMERASETTING);
        mHMCameras = new HashMap<>();
        mLLDpi = new LinkedList<>();
        mLLBackCameraDpi = new LinkedList<>();
        mLLFrontCameraDpi = new LinkedList<>();

        mCPBack = new CameraParam(null);
        mCPFront = new CameraParam(null);

        mRBBackCamera = (RadioButton)findViewById(R.id.acrb_cs_backcamera);
        mRBFrontCamera = (RadioButton)findViewById(R.id.acrb_cs_frontcamera);
        mRBBackCamera.setOnClickListener(this);
        mRBFrontCamera.setOnClickListener(this);

        mSWAutoFocus = (Switch)findViewById(R.id.acsw_cs_autofocus);
        mSWAutoFlash = (Switch)findViewById(R.id.acsw_cs_autoflash);

        assert mSWAutoFlash != null;
        assert mSWAutoFocus != null;
        mSWAutoFlash.setOnClickListener(this);
        mSWAutoFocus.setOnClickListener(this);

        mSPPhotoSize = (Spinner)findViewById(R.id.acsp_cs_dpi);
        mAAPhotoSize = new ArrayAdapter<>(this,
                            R.layout.listitem_photosize,
                            R.id.ItemPhotoSize);
        mSPPhotoSize.setAdapter(mAAPhotoSize);

        if(ContextUtil.useNewCamera())
            load_camerainfo_new();
        else
            load_camerainfo_old();

        load_cameraparam(cp);
        mRBBackCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch_to_backcamera();
            }

            private void switch_to_backcamera() {
                mCPFront.Copy(get_cur_param());
                fill_backcamera();
            }
        });

        mRBFrontCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch_to_frontcamera();
            }

            private void switch_to_frontcamera() {
                mCPBack.Copy(get_cur_param());
                fill_frontcamera();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.acmeu_cameraset_actbar, menu);
        return true;
    }

    /**
     * 保存view当前的相机参数
     * @return  当前相机参数
     */
    private CameraParam get_cur_param() {
        CameraParam cp = new CameraParam(null);
        if(mRBBackCamera.isChecked())
            cp.mFace = CameraParam.LENS_FACING_BACK;
        else
            cp.mFace = CameraParam.LENS_FACING_FRONT;

        Object sel = mSPPhotoSize.getSelectedItem();
        if(null != sel)
            cp.mPhotoSize = MySize.parseSize(sel.toString());
        else
            cp.mPhotoSize = MySize.parseSize(mSPPhotoSize.getItemAtPosition(0).toString());

        cp.mAutoFlash = mSWAutoFlash.isChecked();
        cp.mAutoFocus = mSWAutoFocus.isChecked();
        return cp;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.meuitem_cameraset_accept : {
                do_save();
                finish();
            }
            break;

            case R.id.meuitem_cameraset_giveup : {
                do_giveup();
                finish();
            }
            break;

            default:
                return super.onOptionsItemSelected(item);

        }

        return true;
    }

    /**
     * 保存任务并返回前activity
     */
    private void do_save()  {
        Intent data = new Intent();
        CameraParam cp = new CameraParam(null);
        cp.Copy(get_cur_param());

        data.putExtra(GlobalDef.STR_LOAD_CAMERASETTING, cp);
        setResult(GlobalDef.INTRET_CS_ACCEPT, data);
    }

    /**
     * 放弃当前任务
     */
    private void do_giveup()  {
        Intent data = new Intent();
        setResult(GlobalDef.INTRET_CS_GIVEUP, data);
    }


    private void load_camerainfo_old()  {
        class CompareSizesByArea implements Comparator<MySize> {
            @Override
            public int compare(MySize lhs, MySize rhs) {
                // We cast here to ensure the multiplications won't overflow
                return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                        (long) rhs.getWidth() * rhs.getHeight());
            }
        }

        String mBackCameraID = "";
        String mFrontCameraID = "";

        int cc = Camera.getNumberOfCameras();
        Camera.CameraInfo ci = new Camera.CameraInfo();
        for(int id = 0; id < cc; id++)  {
            Camera ca = Camera.open(id);
            Camera.Parameters cpa = ca.getParameters();

            LinkedList<MySize> ls_sz = new LinkedList<>();
            for(Camera.Size cs : cpa.getSupportedPictureSizes())    {
                ls_sz.add(new MySize(cs.width, cs.height));
            }

            Collections.sort(ls_sz, new CompareSizesByArea());
            for (MySize sz : ls_sz)    {
                HashMap<String, String> hmap = new HashMap<>();
                hmap.put(GlobalDef.STR_CAMERA_DPI, sz.toString());
                mLLDpi.add(hmap);
            }

            // 前后相机只采用第一个
            Camera.getCameraInfo(id, ci);
            if(Camera.CameraInfo.CAMERA_FACING_BACK == ci.facing
                    && mBackCameraID.isEmpty())   {
                mBackCameraID = Integer.toString(id);
                mLLBackCameraDpi.addAll(mLLDpi);
            }

            if(Camera.CameraInfo.CAMERA_FACING_FRONT == ci.facing
                    && mFrontCameraID.isEmpty())   {
                mFrontCameraID = Integer.toString(id);
                mLLFrontCameraDpi.addAll(mLLDpi);
            }

            ca.release();
        }
    }





    /**
     * 加载系统相机信息
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void load_camerainfo_new() {
        class CompareSizesByArea implements Comparator<Size> {
            @Override
            public int compare(Size lhs, Size rhs) {
                // We cast here to ensure the multiplications won't overflow
                return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                        (long) rhs.getWidth() * rhs.getHeight());
            }
        }

        String mBackCameraID = "";
        String mFrontCameraID = "";
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
                    ArrayList<Size> sz_ls = new ArrayList<>();
                    Collections.addAll(sz_ls, sz_arr);

                    Collections.sort(sz_ls, new CompareSizesByArea());
                    for (Size sz : sz_ls)    {
                        HashMap<String, String> hmap = new HashMap<>();
                        hmap.put(GlobalDef.STR_CAMERA_DPI, UtilFun.SizeToString(sz));
                        mLLDpi.add(hmap);
                    }
                }

                // 前后相机只采用第一个
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if(null != facing) {
                    if (CameraCharacteristics.LENS_FACING_BACK == facing
                            && mBackCameraID.isEmpty()) {
                        mBackCameraID = cameraId;
                        mLLBackCameraDpi.addAll(mLLDpi);
                    }

                    if (CameraCharacteristics.LENS_FACING_FRONT == facing
                            && mFrontCameraID.isEmpty()) {
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
        if(CameraCharacteristics.LENS_FACING_BACK == cp.mFace)  {
            mCPBack.Copy(cp);
            fill_backcamera();
        }
        else    {
            mCPFront.Copy(cp);
            fill_frontcamera();
        }
    }

    /**
     * 用后置相机填充view
     */
    private void fill_backcamera()  {
        mRBBackCamera.setChecked(true);
        mRBFrontCamera.setChecked(false);

        mAAPhotoSize.clear();
        for(HashMap<String, String> map : mLLBackCameraDpi) {
            String dpi = map.get(GlobalDef.STR_CAMERA_DPI);
            mAAPhotoSize.add(dpi);
        }
        mAAPhotoSize.notifyDataSetChanged();

        fill_others(mCPBack);
    }


    /**
     * 用前置相机填充view
     */
    private void fill_frontcamera()  {
        mRBBackCamera.setChecked(false);
        mRBFrontCamera.setChecked(true);

        mAAPhotoSize.clear();
        for(HashMap<String, String> map : mLLFrontCameraDpi) {
            String dpi = map.get(GlobalDef.STR_CAMERA_DPI);
            mAAPhotoSize.add(dpi);
        }
        mAAPhotoSize.notifyDataSetChanged();

        fill_others(mCPFront);
    }

    /**
     * 填充公共部分
     * @param cp 填充参数
     */
    private void fill_others(CameraParam cp)    {
        String dpi = cp.mPhotoSize.toString();
        int pos = mAAPhotoSize.getPosition(dpi);
        if(-1 == pos)   {
            mSPPhotoSize.setSelection(0);
        }
        else    {
            mSPPhotoSize.setSelection(pos);
        }

        if(cp.mAutoFlash)
            mSWAutoFlash.setChecked(true);
        else
            mSWAutoFlash.setChecked(false);


        if(cp.mAutoFocus)
            mSWAutoFocus.setChecked(true);
        else
            mSWAutoFocus.setChecked(false);
    }


    @Override
    public void onClick(View v) {
    }
}
