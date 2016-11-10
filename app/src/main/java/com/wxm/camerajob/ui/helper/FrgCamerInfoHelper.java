package com.wxm.camerajob.ui.helper;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.CameraParam;

import cn.wxm.andriodutillib.util.UtilFun;

/**
 * 填充相机信息的辅助类
 * Created by 123 on 2016/11/9.
 */
public class FrgCamerInfoHelper {
    /**
     * 用新数据重绘layout
     * @param rl    必须是"rl_camera_info"
     * @param cp    待绘制参数
     */
    public static void refillLayout(RelativeLayout rl, CameraParam cp)  {
        if(R.id.rl_camera_info != rl.getId())
            return;

        Context ct = rl.getContext();
        TextView mTVCameraFace = UtilFun.cast_t(rl.findViewById(R.id.tv_camera_face));
        TextView mTVCameraDpi = UtilFun.cast_t(rl.findViewById(R.id.tv_camera_dpi));
        TextView mTVCameraFlash = UtilFun.cast_t(rl.findViewById(R.id.tv_camera_flash));
        TextView mTVCameraFocus = UtilFun.cast_t(rl.findViewById(R.id.tv_camera_focus));

        mTVCameraFace.setText(rl.getContext().getString(CameraParam.LENS_FACING_BACK == cp.mFace ?
                R.string.cn_backcamera : R.string.cn_frontcamera));

        mTVCameraDpi.setText(cp.mPhotoSize.toString());
        mTVCameraFlash.setText(ct.getString(cp.mAutoFlash ?
                        R.string.cn_autoflash : R.string.cn_flash_no));
        mTVCameraFocus.setText(ct.getString(cp.mAutoFocus?
                        R.string.cn_autofocus : R.string.cn_focus_no));
    }
}
