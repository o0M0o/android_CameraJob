package com.wxm.camerajob.ui.helper;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

/**
 * 发送邮件的辅助类
 * Created by 123 on 2016/11/10.
 */
public class SendEmailHelper {

    public static boolean sendEmail(SendEmailPara sp) {
        return true;
    }
}


public class SMTPMail {
    private MimeMessage mimeMsg;
    private Session session;
    private Properties props;
    private boolean needAuth = false;
    private String username = "";
    private String password = "";
    private Multipart mp;

    public SMTPMail(String smtp) {
        setSmtpHost(smtp);
        createMimeMessage();
    }

    /**
     * 设置系统属性
     * @param hostName String
     */
    public void setSmtpHost(String hostName) {
        if (props == null)
            props = System.getProperties();
        props.put("mail.smtp.host", hostName);
    }

    /**
     * @return boolean
     */
    public boolean createMimeMessage() {
        try {
            session = Session.getDefaultInstance(props, null);
            mimeMsg = new MimeMessage(session);
            mp = new MimeMultipart();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param need boolean
     */
    public void setNeedAuth(boolean need) {
        System.out.println("设置smtp身份认证：mail.smtp.auth = " + need);
        if (props == null)
            props = System.getProperties();
        if (need) {
            props.put("mail.smtp.auth", "true");
        } else {
            props.put("mail.smtp.auth", "false");
        }
    }

    /**
     * @param name String
     * @param pass String
     */
    public void setNamePass(String name, String pass) {
        System.out.println("程序得到用户名与密码");
        username = name;
        password = pass;
    }

    /**
     * @param mailSubject String
     * @return boolean
     */
    public boolean setSubject(String mailSubject) {
        System.out.println("设置邮件主题！");
        try {
            mimeMsg.setSubject(mailSubject);
            return true;
        } catch (Exception e) {
            System.err.println("设置邮件主题发生错误！");
            return false;
        }
    }

    /**
     * @param mailBody String
     */
    public boolean setBody(String mailBody) {
        try {
            System.out.println("设置邮件体格式");
            BodyPart bp = new MimeBodyPart();
            bp.setContent(
                    "<meta http-equiv=Content-Type content=text/html; charset=gb2312>"
                            + mailBody, "text/html;charset=GB2312");
            mp.addBodyPart(bp);
            return true;
        } catch (Exception e) {
            System.err.println("设置邮件正文时发生错误！" + e);
            return false;
        }
    }

    /**
     * @param filename String
     */
    public boolean addFileAffix(String filename) {
        System.out.println("增加邮件附件：" + filename);
        try {
            BodyPart bp = new MimeBodyPart();
            FileDataSource fileds = new FileDataSource(filename);
            bp.setDataHandler(new DataHandler(fileds));
            bp.setFileName(fileds.getName());
            mp.addBodyPart(bp);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param from String
     */
    public boolean setFrom(String from) {
        try {
            mimeMsg.setFrom(new InternetAddress(from)); // 设置发信人
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param to String
     */
    public boolean setTo(String to) {
        System.out.println("设置收信人");
        if (to == null)
            return false;
        try {
            mimeMsg.setRecipients(Message.RecipientType.TO, InternetAddress
                    .parse(to));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param copyto String
     */
    public boolean setCopyTo(String copyto) {
        System.out.println("发送附件到");
        if (copyto == null)
            return false;
        try {
            mimeMsg.setRecipients(Message.RecipientType.CC,
                    (Address[]) InternetAddress.parse(copyto));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 发送邮件
     *
     * @return 发送成功返回true
     */
    public boolean sendout() {
        Transport transport = null;
        try {
            mimeMsg.setContent(mp);
            mimeMsg.saveChanges();

            Session mailSession = Session.getInstance(props, null);
            transport = mailSession.getTransport("smtp");
            transport.connect((String) props.get("mail.smtp.host"), username, password);
            transport.sendMessage(mimeMsg, mimeMsg.getRecipients(Message.RecipientType.TO));
            Transport.send(mimeMsg);

            return true;
        } catch (Exception e) {
            return false;
        } finally {

            if (null != transport)
                try {
                    transport.close();
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
        }
    }

    /**
     * Just do it as this
     */
    public static void main(String[] args) {
        String mailbody = "http://www.laabc.com 用户邮件注册测试 <font color=red>欢迎光临</font> <a href=\"http://www.laabc.com\">啦ABC</a>";
        SMTPMail themail = new SMTPMail("smtp.126.com");
        themail.setNeedAuth(true);
        if (themail.setSubject("laabc.com邮件测试") == false)
            return;
//邮件内容 支持html 如 <font color=red>欢迎光临</font> <a href=\"http://www.laabc.com\">啦ABC</a>
        if (themail.setBody(mailbody) == false)
            return;
//收件人邮箱
        if (themail.setTo("shengshuai@126.com") == false)
            return;
//发件人邮箱
        if (themail.setFrom("shengshuai@126.com") == false)
            return;
//设置附件
//if (themail.addFileAffix("#######") == false)
//return; // 附件在本地机子上的绝对路径
        themail.setNamePass("用户名", "密码"); // 用户名与密码
        if (themail.sendout() == false)
            return;
    }
}

