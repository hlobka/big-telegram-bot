package helper.mail;

import helper.file.SharedObject;
import helper.logger.ConsoleLogger;
import telegram.bot.data.Common;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class MailHelper {

    public static void tryToSendMail(String to, String title, String messageContent) {
        tryToSendMail(to, title, messageContent, "text/html");
    }

    public static void tryToSendMail(String to, String title, String messageContent, String type) {
        try {
            sendMail(to, title, messageContent, type);
        } catch (MessagingException e) {
            ConsoleLogger.logErrorFor(MailHelper.class, e);
        }
    }

    public static void sendMail(String to, String title, String messageContent, String type) throws MessagingException {
        final String username = Common.EMAIL.login;
        final String password = Common.EMAIL.pass;

        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", Common.EMAIL.url);
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(
            Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(title);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(messageContent, type);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);

        Transport.send(message);
    }
}
