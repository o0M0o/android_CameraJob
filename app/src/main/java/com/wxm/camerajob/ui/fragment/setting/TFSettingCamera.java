package com.wxm.camerajob.ui.fragment.setting;


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
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.CameraParam;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.handler.GlobalContext;
import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.base.utility.PreferencesUtil;
import com.wxm.camerajob.ui.acutility.ACCameraPreview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import cn.wxm.andriodutillib.type.MySize;
import cn.wxm.andriodutillib.util.UtilFun;

/**
 * 相机设置页面
 * Created by 123 on 2016/10/10.
 */
public class TFSettingCamera extends TFSettingBase {
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

    private Button mBTPreview;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_setting_camera, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (null != view) {
            CameraParam cp = PreferencesUtil.loadCameraParam();
            mHMCameras = new HashMap<>();
            mLLDpi = new LinkedList<>();
            mLLBackCameraDpi = new LinkedList<>();
            mLLFrontCameraDpi = new LinkedList<>();

            mCPBack = new CameraParam(null);
            mCPFront = new CameraParam(null);

            mRBBackCamera = UtilFun.cast(view.findViewById(R.id.acrb_cs_backcamera));
            mRBFrontCamera = UtilFun.cast(view.findViewById(R.id.acrb_cs_frontcamera));

            mSWAutoFocus = UtilFun.cast(view.findViewById(R.id.acsw_cs_autofocus));
            mSWAutoFlash = UtilFun.cast(view.findViewById(R.id.acsw_cs_autoflash));
            ContextUtil.throwExIf(null == mSWAutoFlash || null == mSWAutoFocus);
            mSWAutoFlash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mBSettingDirty = true;
                }
            });

            mSWAutoFocus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mBSettingDirty = true;
                }
            });

            mSPPhotoSize = UtilFun.cast(view.findViewById(R.id.acsp_cs_dpi));
            ContextUtil.throwExIf(null == mAAPhotoSize);
            mAAPhotoSize = new ArrayAdapter<>(getContext(),
                                R.layout.listitem_photosize, R.id.ItemPhotoSize);
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

            load_cameraparam(cp);
            mRBBackCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCPFront.Copy(get_cur_param());
                    fill_backcamera();

                    mBSettingDirty = true;
                }
            });

            mRBFrontCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCPBack.Copy(get_cur_param());
                    fill_frontcamera();

                    mBSettingDirty = true;
                }
            });

            // for camera preview
            mBTPreview = UtilFun.cast(view.findViewById(R.id.bt_preview));
            mBTPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(0 < GlobalContext.GetDBManager().mCameraJobHelper.GetActiveJobCount()) {
                        Dialog alertDialog = new AlertDialog.Builder(getContext()).
                                setTitle("无法进行预览").
                                setMessage("有任务在运行中，请删除或暂停任务后进行预览!").
                                create();
                        alertDialog.show();
                    }   else {
                        Intent it = new Intent(getActivity(), ACCameraPreview.class);
                        it.putExtra(GlobalDef.STR_LOAD_CAMERASETTING, get_cur_param());
                        startActivityForResult(it, 1);
                    }
                }
            });
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
}
