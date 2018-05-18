package com.wxm.camerajob.ui.Utility.Setting


import android.annotation.TargetApi
import android.content.Intent
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.*
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.CameraParam
import com.wxm.camerajob.data.define.EAction
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.data.define.PreferencesUtil
import com.wxm.camerajob.ui.Base.EventHelper
import com.wxm.camerajob.ui.Camera.CameraPreview.ACCameraPreview
import com.wxm.camerajob.utility.DlgUtility
import com.wxm.camerajob.utility.ContextUtil
import kotterknife.bindView
import wxm.androidutil.type.MySize
import wxm.androidutil.util.UtilFun
import java.util.*


/**
 * UI for camera setting
 * Created by WangXM on 2016/10/10.
 */
class TFSettingCamera : TFSettingBase() {
    private val mRBFrontCamera: RadioButton by bindView(R.id.acrb_cs_frontcamera)
    private val mRBBackCamera: RadioButton by bindView(R.id.acrb_cs_backcamera)
    private val mSWAutoFocus: Switch by bindView(R.id.acsw_cs_autofocus)
    private val mSWAutoFlash: Switch by bindView(R.id.acsw_cs_autoflash)
    private val mSPPhotoSize: Spinner by bindView(R.id.acsp_cs_dpi)

    private var mAAPhotoSize: ArrayAdapter<String>? = null

    private val mLLBackCameraDpi: LinkedList<HashMap<String, String>> = LinkedList()
    private val mLLFrontCameraDpi: LinkedList<HashMap<String, String>> = LinkedList()

    private var mCPBack: CameraParam = CameraParam(null)
    private var mCPFront: CameraParam = CameraParam(null)

    override fun isUseEventBus(): Boolean = false
    override fun getLayoutID(): Int = R.layout.frg_setting_camera

    /**
     * 保存view当前的相机参数
     * @return  当前相机参数
     */
    private val curCameraParam: CameraParam
        get() {
            return CameraParam(null).apply {
                mFace = if (mRBBackCamera.isChecked) CameraParam.LENS_FACING_BACK
                else CameraParam.LENS_FACING_FRONT

                mSPPhotoSize.selectedItem.let {
                    mPhotoSize = UtilFun.StringToSize(it?.toString()
                            ?: mSPPhotoSize.getItemAtPosition(0).toString())
                }

                mAutoFlash = mSWAutoFlash.isChecked
                mAutoFocus = mSWAutoFocus.isChecked
            }
        }

    override fun initUI(savedInstanceState: Bundle?) {
        arrayOf(mSWAutoFlash, mSWAutoFocus).forEach {
            it.setOnCheckedChangeListener { _, _ -> isSettingDirty = true }
        }

        mAAPhotoSize = ArrayAdapter(context, R.layout.li_photo_size, R.id.ItemPhotoSize)
        mSPPhotoSize.adapter = mAAPhotoSize
        mSPPhotoSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                isSettingDirty = true
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        if (ContextUtil.useNewCamera())
            loadCameraInfo()

        EventHelper.setOnClickOperator(view!!,
                intArrayOf(R.id.acrb_cs_backcamera, R.id.acrb_cs_frontcamera, R.id.rl_switch),
                ::clickProcess)

        loadUI(savedInstanceState)
    }

    override fun loadUI(savedInstanceState: Bundle?) {
        PreferencesUtil.loadCameraParam().let {
            if (CameraCharacteristics.LENS_FACING_BACK == it.mFace) {
                mCPBack = it.clone()
                fillBackCamera()
            } else {
                mCPFront = it.clone()
                fillFrontCamera()
            }

            Unit
        }
    }

    private fun clickProcess(v: View)   {
        when(v.id)  {
            R.id.acrb_cs_backcamera -> {
                mCPFront = curCameraParam.clone()
                fillBackCamera()

                isSettingDirty = true
            }

            R.id.acrb_cs_frontcamera -> {
                mCPBack = curCameraParam.clone()
                fillFrontCamera()

                isSettingDirty = true
            }

            R.id.rl_switch -> {
                if (0 < ContextUtil.getCameraJobUtility().getActiveJobCount()) {
                    DlgUtility.showAlert(context, R.string.warn, R.string.info_need_stop_job)
                } else {
                    Intent(activity, ACCameraPreview::class.java).let {
                        it.putExtra(EAction.LOAD_CAMERA_SETTING.actName, curCameraParam)
                        startActivityForResult(it, 1)
                    }
                }
            }
        }
    }

    override fun updateSetting() {
        if (isSettingDirty) {
            val cp = curCameraParam
            PreferencesUtil.saveCameraParam(cp)

            isSettingDirty = false
        }
    }

    /**
     * 加载新版系统相机信息
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun loadCameraInfo() {
        class CompareSizesByArea : Comparator<MySize> {
            override fun compare(lhs: MySize, rhs: MySize): Int {
                // We cast here to ensure the multiplications won't overflow
                return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
            }
        }

        var mBackCameraID = ""
        var mFrontCameraID = ""
        ContextUtil.getCameraManager()?.let {
            val manager = it
            try {
                val lsDpi = LinkedList<HashMap<String, String>>()
                manager.cameraIdList.forEach {
                    val cameraId = it
                    lsDpi.clear()
                    manager.getCameraCharacteristics(it)?.let {
                        it.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.let {
                            lsDpi.addAll(it.getOutputSizes(ImageFormat.JPEG)
                                    .map { MySize(it.width, it.height) }
                                    .sortedWith(CompareSizesByArea())
                                    .map {
                                        HashMap<String, String>().apply {
                                            put(GlobalDef.STR_CAMERA_DPI, UtilFun.SizeToString(it))
                                        }
                                    }
                            )

                            Unit
                        }

                        // 前后相机只采用第一个
                        it.get(CameraCharacteristics.LENS_FACING)?.let {
                            if (CameraCharacteristics.LENS_FACING_BACK == it && mBackCameraID.isEmpty()) {
                                mBackCameraID = cameraId
                                mLLBackCameraDpi.addAll(lsDpi)
                            }

                            if (CameraCharacteristics.LENS_FACING_FRONT == it && mFrontCameraID.isEmpty()) {
                                mFrontCameraID = cameraId
                                mLLFrontCameraDpi.addAll(lsDpi)
                            }
                        }

                        Unit
                    }
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 用后置相机填充view
     */
    private fun fillBackCamera() {
        mRBBackCamera.isChecked = true
        mRBFrontCamera.isChecked = false

        mAAPhotoSize!!.let {
            it.clear()
            it.addAll(mLLBackCameraDpi.map { it[GlobalDef.STR_CAMERA_DPI] })
            it.notifyDataSetChanged()
        }

        fillOthers(mCPBack)
    }


    /**
     * 用前置相机填充view
     */
    private fun fillFrontCamera() {
        mRBBackCamera.isChecked = false
        mRBFrontCamera.isChecked = true

        mAAPhotoSize!!.let {
            it.clear()
            it.addAll(mLLFrontCameraDpi.map { it[GlobalDef.STR_CAMERA_DPI] })
            it.notifyDataSetChanged()
        }

        fillOthers(mCPFront)
    }

    /**
     * 填充公共部分
     * @param cp 填充参数
     */
    private fun fillOthers(cp: CameraParam) {
        mAAPhotoSize!!.getPosition(cp.mPhotoSize.toString()).let {
            mSPPhotoSize.setSelection(if (-1 == it) 0 else it)
        }

        mSWAutoFlash.isChecked = cp.mAutoFlash
        mSWAutoFocus.isChecked = cp.mAutoFocus
    }
}
