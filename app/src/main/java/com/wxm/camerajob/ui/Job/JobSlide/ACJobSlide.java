package com.wxm.camerajob.ui.Job.JobSlide;

import android.content.Intent;

import com.wxm.camerajob.data.define.EAction;
import com.wxm.camerajob.data.define.GlobalDef;

import java.util.LinkedList;

import wxm.androidutil.ExActivity.BaseAppCompatActivity;
import wxm.androidutil.util.FileUtil;
import wxm.androidutil.util.UtilFun;

/**
 * slide show ui for job
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

        String sp = it.getStringExtra(EAction.LOAD_PHOTO_DIR.getName());
        if (UtilFun.StringIsNullOrEmpty(sp))
            return;

        LinkedList<String> ll_photo = FileUtil.getDirFiles(sp, "jpg", false);
        if (UtilFun.ListIsNullOrEmpty(ll_photo))
            return;

        mFGHolder = FrgJobSlide.newInstance(ll_photo);
    }
}


