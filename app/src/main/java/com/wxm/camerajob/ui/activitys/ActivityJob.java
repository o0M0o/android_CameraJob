package com.wxm.camerajob.ui.activitys;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.handler.GlobalContext;
import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.base.utility.PreferencesUtil;

import java.sql.Timestamp;
import java.util.Calendar;

import cn.wxm.andriodutillib.util.UtilFun;

public class ActivityJob
        extends AppCompatActivity
        implements View.OnClickListener, View.OnTouchListener {
    private final static String TAG = "ActivityJob";
    private final static String JOB_ENDTIME     = "Endtime";
    private final static String JOB_STARTTIME   = "Starttime";

//    private Button              mBtSave;
//    private Button              mBtGiveup;
    private EditText            mEtJobName;
    private EditText            mEtJobEndDate;
    private EditText            mEtJobStartDate;

    private ArrayAdapter<CharSequence> mAPJobType;
    private ArrayAdapter<CharSequence>  mAPJobPoint;
    private Spinner                     mSPJobType;
    private Spinner                     mSPJobPoint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_job);
        ContextUtil.getInstance().addActivity(this);
        initActivity();

        final ActivityJob home = this;
        if(!PreferencesUtil.checkCameraIsSet())     {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("相机未设置，需要先设置相机");
            builder.setPositiveButton("确 定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent data = new Intent(home, ACCameraSetting.class);
                    data.putExtra(GlobalDef.STR_LOAD_CAMERASETTING,
                            PreferencesUtil.loadCameraParam());

                    startActivityForResult(data, 1);
                }
            });

            Dialog dialog = builder.create();
            dialog.show();
        }
    }

    private void initActivity() {
        // init
//        mBtSave = (Button)findViewById(R.id.acbt_job_save);
//        mBtGiveup = (Button)findViewById(R.id.acbt_job_giveup);
        mEtJobName = (EditText)findViewById(R.id.acet_job_name);
        mEtJobEndDate = (EditText)findViewById(R.id.acet_job_enddate);
        mEtJobStartDate = (EditText)findViewById(R.id.acet_job_startdate);
//        mBtSave.setOnClickListener(this);
//        mBtGiveup.setOnClickListener(this);

        // 任务默认开始时间是“当前时间"
        // 任务默认结束时间是“一周”
        mEtJobStartDate.setText(UtilFun.MilliSecsToString(System.currentTimeMillis()));
        mEtJobEndDate.setText(UtilFun.MilliSecsToString(System.currentTimeMillis()
                                    + 1000 * 3600 * 24 * 7));

        mEtJobEndDate.setOnTouchListener(this);
        mEtJobStartDate.setOnTouchListener(this);

        mSPJobType = (Spinner)findViewById(R.id.acsp_job_type);
        mSPJobPoint = (Spinner)findViewById(R.id.acsp_job_point);

        mAPJobType = ArrayAdapter.createFromResource(this,
                R.array.job_type, R.layout.spinner_jobtype);
        mAPJobType.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mSPJobType.setAdapter(mAPJobType);
        mSPJobType.setSelection(0);    // '每分钟'

        mAPJobPoint = ArrayAdapter.createFromResource(this,
                R.array.minutely_invoke, R.layout.spinner_jobpoint);
        mAPJobPoint.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mSPJobPoint.setAdapter(mAPJobPoint);

        mAPJobPoint = ArrayAdapter.createFromResource(this,
                R.array.hourly_invoke, R.layout.spinner_jobpoint);
        mAPJobPoint.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mSPJobPoint.setAdapter(mAPJobPoint);

        final Activity home = this;
        mSPJobType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selitem = mAPJobType.getItem(position).toString();

                try {
                    boolean modify = false;
                    switch (selitem) {
                        case GlobalDef.CNSTR_JOBTYPE_MINUTELY: {
                            mAPJobPoint = ArrayAdapter.createFromResource(home,
                                    R.array.minutely_invoke, R.layout.spinner_jobpoint);
                            modify = true;
                        }
                        break;

                        case GlobalDef.CNSTR_JOBTYPE_HOURLY: {
                            mAPJobPoint = ArrayAdapter.createFromResource(home,
                                    R.array.hourly_invoke, R.layout.spinner_jobpoint);
                            modify = true;
                        }
                        break;

                        case GlobalDef.CNSTR_JOBTYPE_DAILY: {
                            mAPJobPoint = ArrayAdapter.createFromResource(home,
                                    R.array.daily_invoke, R.layout.spinner_jobpoint);
                            modify = true;
                        }
                        break;
                    }

                    if(modify) {
                        mSPJobPoint.setAdapter(mAPJobPoint);
                        mAPJobPoint.notifyDataSetChanged();
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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.acmeu_camerajob_actbar, menu);
        return true;
    }


    @Override
    public void onClick(View v)    {
        /*
        int vid = v.getId();
        switch (vid)    {
            case R.id.acbt_job_save :
                if(do_save())
                    finish();
                break;

            case R.id.acbt_job_giveup:
                do_giveup();
                finish();
                break;
        }
        */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(resultCode)  {
            case GlobalDef.INTRET_CS_ACCEPT:    {
                Message m = Message.obtain(GlobalContext.getMsgHandlder(),
                        GlobalDef.MSGWHAT_CS_CHANGECAMERA);
                m.obj = PreferencesUtil.loadCameraParam();
                m.sendToTarget();
            }
            break;

            case GlobalDef.INTRET_CS_GIVEUP :   {
                final ActivityJob home = this;

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("相机未设置，需要先设置相机");
                builder.setPositiveButton("确 定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent data = new Intent(home, ACCameraSetting.class);
                        data.putExtra(GlobalDef.STR_LOAD_CAMERASETTING,
                                PreferencesUtil.loadCameraParam());

                        startActivityForResult(data, 1);
                    }
                });

                Dialog dialog = builder.create();
                dialog.show();
            }

            default:    {
                Log.i(TAG, "不处理的 resultCode = " + resultCode);
            }
            break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.meuitem_camerajob_accept: {
                if(do_save())
                    finish();
            }
            break;

            case R.id.meuitem_camerajob_giveup  : {
                do_giveup();
                finish();
            }
            break;

            default:
                return super.onOptionsItemSelected(item);

        }

        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN)    {
            switch(v.getId())   {
                case R.id.acet_job_enddate :    {
                    onTouchDate(event, JOB_ENDTIME);
                }
                break;

                case R.id.acet_job_startdate  :    {
                    onTouchDate(event, JOB_STARTTIME);
                }
                break;
            }
        }

        return true;
    }

    /**
     * 生成日期选择dialog
     * @param event     事件
     * @param timetype  日期类型
     */
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

    /**
     * 保存任务并返回前activity
     * @return 如果成功返回true,否则返回false
     */
    private boolean do_save()  {
        String job_name  = mEtJobName.getText().toString();
        String job_type  = mSPJobType.getSelectedItem().toString();
        String job_point = mSPJobPoint.getSelectedItem().toString();
        String job_starttime = mEtJobStartDate.getText().toString();
        String job_endtime = mEtJobEndDate.getText().toString();

        if(job_name.isEmpty())  {
            Log.i(TAG, "job name为空");
            mEtJobName.requestFocus();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("请输入任务名!").setTitle("警告");
            AlertDialog dlg = builder.create();
            dlg.show();
            return false;
        }

        if(job_type.isEmpty())  {
            Log.i(TAG, "job type为空");
            mSPJobType.requestFocus();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("请选择任务类型!").setTitle("警告");
            AlertDialog dlg = builder.create();
            dlg.show();
            return false;
        }

        if(job_point.isEmpty())  {
            Log.i(TAG, "job point为空");
            mSPJobPoint.requestFocus();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("请选择任务激活方式!").setTitle("警告");
            AlertDialog dlg = builder.create();
            dlg.show();
            return false;
        }

        /*
        // 任务默认开始时间是“当前时间"
        // 任务默认结束时间是“一周”
        if(Starttime.isEmpty()) {
            Starttime = UtilFun.MilliSecsToString(System.currentTimeMillis());
        }

        if(Endtime.isEmpty())   {
            Endtime = UtilFun.MilliSecsToString(System.currentTimeMillis()
                                + 1000 * 3600 * 24 * 7);
        }
        */

        Timestamp st = UtilFun.StringToTimestamp(job_starttime);
        Timestamp et = UtilFun.StringToTimestamp(job_endtime);
        if(0 <= st.compareTo(et))   {
            String show = String.format("任务开始时间(%s)比结束时间(%s)晚", job_starttime, job_endtime);
            Log.w(TAG, show);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(show).setTitle("警告");
            AlertDialog dlg = builder.create();
            dlg.show();
            return false;
        }

        Intent data = new Intent();
        CameraJob cj = new CameraJob();
        cj.setName(job_name);
        cj.setType(job_type);
        cj.setPoint(job_point);
        cj.setStarttime(st);
        cj.setEndtime(et);
        cj.getTs().setTime(System.currentTimeMillis());
        cj.getStatus().setJob_status(GlobalDef.STR_CAMERAJOB_RUN);

        data.putExtra(GlobalDef.STR_LOAD_JOB, cj);
        setResult(GlobalDef.INTRET_CAMERAJOB_ACCEPT, data);
        return true;
    }

    /**
     * 放弃当前任务
     */
    private void do_giveup()  {
        Intent data = new Intent();
        setResult(GlobalDef.INTRET_CAMERAJOB_GIVEUP, data);
    }


}






