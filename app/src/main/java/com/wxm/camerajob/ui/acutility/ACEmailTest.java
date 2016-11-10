package com.wxm.camerajob.ui.acutility;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.wxm.camerajob.R;
import com.wxm.camerajob.ui.helper.SendEmailHelper;
import com.wxm.camerajob.ui.helper.SendEmailPara;

import cn.wxm.andriodutillib.util.UtilFun;

public class ACEmailTest extends AppCompatActivity implements View.OnClickListener {
    // for action
    private Button  mBTSend;
    private Button  mBTAttach;

    // for setting
    private TextInputEditText mETEmailSender;
    private TextInputEditText mETEmailSenderPWD;
    private TextInputEditText mETEmailServerType;
    private TextInputEditText mETEmailReceiver;

    // for email
    private TextInputEditText mETEmailTitle;
    private TextInputEditText mETEmailBody;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_email_test);
        init_ui();
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        switch (vid)    {
            case R.id.bt_send : {
                send_email();
            }
            break;
        }
    }



    // BEGIN PRIVATE
    private void init_ui() {
        mBTSend = UtilFun.cast_t(findViewById(R.id.bt_send));
        mBTAttach = UtilFun.cast_t(findViewById(R.id.bt_add_attach));
        mBTSend.setOnClickListener(this);

        mETEmailSender = UtilFun.cast_t(findViewById(R.id.et_email_sender));
        mETEmailSenderPWD = UtilFun.cast_t(findViewById(R.id.et_email_sender_pwd));
        mETEmailServerType = UtilFun.cast_t(findViewById(R.id.et_email_server_type));
        mETEmailReceiver = UtilFun.cast_t(findViewById(R.id.et_email_recv));

        mETEmailTitle = UtilFun.cast_t(findViewById(R.id.et_email_tiltle));
        mETEmailBody = UtilFun.cast_t(findViewById(R.id.et_email_body));
    }

    /**
     * 发送邮件
     */
    private void send_email()   {
        if(!check_send_email())
            return;

        do_send_email();
    }

    /**
     * 执行发送邮件动作
     */
    private void do_send_email() {
        SendEmailPara sp = new SendEmailPara();
        sp.mSendUsr = mETEmailSender.getText().toString();
        sp.mSendPWD = mETEmailSenderPWD.getText().toString();
        sp.mSendServerType = mETEmailServerType.getText().toString();
        sp.mRecvUsr = mETEmailReceiver.getText().toString();

        sp.mEmailTitle = mETEmailTitle.getText().toString();
        sp.mEmailBody = mETEmailBody.getText().toString();

        SendEmailHelper.sendEmail(sp);
    }

    /**
     * 检查是否能发送邮件
     * @return  若能发送邮件返回true
     */
    private boolean check_send_email() {
        final String str_warn = "警告";
        if(UtilFun.StringIsNullOrEmpty(mETEmailSender.getText().toString()))   {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("请输入邮件发送方!").setTitle(str_warn);

            AlertDialog dlg = builder.create();
            dlg.show();
            return false;
        }

        if(UtilFun.StringIsNullOrEmpty(mETEmailSenderPWD.getText().toString()))   {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("请输入邮件发送方登录密码!").setTitle(str_warn);

            AlertDialog dlg = builder.create();
            dlg.show();
            return false;
        }

        if(UtilFun.StringIsNullOrEmpty(mETEmailServerType.getText().toString()))   {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("请输入邮件发送服务器协议类型!").setTitle(str_warn);

            AlertDialog dlg = builder.create();
            dlg.show();
            return false;
        }

        if(UtilFun.StringIsNullOrEmpty(mETEmailReceiver.getText().toString()))   {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("请输入邮件接收方!").setTitle(str_warn);

            AlertDialog dlg = builder.create();
            dlg.show();
            return false;
        }

        if(UtilFun.StringIsNullOrEmpty(mETEmailTitle.getText().toString()))   {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("请输入邮件标题!").setTitle(str_warn);

            AlertDialog dlg = builder.create();
            dlg.show();
            return false;
        }

        if(UtilFun.StringIsNullOrEmpty(mETEmailBody.getText().toString()))   {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("请输入邮件正文!").setTitle(str_warn);

            AlertDialog dlg = builder.create();
            dlg.show();
            return false;
        }

        return true;
    }

    // END PRIVATE
}
