package main.java.io.mosip.ivv.utils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import com.sun.mail.smtp.SMTPTransport;
import main.java.io.mosip.ivv.base.BaseHelper;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

public class SendEmail {

    public static void sendEmailTestRigStatus(String email_server, String emailUsername, String emailPassword,
                                               ArrayList<String> email_recipient, String emailSubject,
                                               String emailText, ExtentTest extentTest, ArrayList<String> emailAttachments) {

        Properties prop = System.getProperties();
        if (email_server.contains("smtpout.secureserver.net")) {
            prop.put("mail.smtp.auth", "true");
        } else if (email_server.contains("outlook.office365.com")) {
            prop.setProperty("mail.smtp.auth", "true");
            prop.setProperty("mail.smtp.starttls.enable", "true");
            prop.setProperty("mail.smtp.port", "587");
            prop.setProperty("mail.smtp.host", BaseHelper.otpEmail_hostname);
        }

        Session session = Session.getInstance(prop, null);
        Message msg = new MimeMessage(session);

        try {
            msg.setFrom(new InternetAddress(emailUsername));
            for (String emailTo : email_recipient) {
                msg.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo.toString()));
            }

            msg.setSubject(emailSubject);

            MimeBodyPart msgBody = new MimeBodyPart();
            msgBody.setText(emailText);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(msgBody);

            for (String emailAttachment: emailAttachments) {
                MimeBodyPart msgAttachment = new MimeBodyPart();
                FileDataSource fileSource = new FileDataSource(emailAttachment);
                msgAttachment.setDataHandler(new DataHandler(fileSource));
                msgAttachment.setFileName(fileSource.getName());
                multipart.addBodyPart(msgAttachment);
            }

            msg.setContent(multipart);
            if (email_server.contains("smtpout.secureserver.net")) {
                SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
                t.connect(email_server, emailUsername, emailPassword);
                t.sendMessage(msg, msg.getAllRecipients());
                t.close();
            } else if (email_server.contains("outlook.office365.com")) {
                SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
                t.connect(BaseHelper.otpEmail_hostname, 587, BaseHelper.otpEmail_username, BaseHelper.otpEmail_password);
                t.sendMessage(msg, msg.getAllRecipients());
                t.close();
            }

            extentTest.log(Status.INFO,"Test Rig - Daily execution status email sent");
            Utils.auditLog.fine("Test Rig - Daily execution status email sent");
        } catch (MessagingException e) {
            e.printStackTrace();
            extentTest.log(Status.FAIL,"Failed to send Test Rig - Daily execution status email :" + e.getMessage());
            Utils.auditLog.fine("Test Rig - Daily execution status email sent");
        }
    }
}