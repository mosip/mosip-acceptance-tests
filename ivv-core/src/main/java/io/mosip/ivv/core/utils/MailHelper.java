package io.mosip.ivv.core.utils;

import com.sun.mail.imap.protocol.FLAGS;
import lombok.Getter;
import lombok.Setter;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class MailHelper {

    @Getter
    @Setter
    public static class MailHelperResponse {
        private String body;
        private String regexout;
        public MailHelperResponse(String b, String r){
            this.body = b;
            this.regexout = r;
        }
    }

    /**
     *
     * @param subjects (Message Otp, UIN Generated, UIN XXXXXXXX05: OTP Request, UIN XXXXXXXX05: Requête OTP)
     * @param regex (otp\s([0-9]{6}))
     * @param recipient
     * @param maxMessageCount
     * @return
     */
    public static MailHelperResponse readviaRegex(ArrayList<String> subjects, String regex, String recipient, int maxMessageCount) {
        Message[] messages = null;
        Folder emailInbox;

        Properties sysProps = System.getProperties();
        sysProps.setProperty("mail.imap.ssl.enable", "true");
        sysProps.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getInstance(sysProps, null);
            Store store = session.getStore();
            //store.connect(BaseHelper.email_hostname, BaseHelper.email_username, BaseHelper.email_password);
            store.connect("outlook.office365.com", 993, System.getProperty("ivv.email.server.user"), System.getProperty("ivv.email.server.pass"));
            Thread.sleep(1000);
            emailInbox = store.getFolder("Inbox");
            emailInbox.open(Folder.READ_WRITE);

            messages = emailInbox.getMessages();
            for (int i = messages.length - 1; i > messages.length - maxMessageCount && i > 0; i--) {
                Message message = messages[i];
                try {
                    if (message.getSubject() != null) {
                        for(String sub: subjects){
                            if(message.getSubject().contains(sub)){
                                Address[] recipients = message.getRecipients(Message.RecipientType.TO);
                                for (Address address : recipients) {
                                    if(recipient.equals(address.toString())){
                                        String msg = getTextFromMessage(message);
                                        String regexout = Utils.regex(regex, msg);
                                        message.setFlag(FLAGS.Flag.SEEN, true);
                                        message.setFlag(FLAGS.Flag.DELETED, true);
                                        return new MailHelperResponse(msg, regexout);
                                    }
                                }
                            }
                        }
                    }
                } catch (MessagingException me) {
                    //to do
                }
            }
            emailInbox.close(true);
            store.close();
        } catch (Exception mex) {
            mex.printStackTrace();
        }
        return null;
    }

    public static String UINMail(String recipient, int maxMessageCount) {
        String emailBody = "";
        Message[] messages = null;
        Folder emailInbox;
        Properties sysProps = System.getProperties();
        sysProps.setProperty("mail.imap.ssl.enable", "true");
        sysProps.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getInstance(sysProps, null);
            Store store = session.getStore();
            //store.connect(BaseHelper.email_hostname, BaseHelper.email_username, BaseHelper.email_password);
            store.connect("outlook.office365.com", 993, System.getProperty("ivv.email.server.user"), System.getProperty("ivv.email.server.pass"));
            Thread.sleep(1000);
            emailInbox = store.getFolder("Inbox");
            emailInbox.open(Folder.READ_WRITE);

            messages = emailInbox.getMessages();
            for (int i = messages.length - 1; i > messages.length - maxMessageCount; i--) {
                Message message = messages[i];
                try {
                    if (message.getSubject() != null && message.getSubject().equals("UIN Generated")) {
                        Address[] recipients = message.getRecipients(Message.RecipientType.TO);
                        for (Address address : recipients) {
                            if(recipient.equals(address.toString())){
                                emailBody = getTextFromMessage(message);
                                message.setFlag(FLAGS.Flag.SEEN, true);
                                message.setFlag(FLAGS.Flag.DELETED, true);
                                return emailBody;
                            }
                        }
                    }
                } catch (MessagingException me) {
                    //to do
                }
            }
            emailInbox.close(true);
            store.close();
        } catch (Exception mex) {
            mex.printStackTrace();
        }
        return emailBody;
    }

    public static void deleteOTPEmails(String email, String pass) {
        Message[] messages = null;
        Folder emailInbox;

        Properties sysProps = System.getProperties();
        sysProps.setProperty("mail.imap.ssl.enable", "true");
        sysProps.setProperty("mail.store.protocol", "imaps");
        try
        {
            Session session = Session.getInstance(sysProps, null);
            Store store = session.getStore();
            store.connect("outlook.office365.com", 993, email, pass);
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
                    //to do
                }
            }
            emailInbox.close(true);
            store.close();
        } catch (Exception mex) {
            mex.printStackTrace();
        }
    }

    public static String readOtpFromOtherUser(String email, String pass) {
        int emailFoundOrNot = 0;
        String otp = null;
        Message[] messages = null;
        Folder emailInbox;

        Properties sysProps = System.getProperties();
        sysProps.setProperty("mail.imap.ssl.enable", "true");
        sysProps.setProperty("mail.store.protocol", "imaps");
        try {
            while (emailFoundOrNot < 1) {
                Session session = Session.getInstance(sysProps, null);
                Store store = session.getStore();
                //store.connect(BaseHelper.email_hostname, BaseHelper.email_username, BaseHelper.email_password);
                //store.connect("outlook.office365.com", 993, BaseHelper.email_username, BaseHelper.email_password);
                store.connect("outlook.office365.com", 993, email, pass);
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

    private static String getOTP(String msg) {
        String otp = Utils.regex("otp\\s([0-9]{6})", msg);
        return otp;
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
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break;
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return result;
    }
}
