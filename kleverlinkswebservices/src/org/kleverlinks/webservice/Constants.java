package org.kleverlinks.webservice;

public class Constants {

	public static final String GCM_APIKEY = "AIzaSyA4GRicJNhmwQbTMGOYx75RcfRgKWWPSts";
	public static final String GOOGLE_DISTANCE_MATRIX_APIKEY = "AIzaSyBqdC9UjwJCZTDDJtLlHYNH87AJs8tkMAg";
	public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	//local db configuration
	public static final String DB_URL = "jdbc:mysql://localhost:3306/FrissDB";
	public static final String USER = "root";
	public static final String PASS = "test";
	
	//server db configuration
	/*  public static final String DB_URL = "jdbc:mysql://frissbijava.cloudapp.net/frissdb";
		public static final String USER = "Friss_App_User";
		public static final String PASS = "FrissApp2015!";*/
	
	//sending mails constant
	public static final String SMTP_HOST_NAME = "smtp.sendgrid.net";
	public static final String SMTP_AUTH_USER = "saikrishnakn";
	public static final String SMTP_AUTH_PWD = "KLEVERLINKS@30s";
	public static final String MAIL_TRANSPORT_PROTOCOL = "smtp";
	public static final int MAIL_SMTP_PORT = 587;
	public static final String MAIL_SMTP_AUTH = "true";
	public static final String SEND_EMAIL_FROM = "sunilvermaec@gmail.com";
	
	
	//local Server Urls
	public static final String SERVER_URL = "http://192.168.2.71:9090";
	
	public static String getDBURL(){
		return DB_URL;
	}
}
