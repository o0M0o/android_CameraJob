package com.wxm.camerajob.ui.test.email

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.TextInputEditText
import android.support.v7.app.AppCompatActivity
import android.view.View

import com.wxm.camerajob.R
import com.wxm.camerajob.ui.base.SendEmailHelper
import com.wxm.camerajob.ui.base.SendEmailPara
import kotterknife.bindView
import wxm.androidutil.log.TagLog
import wxm.androidutil.ui.dialog.DlgAlert

class ACTestEmail : AppCompatActivity(), View.OnClickListener {
    // for setting
    private val mETEmailSender: TextInputEditText by bindView(R.id.et_email_sender)
    private val mETEmailSenderPWD: TextInputEditText by bindView(R.id.et_email_sender_pwd)
    private val mETEmailServerType: TextInputEditText by bindView(R.id.et_email_server_type)
    private val mETEmailServerHost: TextInputEditText by bindView(R.id.et_email_server_host)
    private val mETEmailReceiver: TextInputEditText by bindView(R.id.et_email_recv)

    // for email
    private val mETEmailTitle: TextInputEditText by bindView(R.id.et_email_tiltle)
    private val mETEmailBody: TextInputEditText by bindView(R.id.et_email_body)

    // for message
    private lateinit var mMHHandler: ACSendMailMsgHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_email_test)
        initUI()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.bt_send -> {
                sendEmail()
            }
        }
    }


    // BEGIN PRIVATE
    private fun initUI() {
        findViewById<View>(R.id.bt_send).setOnClickListener(this)
        mMHHandler = ACSendMailMsgHandler(this)
    }

    /**
     * 发送邮件
     */
    private fun sendEmail() {
        if (!checkSendEmail())
            return

        doSendEmail()
    }

    /**
     * 执行发送邮件动作
     */
    private fun doSendEmail() {
        SendEmailPara().apply {
            mSendUsr = mETEmailSender.text.toString()
            mSendPWD = mETEmailSenderPWD.text.toString()
            mSendServerType = mETEmailServerType.text.toString()
            mSendServerHost = mETEmailServerHost.text.toString()
            mRecvUsr = mETEmailReceiver.text.toString()

            mEmailTitle = mETEmailTitle.text.toString()
            mEmailBody = mETEmailBody.text.toString()

            mIFOnResult = object : SendEmailPara.SendEmailResult {
                override fun onSendFailure() {
                    mMHHandler.sendEmptyMessage(MSG_TYPE_SEND_EMAIL_FAILURE)
                }

                override fun onSendSuccess() {
                    mMHHandler.sendEmptyMessage(MSG_TYPE_SEND_EMAIL_SUCCESS)
                }
            }
        }.let {
            SendEmailHelper().sendEmail(it)
        }
    }

    /**
     * 检查是否能发送邮件
     * @return  若能发送邮件返回true
     */
    private fun checkSendEmail(): Boolean {
        val szWarn = "警告"
        if (mETEmailSender.text.isNullOrEmpty()) {
            DlgAlert.showAlert(this, szWarn, "请输入邮件发送方!")
            return false
        }

        if (mETEmailSenderPWD.text.isNullOrEmpty()) {
            DlgAlert.showAlert(this, szWarn, "请输入邮件发送方登录密码!")
            return false
        }

        if (mETEmailServerType.text.isNullOrEmpty()) {
            DlgAlert.showAlert(this, szWarn, "请输入邮件发送服务器协议类型!")
            return false
        }

        if (mETEmailServerHost.text.isNullOrEmpty()) {
            DlgAlert.showAlert(this, szWarn, "请输入邮件发送服务器地址!")
            return false
        }

        if (mETEmailReceiver.text.isNullOrEmpty()) {
            DlgAlert.showAlert(this, szWarn, "请输入邮件接收方!")
            return false
        }

        if (mETEmailTitle.text.isNullOrEmpty()) {
            DlgAlert.showAlert(this, szWarn, "请输入邮件标题!")
            return false
        }

        if (mETEmailBody.text.isNullOrEmpty()) {
            DlgAlert.showAlert(this, szWarn, "请输入邮件正文!")
            return false
        }

        return true
    }

    // END PRIVATE


    private class ACSendMailMsgHandler internal constructor(private val mACHome: Activity) : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_TYPE_SEND_EMAIL_SUCCESS -> {
                    DlgAlert.showAlert(mACHome, "提示", "邮件发送成功!")
                }

                MSG_TYPE_SEND_EMAIL_FAILURE -> {
                    DlgAlert.showAlert(mACHome, "警告", "邮件发送失败!")
                }

                else -> TagLog.e("$msg can not process")
            }
        }
    }

    companion object {
        private const val MSG_TYPE_SEND_EMAIL_SUCCESS = 1
        private const val MSG_TYPE_SEND_EMAIL_FAILURE = 2
    }
}
