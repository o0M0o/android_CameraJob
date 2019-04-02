package com.wxm.camerajob.ui.setting


import android.content.Intent
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.os.Bundle
import android.view.View
import android.widget.*
import com.wxm.camerajob.R
import com.wxm.camerajob.data.entity.CameraParam
import com.wxm.camerajob.data.define.EAction
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.preference.PreferencesUtil
import com.wxm.camerajob.ui.camera.preview.ACCameraPreview
import com.wxm.camerajob.App
import kotterknife.bindView
import wxm.androidutil.improve.let1
import wxm.androidutil.type.MySize
import wxm.androidutil.ui.dialog.DlgAlert
import wxm.androidutil.ui.view.EventHelper
import java.util.*


/**
 * UI for camera setting
 * Created by WangXM on 2016/10/10.
 */
class TFSettingCamera : TFSettingBase() {
    private val mRBFrontCamera: RadioButton by bindView(R.id.rb_front_camera)
    private val mRBBackCamera: RadioButton by bindView(R.id.rb_back_camera)
    private val mSWAutoFocus: Switch by bindView(R.id.sw_auto_focus)
    private val mSWAutoFlash: Switch by bindView(R.id.sw_auto_flash)
    private val mSPPhotoSize: Spinner by bindView(R.id.sp_cs_dpi)
    private val mSPCaptureTryCount: Spinner by bindView(R.id.sp_try_count)
    private val mSPCaptureSkipFrame: Spinner by bindView(R.id.sp_skip_count)

    private val mLLBackCameraDpi: LinkedList<HashMap<String, String>> = LinkedList()
    private val mLLFrontCameraDpi: LinkedList<HashMap<String, String>> = LinkedList()

    private var mCPBack: CameraParam = CameraParam(null)
    private var mCPFront: CameraParam = CameraParam(null)

    override fun isUseEventBus(): Boolean = false
    override fun getLayoutID(): Int = R.layout.pg_setting_camera

    /**
     * 保存view当前的相机参数
     * @return  当前相机参数
     */
    private val curCameraParam: CameraParam
        get() {
            return CameraParam(null).apply {
                mFace = if (mRBBackCamera.isChecked) CameraParam.LENS_FACING_BACK
                else CameraParam.LENS_FACING_FRONT

                mSPPhotoSize.selectedItem.let1 {
                    mPhotoSize = MySize.parseSize(it?.toString()
                            ?: mSPPhotoSize.getItemAtPosition(0).toString())
                }

                mSPCaptureTryCount.selectedItem?.let1 {
                    mCaptureTryCount = it as Int
                }

                mSPCaptureSkipFrame.selectedItem?.let1 {
                    mCaptureSkipFrame = it as Int
                }

                mAutoFlash = mSWAutoFlash.isChecked
                mAutoFocus = mSWAutoFocus.isChecked
            }
        }

    override fun initUI(savedInstanceState: Bundle?) {
        arrayOf(mSWAutoFlash, mSWAutoFocus).forEach {
            it.setOnCheckedChangeListener { _, _ -> isSettingDirty = true }
        }

        // for spinner
        val il = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                isSettingDirty = true
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        mSPPhotoSize.onItemSelectedListener = il
        mSPCaptureTryCount.onItemSelectedListener = il
        mSPCaptureSkipFrame.onItemSelectedListener = il

        loadCameraInfo()

        EventHelper.setOnClickOperator(view!!,
                intArrayOf(R.id.rb_back_camera, R.id.rb_front_camera, R.id.rl_switch),
                ::clickProcess)

        loadUI(savedInstanceState)
    }

    override fun loadUI(savedInstanceState: Bundle?) {
        PreferencesUtil.loadCameraParam().let1 {
            if (CameraCharacteristics.LENS_FACING_BACK == it.mFace) {
                mCPBack = it.clone()
                renderBackCamera()
            } else {
                mCPFront = it.clone()
                renderFrontCamera()
            }
        }
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(!hidden) {
            reloadUI()
        }
    }

    override fun updateSetting() {
        if (isSettingDirty) {
            PreferencesUtil.saveCameraParam(curCameraParam)
            isSettingDirty = false
        }
    }

    /// PRIVATE START

    private fun clickProcess(v: View)   {
        when(v.id)  {
            R.id.rb_back_camera -> {
                mCPFront = curCameraParam.clone()
                renderBackCamera()

                isSettingDirty = true
            }

            R.id.rb_front_camera -> {
                mCPBack = curCameraParam.clone()
                renderFrontCamera()

                isSettingDirty = true
            }

            R.id.rl_switch -> {
                if (0 < App.getCameraJobHelper().getActiveJobCount()) {
                    DlgAlert.showAlert(context!!, R.string.dlg_warn, R.string.info_need_stop_job)
                } else {
                    Intent(activity, ACCameraPreview::class.java).let {
                        it.putExtra(EAction.LOAD_CAMERA_SETTING.actName, curCameraParam)
                        startActivityForResult(it, 1)
                    }
                }
            }
        }
    }

    /**
     * 加载新版系统相机信息
     */
    private fun loadCameraInfo() {
        class CompareSizesByArea : Comparator<MySize> {
            override fun compare(lhs: MySize, rhs: MySize): Int {
                // We cast here to ensure the multiplications won't overflow
                return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
            }
        }

        mLLBackCameraDpi.clear()
        mLLFrontCameraDpi.clear()
        var mBackCameraID = ""
        var mFrontCameraID = ""
        App.getCameraManager()?.let {cm ->
            try {
                val lsDpi = LinkedList<HashMap<String, String>>()
                cm.cameraIdList.forEach {cameraID ->
                    lsDpi.clear()
                    cm.getCameraCharacteristics(cameraID).let1 {cc ->
                        cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.let1 {map ->
                            lsDpi.addAll(map.getOutputSizes(ImageFormat.JPEG)
                                    .map { MySize(it.width, it.height) }
                                    .sortedWith(CompareSizesByArea())
                                    .map {
                                        HashMap<String, String>().apply {
                                            put(GlobalDef.STR_CAMERA_DPI, it.toString())
                                        }
                                    }
                            )
                        }

                        // 前后相机只采用第一个
                        cc.get(CameraCharacteristics.LENS_FACING)?.let {face ->
                            if (CameraCharacteristics.LENS_FACING_BACK == face && mBackCameraID.isEmpty()) {
                                mBackCameraID = cameraID
                                mLLBackCameraDpi.addAll(lsDpi)
                            }

                            if (CameraCharacteristics.LENS_FACING_FRONT == face && mFrontCameraID.isEmpty()) {
                                mFrontCameraID = cameraID
                                mLLFrontCameraDpi.addAll(lsDpi)
                            }
                        }
                    }
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * render view when use back camera
     */
    private fun renderBackCamera() {
        mRBBackCamera.isChecked = true
        mRBFrontCamera.isChecked = false

        mSPPhotoSize.adapter = ArrayAdapter<String>(context!!, R.layout.li_photo_size, R.id.ItemPhotoSize).apply{
            addAll(mLLBackCameraDpi.map { it[GlobalDef.STR_CAMERA_DPI] })
        }

        renderOthers(mCPBack)
    }


    /**
     * render view when use front camera
     */
    private fun renderFrontCamera() {
        mRBBackCamera.isChecked = false
        mRBFrontCamera.isChecked = true

        mSPPhotoSize.adapter = ArrayAdapter<String>(context, R.layout.li_photo_size, R.id.ItemPhotoSize).apply{
            addAll(mLLFrontCameraDpi.map { it[GlobalDef.STR_CAMERA_DPI] })
        }

        renderOthers(mCPFront)
    }

    /**
     * render public view
     * [cp] is current camera
     */
    private fun renderOthers(cp: CameraParam) {
        @Suppress("UNCHECKED_CAST")
        (mSPPhotoSize.adapter as ArrayAdapter<String>).getPosition(cp.mPhotoSize.toString()).let {
            mSPPhotoSize.setSelection(if (-1 == it) 0 else it)
        }

        // try count
        mSPCaptureTryCount.adapter = ArrayAdapter<Int>(context!!, R.layout.li_photo_size, R.id.ItemPhotoSize).apply{
            addAll(5, 6, 7, 8, 9, 10)
        }

        @Suppress("UNCHECKED_CAST")
        (mSPCaptureTryCount.adapter as ArrayAdapter<Int>).getPosition(cp.mCaptureTryCount).let {
            mSPCaptureTryCount.setSelection(if (-1 == it) 0 else it)
        }

        // skip frame
        mSPCaptureSkipFrame.adapter = ArrayAdapter<Int>(context!!, R.layout.li_photo_size, R.id.ItemPhotoSize).apply{
            addAll(1, 2, 3, 4, 5)
        }

        @Suppress("UNCHECKED_CAST")
        (mSPCaptureSkipFrame.adapter as ArrayAdapter<Int>).getPosition(cp.mCaptureSkipFrame).let {
            mSPCaptureSkipFrame.setSelection(if (-1 == it) 0 else it)
        }

        // other
        mSWAutoFlash.isChecked = cp.mAutoFlash
        mSWAutoFocus.isChecked = cp.mAutoFocus
    }
    /// PRIVATE END
}
