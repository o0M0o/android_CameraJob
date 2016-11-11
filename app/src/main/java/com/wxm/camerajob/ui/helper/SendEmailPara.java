package com.wxm.camerajob.ui.helper;

/**
 * 发送邮件参数
 * Created by 123 on 2016/11/10.
 */
public class SendEmailPara {
    public interface onSendEmailResult  {
        void onSendFailure();
        void onSendSuccess();
    }

    public onSendEmailResult    mIFOnResult;

    public String   mSendUsr;
    public String   mSendPWD;
    public String   mRecvUsr;
    public String   mSendServerType;
    public String   mSendServerHost;

    public String   mEmailTitle;
    public String   mEmailBody;
}



