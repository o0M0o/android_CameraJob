package com.wxm.camerajob.ui.activitys;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.wxm.camerajob.R;

public class ActivityJob extends AppCompatActivity {
    private final static String TAG = "ActivityJob";
    private Button mBtSave;
    private Button mBtGiveup;
    private EditText mEtJobName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job);

        // init
        mBtSave = (Button)findViewById(R.id.acbt_job_save);
        mBtGiveup = (Button)findViewById(R.id.acbt_job_giveup);

        mEtJobName = (EditText)findViewById(R.id.acet_job_name);


    }
}
