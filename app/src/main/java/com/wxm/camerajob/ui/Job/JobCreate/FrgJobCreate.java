package com.wxm.camerajob.ui.Job.JobCreate;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.wxm.camerajob.R;
import com.wxm.camerajob.data.define.CameraJob;
import com.wxm.camerajob.data.define.CameraParam;
import com.wxm.camerajob.data.define.GlobalDef;
import com.wxm.camerajob.data.define.PreferencesChangeEvent;
import com.wxm.camerajob.data.define.PreferencesUtil;
import com.wxm.camerajob.ui.Camera.CameraPreview.ACCameraPreview;
import com.wxm.camerajob.ui.Camera.CameraSetting.ACCameraSetting;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wxm.androidutil.Dialog.DlgDatePicker;
import wxm.androidutil.Dialog.DlgOKOrNOBase;
import wxm.androidutil.FrgUtility.FrgUtilitySupportBase;
import wxm.androidutil.util.UtilFun;

/**
 * 相机预览fragment
 * Created by 123 on 2016/10/14.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class FrgJobCreate extends FrgUtilitySupportBase {

    private final static String KEY_JOB_TYPE = "job_type";
    private final static String KEY_JOB_POINT = "job_point";

    // for job setting
    @BindView(R.id.acet_job_name)
    EditText mETJobName;

    @BindView(R.id.tv_date_start)
    TextView mTVJobStartDate;

    @BindView(R.id.tv_date_end)
    TextView mTVJobEndDate;

    @BindView(R.id.gv_job_type)
    GridView mGVJobType;

    @BindView(R.id.gv_job_point)
    GridView mGVJobPoint;

    @BindView(R.id.sw_send_pic)
    Switch mSTSendPic;

    @BindView(R.id.vs_email_detail)
    ViewSwitcher mVSEmailDetail;

    //private Switch      mSWSendPicByEmail;

    private ArrayList<HashMap<String, String>> mALJobPoint;
    private GVJobPointAdapter mGAJobPoint;

    // for camera setting
    @BindView(R.id.tv_camera_face)
    TextView mTVCameraFace;

    @BindView(R.id.tv_camera_dpi)
    TextView mTVCameraDpi;

    @BindView(R.id.tv_camera_flash)
    TextView mTVCameraFlash;

    @BindView(R.id.tv_camera_focus)
    TextView mTVCameraFocus;

    // for send pic
    @BindView(R.id.tv_email_sender)
    TextView mTVEmailSender;

    @BindView(R.id.tv_email_server_type)
    TextView mTVEmailSendServerType;

    @BindView(R.id.tv_email_send_type)
    TextView mTVEmailSendType;

    @BindView(R.id.tv_email_recv_address)
    TextView mTVEmailReceiver;

    public static FrgJobCreate newInstance() {
        return new FrgJobCreate();
    }

    @Override
    protected void enterActivity() {
        super.enterActivity();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void leaveActivity() {
        EventBus.getDefault().unregister(this);
        super.leaveActivity();
    }

    @Override
    protected View inflaterView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        LOG_TAG = "FrgJobCreate";
        View rootView = inflater.inflate(R.layout.vw_job_creater, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    protected void initUiComponent(View view) {
        init_job_setting();
        init_camera_setting();
    }

    @Override
    protected void loadUI() {
    }

    /**
     * 配置变化处理器
     *
     * @param event 事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPreferencesChangeEvent(PreferencesChangeEvent event) {
        if (GlobalDef.STR_CAMERAPROPERTIES_NAME.equals(event.getPreferencesName())) {
            load_camera_setting();
        }
    }


    /**
     * 接受输入参数
     *
     * @return 若输入参数合法，返回对应值，否则返回null
     */
    public CameraJob onAccept() {
        String job_name = mETJobName.getText().toString();
        String job_type = ((GVJobTypeAdapter) mGVJobType.getAdapter()).getSelectJobType();
        String job_point = job_type.isEmpty() ? ""
                : ((GVJobPointAdapter) mGVJobPoint.getAdapter()).getSelectJobPoint();
        String job_starttime = mTVJobStartDate.getText().toString() + ":00";
        String job_endtime = mTVJobEndDate.getText().toString() + ":00";

        Context ct = getContext();
        if (job_name.isEmpty()) {
            Log.i(LOG_TAG, "job name为空");
            mETJobName.requestFocus();

            AlertDialog.Builder builder = new AlertDialog.Builder(ct);
            builder.setMessage("请输入任务名!").setTitle("警告");
            AlertDialog dlg = builder.create();
            dlg.show();
            return null;
        }

        if (job_type.isEmpty()) {
            Log.i(LOG_TAG, "job type为空");
            mGVJobType.requestFocus();

            AlertDialog.Builder builder = new AlertDialog.Builder(ct);
            builder.setMessage("请选择任务类型!").setTitle("警告");
            AlertDialog dlg = builder.create();
            dlg.show();
            return null;
        }

        if (job_point.isEmpty()) {
            Log.i(LOG_TAG, "job point为空");
            mGVJobPoint.requestFocus();

            AlertDialog.Builder builder = new AlertDialog.Builder(ct);
            builder.setMessage("请选择任务激活方式!").setTitle("警告");
            AlertDialog dlg = builder.create();
            dlg.show();
            return null;
        }

        Timestamp st = UtilFun.StringToTimestamp(job_starttime);
        Timestamp et = UtilFun.StringToTimestamp(job_endtime);
        if (0 <= st.compareTo(et)) {
            String show = String.format("任务开始时间(%s)比结束时间(%s)晚", job_starttime, job_endtime);
            Log.w(LOG_TAG, show);

            AlertDialog.Builder builder = new AlertDialog.Builder(ct);
            builder.setMessage(show).setTitle("警告");
            AlertDialog dlg = builder.create();
            dlg.show();
            return null;
        }

        CameraJob cj = new CameraJob();
        cj.setName(job_name);
        cj.setType(job_type);
        cj.setPoint(job_point);
        cj.setStarttime(st);
        cj.setEndtime(et);
        cj.getTs().setTime(System.currentTimeMillis());
        cj.getStatus().setJob_status(GlobalDef.STR_CAMERAJOB_RUN);
        return cj;
    }

    /// BEGIN PRIVATE

    /**
     * 初始化任务设置
     */
    private void init_job_setting() {
        // 任务默认开始时间是“当前时间"
        // 任务默认结束时间是“一周”
        mTVJobStartDate.setText(UtilFun.MilliSecsToString(
                System.currentTimeMillis()).substring(0, 16));
        mTVJobEndDate.setText(UtilFun.MilliSecsToString(System.currentTimeMillis()
                + 1000 * 3600 * 24 * 7).substring(0, 16));

        // for job type & job point
        String[] str_arr = getResources().getStringArray(R.array.job_type);
        ArrayList<HashMap<String, String>> al_hm = new ArrayList<>();
        for (String i : str_arr) {
            HashMap<String, String> hm = new HashMap<>();
            hm.put(KEY_JOB_TYPE, i);

            al_hm.add(hm);
        }

        GVJobTypeAdapter ga = new GVJobTypeAdapter(getContext(), al_hm,
                new String[]{KEY_JOB_TYPE}, new int[]{R.id.tv_job_type});
        mGVJobType.setAdapter(ga);
        ga.notifyDataSetChanged();

        mALJobPoint = new ArrayList<>();
        mGAJobPoint = new GVJobPointAdapter(getContext(), mALJobPoint,
                new String[]{KEY_JOB_POINT}, new int[]{R.id.tv_job_point});
        mGVJobPoint.setAdapter(mGAJobPoint);
        mGAJobPoint.notifyDataSetChanged();

        // for send pic
        mSTSendPic.setOnCheckedChangeListener((buttonView, isChecked) ->
                mVSEmailDetail.setDisplayedChild(isChecked ? 0 : 1));

        mSTSendPic.setChecked(false);
        mVSEmailDetail.setDisplayedChild(1);

        mTVEmailSender.setText("请设置邮件发送者");
        mTVEmailReceiver.setText("请设置邮件接收者");
    }


    @OnClick({R.id.iv_clock_start, R.id.iv_clock_end})
    public void onClockClick(View v) {
        final int vid = v.getId();
        final TextView hot_tv = R.id.iv_clock_start == vid ? mTVJobStartDate : mTVJobEndDate;

        DlgDatePicker dp = new DlgDatePicker();
        dp.setInitDate(hot_tv.getText().toString() + ":00");
        dp.addDialogListener(new DlgOKOrNOBase.DialogResultListener() {
            @Override
            public void onDialogPositiveResult(DialogFragment dialog) {
                DlgDatePicker cur_dp = UtilFun.cast_t(dialog);
                String cur_date = cur_dp.getCurDate();

                if (!UtilFun.StringIsNullOrEmpty(cur_date))
                    hot_tv.setText(cur_date.substring(0, 16));

                hot_tv.requestFocus();
            }

            @Override
            public void onDialogNegativeResult(DialogFragment dialog) {
                hot_tv.requestFocus();
            }
        });

        dp.show(getFragmentManager(),
                R.id.iv_clock_start == vid ? "选择任务启动时间" : "选择任务结束时间");
    }

    /**
     * 处理“设置”和“预览”点击事件
     *
     * @param v 点击目标视图
     */
    @OnClick({R.id.rl_setting, R.id.rl_preview})
    public void onRLClick(View v) {
        Activity ac = getActivity();
        int vid = v.getId();
        switch (vid) {
            case R.id.rl_setting: {
                Intent data = new Intent(ac, ACCameraSetting.class);
                ac.startActivityForResult(data, 1);
            }
            break;

            case R.id.rl_preview: {
                Intent it = new Intent(ac, ACCameraPreview.class);
                it.putExtra(GlobalDef.STR_LOAD_CAMERASETTING, PreferencesUtil.loadCameraParam());
                ac.startActivityForResult(it, 1);
            }
            break;
        }
    }


    /**
     * 初始化摄像头设置
     */
    private void init_camera_setting() {
        // for camera setting
        RelativeLayout rl = UtilFun.cast_t(getView().findViewById(R.id.rl_preview));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            rl.setVisibility(View.INVISIBLE);
        }

        load_camera_setting();
    }

    /**
     * 加载配置信息中的摄像头设置并显示
     */
    private void load_camera_setting() {
        CameraParam cp = PreferencesUtil.loadCameraParam();
        mTVCameraFace.setText(getString(CameraParam.LENS_FACING_BACK == cp.mFace ?
                R.string.cn_backcamera : R.string.cn_frontcamera));

        mTVCameraDpi.setText(cp.mPhotoSize.toString());
        mTVCameraFlash.setText(getString(cp.mAutoFlash ?
                R.string.cn_autoflash : R.string.cn_flash_no));
        mTVCameraFocus.setText(getString(cp.mAutoFocus ?
                R.string.cn_autofocus : R.string.cn_focus_no));
    }
    /// END PRIVATE


    /**
     * gridview适配器类
     */
    public class GVJobTypeAdapter
            extends SimpleAdapter
            implements View.OnClickListener {
        private int mCLSelected;
        private int mCLNotSelected;
        private int mLastSelected;

        public GVJobTypeAdapter(Context context, List<? extends Map<String, ?>> data,
                                String[] from, int[] to) {
            super(context, data, R.layout.gi_job_type, from, to);


            mCLSelected = getResources().getColor(R.color.linen);
            mCLNotSelected = getResources().getColor(R.color.white);

            mLastSelected = GlobalDef.INT_INVALID_ID;
        }

        @Override
        public int getViewTypeCount() {
            int org_ct = getCount();
            return org_ct < 1 ? 1 : org_ct;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View view, ViewGroup arg2) {
            View v = super.getView(position, view, arg2);
            if (null != v) {
                v.setOnClickListener(this);
                v.setBackgroundColor(mLastSelected == position ? mCLSelected : mCLNotSelected);
            }

            return v;
        }

        @Override
        public void onClick(View v) {
            int pos = mGVJobType.getPositionForView(v);
            if (mLastSelected == pos)
                return;

            invoke_job_point(pos);
            mLastSelected = pos;
            notifyDataSetChanged();
        }

        public String getSelectJobType() {
            if (GlobalDef.INT_INVALID_ID == mLastSelected)
                return "";

            HashMap<String, Object> hmd = UtilFun.cast(getItem(mLastSelected));
            return UtilFun.cast_t(hmd.get(KEY_JOB_TYPE));
        }

        private void invoke_job_point(int pos) {
            HashMap<String, Object> hmd = UtilFun.cast(getItem(pos));
            String hv = UtilFun.cast_t(hmd.get(KEY_JOB_TYPE));
            try {
                String[] str_arr = null;
                switch (hv) {
                    case GlobalDef.CNSTR_JOBTYPE_MINUTELY: {
                        str_arr = getResources().getStringArray(R.array.minutely_invoke);
                    }
                    break;

                    case GlobalDef.CNSTR_JOBTYPE_HOURLY: {
                        str_arr = getResources().getStringArray(R.array.hourly_invoke);
                    }
                    break;

                    case GlobalDef.CNSTR_JOBTYPE_DAILY: {
                        str_arr = getResources().getStringArray(R.array.daily_invoke);
                    }
                    break;
                }

                if (null != str_arr) {
                    mALJobPoint.clear();
                    for (String i : str_arr) {
                        HashMap<String, String> hm = new HashMap<>();
                        hm.put(KEY_JOB_POINT, i);

                        mALJobPoint.add(hm);
                    }

                    mGAJobPoint.cleanSelected();
                    mGAJobPoint.notifyDataSetChanged();
                }
            } catch (Resources.NotFoundException e) {
                Log.e(LOG_TAG, "Not find string array for '" + hv + "'");
                e.printStackTrace();
            }
        }
    }


    /**
     * gridview适配器类
     */
    public class GVJobPointAdapter
            extends SimpleAdapter
            implements View.OnClickListener {
        private int mCLSelected;
        private int mCLNotSelected;
        private int mLastSelected;

        public GVJobPointAdapter(Context context, List<? extends Map<String, ?>> data,
                                 String[] from, int[] to) {
            super(context, data, R.layout.gi_job_point, from, to);

            mCLSelected = getResources().getColor(R.color.linen);
            mCLNotSelected = getResources().getColor(R.color.white);

            mLastSelected = GlobalDef.INT_INVALID_ID;
        }

        @Override
        public int getViewTypeCount() {
            int org_ct = getCount();
            return org_ct < 1 ? 1 : org_ct;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View view, ViewGroup arg2) {
            View v = super.getView(position, view, arg2);
            if (null != v) {
                v.setOnClickListener(this);
                v.setBackgroundColor(mLastSelected == position ? mCLSelected : mCLNotSelected);
            }

            return v;
        }

        @Override
        public void onClick(View v) {
            int pos = mGVJobPoint.getPositionForView(v);
            if (mLastSelected == pos)
                return;

            mLastSelected = pos;
            notifyDataSetChanged();
        }

        public String getSelectJobPoint() {
            if (GlobalDef.INT_INVALID_ID == mLastSelected)
                return "";

            HashMap<String, Object> hmd = UtilFun.cast(getItem(mLastSelected));
            return UtilFun.cast_t(hmd.get(KEY_JOB_POINT));
        }

        public void cleanSelected() {
            mLastSelected = GlobalDef.INT_INVALID_ID;
        }
    }
}
