package com.wxm.camerajob.ui.activitys;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.CameraJobStatus;
import com.wxm.camerajob.base.data.CameraParam;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.handler.GlobalContext;
import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.base.utility.FileLogger;
import com.wxm.camerajob.base.utility.PreferencesUtil;
import com.wxm.camerajob.base.utility.UtilFun;
import com.wxm.camerajob.ui.activitys.test.ActivityTest;
import com.wxm.camerajob.ui.activitys.test.ActivityTestSilentCamera;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityNavStart
        extends AppCompatActivity
        implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "ActivityNavStart";
    private final static String ALIVE_JOB   = "alive";
    private final static String DIED_JOB    = "died";

    private ACNavStartMsgHandler   mSelfHandler;

    // listview used to show jobs
    private ListView                            mLVJobs;
    private MySimpleAdapter                     mLVAdapter;
    private ArrayList<HashMap<String, String>>  mLVList = new ArrayList<>();

    private Timer                               mTimer;
    private TimerTask                           mTimerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_nav_start);

        try {
            // set nav view
            Toolbar tb = (Toolbar) findViewById(R.id.ac_navw_toolbar);
            setSupportActionBar(tb);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.ac_start_outerlayout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, tb,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView nv = (NavigationView) findViewById(R.id.start_nav_view);
            nv.setNavigationItemSelectedListener(this);
        }
        catch (NullPointerException e) {
            FileLogger.getLogger().severe(UtilFun.ThrowableToString(e));
        }

        // init list view
        mLVJobs = (ListView) findViewById(R.id.aclv_start_jobs);
        mLVAdapter= new MySimpleAdapter(this,
                ContextUtil.getInstance(),
                mLVList,
                R.layout.listitem_jobstatus,
                new String[]{GlobalDef.STR_ITEM_TITLE, GlobalDef.STR_ITEM_TEXT},
                new int[]{R.id.ItemTitle, R.id.ItemText});

        mLVJobs.setAdapter(mLVAdapter);
        mSelfHandler = new ACNavStartMsgHandler(this);

        updateJobs();

        // set timer
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mSelfHandler.sendEmptyMessage(GlobalDef.MSGWHAT_ACSTART_UPDATEJOBS);
            }
        };

        mTimer.schedule(mTimerTask, 5000, 5000);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.acmeu_start_actbar, menu);
        return true;
    }


    /**
     * 加载并显示数据
     */
    public void updateJobs()  {
        mSelfHandler.sendEmptyMessage(GlobalDef.MSGWHAT_ACSTART_UPDATEJOBS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.meuitem_camerajob_add : {
                Intent intent = new Intent(this, ActivityJob.class);
                startActivityForResult(intent, 1);
            }
            break;

            case R.id.meuitem_camera_setting :  {
                Intent data = new Intent(this, ActivityCameraSetting.class);
                data.putExtra(GlobalDef.STR_LOAD_CAMERASETTING,
                        PreferencesUtil.loadCameraParam());

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
    public void onClick(View v) {
        int pos = mLVJobs.getPositionForView(v);
        HashMap<String, String> map = mLVList.get(pos);

        switch (v.getId())  {
            case R.id.liib_jobstatus_stop : {
                String type = map.get(GlobalDef.STR_ITEM_TYPE);
                if(type.equals(ALIVE_JOB)) {
                    ImageButton ib = (ImageButton) v;
                    Message m = Message.obtain(GlobalContext.getInstance().mMsgHandler,
                            GlobalDef.MSGWHAT_CAMERAJOB_REMOVE);
                    m.obj = new Object[]{map.get(GlobalDef.STR_ITEM_ID), mSelfHandler};
                    m.sendToTarget();
                }
                else    {
                    String id = map.get(GlobalDef.STR_ITEM_ID);
                    int _id = Integer.parseInt(id);
                    String path = ContextUtil.getInstance().getCameraJobPhotoDir(_id);
                    File f = new File(path);
                    if(f.isDirectory()) {
                        File[] childFiles = f.listFiles();
                        if (childFiles == null || childFiles.length == 0) {
                            f.delete();
                        } else  {
                            for(File ff : childFiles)   {
                                ff.delete();
                            }

                            f.delete();
                        }
                    }

                    updateJobs();
                }
            }
            break;

            case R.id.liib_jobstatus_run_pause :    {
                ImageButton ib = (ImageButton)v;

                ib.setClickable(false);
                String iid = map.get(GlobalDef.STR_ITEM_ID);
                CameraJobStatus cjs = GlobalContext.getInstance()
                        .mJobProcessor.getCameraJobStatus(Integer.parseInt(iid));
                if(null != cjs) {
                    if(cjs.camerajob_status.equals(GlobalDef.STR_CAMERAJOB_RUN))    {
                        Log.i(TAG, "camerjob(id = " + cjs.camerjob_id + ") will pause");
                        cjs.camerajob_status = GlobalDef.STR_CAMERAJOB_PAUSE;
                        ib.setBackgroundResource(android.R.drawable.ic_media_play);
                    }
                    else    {
                        Log.i(TAG, "camerjob(id = " + cjs.camerjob_id + ") will run");
                        cjs.camerajob_status = GlobalDef.STR_CAMERAJOB_RUN;
                        ib.setBackgroundResource(android.R.drawable.ic_media_pause);
                    }

                    GlobalContext.getInstance().mJobProcessor.modifyCameraJobStatus(cjs);
                } else  {
                    Log.e(TAG, "can not find camerjob status");
                }

                ib.setClickable(true);
            }
            break;

            case R.id.liib_jobstatus_look :    {
                Intent intent = new Intent(this, ActivityCameraJobPhotos.class);
                intent.putExtra(GlobalDef.STR_LOAD_PHOTODIR,
                        ContextUtil.getInstance()
                                .getCameraJobPhotoDir(
                                        Integer.parseInt(map.get(GlobalDef.STR_ITEM_ID))));
                startActivityForResult(intent, 1);
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(resultCode)  {
            case GlobalDef.INTRET_CAMERAJOB_ACCEPT:    {
                CameraJob cj = data.getParcelableExtra(GlobalDef.STR_LOAD_JOB);
                Log.i(TAG, "camerajob : " + cj.toString());

                Message m = Message.obtain(GlobalContext.getInstance().mMsgHandler,
                        GlobalDef.MSGWHAT_CAMERAJOB_ADD);
                m.obj = new Object[] {cj, mSelfHandler};
                m.sendToTarget();
            }
            break;

            case GlobalDef.INTRET_CS_ACCEPT:    {
                CameraParam cp = data.getParcelableExtra(GlobalDef.STR_LOAD_CAMERASETTING);
                Log.i(TAG, "cameraparam : " + cp.toString());
                PreferencesUtil.saveCameraParam(cp);

                Message m = Message.obtain(GlobalContext.getInstance().mMsgHandler,
                        GlobalDef.MSGWHAT_CS_CHANGECAMERA);
                m.obj = cp;
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
                /*Toast.makeText(getApplicationContext(),
                        "invoke help!",
                        Toast.LENGTH_SHORT).show();*/
            }
            break;

            case R.id.nav_setting :    {
                Toast.makeText(getApplicationContext(),
                        "invoke setting!",
                        Toast.LENGTH_SHORT).show();
            }
            break;

            case R.id.nav_share_app :    {
                Toast.makeText(getApplicationContext(),
                        "invoke share!",
                        Toast.LENGTH_SHORT).show();
            }
            break;

            case R.id.nav_contact_writer :    {
                contactWriter();
            }
            break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.ac_start_outerlayout);
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

    public class MySimpleAdapter extends SimpleAdapter {
        private ActivityNavStart mHome;

        public MySimpleAdapter(ActivityNavStart home,
                               Context context, List<? extends Map<String, ?>> data,
                               int resource, String[] from,
                               int[] to) {
            super(context, data, resource, from, to);
            mHome = home;
        }

        @Override
        public View getView(final int position, View view, ViewGroup arg2) {
            View v = super.getView(position, view, arg2);
            if(null != v)   {
                ImageButton ib_play = (ImageButton)v.findViewById(R.id.liib_jobstatus_run_pause);
                ImageButton ib_delete = (ImageButton)v.findViewById(R.id.liib_jobstatus_stop);
                ImageButton ib_look = (ImageButton)v.findViewById(R.id.liib_jobstatus_look);

                HashMap<String, String> map = mLVList.get(position);
                String iid = map.get(GlobalDef.STR_ITEM_ID);
                CameraJobStatus cjs = GlobalContext.getInstance()
                        .mJobProcessor.getCameraJobStatus(Integer.parseInt(iid));
                if(null != cjs) {
                    if(cjs.camerajob_status.equals(GlobalDef.STR_CAMERAJOB_RUN))    {
                        ib_play.setBackgroundResource(android.R.drawable.ic_media_pause);
                    }
                    else    {
                        Log.i(TAG, "camerjob(id = " + cjs.camerjob_id + ") will run");
                        cjs.camerajob_status = GlobalDef.STR_CAMERAJOB_RUN;
                        ib_play.setBackgroundResource(android.R.drawable.ic_media_play);
                    }
                } else  {
                    Log.e(TAG, "can not find camerjob status");
                }

                ib_play.setOnClickListener(mHome);
                ib_delete.setOnClickListener(mHome);
                ib_look.setOnClickListener(mHome);

                String type = map.get(GlobalDef.STR_ITEM_TYPE);
                if(type.equals(DIED_JOB))   {
                    //ib_play.setClickable(false);
                    ib_play.setVisibility(View.INVISIBLE);
                }
            }

            return v;
        }
    }

    public class ACNavStartMsgHandler extends Handler {
        private static final String TAG = "ACNavStartMsgHandler";
        private ActivityNavStart mActivity;

        public ACNavStartMsgHandler(ActivityNavStart acstart) {
            super();
            mActivity = acstart;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GlobalDef.MSGWHAT_ANSWER_CAMERAJOB:
                case GlobalDef.MSGWHAT_CAMERAJOB_UPDATE :
                case GlobalDef.MSGWHAT_ACSTART_UPDATEJOBS :
                    //processor_answer_camerjob(msg);
                    load_camerajobs();
                    break;

                default:
                    Log.e(TAG, String.format("msg(%s) can not process", msg.toString()));
                    break;
            }
        }

        private void load_camerajobs() {
            LinkedList<String> dirs = UtilFun.getDirDirs(
                    ContextUtil.getInstance().getAppPhotoRootDir(),
                    false);
            List<CameraJob> ls_job = GlobalContext.GetJobProcess().GetAllJobs();
            List<CameraJobStatus> ls_jobstatus = GlobalContext.GetJobProcess().GetAllJobStatus();

            mLVList.clear();
            for(CameraJob cj : ls_job)  {
                CameraJobStatus curjs = null;
                for(CameraJobStatus ji : ls_jobstatus)    {
                    if(ji.camerjob_id == cj._id)  {
                        curjs = ji;
                        break;
                    }
                }

                alive_camerjob(cj, curjs);

                String dir = ContextUtil.getInstance().getCameraJobPhotoDir(cj._id);
                if(!UtilFun.StringIsNullOrEmpty(dir))
                    dirs.remove(dir);
            }

            for(String dir : dirs)  {
                died_camerajob(dir);
            }

            mLVAdapter.notifyDataSetChanged();
        }

        private void died_camerajob(String dir) {
            CameraJob cj = ContextUtil.getInstance().getCameraJobFromPath(dir);
            if(null == cj)
                return;

            String jobname = "任务 : " + cj.job_name + "(已移除)";
            String show  = "可以查看本任务已获取图片\n可以移除本任务占据空间";

            HashMap<String, String> map = new HashMap<>();
            map.put(GlobalDef.STR_ITEM_TITLE, jobname);
            map.put(GlobalDef.STR_ITEM_TEXT, show);
            map.put(GlobalDef.STR_ITEM_ID,  Integer.toString(cj._id));
            map.put(GlobalDef.STR_ITEM_JOBNAME,  cj.job_name);
            map.put(GlobalDef.STR_ITEM_TYPE, DIED_JOB);
            mLVList.add(map);
        }

        private void alive_camerjob(CameraJob cj,  CameraJobStatus curjs)     {
            String show = String.format("周期/频度  : %s/%s\n开始时间 : %s\n结束时间 : %s"
                    ,cj.job_type  ,cj.job_point
                    ,UtilFun.TimestampToString(cj.job_starttime)
                    ,UtilFun.TimestampToString(cj.job_endtime));

            String jobname = "任务 : " + cj.job_name;
            if(null != curjs)   {
                    /*
                    show = String.format("%s\n执行成功%d次，失败%d次\n最后拍摄时间 : %s"
                                        ,show
                                        ,curjs.camerajob_photo_count ,0
                                        ,UtilFun.TimestampToString(curjs.ts));
                                        */
                jobname = jobname + "(" + curjs.camerajob_status + ")";
                if(0 != curjs.camerajob_photo_count) {
                    show = String.format("%s\n执行成功%d次\n最后拍摄时间 : %s"
                            , show, curjs.camerajob_photo_count
                            , UtilFun.TimestampToString(curjs.ts));
                }
                else    {
                    show = String.format("%s\n执行成功%d次"
                            , show, curjs.camerajob_photo_count);
                }
            }

            HashMap<String, String> map = new HashMap<>();
            map.put(GlobalDef.STR_ITEM_TITLE, jobname);
            map.put(GlobalDef.STR_ITEM_TEXT, show);
            map.put(GlobalDef.STR_ITEM_ID,  Integer.toString(cj._id));
            map.put(GlobalDef.STR_ITEM_JOBNAME,  cj.job_name);
            map.put(GlobalDef.STR_ITEM_TYPE, ALIVE_JOB);
            mLVList.add(map);
        }
    }
}
