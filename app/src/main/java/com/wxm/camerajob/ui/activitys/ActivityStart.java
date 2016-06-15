package com.wxm.camerajob.ui.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.handler.GlobalContext;
import com.wxm.camerajob.base.utility.ContextUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActivityStart
        extends AppCompatActivity
        implements View.OnClickListener {
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

                default:
                    Log.e(TAG, String.format("msg(%s) can not process", msg.toString()));
                    break;
            }
        }


        private void processor_answer_camerjob(Message msg) {
            List<CameraJob> ls_job = (List<CameraJob>) msg.obj;

            mActivity.mLVList.clear();
            for(CameraJob cj : ls_job)  {
                String show = String.format("周期 : %s\n频度 : %s\n结束时间 : %s"
                                        ,cj.job_type
                                        ,cj.job_point
                                        ,"2016-06-17 23:59");

                HashMap<String, String> map = new HashMap<>();
                map.put(GlobalDef.STR_ITEM_TITLE, "任务 : " + cj.job_name);
                map.put(GlobalDef.STR_ITEM_TEXT, show);
                map.put(GlobalDef.STR_ITEM_ID,  Integer.toString(cj._id));

                mActivity.mLVList.add(map);
            }
            mActivity.mLVAdapter.notifyDataSetChanged();

//            int ct = mActivity.mLVJobs.getChildCount();
//            for(int i = 1; i < ct; ++i) {
//                View v = mActivity.mLVJobs.getChildAt(i);
//
//                ImageButton ib_play = (ImageButton)v.findViewById(R.id.liib_jobstatus_run_pause);
//                ImageButton ib_delete = (ImageButton)v.findViewById(R.id.liib_jobstatus_stop);
//
//                ib_play.setOnClickListener(mActivity);
//                ib_delete.setOnClickListener(mActivity);
//            }
        }
    }

    private final static String TAG = "ActivityStart";
    private ACStartMsgHandler   mSelfHandler;

    // listview used to show jobs
    private ListView                            mLVJobs;
    private SimpleAdapter                       mLVAdapter;
    private ArrayList<HashMap<String, String>>  mLVList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //final Activity home = (Activity)this;
        // init view
        mLVJobs = (ListView) findViewById(R.id.aclv_start_jobs);

        final ActivityStart home = this;
        mLVAdapter= new SimpleAdapter(ContextUtil.getInstance(),
                mLVList,
                R.layout.listitem_jobstatus,
                new String[]{GlobalDef.STR_ITEM_TITLE, GlobalDef.STR_ITEM_TEXT},
                new int[]{R.id.ItemTitle, R.id.ItemText}) {
            @Override
            public View getView(final int position, View view, ViewGroup arg2) {
                if(view == null){
                    LayoutInflater inflater = LayoutInflater.from(ContextUtil.getInstance());
                    view = inflater.inflate(R.layout.listitem_jobstatus, null);
                }

                HashMap<String, String> htxt = mLVList.get(position);
                TextView title = (TextView)view.findViewById(R.id.ItemTitle);
                TextView subtitle = (TextView)view.findViewById(R.id.ItemText);
                title.setText(htxt.get(GlobalDef.STR_ITEM_TITLE));
                subtitle.setText(htxt.get(GlobalDef.STR_ITEM_TEXT));

                ImageButton ib_play = (ImageButton)view.findViewById(R.id.liib_jobstatus_run_pause);
                ImageButton ib_delete = (ImageButton)view.findViewById(R.id.liib_jobstatus_stop);

                ib_play.setOnClickListener(home);
                ib_delete.setOnClickListener(home);

                return view;
            }};

        mLVJobs.setAdapter(mLVAdapter);
        mSelfHandler = new ACStartMsgHandler(this);

        updateJobs();

        /*
        // 设置全局jobservice
        Message m = Message.obtain(GlobalContext.getInstance().mMsgHandler,
                                    GlobalDef.MSGWHAT_JOB_ADD_GLOBAL);
        m.obj = this;
        m.sendToTarget();
        */
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

            default:
                return super.onOptionsItemSelected(item);

        }

        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(resultCode)  {
            case GlobalDef.INTRET_JOB_SAVE :    {
                CameraJob cj = data.getParcelableExtra(GlobalDef.STR_LOAD_JOB);
                Log.i(TAG, "job : " + cj.toString());

                Message m = Message.obtain(GlobalContext.getInstance().mMsgHandler,
                                            GlobalDef.MSGWHAT_CAMERAJOB_ADD);
                m.obj = new Object[] {cj, mSelfHandler};
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
