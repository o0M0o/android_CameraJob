package com.wxm.camerajob.ui.Base;

import android.util.Log;

import java.util.*;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * helper for send email
 * Created by 123 on 2016/11/10.
 */
public class SendEmailHelper extends javax.mail.Authenticator {
    private final static String    TAG = "SendEmailHelper";
    private SendEmailPara   mSEPara;

    private MimeMessage     mMMMsg;
    private Properties      mPPprops;

    public SendEmailHelper()    {
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(mSEPara.mSendUsr, mSEPara.mSendPWD);
    }

    public void sendEmail(SendEmailPara sp) {
        mSEPara = sp;
        Thread thread = new Thread(() -> {
            try {
                if(init_context()) {
                    if(send_out())
                        mSEPara.mIFOnResult.onSendSuccess();
                    else
                        mSEPara.mIFOnResult.onSendFailure();
                } else {
                    mSEPara.mIFOnResult.onSendFailure();
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });
        thread.start();
    }

    private boolean send_out() {
        try {
            Transport.send(mMMMsg);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean init_context()   {
        // for system property
        mPPprops = System.getProperties();
        mPPprops.put("mail.smtp.host", mSEPara.mSendServerHost);
        mPPprops.put("mail.smtp.auth", "true");
        mPPprops.put("mail.debug", "true");

        mPPprops.put("mail.smtp.socketFactory.port", "465");
        mPPprops.put("mail.smtp.port", "465");
        mPPprops.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        mPPprops.put("mail.smtp.socketFactory.fallback", "false");


        // for holder
        Session mSNsession = Session.getDefaultInstance(mPPprops, this);
        mMMMsg = new MimeMessage(mSNsession);
        Multipart mMPparts = new MimeMultipart();

        // There is something wrong with MailCap, javamail can not find a handler for the multipart/mixed part, so this bit needs to be added.
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);

        // for email info
        try {
            // setup message body
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(mSEPara.mEmailBody);
            mMPparts.addBodyPart(messageBodyPart);

            mMMMsg.setSubject(mSEPara.mEmailTitle);
            mMMMsg.setFrom(new InternetAddress(mSEPara.mSendUsr));
            mMMMsg.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(mSEPara.mRecvUsr));
            mMMMsg.setSentDate(new Date());
            mMMMsg.setContent(mMPparts);
            mMMMsg.saveChanges();
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}


