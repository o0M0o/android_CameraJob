package com.wxm.camerajob.ui.acutility;

import android.content.Intent;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.ui.fragment.utility.FrgJobSlide;

import java.util.LinkedList;

import cn.wxm.andriodutillib.ExActivity.BaseAppCompatActivity;
import cn.wxm.andriodutillib.util.FileUtil;
import cn.wxm.andriodutillib.util.UtilFun;

/**
 * 幻灯片展示Job
 */
public class ACJobSlide extends BaseAppCompatActivity {

    @Override
    protected void leaveActivity() {
        int ret_data = GlobalDef.INTRET_USR_LOGOUT;

        Intent data = new Intent();
        setResult(ret_data, data);
        finish();
    }

    @Override
    protected void initFrgHolder() {
        LOG_TAG = "ACJobSlide";

        Intent it = getIntent();
        if (null == it)
            return;

        String sp = it.getStringExtra(GlobalDef.STR_LOAD_PHOTODIR);
        if (UtilFun.StringIsNullOrEmpty(sp))
            return;

        LinkedList<String> ll_photo = FileUtil.getDirFiles(sp, "jpg", false);
        if (UtilFun.ListIsNullOrEmpty(ll_photo))
            return;

        mFGHolder = FrgJobSlide.newInstance(ll_photo);
    }
}


