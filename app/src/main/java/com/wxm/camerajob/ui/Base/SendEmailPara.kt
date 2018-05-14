package com.wxm.camerajob.ui.Base

/**
 * parameter for send email
 * Created by 123 on 2016/11/10.
 */
class SendEmailPara {

    var mIFOnResult: onSendEmailResult? = null

    var mSendUsr: String? = null
    var mSendPWD: String? = null
    var mRecvUsr: String? = null
    var mSendServerType: String? = null
    var mSendServerHost: String? = null

    var mEmailTitle: String? = null
    var mEmailBody: String? = null

    interface onSendEmailResult {
        fun onSendFailure()
        fun onSendSuccess()
    }
}



