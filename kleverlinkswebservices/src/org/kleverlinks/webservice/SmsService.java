package org.kleverlinks.webservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class SmsService {
	
	private String api_key;
	private String sender_id;
	private String api_url;
	private String start;
	private String method;
	String time;
	String mob_no;
	String message;
	String unicode;
	String dlr_url;
	String type;
	
	public  void setsender_id(String sid) {
		sender_id = sid;
		return;
		
	}
	public  void setapi_key(String apk) {
		api_key = apk;
		return;
	}
	
	public  void setmethod(String mt) {
		method = mt;
		return;
	}
	
	public  void setapi_url(String ap) {
		String check = ap;
		String str = check.substring(0, 7);
		String t = "http://";
		String s = "https:/";
		String st = "https://";
		if (str.equals(t)) {
			start = t;
			api_url = check.substring(7);
		} else if (check.substring(0, 8).equals(st)) {
			start = st;
			api_url = check.substring(8);
		} else if (str.equals(s)) {
			start = st;
			api_url = check.substring(7);
		} else {
			start = t;
			api_url = ap;
		}
	}
	
	public  void setparams(String ap,String mt,String apk,String sd)
	{ 
		setapi_key(apk);
		setsender_id(sd);
		setapi_url(ap);
		setmethod(mt);
	}
	
	public void addSslCertificate() throws NoSuchAlgorithmException, KeyManagementException {

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

			}
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}
		} };
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return false;
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}

		  public String process_sms(String mob_no,String message,String unicode,String time) throws IOException, KeyManagementException, NoSuchAlgorithmException
			{   	
			    addSslCertificate();
				message=URLEncoder.encode(message, "UTF-8");			
			 	if (unicode==null)
					 unicode="0";
					unicode="&unicode="+unicode;
				if (time==null)
					 time="";
				else 
					time="&time="+URLEncoder.encode(time, "UTF-8");
				
		        URL url = new URL(start+api_url+"/api/v3/?method="+method+"&api_key="+api_key+"&sender="+sender_id+"&to="+mob_no+"&message="+message+unicode+time+"&format=xml" );
			    
			    HttpURLConnection con = (HttpURLConnection) url.openConnection();
			    con.setRequestMethod("POST");
			    con.setDoOutput(true);
			    con.getOutputStream();
			    con.getInputStream();
			    BufferedReader rd;
			    String line;
	            String result = "";
	            rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
	           while ((line = rd.readLine()) != null)
	            {
	               result += line;
	            }
		        rd.close(); 
		        System.out.println("Result is" + result);
				return result;
			}
	     
	     public  void sendSms(String mob_no,String message) throws KeyManagementException, NoSuchAlgorithmException, IOException
			{
	    	 setparams(Constants.SMS_API_URL,Constants.SMS_METHOD,Constants.SMS_API_KEY,Constants.SMS_SENDER);
	    	 process_sms(mob_no, message , unicode=null, time=null);  				
									
			}
}
