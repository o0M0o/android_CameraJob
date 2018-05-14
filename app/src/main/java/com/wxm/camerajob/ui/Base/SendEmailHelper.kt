package com.wxm.camerajob.ui.Base

import android.util.Log

import java.util.*

import javax.activation.CommandMap
import javax.activation.MailcapCommandMap
import javax.mail.*
import javax.mail.internet.*

/**
 * helper for send email
 * Created by 123 on 2016/11/10.
 */
class SendEmailHelper : javax.mail.Authenticator() {
    private var mSEPara: SendEmailPara? = null

    private var mMMMsg: MimeMessage? = null

    public override fun getPasswordAuthentication(): PasswordAuthentication {
        return PasswordAuthentication(mSEPara!!.mSendUsr, mSEPara!!.mSendPWD)
    }

    fun sendEmail(sp: SendEmailPara) {
        mSEPara = sp
        val thread = Thread {
            try {
                if (init_context()) {
                    if (send_out())
                        mSEPara!!.mIFOnResult!!.onSendSuccess()
                    else
                        mSEPara!!.mIFOnResult!!.onSendFailure()
                } else {
                    mSEPara!!.mIFOnResult!!.onSendFailure()
                }
            } catch (e: Exception) {
                Log.e(TAG, e.message)
            }
        }
        thread.start()
    }

    private fun send_out(): Boolean {
        try {
            Transport.send(mMMMsg!!)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true
    }

    private fun init_context(): Boolean {
        // for system property
        val mPPprops = System.getProperties()
        mPPprops["mail.smtp.host"] = mSEPara!!.mSendServerHost
        mPPprops["mail.smtp.auth"] = "true"
        mPPprops["mail.debug"] = "true"

        mPPprops["mail.smtp.socketFactory.port"] = "465"
        mPPprops["mail.smtp.port"] = "465"
        mPPprops["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        mPPprops["mail.smtp.socketFactory.fallback"] = "false"


        // for holder
        val mSNsession = Session.getDefaultInstance(mPPprops, this)
        mMMMsg = MimeMessage(mSNsession)
        val mMPparts = MimeMultipart()

        // There is something wrong with MailCap, javamail can not find a handler for the multipart/mixed part, so this bit needs to be added.
        val mc = CommandMap.getDefaultCommandMap() as MailcapCommandMap
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html")
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml")
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain")
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed")
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822")
        CommandMap.setDefaultCommandMap(mc)

        // for email info
        try {
            // setup message body
            val messageBodyPart = MimeBodyPart()
            messageBodyPart.setText(mSEPara!!.mEmailBody)
            mMPparts.addBodyPart(messageBodyPart)

            mMMMsg!!.subject = mSEPara!!.mEmailTitle
            mMMMsg!!.setFrom(InternetAddress(mSEPara!!.mSendUsr))
            mMMMsg!!.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(mSEPara!!.mRecvUsr))
            mMMMsg!!.sentDate = Date()
            mMMMsg!!.setContent(mMPparts)
            mMMMsg!!.saveChanges()
        } catch (e: MessagingException) {
            e.printStackTrace()
            return false
        }

        return true
    }

    companion object {
        private val TAG = "SendEmailHelper"
    }
}


