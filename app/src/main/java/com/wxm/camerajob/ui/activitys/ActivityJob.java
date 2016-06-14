package com.wxm.camerajob.ui.activitys;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.GlobalDef;

import java.util.Calendar;
import java.util.Date;

public class ActivityJob
        extends AppCompatActivity
        implements View.OnClickListener, View.OnTouchListener {
    private final static String TAG = "ActivityJob";
    private Button              mBtSave;
    private Button              mBtGiveup;
    private EditText            mEtJobName;
    private EditText            mEtJobEndDate;

    private ArrayAdapter<CharSequence> mAPJobType;
    private ArrayAdapter<CharSequence>  mAPJobPoint;
    private Spinner                     mSPJobType;
    private Spinner                     mSPJobPoint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job);

        // init
        mBtSave = (Button)findViewById(R.id.acbt_job_save);
        mBtGiveup = (Button)findViewById(R.id.acbt_job_giveup);
        mEtJobName = (EditText)findViewById(R.id.acet_job_name);
        mEtJobEndDate = (EditText)findViewById(R.id.acet_job_enddate);
        mBtSave.setOnClickListener(this);
        mBtGiveup.setOnClickListener(this);
        mEtJobEndDate.setOnTouchListener(this);

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
    public void onClick(View v)    {
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
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN)    {
            switch(v.getId())   {
                case R.id.acet_job_enddate :    {
                    onTouchDate(event);
                }
                break;
            }
        }

        return true;
    }

    /**
     * 生成日期选择dialog，确定任务结束日期
     * @param event 事件
     */
    private void onTouchDate(MotionEvent event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.dialog_datetime, null);
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

        builder.setTitle("选择任务结束时间");
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
                                12,
                                30));
                                /*
                                timePicker.getHour(),
                                timePicker.getMinute()));
                                */

                mEtJobEndDate.setText(sb);
                mEtJobEndDate.requestFocus();

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

        Date curdt = new Date();
        Intent data = new Intent();
        CameraJob cj = new CameraJob();
        cj.job_name = job_name;
        cj.job_type = job_type;
        cj.job_point = job_point;
        cj.ts.setTime(curdt.getTime());

        data.putExtra(GlobalDef.STR_LOAD_JOB, cj);
        setResult(GlobalDef.INTRET_JOB_SAVE, data);
        return true;
    }

    /**
     * 放弃当前任务
     */
    private void do_giveup()  {
        Intent data = new Intent();
        setResult(GlobalDef.INTRET_JOB_GIVEUP, data);
    }


}






