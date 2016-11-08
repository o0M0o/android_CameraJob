package com.wxm.camerajob.ui.fragment.utility;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.CameraParam;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.data.IPreferenceChangeNotice;
import com.wxm.camerajob.base.utility.PreferencesUtil;
import com.wxm.camerajob.ui.acutility.ACCameraPreview;
import com.wxm.camerajob.ui.acutility.ACCameraSetting;

import java.sql.Timestamp;

import cn.wxm.andriodutillib.Dialog.DlgDatePicker;
import cn.wxm.andriodutillib.Dialog.DlgOKOrNOBase;
import cn.wxm.andriodutillib.util.UtilFun;

/**
 * 相机预览fragment
 * Created by 123 on 2016/10/14.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class FrgJobCreate extends Fragment {
    private final static String     TAG = "FrgJobCreate";

    private View        mVWSelf;

    // for job setting
    private EditText    mETJobName;
    private TextView    mTVJobStartDate;
    private TextView    mTVJobEndDate;
    private Spinner     mSPJobType;
    private Spinner     mSPJobPoint;
    private Switch      mSWSendPicByEmail;

    // for camera setting
    private TextView    mTVCameraFace;
    private TextView    mTVCameraDpi;
    private TextView    mTVCameraFlash;
    private TextView    mTVCameraFocus;

    // for send pic
    private TextView mTVEmailSender;
    private TextView mTVEmailSendServerType;
    private TextView mTVEmailSendType;
    private TextView mTVEmailReceiver;

    // for camera setting change listener
    private IPreferenceChangeNotice  mIPCNCamera = new IPreferenceChangeNotice() {
        @Override
        public void onPreferenceChanged(String PreferenceName) {
            if(GlobalDef.STR_CAMERAPROPERTIES_NAME.equals(PreferenceName))  {
                load_camera_setting();
            }
        }
    };

    public static FrgJobCreate newInstance() {
        return new FrgJobCreate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.vw_job_creater, null);
        return v;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        if(null != view)    {
            mVWSelf = view;
            init_ui();

            PreferencesUtil.getInstance().addChangeNotice(mIPCNCamera);
        }
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferencesUtil.getInstance().removeChangeNotice(mIPCNCamera);
    }


    /**
     * 接受输入参数
     * @return   若输入参数合法，返回对应值，否则返回null
     */
    public CameraJob onAccept()   {
        String job_name  = mETJobName.getText().toString();
        String job_type  = mSPJobType.getSelectedItem().toString();
        String job_point = mSPJobPoint.getSelectedItem().toString();
        String job_starttime = mTVJobStartDate.getText().toString() + ":00";
        String job_endtime = mTVJobEndDate.getText().toString() + ":00";

        Context ct = mVWSelf.getContext();
        if(job_name.isEmpty())  {
            Log.i(TAG, "job name为空");
            mETJobName.requestFocus();

            AlertDialog.Builder builder = new AlertDialog.Builder(ct);
            builder.setMessage("请输入任务名!").setTitle("警告");
            AlertDialog dlg = builder.create();
            dlg.show();
            return null;
        }

        if(job_type.isEmpty())  {
            Log.i(TAG, "job type为空");
            mSPJobType.requestFocus();

            AlertDialog.Builder builder = new AlertDialog.Builder(ct);
            builder.setMessage("请选择任务类型!").setTitle("警告");
            AlertDialog dlg = builder.create();
            dlg.show();
            return null;
        }

        if(job_point.isEmpty())  {
            Log.i(TAG, "job point为空");
            mSPJobPoint.requestFocus();

            AlertDialog.Builder builder = new AlertDialog.Builder(ct);
            builder.setMessage("请选择任务激活方式!").setTitle("警告");
            AlertDialog dlg = builder.create();
            dlg.show();
            return null;
        }

        Timestamp st = UtilFun.StringToTimestamp(job_starttime);
        Timestamp et = UtilFun.StringToTimestamp(job_endtime);
        if(0 <= st.compareTo(et))   {
            String show = String.format("任务开始时间(%s)比结束时间(%s)晚", job_starttime, job_endtime);
            Log.w(TAG, show);

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
     * 初始化UI
     */
    private void init_ui() {
        init_job_setting();
        init_camera_setting();
    }

    /**
     * 初始化任务设置
     */
    private void init_job_setting() {
        mETJobName = UtilFun.cast_t(mVWSelf.findViewById(R.id.acet_job_name));
        mTVJobStartDate = UtilFun.cast_t(mVWSelf.findViewById(R.id.tv_date_start));
        mTVJobEndDate = UtilFun.cast_t(mVWSelf.findViewById(R.id.tv_date_end));

        // 任务默认开始时间是“当前时间"
        // 任务默认结束时间是“一周”
        mTVJobStartDate.setText(UtilFun.MilliSecsToString(
                System.currentTimeMillis()).substring(0, 16));
        mTVJobEndDate.setText(UtilFun.MilliSecsToString(System.currentTimeMillis()
                + 1000 * 3600 * 24 * 7).substring(0, 16));

        mSPJobType = UtilFun.cast_t(mVWSelf.findViewById(R.id.acsp_job_type));
        mSPJobPoint = UtilFun.cast_t(mVWSelf.findViewById(R.id.acsp_job_point));

        // 默认选择'每分钟'
        final ArrayAdapter mAPJobType = ArrayAdapter.createFromResource(mVWSelf.getContext(),
                                R.array.job_type, R.layout.spinner_jobtype);
        mAPJobType.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mSPJobType.setAdapter(mAPJobType);
        mSPJobType.setSelection(0);

        final ArrayAdapter mAPJobPoint = ArrayAdapter.createFromResource(mVWSelf.getContext(),
                                R.array.minutely_invoke, R.layout.spinner_jobpoint);
        mAPJobPoint.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mSPJobPoint.setAdapter(mAPJobPoint);

        final Activity home = getActivity();
        mSPJobType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selitem = mAPJobType.getItem(position).toString();

                try {
                    ArrayAdapter APJobPoint = null;
                    switch (selitem) {
                        case GlobalDef.CNSTR_JOBTYPE_MINUTELY: {
                            APJobPoint = ArrayAdapter.createFromResource(home,
                                        R.array.minutely_invoke, R.layout.spinner_jobpoint);
                        }
                        break;

                        case GlobalDef.CNSTR_JOBTYPE_HOURLY: {
                            APJobPoint = ArrayAdapter.createFromResource(home,
                                        R.array.hourly_invoke, R.layout.spinner_jobpoint);
                        }
                        break;

                        case GlobalDef.CNSTR_JOBTYPE_DAILY: {
                            APJobPoint = ArrayAdapter.createFromResource(home,
                                        R.array.daily_invoke, R.layout.spinner_jobpoint);
                        }
                        break;
                    }

                    if(null != APJobPoint) {
                        mSPJobPoint.setAdapter(APJobPoint);
                        APJobPoint.notifyDataSetChanged();
                    }
                }
                catch (Resources.NotFoundException e)   {
                    Log.e(TAG, "Not find string array for '" + selitem + "'");
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // for start and end time
        View.OnClickListener cl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int vid = v.getId();
                if(R.id.iv_clock_start == vid || R.id.iv_clock_end == vid)  {
                    final TextView hot_tv = R.id.iv_clock_start == vid ? mTVJobStartDate : mTVJobEndDate;

                    DlgDatePicker dp = new DlgDatePicker();
                    dp.setInitDate(hot_tv.getText().toString() + ":00");
                    dp.setDialogListener(new DlgOKOrNOBase.DialogResultListener() {
                        @Override
                        public void onDialogPositiveResult(DialogFragment dialog) {
                            DlgDatePicker cur_dp = UtilFun.cast_t(dialog);
                            String cur_date = cur_dp.getCurDate();

                            if(!UtilFun.StringIsNullOrEmpty(cur_date))
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
            }
        };

        ImageView iv = UtilFun.cast_t(mVWSelf.findViewById(R.id.iv_clock_start));
        iv.setOnClickListener(cl);

        iv = UtilFun.cast_t(mVWSelf.findViewById(R.id.iv_clock_end));
        iv.setOnClickListener(cl);

        // for send pic
        final Switch sw = UtilFun.cast_t(mVWSelf.findViewById(R.id.sw_send_pic));
        final ViewSwitcher vs = UtilFun.cast_t(mVWSelf.findViewById(R.id.vs_email_detail));
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                vs.setDisplayedChild(isChecked ? 0 : 1);
            }
        });

        sw.setChecked(false);
        vs.setDisplayedChild(1);

        mTVEmailSender = UtilFun.cast_t(mVWSelf.findViewById(R.id.tv_email_sender));
        mTVEmailSendServerType = UtilFun.cast_t(mVWSelf.findViewById(R.id.tv_email_server_type));
        mTVEmailSendType = UtilFun.cast_t(mVWSelf.findViewById(R.id.tv_email_send_type));
        mTVEmailReceiver = UtilFun.cast_t(mVWSelf.findViewById(R.id.tv_email_recv_address));
        mTVEmailSender.setText("请设置邮件发送者");
        mTVEmailReceiver.setText("请设置邮件接收者");
    }

    /**
     * 初始化摄像头设置
     */
    private void init_camera_setting() {
        // for camera setting
        mTVCameraFace = UtilFun.cast_t(mVWSelf.findViewById(R.id.tv_camera_face));
        mTVCameraDpi = UtilFun.cast_t(mVWSelf.findViewById(R.id.tv_camera_dpi));
        mTVCameraFlash = UtilFun.cast_t(mVWSelf.findViewById(R.id.tv_camera_flash));
        mTVCameraFocus = UtilFun.cast_t(mVWSelf.findViewById(R.id.tv_camera_focus));

        final Activity ac = getActivity();
        RelativeLayout rl = UtilFun.cast_t(mVWSelf.findViewById(R.id.rl_setting));
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent(ac, ACCameraSetting.class);
                ac.startActivityForResult(data, 1);
            }
        });

        rl = UtilFun.cast_t(mVWSelf.findViewById(R.id.rl_preview));
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(ac, ACCameraPreview.class);
                it.putExtra(GlobalDef.STR_LOAD_CAMERASETTING, PreferencesUtil.loadCameraParam());
                ac.startActivityForResult(it, 1);
            }
        });

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
        mTVCameraFocus.setText(getString(cp.mAutoFocus?
                R.string.cn_autofocus : R.string.cn_focus_no));
    }


    /**
     * 生成日期选择dialog
     * @param event     事件
     * @param timetype  日期类型
    private void onTouchDate(MotionEvent event, String timetype) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.dlg_datetime, null);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.dldt_date);
        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.dldt_time);
        builder.setView(view);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);

        final int inType = mEtJobEndDate.getInputType();
        mEtJobEndDate.setInputType(InputType.TYPE_NULL);
        mEtJobEndDate.onTouchEvent(event);
        mEtJobEndDate.setInputType(inType);
        mEtJobEndDate.setSelection(mEtJobEndDate.getText().length());

        final int inType1 = mEtJobStartDate.getInputType();
        mEtJobStartDate.setInputType(InputType.TYPE_NULL);
        mEtJobStartDate.onTouchEvent(event);
        mEtJobStartDate.setInputType(inType1);
        mEtJobStartDate.setSelection(mEtJobStartDate.getText().length());

        final String tt = timetype;
        if(JOB_ENDTIME.equals(tt))
            builder.setTitle("选择任务结束时间");
        else
            builder.setTitle("选择任务开始时间");

        builder.setPositiveButton("确  定", new DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StringBuffer sb = new StringBuffer();
                sb.append(
                        String.format("%d-%02d-%02d %02d:%02d:00",
                                datePicker.getYear(),
                                datePicker.getMonth() + 1,
                                datePicker.getDayOfMonth(),
                                (Build.VERSION.SDK_INT >= 23 ?
                                        timePicker.getHour() : timePicker.getCurrentHour()),
                                (Build.VERSION.SDK_INT >= 23 ?
                                        timePicker.getMinute() : timePicker.getCurrentMinute())));

                if(JOB_ENDTIME.equals(tt)) {
                    mEtJobEndDate.setText(sb);
                    mEtJobEndDate.requestFocus();
                }
                else    {
                    mEtJobStartDate.setText(sb);
                    mEtJobStartDate.requestFocus();
                }

                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
    }
     */
    /// END PRIVATE
}
