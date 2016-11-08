package com.wxm.camerajob.ui.fragment.utility;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.handler.GlobalContext;
import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.ui.acutility.ACJobGallery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.wxm.andriodutillib.util.FileUtil;
import cn.wxm.andriodutillib.util.UtilFun;

/**
 * 相机预览fragment
 * Created by 123 on 2016/10/14.
 */
public class FrgJobShow extends Fragment {
    private final static String TAG = "FrgJobShow";
    private View mVWSelf;

    public final static String ALIVE_JOB = "alive";
    public final static String DIED_JOB = "died";

    public final static String STR_ITEM_JOB_NAME    = "job_name";
    public final static String STR_ITEM_JOB_DETAIL  = "job_detail";
    public final static String STR_ITEM_JOB_ACTIVE  = "job_active";
    public final static String STR_ITEM_STATUS      = "ITEM_STATUS";
    public final static String STR_ITEM_TYPE        = "ITEM_TYPE";
    public final static String STR_ITEM_ID          = "ITEM_ID";

    private FrgJobShowMsgHandler mSelfHandler;

    // for ui listview
    private ListView                            mLVJobs;
    private LVJobShowAdapter                    mLVAdapter;
    private ArrayList<HashMap<String, String>>  mLVList = new ArrayList<>();
    private Timer                               mTimer;


    public static FrgJobShow newInstance() {
        return new FrgJobShow();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.vw_job_show, null);
        return v;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        if (null != view) {
            mVWSelf = view;
            init_ui();

            // set timer
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mSelfHandler.sendEmptyMessage(GlobalDef.MSGWHAT_ACSTART_UPDATEJOBS);
                }
            }, 5000, 5000);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
    }


    /**
     * 更新数据
     * @param lsdata 新数据
     */
    public void updateData(List<HashMap<String, String>> lsdata) {
        mLVList.clear();
        mLVList.addAll(lsdata);

        mLVAdapter.notifyDataSetChanged();
    }


    /**
     * 用现有数据重新绘制
     */
    public void refreshFrg()    {
        //mLVAdapter.notifyDataSetChanged();
    }


    /// BEGIN PRIVATE

    /**
     * 初始化UI
     */
    private void init_ui() {
        // init list view
        mLVJobs = UtilFun.cast_t(mVWSelf.findViewById(R.id.aclv_start_jobs));
        mLVAdapter= new LVJobShowAdapter(getContext(),
                mLVList,
                new String[]{STR_ITEM_JOB_NAME, STR_ITEM_JOB_DETAIL},
                new int[]{R.id.tv_job_name, R.id.tv_job_detail});

        mLVJobs.setAdapter(mLVAdapter);
        mSelfHandler = new FrgJobShowMsgHandler(this);
    }
    /// END PRIVATE


    /**
     * activity adapter
     * Created by wxm on 2016/8/13.
     */
    public class LVJobShowAdapter extends SimpleAdapter
            implements View.OnClickListener {

        LVJobShowAdapter(Context context, List<? extends Map<String, ?>> data,
                         String[] from, int[] to) {
            super(context, data, R.layout.li_job_show, from, to);
        }

        @Override
        public View getView(final int position, View view, ViewGroup arg2) {
            View v = super.getView(position, view, arg2);
            if(null != v)   {
                HashMap<String, String> map = mLVList.get(position);

                // for dead job
                if(DIED_JOB.equals(map.get(STR_ITEM_TYPE)))     {
                    RelativeLayout rl = UtilFun.cast_t(v.findViewById(R.id.rl_job_info_active));
                    ContextUtil.setViewGroupVisible(rl, View.INVISIBLE);
                } else  {
                    TextView tv = UtilFun.cast_t(v.findViewById(R.id.tv_job_active));
                    tv.setText(map.get(STR_ITEM_JOB_ACTIVE));
                }

                // other ui
                ImageButton ib_play = (ImageButton)v.findViewById(R.id.ib_job_run_or_pause);
                ImageButton ib_delete = (ImageButton)v.findViewById(R.id.ib_job_stop);

                String status = map.get(STR_ITEM_STATUS);
                switch (status) {
                    case GlobalDef.STR_CAMERAJOB_RUN:
                        ib_play.setVisibility(View.VISIBLE);

                        ib_play.setBackgroundResource(R.drawable.ic_pause);
                        ib_play.setClickable(true);
                        ib_play.setOnClickListener(this);
                        break;
                    case GlobalDef.STR_CAMERAJOB_PAUSE:
                        ib_play.setVisibility(View.VISIBLE);

                        ib_play.setBackgroundResource(R.drawable.ic_start);
                        ib_play.setClickable(true);
                        ib_play.setOnClickListener(this);
                        break;
                    default:
                        ib_play.setVisibility(View.INVISIBLE);
                        ib_play.setClickable(false);
                        break;
                }

                ib_delete.setOnClickListener(this);

                ImageButton ib_look = (ImageButton)v.findViewById(R.id.ib_job_look);
                String pp = ContextUtil.getInstance().getCameraJobPhotoDir(
                        Integer.parseInt(map.get(STR_ITEM_ID)));
                if(0 == FileUtil.getDirFilesCount(pp, "jpg", false)) {
                    ib_look.setVisibility(View.INVISIBLE);
                }
                else    {
                    ib_look.setVisibility(View.VISIBLE);
                    ib_look.setOnClickListener(this);
                }
            }

            return v;
        }

        @Override
        public void onClick(View v) {
            int pos = mLVJobs.getPositionForView(v);
            HashMap<String, String> map = mLVList.get(pos);

            switch (v.getId())  {
                case R.id.ib_job_stop: {
                    String type = map.get(STR_ITEM_TYPE);
                    Message m = Message.obtain(GlobalContext.getMsgHandlder(),
                                        type.equals(ALIVE_JOB) ?
                                                GlobalDef.MSGWHAT_CAMERAJOB_REMOVE
                                                : GlobalDef.MSGWHAT_CAMERAJOB_DELETE);

                    int id = Integer.parseInt(map.get(STR_ITEM_ID));
                    m.obj = new Object[]{mSelfHandler, id};
                    m.sendToTarget();
                }
                break;

                case R.id.ib_job_run_or_pause:    {
                    int id = Integer.parseInt(map.get(STR_ITEM_ID));
                    Message m;
                    m = Message.obtain(GlobalContext.getMsgHandlder(),
                            GlobalDef.MSGWHAT_CAMERAJOB_RUNPAUSESWITCH);
                    m.obj = new Object[] {mSelfHandler, id};
                    m.sendToTarget();
                }
                break;

                case R.id.ib_job_look:    {
                    String pp = ContextUtil.getInstance()
                            .getCameraJobPhotoDir(
                                    Integer.parseInt(map.get(STR_ITEM_ID)));

                    ACJobGallery jg = new ACJobGallery();
                    jg.OpenGallery(getActivity(), pp);
                }
                break;
            }
        }
    }


    /**
     * activity msg handler
     * Created by wxm on 2016/8/13.
     */
    private static class FrgJobShowMsgHandler extends Handler {
        private static final String TAG = "FrgJobShowMsgHandler";
        private ArrayList<HashMap<String, String>> mSelfList = new ArrayList<>();
        private FrgJobShow  mFrgHome;

        FrgJobShowMsgHandler(FrgJobShow home) {
            super();
            mFrgHome = home;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GlobalDef.MSGWHAT_CAMERAJOB_UPDATE :
                case GlobalDef.MSGWHAT_ACSTART_UPDATEJOBS : {
                    Message m = Message.obtain(GlobalContext.getMsgHandlder(),
                            GlobalDef.MSGWHAT_CAMERAJOB_ASKALL);
                    m.obj = this;
                    m.sendToTarget();
                }
                break;

                case GlobalDef.MSGWHAT_REPLAY :     {
                    if(GlobalDef.MSGWHAT_CAMERAJOB_ASKALL == msg.arg1) {
                        load_camerajobs(msg);
                    }
                }
                break;

                default:
                    Log.e(TAG, String.format("msg(%s) can not process", msg.toString()));
                    break;
            }
        }

        private void load_camerajobs(Message msg) {
            LinkedList<String> dirs = FileUtil.getDirDirs(
                    ContextUtil.getInstance().getAppPhotoRootDir(),
                    false);
            List<CameraJob> lsjob = UtilFun.cast(msg.obj);
            if(null != lsjob) {
                mSelfList.clear();
                for (CameraJob cj : lsjob) {
                    alive_camerjob(cj);

                    String dir = ContextUtil.getInstance().getCameraJobPhotoDir(cj.get_id());
                    if (!UtilFun.StringIsNullOrEmpty(dir))
                        dirs.remove(dir);
                }
            }

            for(String dir : dirs)  {
                died_camerajob(dir);
            }

            mFrgHome.updateData(mSelfList);
        }

        private void died_camerajob(String dir) {
            CameraJob cj = ContextUtil.getInstance().getCameraJobFromPath(dir);
            if(null == cj)
                return;

            String jobname = cj.getName() + "(已移除)";
            String show  = "可查看已拍摄图片\n可移除本任务文件";

            HashMap<String, String> map = new HashMap<>();
            map.put(STR_ITEM_JOB_NAME, jobname);
            map.put(STR_ITEM_JOB_DETAIL, show);
            map.put(STR_ITEM_ID,  Integer.toString(cj.get_id()));
            map.put(STR_ITEM_STATUS, GlobalDef.STR_CAMERAJOB_STOP);
            map.put(STR_ITEM_TYPE, DIED_JOB);
            mSelfList.add(map);
        }

        private void alive_camerjob(CameraJob cj)     {
            String at = String.format(Locale.CHINA
                    , "%s/%s\n启动 : %s\n结束 : %s"
                    , cj.getType(), cj.getPoint()
                    , UtilFun.TimestampToString(cj.getStarttime())
                    , UtilFun.TimestampToString(cj.getEndtime()));

            String jobname = cj.getName();
            String status = cj.getStatus().getJob_status().equals(GlobalDef.STR_CAMERAJOB_RUN) ?
                    "运行" : "暂停";
            jobname = jobname + "(" + status + ")";

            String detail;
            if(0 != cj.getStatus().getJob_photo_count()) {
                detail = String.format(Locale.CHINA, "已拍摄 : %d\n%s"
                                ,cj.getStatus().getJob_photo_count()
                                ,UtilFun.TimestampToString(cj.getStatus().getTs()));
            }
            else    {
                detail = String.format(Locale.CHINA, "已拍摄 : %d"
                        ,cj.getStatus().getJob_photo_count());
            }

            HashMap<String, String> map = new HashMap<>();
            map.put(STR_ITEM_JOB_NAME, jobname);
            map.put(STR_ITEM_JOB_ACTIVE, at);
            map.put(STR_ITEM_JOB_DETAIL, detail);
            map.put(STR_ITEM_ID,  Integer.toString(cj.get_id()));
            map.put(STR_ITEM_STATUS, cj.getStatus().getJob_status());
            map.put(STR_ITEM_TYPE, ALIVE_JOB);
            mSelfList.add(map);
        }
    }
}

