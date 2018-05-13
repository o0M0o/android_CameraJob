package com.wxm.camerajob.ui.Base;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wxm.camerajob.R;
import com.wxm.camerajob.data.define.CameraParam;

import wxm.androidutil.util.UtilFun;

/**
 * help class for camera info
 * Created by 123 on 2016/11/9.
 */
public class FrgCameraInfoHelper {
    /**
     * use cameraParam redraw layout
     * @param rl    instance for rl_camera_info
     * @param cp    data for redraw
     */
    public static void refillLayout(RelativeLayout rl, CameraParam cp)  {
        if(R.id.rl_camera_info != rl.getId())
            return;

        Context ct = rl.getContext();
        TextView mTVCameraFace = UtilFun.cast_t(rl.findViewById(R.id.tv_camera_face));
        TextView mTVCameraDpi = UtilFun.cast_t(rl.findViewById(R.id.tv_camera_dpi));
        TextView mTVCameraFlash = UtilFun.cast_t(rl.findViewById(R.id.tv_camera_flash));
        TextView mTVCameraFocus = UtilFun.cast_t(rl.findViewById(R.id.tv_camera_focus));

        mTVCameraFace.setText(rl.getContext().getString(CameraParam.LENS_FACING_BACK == cp.getMFace() ?
                R.string.cn_backcamera : R.string.cn_frontcamera));

        mTVCameraDpi.setText(cp.getMPhotoSize().toString());
        mTVCameraFlash.setText(ct.getString(cp.getMAutoFlash() ?
                        R.string.cn_autoflash : R.string.cn_flash_no));
        mTVCameraFocus.setText(ct.getString(cp.getMAutoFocus() ?
                        R.string.cn_autofocus : R.string.cn_focus_no));
    }
}
