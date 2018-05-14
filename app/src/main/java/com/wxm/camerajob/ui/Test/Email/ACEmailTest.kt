package com.wxm.camerajob.ui.Test.Email

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.TextInputEditText
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button

import com.wxm.camerajob.R
import com.wxm.camerajob.ui.Base.SendEmailHelper
import com.wxm.camerajob.ui.Base.SendEmailPara

import wxm.androidutil.util.UtilFun

class ACEmailTest : AppCompatActivity(), View.OnClickListener {
    // for setting
    private var mETEmailSender: TextInputEditText? = null
    private var mETEmailSenderPWD: TextInputEditText? = null
    private var mETEmailServerType: TextInputEditText? = null
    private var mETEmailServerHost: TextInputEditText? = null
    private var mETEmailReceiver: TextInputEditText? = null

    // for email
    private var mETEmailTitle: TextInputEditText? = null
    private var mETEmailBody: TextInputEditText? = null

    // for message
    private var mMHHander: ACSendMailMsgHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_email_test)
        init_ui()
    }

    override fun onClick(v: View) {
        val vid = v.id
        when (vid) {
            R.id.bt_send -> {
                send_email()
            }
        }
    }


    // BEGIN PRIVATE
    private fun init_ui() {
        val mBTSend = UtilFun.cast_t<Button>(findViewById(R.id.bt_send))
        val mBTAttach = UtilFun.cast_t<Button>(findViewById(R.id.bt_add_attach))
        mBTSend.setOnClickListener(this)

        mETEmailSender = UtilFun.cast_t<TextInputEditText>(findViewById(R.id.et_email_sender))
        mETEmailSenderPWD = UtilFun.cast_t<TextInputEditText>(findViewById(R.id.et_email_sender_pwd))
        mETEmailServerType = UtilFun.cast_t<TextInputEditText>(findViewById(R.id.et_email_server_type))
        mETEmailServerHost = UtilFun.cast_t<TextInputEditText>(findViewById(R.id.et_email_server_host))
        mETEmailReceiver = UtilFun.cast_t<TextInputEditText>(findViewById(R.id.et_email_recv))

        mETEmailTitle = UtilFun.cast_t<TextInputEditText>(findViewById(R.id.et_email_tiltle))
        mETEmailBody = UtilFun.cast_t<TextInputEditText>(findViewById(R.id.et_email_body))

        mMHHander = ACSendMailMsgHandler(this)
    }

    /**
     * 发送邮件
     */
    private fun send_email() {
        if (!check_send_email())
            return

        do_send_email()
    }

    /**
     * 执行发送邮件动作
     */
    private fun do_send_email() {
        val sp = SendEmailPara()
        sp.mSendUsr = mETEmailSender!!.text.toString()
        sp.mSendPWD = mETEmailSenderPWD!!.text.toString()
        sp.mSendServerType = mETEmailServerType!!.text.toString()
        sp.mSendServerHost = mETEmailServerHost!!.text.toString()
        sp.mRecvUsr = mETEmailReceiver!!.text.toString()

        sp.mEmailTitle = mETEmailTitle!!.text.toString()
        sp.mEmailBody = mETEmailBody!!.text.toString()

        val ac_home = this
        sp.mIFOnResult = object : SendEmailPara.onSendEmailResult {
            override fun onSendFailure() {
                mMHHander!!.sendEmptyMessage(MSG_TYPE_SEND_EMAIL_FAILURE)
            }

            override fun onSendSuccess() {
                mMHHander!!.sendEmptyMessage(MSG_TYPE_SEND_EMAIL_SUCCESS)
            }
        }

        val sh = SendEmailHelper()
        sh.sendEmail(sp)
    }

    /**
     * 检查是否能发送邮件
     * @return  若能发送邮件返回true
     */
    private fun check_send_email(): Boolean {
        val str_warn = "警告"
        if (UtilFun.StringIsNullOrEmpty(mETEmailSender!!.text.toString())) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("请输入邮件发送方!").setTitle(str_warn)

            val dlg = builder.create()
            dlg.show()
            return false
        }

        if (UtilFun.StringIsNullOrEmpty(mETEmailSenderPWD!!.text.toString())) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("请输入邮件发送方登录密码!").setTitle(str_warn)

            val dlg = builder.create()
            dlg.show()
            return false
        }

        if (UtilFun.StringIsNullOrEmpty(mETEmailServerType!!.text.toString())) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("请输入邮件发送服务器协议类型!").setTitle(str_warn)

            val dlg = builder.create()
            dlg.show()
            return false
        }

        if (UtilFun.StringIsNullOrEmpty(mETEmailServerHost!!.text.toString())) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("请输入邮件发送服务器地址!").setTitle(str_warn)

            val dlg = builder.create()
            dlg.show()
            return false
        }

        if (UtilFun.StringIsNullOrEmpty(mETEmailReceiver!!.text.toString())) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("请输入邮件接收方!").setTitle(str_warn)

            val dlg = builder.create()
            dlg.show()
            return false
        }

        if (UtilFun.StringIsNullOrEmpty(mETEmailTitle!!.text.toString())) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("请输入邮件标题!").setTitle(str_warn)

            val dlg = builder.create()
            dlg.show()
            return false
        }

        if (UtilFun.StringIsNullOrEmpty(mETEmailBody!!.text.toString())) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("请输入邮件正文!").setTitle(str_warn)

            val dlg = builder.create()
            dlg.show()
            return false
        }

        return true
    }

    // END PRIVATE


    private class ACSendMailMsgHandler internal constructor(private val mACHome: Activity) : Handler() {

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_TYPE_SEND_EMAIL_SUCCESS -> {
                    val builder = AlertDialog.Builder(mACHome)
                    builder.setMessage("邮件发送成功!")
                            .setTitle("提示")

                    val dlg = builder.create()
                    dlg.show()
                }

                MSG_TYPE_SEND_EMAIL_FAILURE -> {
                    val builder = AlertDialog.Builder(mACHome)
                    builder.setMessage("邮件发送失败!")
                            .setTitle("警告")

                    val dlg = builder.create()
                    dlg.show()
                }

                else -> Log.e(TAG, String.format("msg(%s) can not process", msg.toString()))
            }
        }

        companion object {
            private val TAG = "ACSendMailMsgHandler"
        }
    }

    companion object {
        private val MSG_TYPE_SEND_EMAIL_SUCCESS = 1
        private val MSG_TYPE_SEND_EMAIL_FAILURE = 2
    }
}
