package com.wxm.camerajob.ui.Job.JobShow;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.wxm.camerajob.BuildConfig;
import com.wxm.camerajob.ui.Job.JobCreate.ACJobCreate;
import com.wxm.camerajob.R;
import com.wxm.camerajob.data.define.CameraJob;
import com.wxm.camerajob.data.define.GlobalDef;
import com.wxm.camerajob.ui.dialog.DlgUsrMessage;
import com.wxm.camerajob.utility.CameraJobUtility;
import com.wxm.camerajob.utility.ContextUtil;
import com.wxm.camerajob.utility.FileLogger;
import com.wxm.camerajob.ui.Utility.Help.ACHelp;
import com.wxm.camerajob.ui.Camera.CameraSetting.ACCameraSetting;
import com.wxm.camerajob.ui.Utility.Setting.ACSetting;
import com.wxm.camerajob.ui.Test.Camera.ACCameraTest;
import com.wxm.camerajob.ui.Test.Camera.ACSilentCameraTest;

import butterknife.BindView;
import butterknife.ButterKnife;
import wxm.androidutil.Dialog.DlgOKOrNOBase;
import wxm.androidutil.util.UtilFun;

/**
 * UI(main UI for app) for show job status
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class ACJobShow
        extends AppCompatActivity
        implements  NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "ACJobShow";
    private final FrgJobShow mFRGJobShow = new FrgJobShow();

    @BindView(R.id.ac_navw_toolbar)
    Toolbar mTBNavw;

    @BindView(R.id.ac_start_outerlayout)
    DrawerLayout mDLOuterLayout;

    @BindView(R.id.start_nav_view)
    NavigationView mNVNav;

    /**
     * 如果有权限，则直接初始化实例
     * 如果无权限，则申请权限后，初始化实例
     * @param savedInstanceState   param
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_job_show);
        ContextUtil.getInstance().addActivity(this);

        ButterKnife.bind(this);
        initActivity();
        if(null == savedInstanceState) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_job_show, mFRGJobShow);
            transaction.commit();
        }
    }

    private void initActivity() {
        // set nav view
        try {
            setSupportActionBar(mTBNavw);

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, mDLOuterLayout, mTBNavw,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close);
            mDLOuterLayout.addDrawerListener(toggle);
            toggle.syncState();

            mNVNav.setNavigationItemSelectedListener(this);
        } catch (NullPointerException e) {
            FileLogger.getLogger().severe(UtilFun.ThrowableToString(e));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        mFRGJobShow.refreshFrg();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.acm_job_show, menu);

        if(!BuildConfig.TestCamera)  {
            MenuItem mi = menu.findItem(R.id.mi_camera_test);
            mi.setVisible(false);

            mi = menu.findItem(R.id.mi_silentcamera_test);
            mi.setVisible(false);
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_job_add: {
                Intent intent = new Intent(this, ACJobCreate.class);
                startActivityForResult(intent, 1);
            }
            break;

            case R.id.mi_setting:  {
                Intent data = new Intent(this, ACSetting.class);
                startActivityForResult(data, 1);
            }
            break;

            case R.id.mi_camera_setting:  {
                Intent data = new Intent(this, ACCameraSetting.class);
                startActivityForResult(data, 1);
            }
            break;

            case R.id.mi_camera_test: {
                Intent data =  new Intent(this, ACCameraTest.class);
                startActivityForResult(data, 1);
            }
            break;

            case R.id.mi_silentcamera_test:   {
                Intent data =  new Intent(this, ACSilentCameraTest.class);
                startActivityForResult(data, 1);
            }

            default:
                return super.onOptionsItemSelected(item);

        }

        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(resultCode)  {
            case GlobalDef.INTRET_CAMERAJOB_ACCEPT:    {
                CameraJob cj = data.getParcelableExtra(GlobalDef.STR_LOAD_JOB);
                Log.d(TAG, "camerajob : " + cj.toString());

                CameraJobUtility.createCameraJob(cj);
            }
            break;

            case GlobalDef.INTRET_CS_ACCEPT:    {
                /*Message m = Message.obtain(GlobalContext.getMsgHandlder(),
                        GlobalDef.MSG_TYPE_CAMERA_MODIFY);
                m.obj = PreferencesUtil.loadCameraParam();
                m.sendToTarget(); */
            }
            break;

            default:    {
                Log.v(TAG, "not match resultCode = " + resultCode);
            }
            break;
        }
    }



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id)  {
            case R.id.nav_help :    {
                Intent d = new Intent(this, ACHelp.class);
                d.putExtra(GlobalDef.STR_HELP_TYPE, GlobalDef.STR_HELP_MAIN);
                startActivityForResult(d, 1);
            }
            break;

            case R.id.nav_setting :    {
            }
            break;

            case R.id.nav_share_app :    {
            }
            break;

            case R.id.nav_contact_writer :    {
                contactWriter();
            }
            break;
        }

        mDLOuterLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 给作者留言
     */
    private void contactWriter()    {
        DlgUsrMessage dlg = new DlgUsrMessage();
        dlg.addDialogListener(new DlgOKOrNOBase.DialogResultListener() {
            @Override
            public void onDialogPositiveResult(DialogFragment dialogFragment) {
            }

            @Override
            public void onDialogNegativeResult(DialogFragment dialogFragment) {
            }
        });

        dlg.show(getSupportFragmentManager(), "send message");
    }
}
