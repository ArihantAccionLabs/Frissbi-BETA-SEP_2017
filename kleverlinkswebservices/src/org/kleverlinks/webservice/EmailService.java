package org.kleverlinks.webservice;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {

	public static void SendMail(String emailTo , String subject , String htmlMessage) throws Exception {
		Properties properties = new Properties();
		properties.put("mail.transport.protocol", Constants.MAIL_TRANSPORT_PROTOCOL);
		properties.put("mail.smtp.host", Constants.SMTP_HOST_NAME);
		properties.put("mail.smtp.port", Constants.MAIL_SMTP_PORT);
		properties.put("mail.smtp.auth", Constants.MAIL_SMTP_AUTH);
		// …
		class SMTPAuthenticator extends javax.mail.Authenticator {
			public PasswordAuthentication getPasswordAuthentication() {
				String username = Constants.SMTP_AUTH_USER;
				String password = Constants.SMTP_AUTH_PWD;
				return new PasswordAuthentication(username, password);
			}
		}
		Authenticator auth = new SMTPAuthenticator();
		Session mailSession = Session.getDefaultInstance(properties, auth);
		MimeMessage message = new MimeMessage(mailSession);
		
		message.setFrom(new InternetAddress(Constants.SEND_EMAIL_FROM));
		
		message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(emailTo));
		
		//InternetAddress[] toAddresses = { new InternetAddress(emailTo) };
		// message.setRecipients(Message.RecipientType.TO, toAddresses);
		 message.setSubject(subject);
		 message.setContent(htmlMessage , "text/html");
		Transport transport = mailSession.getTransport();
		transport.connect();
		transport.send(message);
		transport.close();
		System.out.println("mail sent successfully");

	}

}
