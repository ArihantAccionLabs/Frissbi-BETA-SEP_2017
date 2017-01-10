package org.kleverlinks.webservice;

import java.util.Date;
import java.util.Properties;
 
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
 
public class HtmlEmailSender {
	
	// SMTP server information
    static String host = "smtp.frissbi.com";
    static String port = "25";
    static String mailFrom = "donotreply@frissbi.com";
    static String password = "FRISSBI@123s";
 
    public void sendHtmlEmail(String toAddress,
            String subject, String message) throws AddressException,
            MessagingException {
 
        // sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
 
        // creates a new session with an authenticator
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailFrom, password);
            }
        };
 
        Session session = Session.getInstance(properties, auth);
 
        // creates a new e-mail message
        Message msg = new MimeMessage(session);
 
        msg.setFrom(new InternetAddress(mailFrom));
        InternetAddress[] toAddresses = { new InternetAddress(toAddress) };
        msg.setRecipients(Message.RecipientType.TO, toAddresses);
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        // set plain text message
        msg.setContent(message, "text/html");
 
        // sends the e-mail
        Transport.send(msg);
 
    }
 
    /**
     * Test the send html e-mail method
     *
     */
    public static void main(String[] args) {
        
 
        // outgoing message information
        String mailTo = "dharmakolla85@gmail.com";
        String subject = "Frissbi Account Activation";
 
        // message contains HTML markups
        String message = "<p>HI Username/Firstname,</p>";
        message += "<p>Please click on the activation button below to start using FRISSBI</p>";
        message +="<a href=\"http://www.google.com\" target=\"_parent\"><button>Activate</button></a>";
        message += "<p> OR</p>";
        message +="<p>Copy &amp; paste the below URL into your browser and hit ENTER.</p>";
        message +="<p>http://www.friss.bi/oiuopijopfjiasjpdf</p>";
        message +="<p>";
        message +="_________________________________________________________________________________________________________________________________________________________________</p>";
        message +="<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(c) Frissbi, product of Kleverlinks Network Pvt Ltd.<br />";
        message +="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;www.friss.bi<br />";
        message +="</p>";
 
        HtmlEmailSender mailer = new HtmlEmailSender();
 
        try {
            mailer.sendHtmlEmail(mailTo,
                    subject, message);
            System.out.println("Email sent.");
        } catch (Exception ex) {
            System.out.println("Failed to sent email.");
            ex.printStackTrace();
        }
    }
}