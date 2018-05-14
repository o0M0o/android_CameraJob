package com.wxm.camerajob.ui.Utility.Setting


import android.annotation.TargetApi
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.ImageFormat
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Switch

import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.CameraParam
import com.wxm.camerajob.data.define.EAction
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.utility.ContextUtil
import com.wxm.camerajob.data.define.PreferencesUtil
import com.wxm.camerajob.ui.Camera.CameraPreview.ACCameraPreview

import java.util.ArrayList
import java.util.Collections

import java.util.Comparator
import java.util.HashMap
import java.util.LinkedList

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import wxm.androidutil.type.MySize
import wxm.androidutil.util.UtilFun


/**
 * UI for camera setting
 * Created by WangXM on 2016/10/10.
 */
class TFSettingCamera : TFSettingBase() {
    @BindView(R.id.acrb_cs_frontcamera)
    internal var mRBFrontCamera: RadioButton? = null

    @BindView(R.id.acrb_cs_backcamera)
    internal var mRBBackCamera: RadioButton? = null

    @BindView(R.id.acsw_cs_autofocus)
    internal var mSWAutoFocus: Switch? = null

    @BindView(R.id.acsw_cs_autoflash)
    internal var mSWAutoFlash: Switch? = null

    @BindView(R.id.acsp_cs_dpi)
    internal var mSPPhotoSize: Spinner? = null

    private var mAAPhotoSize: ArrayAdapter<String>? = null

    private var mLLDpi: LinkedList<HashMap<String, String>>? = null
    private var mLLBackCameraDpi: LinkedList<HashMap<String, String>>? = null
    private var mLLFrontCameraDpi: LinkedList<HashMap<String, String>>? = null

    private var mCPBack: CameraParam? = null
    private var mCPFront: CameraParam? = null


    /**
     * 保存view当前的相机参数
     * @return  当前相机参数
     */
    private val _cur_param: CameraParam
        get() {
            val cp = CameraParam(null)
            if (mRBBackCamera!!.isChecked)
                cp.mFace = CameraParam.LENS_FACING_BACK
            else
                cp.mFace = CameraParam.LENS_FACING_FRONT

            val sel = mSPPhotoSize!!.selectedItem
            if (null != sel)
                cp.mPhotoSize = UtilFun.StringToSize(sel.toString())
            else
                cp.mPhotoSize = UtilFun.StringToSize(mSPPhotoSize!!.getItemAtPosition(0).toString())

            cp.mAutoFlash = mSWAutoFlash!!.isChecked
            cp.mAutoFocus = mSWAutoFocus!!.isChecked
            return cp
        }

    override fun inflaterView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle): View {
        LOG_TAG = "TFSettingCamera"
        val rootView = inflater.inflate(R.layout.frg_setting_camera, container, false)
        ButterKnife.bind(this, rootView)
        return rootView
    }

    override fun initUiComponent(view: View) {
        mLLDpi = LinkedList()
        mLLBackCameraDpi = LinkedList()
        mLLFrontCameraDpi = LinkedList()

        mCPBack = CameraParam(null)
        mCPFront = CameraParam(null)

        mSWAutoFlash!!.setOnCheckedChangeListener { buttonView, isChecked -> isSettingDirty = true }
        mSWAutoFocus!!.setOnCheckedChangeListener { buttonView, isChecked -> isSettingDirty = true }

        mAAPhotoSize = ArrayAdapter(context,
                R.layout.li_photo_size, R.id.ItemPhotoSize)
        mSPPhotoSize!!.adapter = mAAPhotoSize
        mSPPhotoSize!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                isSettingDirty = true
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        if (ContextUtil.useNewCamera())
            load_camerainfo_new()
        else
            load_camerainfo_old()

        // for camera preview
        val rl = UtilFun.cast_t<RelativeLayout>(view.findViewById(R.id.rl_switch))
        rl.setOnClickListener { v ->
            if (0 < ContextUtil.getCameraJobUtility().getActiveJobCount()) {
                val alertDialog = AlertDialog.Builder(context).setTitle("无法进行预览").setMessage("有任务在运行中，请删除或暂停任务后进行预览!").create()
                alertDialog.show()
            } else {
                val it = Intent(activity, ACCameraPreview::class.java)
                it.putExtra(EAction.LOAD_CAMERA_SETTING.actName, _cur_param)
                startActivityForResult(it, 1)
            }
        }
    }

    override fun loadUI() {
        val cp = PreferencesUtil.loadCameraParam()
        if (CameraCharacteristics.LENS_FACING_BACK == cp.mFace) {
            mCPBack = cp.clone()
            fill_backcamera()
        } else {
            mCPFront = cp.clone()
            fill_frontcamera()
        }
    }

    @OnClick(R.id.acrb_cs_backcamera, R.id.acrb_cs_frontcamera)
    fun onRadioButtonClick(v: View) {
        val vid = v.id
        when (vid) {
            R.id.acrb_cs_backcamera -> {
                mCPFront = _cur_param.clone()
                fill_backcamera()

                isSettingDirty = true
            }

            R.id.acrb_cs_frontcamera -> {
                mCPBack = _cur_param.clone()
                fill_frontcamera()

                isSettingDirty = true
            }
        }
    }


    override fun updateSetting() {
        if (isSettingDirty) {
            val cp = _cur_param
            PreferencesUtil.saveCameraParam(cp)

            isSettingDirty = false
        }
    }

    /**
     * 加载旧版相机信息
     */
    private fun load_camerainfo_old() {
        class CompareSizesByArea : Comparator<MySize> {
            override fun compare(lhs: MySize, rhs: MySize): Int {
                // We cast here to ensure the multiplications won't overflow
                return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
            }
        }

        var mBackCameraID = ""
        var mFrontCameraID = ""

        val cc = Camera.getNumberOfCameras()
        val ci = Camera.CameraInfo()
        for (id in 0 until cc) {
            val ca = Camera.open(id)
            val cpa = ca.parameters

            val ls_sz = LinkedList<MySize>()
            for (cs in cpa.supportedPictureSizes) {
                ls_sz.add(MySize(cs.width, cs.height))
            }

            Collections.sort(ls_sz, CompareSizesByArea())
            for (sz in ls_sz) {
                val hmap = HashMap<String, String>()
                hmap[GlobalDef.STR_CAMERA_DPI] = sz.toString()
                mLLDpi!!.add(hmap)
            }

            // 前后相机只采用第一个
            Camera.getCameraInfo(id, ci)
            if (Camera.CameraInfo.CAMERA_FACING_BACK == ci.facing && mBackCameraID.isEmpty()) {
                mBackCameraID = Integer.toString(id)
                mLLBackCameraDpi!!.addAll(mLLDpi)
            }

            if (Camera.CameraInfo.CAMERA_FACING_FRONT == ci.facing && mFrontCameraID.isEmpty()) {
                mFrontCameraID = Integer.toString(id)
                mLLFrontCameraDpi!!.addAll(mLLDpi)
            }

            ca.release()
        }
    }

    /**
     * 加载新版系统相机信息
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun load_camerainfo_new() {
        class CompareSizesByArea : Comparator<MySize> {
            override fun compare(lhs: MySize, rhs: MySize): Int {
                // We cast here to ensure the multiplications won't overflow
                return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
            }
        }

        var mBackCameraID = ""
        var mFrontCameraID = ""
        val manager = ContextUtil.instance.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                ?: return

        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                val map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                if (null != map) {
                    mLLDpi!!.clear()

                    val sz_arr = map.getOutputSizes(ImageFormat.JPEG)
                    val mysz_ls = ArrayList<MySize>()
                    for (i in sz_arr) {
                        mysz_ls.add(MySize(i.width, i.height))
                    }

                    Collections.sort(mysz_ls, CompareSizesByArea())
                    for (sz in mysz_ls) {
                        val hmap = HashMap<String, String>()
                        hmap[GlobalDef.STR_CAMERA_DPI] = UtilFun.SizeToString(sz)
                        mLLDpi!!.add(hmap)
                    }
                }

                // 前后相机只采用第一个
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (null != facing) {
                    if (CameraCharacteristics.LENS_FACING_BACK == facing && mBackCameraID.isEmpty()) {
                        mBackCameraID = cameraId
                        mLLBackCameraDpi!!.addAll(mLLDpi)
                    }

                    if (CameraCharacteristics.LENS_FACING_FRONT == facing && mFrontCameraID.isEmpty()) {
                        mFrontCameraID = cameraId
                        mLLFrontCameraDpi!!.addAll(mLLDpi)
                    }
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    /**
     * 用后置相机填充view
     */
    private fun fill_backcamera() {
        mRBBackCamera!!.isChecked = true
        mRBFrontCamera!!.isChecked = false

        mAAPhotoSize!!.clear()
        for (map in mLLBackCameraDpi!!) {
            val dpi = map[GlobalDef.STR_CAMERA_DPI]
            mAAPhotoSize!!.add(dpi)
        }
        mAAPhotoSize!!.notifyDataSetChanged()

        fill_others(mCPBack!!)
    }


    /**
     * 用前置相机填充view
     */
    private fun fill_frontcamera() {
        mRBBackCamera!!.isChecked = false
        mRBFrontCamera!!.isChecked = true

        mAAPhotoSize!!.clear()
        for (map in mLLFrontCameraDpi!!) {
            val dpi = map[GlobalDef.STR_CAMERA_DPI]
            mAAPhotoSize!!.add(dpi)
        }
        mAAPhotoSize!!.notifyDataSetChanged()

        fill_others(mCPFront!!)
    }

    /**
     * 填充公共部分
     * @param cp 填充参数
     */
    private fun fill_others(cp: CameraParam) {
        val dpi = cp.mPhotoSize.toString()
        val pos = mAAPhotoSize!!.getPosition(dpi)
        if (-1 == pos) {
            mSPPhotoSize!!.setSelection(0)
        } else {
            mSPPhotoSize!!.setSelection(pos)
        }

        if (cp.mAutoFlash)
            mSWAutoFlash!!.isChecked = true
        else
            mSWAutoFlash!!.isChecked = false


        if (cp.mAutoFocus)
            mSWAutoFocus!!.isChecked = true
        else
            mSWAutoFocus!!.isChecked = false
    }
}
