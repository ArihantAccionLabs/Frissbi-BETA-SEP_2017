package org.kleverlinks.bean;

import java.text.SimpleDateFormat;

import org.json.JSONObject;

public class AppUserBean {

	
	public AppUserBean() {
		super();
	}
	private String username;
	private Long userId;
	private String password;
	private String email;
	private String contactno;
	private java.util.Date dob;
	private String firstName;
	private String lastName;
	private Boolean isGmailLogin;
	private String image;
	private String deviceRegistrationId;
	
	public String getDeviceRegistrationId() {
		return deviceRegistrationId;
	}
	public void setDeviceRegistrationId(String deviceRegistrationId) {
		this.deviceRegistrationId = deviceRegistrationId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getContactno() {
		return contactno;
	}
	public java.util.Date getDob() {
		return dob;
	}
	public void setDob(java.util.Date dob) {
		this.dob = dob;
	}
	public void setContactno(String contactno) {
		this.contactno = contactno;
	}

	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public Boolean getIsGmailLogin() {
		return isGmailLogin;
	}
	public void setIsGmailLogin(Boolean isGmailLogin) {
		this.isGmailLogin = isGmailLogin;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public void toAppUserBean(JSONObject userJson){
		
		try{
			
			this.username = userJson.getString("userName");
			this.password = userJson.getString("password");
			this.email = userJson.getString("email");
			if (userJson.getString("dob") != null && !userJson.getString("dob").trim().isEmpty()) {
				this.dob = new SimpleDateFormat("yyyy-mm-dd").parse(userJson.getString("dob"));
			}
			this.firstName = userJson.getString("firstName");
			this.lastName = userJson.getString("lastName");
			this.isGmailLogin = userJson.getBoolean("isGmailLogin");
			if(userJson.has("image")){
				this.image = userJson.getString("image");
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public AppUserBean(JSONObject userJson) {
		super();
	try{
		    this.deviceRegistrationId = userJson.getString("deviceRegistrationId");
			this.username = userJson.getString("userName");
			this.password = userJson.getString("password");
			this.email = userJson.getString("email");
			if(userJson.getString("dob") != null && !userJson.getString("dob").trim().isEmpty()){
				this.dob = new SimpleDateFormat("yyyy-mm-dd").parse(userJson.getString("dob"));
			}
			this.firstName = userJson.getString("firstName");
			this.lastName = userJson.getString("lastName");
			this.isGmailLogin = userJson.getBoolean("isGmailLogin");
			if(userJson.has("image")){
				this.image = userJson.getString("image");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
