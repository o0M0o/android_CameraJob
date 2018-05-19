package com.wxm.camerajob.ui.Base

import android.widget.RelativeLayout
import android.widget.TextView
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.CameraParam

/**
 * help class for camera info
 * Created by 123 on 2016/11/9.
 */
object FrgCameraInfoHelper {
    /**
     * use cameraParam redraw layout
     * @param rl    instance for rl_camera_info
     * @param cp    data for redraw
     */
    fun refillLayout(rl: RelativeLayout, cp: CameraParam) {
        if (R.id.rl_camera_info != rl.id)
            return

        val mTVCameraFace = rl.findViewById<TextView>(R.id.tv_camera_face)
        val mTVCameraDpi = rl.findViewById<TextView>(R.id.tv_camera_dpi)
        val mTVCameraFlash = rl.findViewById<TextView>(R.id.tv_camera_flash)
        val mTVCameraFocus = rl.findViewById<TextView>(R.id.tv_camera_focus)

        val ct = rl.context
        mTVCameraFace.text = ct.getString(
                if (CameraParam.LENS_FACING_BACK == cp.mFace) R.string.cn_backcamera
                else R.string.cn_frontcamera)

        mTVCameraDpi.text = cp.mPhotoSize.toString()
        mTVCameraFlash.text = ct.getString(if (cp.mAutoFlash) R.string.cn_autoflash
        else R.string.cn_flash_no)
        mTVCameraFocus.text = ct.getString(if (cp.mAutoFocus) R.string.cn_autofocus
        else R.string.cn_focus_no)
    }
}
