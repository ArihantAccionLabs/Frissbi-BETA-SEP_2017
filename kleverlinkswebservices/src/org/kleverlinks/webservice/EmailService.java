package org.kleverlinks.webservice;


import com.sendgrid.SendGrid;
import com.sendgrid.SendGridException;

public class EmailService {
	private final static String EMAIL_ID = "jagadeesh83";
	private final static String EMAIL_PASSWORD = "iamsendgrid83";

	public static void sendMail() throws SendGridException {
		SendGrid.Email email = new SendGrid.Email();
		email.addTo("jagadeeswara@thrymr.net");
		email.setFrom("sunilvermaec@gmail.com");
		email.setSubject("Sending with SendGrid is Fun");
		email.setHtml("and easy to do anywhere, even with Java");
		SendGrid sendgrid = new SendGrid(EMAIL_ID, EMAIL_PASSWORD);
		//SendGrid sendgrid1 = new SendGrid("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");
		SendGrid.Response response1 = sendgrid.send(email);
		System.out.println("response1=========="+response1.getMessage());
	}
}