package com.wxm.camerajob.ui.activitys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.GlobalContext;
import com.wxm.camerajob.base.GlobalDef;

public class ActivityStart extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        final Activity home = (Activity)this;

        Button bt = (Button)findViewById(R.id.bt_totest);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home, ActivityTest.class);
                startActivityForResult(intent, 1);
            }
        });

        /* 设置全局jobservice */
        Message m = Message.obtain(GlobalContext.getInstance().mMsgHandler,
                                    GlobalDef.MSGWHAT_ADDJOB_GLOBAL);
        m.obj = this;
        GlobalContext.getInstance().mMsgHandler.sendMessageDelayed(m, 3000);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
