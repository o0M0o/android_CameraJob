package com.wxm.camerajob.ui.Utility.Setting;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.wxm.camerajob.R;
import com.wxm.camerajob.data.define.GlobalDef;
import com.wxm.camerajob.utility.utility.ContextUtil;

import cn.wxm.andriodutillib.ExActivity.BaseAppCompatActivity;
import cn.wxm.andriodutillib.util.UtilFun;

/**
 * 设置UI
 */
public class ACSetting
        extends BaseAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContextUtil.getInstance().addActivity(this);
    }

    @Override
    protected void leaveActivity() {
        int ret_data = GlobalDef.INTRET_USR_LOGOUT;

        Intent data = new Intent();
        setResult(ret_data, data);
        finish();
    }

    @Override
    protected void initFrgHolder() {
        LOG_TAG = "ACSetting";
        mFGHolder = new FrgSetting();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.acm_save_giveup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FrgSetting fs = UtilFun.cast_t(mFGHolder);
        switch (item.getItemId()) {
            case R.id.mi_save: {
                if(FrgSetting.PAGE_IDX_MAIN != fs.getCurrentItem()) {
                    final TFSettingBase tb = fs.getCurrentPage();
                    if(tb.isSettingDirty()) {
                        Dialog alertDialog = new AlertDialog.Builder(this).
                                setTitle("配置已经更改").
                                setMessage("是否保存更改的配置?").
                                setPositiveButton("是", (dialog, which) -> {
                                    tb.updateSetting();
                                    changePage(FrgSetting.PAGE_IDX_MAIN);
                                }).
                                setNegativeButton("否", (dialog, which) -> changePage(FrgSetting.PAGE_IDX_MAIN)).
                                create();
                        alertDialog.show();
                    } else  {
                        changePage(FrgSetting.PAGE_IDX_MAIN);
                    }
                } else  {
                    int ret_data = GlobalDef.INTRET_SURE;
                    Intent data = new Intent();
                    setResult(ret_data, data);
                    finish();
                }
            }
            break;

            case R.id.mi_giveup: {
                if (FrgSetting.PAGE_IDX_MAIN != fs.getCurrentItem()) {
                    changePage(FrgSetting.PAGE_IDX_MAIN);
                } else {
                    int ret_data = GlobalDef.INTRET_GIVEUP;
                    Intent data = new Intent();
                    setResult(ret_data, data);
                    finish();
                }
            }
            break;

            default:
                return super.onOptionsItemSelected(item);

        }

        return true;
    }

    /**
     * 切换到新页面
     * @param new_page 新页面postion
     */
    public void changePage(int new_page)  {
        FrgSetting fs = UtilFun.cast_t(mFGHolder);
        fs.setCurrentItem(new_page);
    }
}
