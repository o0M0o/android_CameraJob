package com.wxm.camerajob.ui.acinterface;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.data.PreferencesUtil;
import com.wxm.camerajob.base.handler.GlobalContext;
import com.wxm.camerajob.base.utility.CameraJobUtility;
import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.base.utility.FileLogger;
import com.wxm.camerajob.ui.acutility.ACCameraSetting;
import com.wxm.camerajob.ui.acutility.ACSetting;
import com.wxm.camerajob.ui.fragment.utility.FrgJobShow;
import com.wxm.camerajob.ui.test.ActivityTest;
import com.wxm.camerajob.ui.test.ActivityTestSilentCamera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import cn.wxm.andriodutillib.util.UtilFun;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WAKE_LOCK;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ACNavStart
        extends AppCompatActivity
        implements  NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "ACNavStart";

    private final FrgJobShow mFRGJobShow = FrgJobShow.newInstance();

    private static final int REQUEST_ALL  = 99;
    private ACNavStartMsgHandler    mSelfHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_nav_start);
        ContextUtil.getInstance().addActivity(this);

        mSelfHandler = new ACNavStartMsgHandler(this);
        if(mayRequestPermission()) {
            initActivity();
        }

        if(null == savedInstanceState)  {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_job_show, mFRGJobShow);
            transaction.commit();
        }
    }

    private void initActivity() {
        try {
            // set nav view
            Toolbar tb = (Toolbar) findViewById(R.id.ac_navw_toolbar);
            setSupportActionBar(tb);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.ac_start_outerlayout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, tb,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close);
            assert drawer != null;
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView nv = (NavigationView) findViewById(R.id.start_nav_view);
            assert nv != null;
            nv.setNavigationItemSelectedListener(this);
        }
        catch (NullPointerException e) {
            FileLogger.getLogger().severe(UtilFun.ThrowableToString(e));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        mFRGJobShow.refreshFrg();
    }

    private boolean mayRequestPermission() {
        ArrayList<String> ls_str = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ls_str.add(WRITE_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(this, CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ls_str.add(CAMERA);
        }

        if (ContextCompat.checkSelfPermission(this, WAKE_LOCK)
                != PackageManager.PERMISSION_GRANTED) {
            ls_str.add(WAKE_LOCK);
        }


        if(ls_str.isEmpty()) {
            ContextUtil.getInstance().initAppContext();
            return true;
        }

        String[] str_arr = ls_str.toArray(new String[ls_str.size()]);
        ActivityCompat.requestPermissions(this, str_arr, REQUEST_ALL);
        return false;
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ALL) {
            boolean ct = true;
            for(int pos = 0; pos < grantResults.length; pos++)      {
                if(grantResults[pos] != PackageManager.PERMISSION_GRANTED)  {
                    ct = false;
                    String msg = String.format(Locale.CHINA,
                                    "由于缺少必须的权限(%s)，本APP无法运行!",
                                    permissions[pos]);

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(msg)
                            .setTitle("警告")
                            .setCancelable(false)
                            .setPositiveButton("离开应用", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ContextUtil.getInstance().onTerminate();
                                }
                            });

                    AlertDialog dlg = builder.create();
                    dlg.show();
                }
            }

            if(ct) {
                ContextUtil.getInstance().initAppContext();
                initActivity();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.acmeu_start_actbar, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.meuitem_camerajob_add : {
                Intent intent = new Intent(this, ACJobCreate.class);
                startActivityForResult(intent, 1);
            }
            break;

            case R.id.meuitem_setting :  {
                Intent data = new Intent(this, ACSetting.class);
                startActivityForResult(data, 1);
            }
            break;

            case R.id.meuitem_camera_setting :  {
                Intent data = new Intent(this, ACCameraSetting.class);
                startActivityForResult(data, 1);
            }
            break;

            case R.id.meuitem_camera_test : {
                Intent data =  new Intent(this, ActivityTest.class);
                startActivityForResult(data, 1);
            }
            break;

            case R.id.meuitem_silentcamera_test :   {
                Intent data =  new Intent(this, ActivityTestSilentCamera.class);
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
                Log.i(TAG, "camerajob : " + cj.toString());

                CameraJobUtility.createCameraJob(cj);
            }
            break;

            case GlobalDef.INTRET_CS_ACCEPT:    {
                Message m = Message.obtain(GlobalContext.getMsgHandlder(),
                        GlobalDef.MSGWHAT_CS_CHANGECAMERA);
                m.obj = PreferencesUtil.loadCameraParam();
                m.sendToTarget();
            }
            break;

            default:    {
                Log.i(TAG, "不处理的 resultCode = " + resultCode);
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
//                Toast.makeText(getApplicationContext(),
//                        "invoke setting!",
//                        Toast.LENGTH_SHORT).show();
            }
            break;

            case R.id.nav_share_app :    {
//                Toast.makeText(getApplicationContext(),
//                        "invoke share!",
//                        Toast.LENGTH_SHORT).show();
            }
            break;

            case R.id.nav_contact_writer :    {
                contactWriter();
            }
            break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.ac_start_outerlayout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void contactWriter()    {
        Resources res = getResources();

        Intent data=new Intent(Intent.ACTION_SENDTO);
        Uri ud = Uri.parse(
                        String.format("mailto:%s", res.getString(R.string.contact_email)));
        data.setData(ud);
        //data.putExtra(Intent.EXTRA_SUBJECT, "这是标题");
        //data.putExtra(Intent.EXTRA_TEXT, "这是内容");
        startActivity(data);
    }


    public static class ACNavStartMsgHandler extends Handler {
        private static final String TAG = "FrgJobShowMsgHandler";
        private ACNavStart mActivity;
        private ArrayList<HashMap<String, String>> mLVList = new ArrayList<>();

        ACNavStartMsgHandler(ACNavStart acstart) {
            super();
            mActivity = acstart;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GlobalDef.MSGWHAT_CAMERAJOB_UPDATE :
                case GlobalDef.MSGWHAT_JOBSHOW_UPDATE: {
                    Message m = Message.obtain(GlobalContext.getMsgHandlder(),
                            GlobalDef.MSGWHAT_CAMERAJOB_ASKALL);
                    m.obj = this;
                    m.sendToTarget();
                }
                break;

                default:
                    Log.e(TAG, String.format("msg(%s) can not process", msg.toString()));
                    break;
            }
        }
    }
}
