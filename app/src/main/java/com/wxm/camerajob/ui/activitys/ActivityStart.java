package com.wxm.camerajob.ui.activitys;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.CameraJobStatus;
import com.wxm.camerajob.base.data.CameraParam;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.handler.GlobalContext;
import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.base.utility.PreferencesUtil;
import com.wxm.camerajob.base.utility.UtilFun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityStart
        extends AppCompatActivity
        implements View.OnClickListener {
    public class MySimpleAdapter extends SimpleAdapter {
        private ActivityStart mHome;

        public MySimpleAdapter(ActivityStart home, Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            mHome = home;
        }

        @Override
        public View getView(final int position, View view, ViewGroup arg2) {
            View v = super.getView(position, view, arg2);
            if(null != v)   {
                ImageButton ib_play = (ImageButton)v.findViewById(R.id.liib_jobstatus_run_pause);
                ImageButton ib_delete = (ImageButton)v.findViewById(R.id.liib_jobstatus_stop);

                ib_play.setOnClickListener(mHome);
                ib_delete.setOnClickListener(mHome);
            }

            return v;
        }
    }

    public class ACStartMsgHandler extends Handler {
        private static final String TAG = "ACStartMsgHandler";
        private ActivityStart mActivity;

        public ACStartMsgHandler(ActivityStart acstart) {
            super();
            mActivity = acstart;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GlobalDef.MSGWHAT_ANSWER_CAMERAJOB:
                    processor_answer_camerjob(msg);
                    break;

                case GlobalDef.MSGWHAT_CAMERAJOB_UPDATE :
                    processor_answer_camerjob(msg);
                    break;

                case GlobalDef.MSGWHAT_ACSTART_UPDATEJOBS :
                    updateJobs();
                    break;

                default:
                    Log.e(TAG, String.format("msg(%s) can not process", msg.toString()));
                    break;
            }
        }

        private void processor_answer_camerjob(Message msg) {
            Object[] obj_arr = (Object[])msg.obj;
            List<CameraJob> ls_job = (List<CameraJob>) obj_arr[0];
            List<CameraJobStatus> ls_jobstatus = (List<CameraJobStatus>) obj_arr[1];

            mActivity.mLVList.clear();
            for(CameraJob cj : ls_job)  {
                CameraJobStatus curjs = null;
                for(CameraJobStatus ji : ls_jobstatus)    {
                    if(ji.camerjob_id == cj._id)  {
                        curjs = ji;
                        break;
                    }
                }

                String show = String.format("周期/频度  : %s/%s\n开始时间 : %s\n结束时间 : %s"
                                        ,cj.job_type  ,cj.job_point
                                        ,UtilFun.TimestampToString(cj.job_starttime)
                                        ,UtilFun.TimestampToString(cj.job_endtime));

                if(null != curjs)   {
                    /*
                    show = String.format("%s\n执行成功%d次，失败%d次\n最后拍摄时间 : %s"
                                        ,show
                                        ,curjs.camerajob_photo_count ,0
                                        ,UtilFun.TimestampToString(curjs.ts));
                                        */
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
                map.put(GlobalDef.STR_ITEM_TITLE, "任务 : " + cj.job_name);
                map.put(GlobalDef.STR_ITEM_TEXT, show);
                map.put(GlobalDef.STR_ITEM_ID,  Integer.toString(cj._id));

                mActivity.mLVList.add(map);
            }
            mActivity.mLVAdapter.notifyDataSetChanged();
        }
    }

    private final static String TAG = "ActivityStart";
    private ACStartMsgHandler   mSelfHandler;

    // listview used to show jobs
    private ListView                            mLVJobs;
    private MySimpleAdapter                     mLVAdapter;
    private ArrayList<HashMap<String, String>>  mLVList = new ArrayList<>();

    private Timer                               mTimer;
    private TimerTask                           mTimerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // init view
        mLVJobs = (ListView) findViewById(R.id.aclv_start_jobs);

        mLVAdapter= new MySimpleAdapter(this,
                ContextUtil.getInstance(),
                mLVList,
                R.layout.listitem_jobstatus,
                new String[]{GlobalDef.STR_ITEM_TITLE, GlobalDef.STR_ITEM_TEXT},
                new int[]{R.id.ItemTitle, R.id.ItemText});

        mLVJobs.setAdapter(mLVAdapter);
        mSelfHandler = new ACStartMsgHandler(this);

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
    public void finish() {
        Log.i(TAG, "activity finished");
        super.finish();
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
        Message m = Message.obtain(GlobalContext.getInstance().mMsgHandler,
                GlobalDef.MSGWHAT_ASK_CAMERAJOB);
        m.obj = mSelfHandler;
        m.sendToTarget();
    }


    @Override
    public void onClick(View v) {
        int pos = mLVJobs.getPositionForView(v);
        HashMap<String, String> map = mLVList.get(pos);

        switch (v.getId())  {
            case R.id.liib_jobstatus_stop : {
                Message m = Message.obtain(GlobalContext.getInstance().mMsgHandler,
                        GlobalDef.MSGWHAT_CAMERAJOB_REMOVE);
                m.obj = new Object[]{map.get(GlobalDef.STR_ITEM_ID), mSelfHandler};
                m.sendToTarget();
            }
            break;
        }
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
}
