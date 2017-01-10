package org.kleverlinks.webservice;

public class SMTPMailSender  {
	private  Mail m;
	public  void sendMessage(String toAddress,String subject,String body) throws Exception{
		m = new Mail("donotreply@frissbi.com", "FRISSBI@123s"); 
		String[] toArr = {toAddress}; // This is an array, you can add more emails, just separate them with a coma
		m.setTo(toArr); // load array to setTo function
		m.setFrom("donotreply@frissbi.com"); // who is sending the email 
		m.setSubject(subject); 
		m.setBody(body); 

		m.send();
 
	}
	
	public static void main(String args[] ) throws Exception{
		SMTPMailSender mailSender = new SMTPMailSender();
		mailSender.sendMessage("dharmakolla85@gmail.com", "Email Verification","Please click on the link for email verification");
	}
}
