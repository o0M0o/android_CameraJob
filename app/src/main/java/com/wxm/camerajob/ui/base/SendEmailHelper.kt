package com.wxm.camerajob.ui.base

import wxm.androidutil.log.TagLog
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
                TagLog.e("send email failure", e)
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
            it[MAIL_HOST] = mSEPara!!.mSendServerHost
            it[MAIL_AUTH] = "true"
            it[MAIL_DEBUG] = "true"
            it[MAIL_PORT] = "465"

            it[MAIL_SOCKETFACTORY_PORT] = "465"
            it[MAIL_SOCKETFACTORY_CLASS] = "javax.net.ssl.SSLSocketFactory"
            it[MAIL_SOCKETFACTORY_FALLBACK] = "false"

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
            mMMMsg!!.apply {
                subject = mSEPara!!.mEmailTitle
                setFrom(InternetAddress(mSEPara!!.mSendUsr))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(mSEPara!!.mRecvUsr))
                sentDate = Date()
                setContent(MimeMultipart().apply {
                    addBodyPart(MimeBodyPart().apply { setText(mSEPara!!.mEmailBody) })
                })
                saveChanges()
            }
        } catch (e: MessagingException) {
            e.printStackTrace()
            return false
        }

        return true
    }

    companion object {
        private const val MAIL_HOST = "mail.smtp.host"
        private const val MAIL_AUTH = "mail.smtp.auth"
        private const val MAIL_DEBUG = "mail.debug"

        private const val MAIL_PORT = "mail.smtp.port"
        private const val MAIL_SOCKETFACTORY_PORT = "mail.smtp.socketFactory.port"
        private const val MAIL_SOCKETFACTORY_CLASS = "mail.smtp.socketFactory.port"
        private const val MAIL_SOCKETFACTORY_FALLBACK = "mail.smtp.socketFactory.fallback"
    }
}


