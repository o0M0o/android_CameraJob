package com.wxm.camerajob.ui.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.handler.GlobalContext;
import com.wxm.camerajob.base.utility.ContextUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActivityStart extends AppCompatActivity {
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

                mActivity.mLVList.add(map);
            }

            mActivity.mLVAdapter.notifyDataSetChanged();
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

        mLVAdapter= new SimpleAdapter(ContextUtil.getInstance(),
                mLVList,
                R.layout.listitem_jobstatus,
                new String[]{GlobalDef.STR_ITEM_TITLE, GlobalDef.STR_ITEM_TEXT},
                new int[]{R.id.ItemTitle, R.id.ItemText}) {
            @Override
            public int getViewTypeCount() {
                int org_ct = getCount();
                return org_ct < 1 ? 1 : org_ct;
            }

            @Override
            public int getItemViewType(int position) {
                return position;
            }
        };
        mLVJobs.setAdapter(mLVAdapter);
        mSelfHandler = new ACStartMsgHandler(this);

        updateJobs();

        /* 设置全局jobservice */
        Message m = Message.obtain(GlobalContext.getInstance().mMsgHandler,
                                    GlobalDef.MSGWHAT_JOB_ADD_GLOBAL);
        m.obj = this;
        m.sendToTarget();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(resultCode)  {
            case GlobalDef.INTRET_JOB_SAVE :    {
                CameraJob cj = data.getParcelableExtra(GlobalDef.STR_LOAD_JOB);
                Log.i(TAG, "job : " + cj.toString());

                Message m = Message.obtain(GlobalContext.getInstance().mMsgHandler,
                                            GlobalDef.MSGWHAT_CAMERAJOB_ADD);
                m.obj = cj;
                GlobalContext.getInstance().mMsgHandler.sendMessage(m);
            }
            break;

            default:    {
                Log.i(TAG, "不处理的 resultCode = " + resultCode);
            }
            break;
        }
    }
}
