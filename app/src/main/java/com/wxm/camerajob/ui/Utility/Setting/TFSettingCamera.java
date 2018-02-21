package com.wxm.camerajob.ui.Utility.Setting;


import android.annotation.TargetApi;
import android.app.Dialog;
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
import android.support.v7.app.AlertDialog;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;

import com.wxm.camerajob.R;
import com.wxm.camerajob.data.define.CameraParam;
import com.wxm.camerajob.data.define.EAction;
import com.wxm.camerajob.data.define.GlobalDef;
import com.wxm.camerajob.utility.ContextUtil;
import com.wxm.camerajob.data.define.PreferencesUtil;
import com.wxm.camerajob.ui.Camera.CameraPreview.ACCameraPreview;

import java.util.ArrayList;
import java.util.Collections;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wxm.androidutil.type.MySize;
import wxm.androidutil.util.UtilFun;


/**
 * UI for camera setting
 * Created by 123 on 2016/10/10.
 */
public class TFSettingCamera extends TFSettingBase {
    @BindView(R.id.acrb_cs_frontcamera)
    RadioButton  mRBFrontCamera;

    @BindView(R.id.acrb_cs_backcamera)
    RadioButton  mRBBackCamera;

    @BindView(R.id.acsw_cs_autofocus)
    Switch       mSWAutoFocus;

    @BindView(R.id.acsw_cs_autoflash)
    Switch       mSWAutoFlash;

    @BindView(R.id.acsp_cs_dpi)
    Spinner      mSPPhotoSize;

    private ArrayAdapter<String>        mAAPhotoSize;

    private LinkedList<HashMap<String, String>>     mLLDpi;
    private LinkedList<HashMap<String, String>>     mLLBackCameraDpi;
    private LinkedList<HashMap<String, String>>     mLLFrontCameraDpi;

    private CameraParam         mCPBack;
    private CameraParam         mCPFront;

    @Override
    protected View inflaterView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        LOG_TAG = "TFSettingCamera";
        View rootView = inflater.inflate(R.layout.frg_setting_camera, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    protected void initUiComponent(View view) {
        mLLDpi = new LinkedList<>();
        mLLBackCameraDpi = new LinkedList<>();
        mLLFrontCameraDpi = new LinkedList<>();

        mCPBack = new CameraParam(null);
        mCPFront = new CameraParam(null);

        mSWAutoFlash.setOnCheckedChangeListener((buttonView, isChecked) -> mBSettingDirty = true);
        mSWAutoFocus.setOnCheckedChangeListener((buttonView, isChecked) -> mBSettingDirty = true);

        mAAPhotoSize = new ArrayAdapter<>(getContext(),
                R.layout.li_photo_size, R.id.ItemPhotoSize);
        mSPPhotoSize.setAdapter(mAAPhotoSize);
        mSPPhotoSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mBSettingDirty = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if(ContextUtil.useNewCamera())
            load_camerainfo_new();
        else
            load_camerainfo_old();

        // for camera preview
        RelativeLayout rl = UtilFun.cast_t(view.findViewById(R.id.rl_switch));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rl.setOnClickListener(v -> {
                if(0 < ContextUtil.GetCameraJobUtility().GetActiveJobCount()) {
                    Dialog alertDialog = new AlertDialog.Builder(getContext()).
                            setTitle("无法进行预览").
                            setMessage("有任务在运行中，请删除或暂停任务后进行预览!").
                            create();
                    alertDialog.show();
                }   else {
                    Intent it = new Intent(getActivity(), ACCameraPreview.class);
                    it.putExtra(EAction.LOAD_CAMERA_SETTING.getName(), get_cur_param());
                    startActivityForResult(it, 1);
                }
            });
        } else  {
            rl.setVisibility(View.GONE);
        }
    }

    @Override
    protected void loadUI() {
        CameraParam cp = PreferencesUtil.loadCameraParam();
        if(CameraCharacteristics.LENS_FACING_BACK == cp.mFace)  {
            mCPBack = cp.clone();
            fill_backcamera();
        }
        else    {
            mCPFront = cp.clone();
            fill_frontcamera();
        }
    }

    @OnClick({R.id.acrb_cs_backcamera, R.id.acrb_cs_frontcamera})
    public void onRadioButtonClick(View v)  {
        int vid = v.getId();
        switch (vid)    {
            case R.id.acrb_cs_backcamera : {
                mCPFront = get_cur_param().clone();
                fill_backcamera();

                mBSettingDirty = true;
            }
            break;

            case R.id.acrb_cs_frontcamera : {
                mCPBack = get_cur_param().clone();
                fill_frontcamera();

                mBSettingDirty = true;
            }
            break;
        }
    }


    @Override
    public void updateSetting() {
        if(mBSettingDirty)  {
            CameraParam cp = get_cur_param();
            PreferencesUtil.saveCameraParam(cp);

            mBSettingDirty = false;
        }
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
            cp.mPhotoSize = UtilFun.StringToSize(sel.toString());
        else
            cp.mPhotoSize = UtilFun.StringToSize(mSPPhotoSize.getItemAtPosition(0).toString());

        cp.mAutoFlash = mSWAutoFlash.isChecked();
        cp.mAutoFocus = mSWAutoFocus.isChecked();
        return cp;
    }

    /**
     * 加载旧版相机信息
     */
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
     * 加载新版系统相机信息
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void load_camerainfo_new() {
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
        CameraManager manager =
                (CameraManager) ContextUtil.getInstance().getSystemService(Context.CAMERA_SERVICE);
        if(null == manager)
            return;

        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if(null != map) {
                    mLLDpi.clear();

                    Size[] sz_arr = map.getOutputSizes(ImageFormat.JPEG);
                    ArrayList<MySize> mysz_ls = new ArrayList<>();
                    for(Size i : sz_arr)    {
                        mysz_ls.add(new MySize(i.getWidth(), i.getHeight()));
                    }

                    Collections.sort(mysz_ls, new CompareSizesByArea());
                    for (MySize sz : mysz_ls)    {
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
}
