package main.java.io.mosip.ivv.utils;

import com.sun.mail.imap.protocol.FLAGS;
import main.java.io.mosip.ivv.base.BaseHelper;
import java.io.IOException;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.MimeMultipart;

public class OTPReader {

    public static String readOTP() {
        int emailFoundOrNot = 0;
        String otp = null;
        Message[] messages;
        Folder emailInbox;

        Properties sysProps = System.getProperties();
        sysProps.setProperty("mail.imap.ssl.enable", "true");
        sysProps.setProperty("mail.store.protocol", "imaps");
        try {
            while (emailFoundOrNot < 1) {
                Session session = Session.getInstance(sysProps, null);
                Store store = session.getStore();
                store.connect(BaseHelper.otpEmail_hostname, 993, BaseHelper.otpEmail_username, BaseHelper.otpEmail_password);
                Thread.sleep(1000);
                emailInbox = store.getFolder("Inbox");
                emailInbox.open(Folder.READ_WRITE);

                messages = emailInbox.getMessages();
                for (int i = messages.length - 1; i < messages.length; i++) {
                    Message message = messages[i];
                    try {
                        if (message.getSubject().equals("Otp message")) {
                            String msg = getTextFromMessage(message);
                            otp = getOTP(msg);
                            message.setFlag(FLAGS.Flag.SEEN, true);
                            message.setFlag(FLAGS.Flag.DELETED, true);
                            emailFoundOrNot = 1;
                            break;
                        }
                    } catch (MessagingException | NullPointerException me) {
                        //ToDo
                    }

                }
                emailInbox.close(true);
                store.close();
            }
        } catch (Exception mex) {
            mex.printStackTrace();
        }
        return otp;
    }

    public static void deleteOTPEmails() {
        Message[] messages;
        Folder emailInbox;

        Properties sysProps = System.getProperties();
        sysProps.setProperty("mail.imap.ssl.enable", "true");
        sysProps.setProperty("mail.store.protocol", "imaps");
        try
        {
            Session session = Session.getInstance(sysProps, null);
            Store store = session.getStore();
            store.connect(BaseHelper.otpEmail_hostname, 993, BaseHelper.otpEmail_username, BaseHelper.otpEmail_password);
            Thread.sleep(1000);
            emailInbox = store.getFolder("Inbox");
            emailInbox.open(Folder.READ_WRITE);

            messages = emailInbox.getMessages();
            for (int i = messages.length - 1; i < messages.length; i++) {
                Message message = messages[i];
                try
                {
                    if (message.getSubject().equals("Otp message")) {
                        message.setFlag(FLAGS.Flag.SEEN, true);
                        message.setFlag(FLAGS.Flag.DELETED, true);
                        break;
                    }
                } catch (MessagingException me) {
                    //ToDo
                } catch (NullPointerException ne) {
                    //ToDo
                }
            }
            emailInbox.close(true);
            store.close();
        } catch (Exception mex) {
            mex.printStackTrace();
        }
    }

    public static String readOtpFromOtherUser() {
        int emailFoundOrNot = 0;
        String otp = null;
        Message[] messages;
        Folder emailInbox;

        Properties sysProps = System.getProperties();
        sysProps.setProperty("mail.imap.ssl.enable", "true");
        sysProps.setProperty("mail.store.protocol", "imaps");
        try {
            while (emailFoundOrNot < 1) {
                Session session = Session.getInstance(sysProps, null);
                Store store = session.getStore();
                store.connect(BaseHelper.otpEmail_hostname, 993, BaseHelper.otpEmail_username, BaseHelper.otpEmail_password);

                Thread.sleep(1000);
                emailInbox = store.getFolder("Inbox");
                emailInbox.open(Folder.READ_WRITE);

                messages = emailInbox.getMessages();
                for (int i = messages.length - 1; i < messages.length; i++) {
                    Message message = messages[i];
                    try {
                        if (message.getSubject().equals("Otp message")) {
                            String msg = getTextFromMessage(message);
                            otp = getOTP(msg);
                            message.setFlag(FLAGS.Flag.SEEN, true);
                            message.setFlag(FLAGS.Flag.DELETED, true);
                            emailFoundOrNot = 1;
                            break;
                        }
                    } catch (MessagingException me) {
                        //to do
                    }
                }
                emailInbox.close(true);
                store.close();
            }
        } catch (Exception mex) {
            mex.printStackTrace();
        }
        return otp;
    }

    private static String getOTP(String result) {
        String[] split = result.split(" ");
        return split[split.length - 1];
    }

    private static String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append("\n").append(bodyPart.getContent());
                break;
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }
}