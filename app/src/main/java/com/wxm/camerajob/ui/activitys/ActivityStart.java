package com.wxm.camerajob.ui.activitys;

import android.app.Activity;
import android.content.Intent;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.handler.GlobalContext;
import com.wxm.camerajob.base.utility.SilentTakePhoto;

public class ActivityStart extends AppCompatActivity {
    private final static String TAG = "ActivityStart";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        final Activity home = (Activity)this;

        Button bt = (Button)findViewById(R.id.acbt_start_testcamera);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home, ActivityTest.class);
                startActivityForResult(intent, 1);
            }
        });


        Button bt1 = (Button)findViewById(R.id.acbt_start_testjob);
        bt1.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) {
                                      Intent intent = new Intent(home, ActivityJob.class);
                                      startActivityForResult(intent, 1);
                                  }
                              });

        Button bt2 = (Button)findViewById(R.id.acbt_start_testslient);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SilentTakePhoto st = new SilentTakePhoto("slient.jpg");
                st.openCamera(CameraCharacteristics.LENS_FACING_BACK, 1280, 960);
                st.captureStillPicture();
                st.closeCamera();
            }
        });

        /* 设置全局jobservice */
        Message m = Message.obtain(GlobalContext.getInstance().mMsgHandler,
                                    GlobalDef.MSGWHAT_JOB_ADD_GLOBAL);
        m.obj = this;
        GlobalContext.getInstance().mMsgHandler.sendMessageDelayed(m, 3000);
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
