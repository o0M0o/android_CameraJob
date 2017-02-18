package com.wxm.camerajob.ui.acutility;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.wxm.camerajob.R;
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
        if(null == it)
            return;

        String sp = it.getStringExtra(GlobalDef.STR_LOAD_PHOTODIR);
        if(UtilFun.StringIsNullOrEmpty(sp))
            return;

        LinkedList<String> ll_photo = FileUtil.getDirFiles(sp, "jpg", false);
        if(UtilFun.ListIsNullOrEmpty(ll_photo))
            return;

        mFGHolder = FrgJobSlide.newInstance(ll_photo);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.acm_leave, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_leave  : {
                Intent data = new Intent();
                setResult(GlobalDef.INTRET_CS_GIVEUP, data);
                finish();
            }
            break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }}
