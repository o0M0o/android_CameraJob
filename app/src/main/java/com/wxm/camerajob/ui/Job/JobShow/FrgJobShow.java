package com.wxm.camerajob.ui.Job.JobShow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

import com.wxm.camerajob.R;
import com.wxm.camerajob.data.define.CameraJob;
import com.wxm.camerajob.data.define.CameraJobStatus;
import com.wxm.camerajob.data.define.DBDataChangeEvent;
import com.wxm.camerajob.data.define.EAction;
import com.wxm.camerajob.data.define.EMsgType;
import com.wxm.camerajob.data.define.GlobalDef;
import com.wxm.camerajob.data.define.EJobStatus;
import com.wxm.camerajob.data.define.PreferencesChangeEvent;
import com.wxm.camerajob.data.define.PreferencesUtil;
import com.wxm.camerajob.utility.CameraJobUtility;
import com.wxm.camerajob.utility.ContextUtil;
import com.wxm.camerajob.ui.Base.JobGallery;
import com.wxm.camerajob.ui.Job.JobSlide.ACJobSlide;
import com.wxm.camerajob.ui.Base.FrgCameraInfoHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import wxm.androidutil.FrgUtility.FrgUtilitySupportBase;
import wxm.androidutil.util.FileUtil;
import wxm.androidutil.util.UtilFun;


/**
 * fragment for show job
 * Created by 123 on 2016/10/14.
 */
public class FrgJobShow extends FrgUtilitySupportBase {
    public final static String ALIVE_JOB    = "alive";
    public final static String DIED_JOB     = "died";

    public final static String KEY_JOB_NAME     = "job_name";
    public final static String KEY_JOB_DETAIL   = "job_detail";
    public final static String KEY_JOB_ACTIVE   = "job_active";
    public final static String KEY_STATUS       = "key_status";
    public final static String KEY_TYPE         = "key_type";
    public final static String KEY_ID           = "key_id";

    @BindView(R.id.aclv_start_jobs)
    ListView            mLVJobs;

    private Timer                   mTimer;

    @Override
    protected View inflaterView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        LOG_TAG = "FrgJobShow";
        View rootView = inflater.inflate(R.layout.vw_job_show, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    /**
     * 数据库内数据变化处理器
     * @param event     事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDBDataChangeEvent(DBDataChangeEvent event) {
        int ms = DBDataChangeEvent.EVENT_CREATE == event.getEventType() ? 1200 : 0;
        FrgJobShow h = this;
        new Handler((Handler.Callback) this).postDelayed(h::refreshFrg, ms);
    }

    /**
     * 配置变化处理器
     * @param event     事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPreferencesChangeEvent(PreferencesChangeEvent event) {
        if(GlobalDef.STR_CAMERAPROPERTIES_NAME.equals(event.getPreferencesName()))  {
            refreshFrg();
        }
    }

    @Override
    protected void enterActivity()  {
        Log.d(LOG_TAG, "in enterActivity");
        super.enterActivity();

        EventBus.getDefault().register(this);

        // update jobs info every 3 seconds
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                refreshCameraJobs();
            }
        }, 100, 3000);
    }

    @Override
    protected void leaveActivity()  {
        Log.d(LOG_TAG, "in leaveActivity");

        mTimer.cancel();
        EventBus.getDefault().unregister(this);

        super.leaveActivity();
    }

    @Override
    protected void initUiComponent(View view) {
    }

    @Override
    protected void loadUI() {
        // for listview
        LVJobShowAdapter mLVAdapter = new LVJobShowAdapter(getContext(),
                new ArrayList<HashMap<String, String>>(),
                new String[]{KEY_JOB_NAME, KEY_JOB_ACTIVE, KEY_JOB_DETAIL},
                new int[]{R.id.tv_job_name, R.id.tv_job_active,  R.id.tv_job_detail});
        mLVJobs.setAdapter(mLVAdapter);
    }


    /**
     * 更新数据
     * @param lsdata 新数据
     */
    public void updateData(List<HashMap<String, String>> lsdata) {
        ArrayList<HashMap<String, String>> al_data = new ArrayList<>();
        al_data.addAll(lsdata);
        LVJobShowAdapter mLVAdapter = new LVJobShowAdapter(getContext(),
                al_data,
                new String[]{KEY_JOB_NAME, KEY_JOB_ACTIVE, KEY_JOB_DETAIL},
                new int[]{R.id.tv_job_name, R.id.tv_job_active,  R.id.tv_job_detail});
        mLVJobs.setAdapter(mLVAdapter);
    }


    /**
     * update ui
     */
    public void refreshFrg()    {
        refreshCameraJobs();
    }

    /// BEGIN PRIVATE
    /**
     * update jobs status in UI
     */
    private void refreshCameraJobs() {
        List<HashMap<String, String>> lshm_jobs = new ArrayList<>();

        LinkedList<String> dirs = FileUtil.getDirDirs(
                ContextUtil.getInstance().getAppPhotoRootDir(), false);
        List<CameraJob> ls_job = ContextUtil.GetCameraJobUtility().getAllData();
        if(!UtilFun.ListIsNullOrEmpty(ls_job))   {
            ls_job.sort(Comparator.comparingInt(CameraJob::get_id));
            for (CameraJob cj : ls_job) {
                alive_camerjob(lshm_jobs, cj);

                String dir = ContextUtil.getInstance().getCameraJobPhotoDir(cj.get_id());
                dirs.remove(dir);
            }
        }

        if(!UtilFun.ListIsNullOrEmpty(dirs)) {
            dirs.sort(String::compareTo);

            for (String dir : dirs) {
                died_camerajob(lshm_jobs, dir);
            }
        }

        updateData(lshm_jobs);
    }

    private void died_camerajob(List<HashMap<String, String>> jobs, String dir) {
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
        map.put(KEY_STATUS, EJobStatus.STOP.getStatus());
        map.put(KEY_TYPE, DIED_JOB);
        jobs.add(map);
    }

    private void alive_camerjob(List<HashMap<String, String>> jobs, CameraJob cj)     {
        String at = String.format(Locale.CHINA
                , "%s/%s\n%s -\n%s"
                , cj.getType(), cj.getPoint()
                , UtilFun.TimestampToString(cj.getStarttime()).substring(0, 16)
                , UtilFun.TimestampToString(cj.getEndtime()).substring(0, 16));

        String jobname = cj.getName();
        String status = cj.getStatus().getJob_status().equals(EJobStatus.RUN.getStatus()) ?
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
        jobs.add(map);
    }
    /// END PRIVATE

    /**
     * adapter for listview to show jobs status
     * Created by wxm on 2016/8/13.
     */
    public class LVJobShowAdapter extends SimpleAdapter
            implements View.OnClickListener {
        private RelativeLayout[]  mRLCameraInfo;
        LVJobShowAdapter(Context context, List<? extends Map<String, ?>> data,
                         String[] from, int[] to) {
            super(context, data, R.layout.li_job_show, from, to);
            mRLCameraInfo = new RelativeLayout[data.size()];
        }


        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            mRLCameraInfo = new RelativeLayout[getCount()];
        }

        @Override
        public View getView(final int position, View view, ViewGroup arg2) {
            View v = super.getView(position, view, arg2);
            if(null != v)   {
                init_ui(v, position);

                mRLCameraInfo[position] = UtilFun.cast_t(v.findViewById(R.id.rl_camera_info));
                fill_camera_info(position);
            }

            return v;
        }

        void init_ui(View v, int position) {
            HashMap<String, String> map = UtilFun.cast_t(getItem(position));

            // for imagebutton
            ImageButton ib_play = (ImageButton)v.findViewById(R.id.ib_job_run_or_pause);
            ImageButton ib_delete = (ImageButton)v.findViewById(R.id.ib_job_stop);

            String status = map.get(KEY_STATUS);
            if(status.equals(EJobStatus.RUN.getStatus()))    {
                ib_play.setVisibility(View.VISIBLE);

                ib_play.setBackgroundResource(R.drawable.ic_pause);
                ib_play.setClickable(true);
                ib_play.setOnClickListener(this);
            } else if(status.equals(EJobStatus.PAUSE.getStatus())) {
                ib_play.setVisibility(View.VISIBLE);

                ib_play.setBackgroundResource(R.drawable.ic_start);
                ib_play.setClickable(true);
                ib_play.setOnClickListener(this);
            } else  {
                ib_play.setVisibility(View.INVISIBLE);
                ib_play.setClickable(false);
            }

            ib_delete.setOnClickListener(this);

            ImageButton ib_look = (ImageButton)v.findViewById(R.id.ib_job_look);
            ImageButton ib_slide = UtilFun.cast_t(v.findViewById(R.id.ib_job_slide_look));
            String pp = ContextUtil.getInstance().getCameraJobPhotoDir(
                    Integer.parseInt(map.get(KEY_ID)));
            if(0 == FileUtil.getDirFilesCount(pp, "jpg", false)) {
                ib_look.setVisibility(View.INVISIBLE);

                ib_slide.setVisibility(View.INVISIBLE);
            }
            else    {
                ib_look.setVisibility(View.VISIBLE);
                ib_look.setOnClickListener(this);

                ib_slide.setVisibility(View.VISIBLE);
                ib_slide.setOnClickListener(this);
            }
        }

        void fill_camera_info(int pos) {
            RelativeLayout rl_hot = mRLCameraInfo[pos];
            RelativeLayout rl = UtilFun.cast_t(rl_hot.findViewById(R.id.rl_preview));
            rl.setVisibility(View.INVISIBLE);
            rl = UtilFun.cast_t(rl_hot.findViewById(R.id.rl_setting));
            rl.setVisibility(View.INVISIBLE);

            FrgCameraInfoHelper.refillLayout(rl_hot, PreferencesUtil.loadCameraParam());
        }

        @Override
        public void onClick(View v) {
            int pos = mLVJobs.getPositionForView(v);
            HashMap<String, String> map = UtilFun.cast_t(getItem(pos));

            switch (v.getId())  {
                case R.id.ib_job_stop: {
                    int id = Integer.parseInt(map.get(KEY_ID));
                    String type = map.get(KEY_TYPE);
                    if(ALIVE_JOB.equals(type))  {
                        CameraJobUtility.removeCamerJob(id);
                    } else  {
                        CameraJobUtility.deleteCamerJob(id);
                        refreshFrg();
                    }
                }
                break;

                case R.id.ib_job_run_or_pause:    {
                    int id = Integer.parseInt(map.get(KEY_ID));
                    CameraJob cj = ContextUtil.GetCameraJobUtility().getData(id);
                    if(null != cj) {
                        CameraJobStatus cjs = cj.getStatus();
                        String sz_run = EJobStatus.RUN.getStatus();
                        String sz_pause = EJobStatus.PAUSE.getStatus();
                        cjs.setJob_status(cjs.getJob_status().equals(sz_pause) ?
                                sz_run : sz_pause);
                        ContextUtil.GetCameraJobStatusUtility().modifyData(cjs);

                        refreshFrg();
                    }
                }
                break;

                case R.id.ib_job_look:    {
                    String pp = ContextUtil.getInstance()
                            .getCameraJobPhotoDir(
                                    Integer.parseInt(map.get(KEY_ID)));

                    JobGallery jg = new JobGallery();
                    jg.OpenGallery(getActivity(), pp);
                }
                break;

                case R.id.ib_job_slide_look :   {
                    String pp = ContextUtil.getInstance()
                            .getCameraJobPhotoDir(
                                    Integer.parseInt(map.get(KEY_ID)));

                    Intent it = new Intent(getActivity(), ACJobSlide.class);
                    it.putExtra(EAction.LOAD_PHOTO_DIR.getName(), pp);
                    startActivityForResult(it, 1);
                }
            }
        }
    }
}

