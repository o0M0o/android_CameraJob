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
import android.widget.SimpleAdapter;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.CameraJobStatus;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.db.IDataChangeNotice;
import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.ui.acutility.ACJobGallery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.wxm.andriodutillib.util.FileUtil;
import cn.wxm.andriodutillib.util.UtilFun;

import static com.wxm.camerajob.base.handler.GlobalContext.GetDBManager;

/**
 * 相机预览fragment
 * Created by 123 on 2016/10/14.
 */
public class FrgJobShow extends Fragment {
    private final static String TAG = "FrgJobShow";
    private View mVWSelf;

    public final static String ALIVE_JOB    = "alive";
    public final static String DIED_JOB     = "died";

    public final static String KEY_JOB_NAME     = "job_name";
    public final static String KEY_JOB_DETAIL   = "job_detail";
    public final static String KEY_JOB_ACTIVE   = "job_active";
    public final static String KEY_STATUS       = "key_status";
    public final static String KEY_TYPE         = "key_type";
    public final static String KEY_ID           = "key_id";

    private FrgJobShowMsgHandler mSelfHandler;

    // for ui listview
    private ListView                            mLVJobs;
    private LVJobShowAdapter                    mLVAdapter;
    private ArrayList<HashMap<String, String>>  mLVList = new ArrayList<>();
    private Timer                               mTimer;

    /**
     *  任务数据变化回调类
     */
    private IDataChangeNotice mIDCJobNotice = new IDataChangeNotice() {
        @Override
        public void DataModifyNotice() {
            reLoadFrg(0);
        }

        @Override
        public void DataCreateNotice() {
            reLoadFrg(1500);
        }

        @Override
        public void DataDeleteNotice() {
            reLoadFrg(0);
        }

        private void reLoadFrg(long delayMs)    {
            mSelfHandler.sendEmptyMessageDelayed(GlobalDef.MSGWHAT_JOBSHOW_UPDATE, delayMs);
        }
    };


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

            // for notice
            GetDBManager().getCameraJobUtility().addDataChangeNotice(mIDCJobNotice);
            GetDBManager().getCameraJobStatusUtility().addDataChangeNotice(mIDCJobNotice);

            // set timer
            // 使用定时器定时全面刷新显示
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mSelfHandler.sendEmptyMessage(GlobalDef.MSGWHAT_JOBSHOW_UPDATE);
                }
            }, 5000, 10000);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        GetDBManager().getCameraJobUtility().removeDataChangeNotice(mIDCJobNotice);
        GetDBManager().getCameraJobStatusUtility().removeDataChangeNotice(mIDCJobNotice);

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
        mSelfHandler.refreshCameraJobs();
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
                new String[]{KEY_JOB_NAME, KEY_JOB_ACTIVE, KEY_JOB_DETAIL},
                new int[]{R.id.tv_job_name, R.id.tv_job_active,  R.id.tv_job_detail});

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

                // for imagebutton
                ImageButton ib_play = (ImageButton)v.findViewById(R.id.ib_job_run_or_pause);
                ImageButton ib_delete = (ImageButton)v.findViewById(R.id.ib_job_stop);

                String status = map.get(KEY_STATUS);
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
                        Integer.parseInt(map.get(KEY_ID)));
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
                    int id = Integer.parseInt(map.get(KEY_ID));
                    String type = map.get(KEY_TYPE);
                    if(ALIVE_JOB.equals(type))  {
                        GetDBManager().getCameraJobUtility().RemoveJob(id);
                    } else  {
                        String path = ContextUtil.getInstance().getCameraJobPhotoDir(id);
                        FileUtil.DeleteDirectory(path);

                        mSelfHandler.refreshCameraJobs();
                    }
                }
                break;

                case R.id.ib_job_run_or_pause:    {
                    int id = Integer.parseInt(map.get(KEY_ID));
                    CameraJob cj = GetDBManager().getCameraJobUtility().GetJob(id);
                    if(null != cj) {
                        CameraJobStatus cjs = cj.getStatus();
                        cjs.setJob_status(cjs.getJob_status().equals(GlobalDef.STR_CAMERAJOB_PAUSE) ?
                                GlobalDef.STR_CAMERAJOB_RUN : GlobalDef.STR_CAMERAJOB_PAUSE);
                        GetDBManager().getCameraJobStatusUtility().ModifyJobStatus(cjs);

                        mSelfHandler.refreshCameraJobs();
                    }
                }
                break;

                case R.id.ib_job_look:    {
                    String pp = ContextUtil.getInstance()
                            .getCameraJobPhotoDir(
                                    Integer.parseInt(map.get(KEY_ID)));

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
                case GlobalDef.MSGWHAT_JOBSHOW_UPDATE: {
                    refreshCameraJobs();
                }
                break;

                default:
                    Log.e(TAG, String.format("msg(%s) can not process", msg.toString()));
                    break;
            }
        }

        /**
         * 更新任务数据
         */
        void refreshCameraJobs() {
            mSelfList.clear();
            LinkedList<String> dirs = FileUtil.getDirDirs(
                                ContextUtil.getInstance().getAppPhotoRootDir(), false);
            List<CameraJob> lsjob = GetDBManager().getCameraJobUtility().GetJobs();
            if(!UtilFun.ListIsNullOrEmpty(lsjob))   {
                Collections.sort(lsjob, new Comparator<CameraJob>() {
                    @Override
                    public int compare(CameraJob lhs, CameraJob rhs) {
                        return lhs.get_id() - rhs.get_id();
                    }
                });
                for (CameraJob cj : lsjob) {
                    alive_camerjob(cj);

                    String dir = ContextUtil.getInstance().getCameraJobPhotoDir(cj.get_id());
                    dirs.remove(dir);
                }
            }

            if(!UtilFun.ListIsNullOrEmpty(dirs)) {
                Collections.sort(dirs, new Comparator<String>() {
                    @Override
                    public int compare(String lhs, String rhs) {
                        return lhs.compareTo(rhs);
                    }
                });

                for (String dir : dirs) {
                    died_camerajob(dir);
                }
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
            map.put(KEY_JOB_NAME, jobname);
            map.put(KEY_JOB_ACTIVE, "");
            map.put(KEY_JOB_DETAIL, show);
            map.put(KEY_ID,  Integer.toString(cj.get_id()));
            map.put(KEY_STATUS, GlobalDef.STR_CAMERAJOB_STOP);
            map.put(KEY_TYPE, DIED_JOB);
            mSelfList.add(map);
        }

        private void alive_camerjob(CameraJob cj)     {
            String at = String.format(Locale.CHINA
                    , "%s/%s\n%s -\n%s"
                    , cj.getType(), cj.getPoint()
                    , UtilFun.TimestampToString(cj.getStarttime()).substring(0, 16)
                    , UtilFun.TimestampToString(cj.getEndtime()).substring(0, 16));

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
            map.put(KEY_JOB_NAME, jobname);
            map.put(KEY_JOB_ACTIVE, at);
            map.put(KEY_JOB_DETAIL, detail);
            map.put(KEY_ID,  Integer.toString(cj.get_id()));
            map.put(KEY_STATUS, cj.getStatus().getJob_status());
            map.put(KEY_TYPE, ALIVE_JOB);
            mSelfList.add(map);
        }
    }
}

