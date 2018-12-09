package com.wxm.camerajob.ui.base

import android.widget.RelativeLayout
import com.wxm.camerajob.R
import com.wxm.camerajob.data.entity.CameraParam
import wxm.androidutil.improve.doJudge
import wxm.androidutil.improve.let1
import wxm.androidutil.ui.view.ViewHelper

/**
 * help class for camera info
 * Created by WangXM on 2016/11/9.
 */
object FrgCameraInfoHelper {
    /**
     * use cameraParam redraw layout
     * @param rl    instance for rl_camera_info
     * @param cp    data for redraw
     */
    fun refillLayout(rl: RelativeLayout, cp: CameraParam) {
        if (R.id.rl_camera_info == rl.id) {
            ViewHelper(rl).let1 {
                it.setText(R.id.tv_camera_face, (CameraParam.LENS_FACING_BACK == cp.mFace)
                        .doJudge(R.string.cn_backcamera, R.string.cn_frontcamera))
                it.setText(R.id.tv_camera_dpi, cp.mPhotoSize.toString())
                it.setText(R.id.tv_camera_flash, cp.mAutoFlash
                        .doJudge(R.string.cn_autoflash, R.string.cn_flash_no))
                it.setText(R.id.tv_camera_focus, cp.mAutoFocus
                        .doJudge(R.string.cn_autofocus, R.string.cn_focus_no))
            }
        }
    }
}
