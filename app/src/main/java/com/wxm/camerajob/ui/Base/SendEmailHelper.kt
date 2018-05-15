package com.wxm.camerajob.ui.Base

import android.util.Log
import java.util.*
import javax.activation.CommandMap
import javax.activation.MailcapCommandMap
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

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
                if (initContext()) {
                    if (sendOut())
                        mSEPara!!.mIFOnResult!!.onSendSuccess()
                    else
                        mSEPara!!.mIFOnResult!!.onSendFailure()
                } else {
                    mSEPara!!.mIFOnResult!!.onSendFailure()
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, e.message)
            }
        }
        thread.start()
    }

    private fun sendOut(): Boolean {
        try {
            Transport.send(mMMMsg!!)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true
    }

    private fun initContext(): Boolean {
        // for system property
        val mAttr = System.getProperties().let {
            it["mail.smtp.host"] = mSEPara!!.mSendServerHost
            it["mail.smtp.auth"] = "true"
            it["mail.debug"] = "true"

            it["mail.smtp.socketFactory.port"] = "465"
            it["mail.smtp.port"] = "465"
            it["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
            it["mail.smtp.socketFactory.fallback"] = "false"

            it
        }

        // for holder
        val mSNsession = Session.getDefaultInstance(mAttr, this)
        mMMMsg = MimeMessage(mSNsession)

        // There is something wrong with MailCap, javamail can not find a handler for the multipart/mixed part, so this bit needs to be added.
        (CommandMap.getDefaultCommandMap() as MailcapCommandMap).apply {
            addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html")
            addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml")
            addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain")
            addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed")
            addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822")
            CommandMap.setDefaultCommandMap(this)
        }

        // for email info
        try {
            // setup message body
            MimeMultipart().apply {
                addBodyPart(MimeBodyPart().apply { setText(mSEPara!!.mEmailBody) })
            }.let {
                mMMMsg!!.apply {
                    subject = mSEPara!!.mEmailTitle
                    setFrom(InternetAddress(mSEPara!!.mSendUsr))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(mSEPara!!.mRecvUsr))
                    sentDate = Date()
                    setContent(it)
                    saveChanges()
                }
            }
        } catch (e: MessagingException) {
            e.printStackTrace()
            return false
        }

        return true
    }

    companion object {
        private val LOG_TAG = ::SendEmailHelper.javaClass.simpleName
    }
}


