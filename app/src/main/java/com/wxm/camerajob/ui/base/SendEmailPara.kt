package com.wxm.camerajob.ui.base

/**
 * parameter for send email
 * Created by WangXM on 2016/11/10.
 */
class SendEmailPara {
    var mIFOnResult: SendEmailResult? = null

    var mSendUsr: String? = null
    var mSendPWD: String? = null
    var mRecvUsr: String? = null
    var mSendServerType: String? = null
    var mSendServerHost: String? = null

    var mEmailTitle: String? = null
    var mEmailBody: String? = null

    interface SendEmailResult {
        fun onSendFailure()
        fun onSendSuccess()
    }
}



