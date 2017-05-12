package org.kleverlinks.bean;

import java.text.SimpleDateFormat;

import org.json.JSONObject;
import org.util.Utility;

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

	public AppUserBean(JSONObject userJson) {
		super();
		try {

			if (userJson.has("userId")) {
				this.userId = userJson.getLong("userId");
			}
			if (userJson.has("deviceRegistrationId")) {
				this.deviceRegistrationId = userJson.getString("deviceRegistrationId");
			}
			if (userJson.has("password")) {
				this.password = userJson.getString("password");
			}
			this.email = userJson.getString("email");
			if (userJson.has("contactno") && Utility.checkValidString(userJson.getString("contactno"))) {
				this.contactno = userJson.getString("contactno").trim();
			}
			try {
				if (userJson.has("dob") && Utility.checkValidString(userJson.getString("dob"))) {
					this.dob = new SimpleDateFormat("yyyy-mm-dd").parse(userJson.getString("dob"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (userJson.has("firstName")) {

				this.firstName = userJson.getString("firstName");
			}
			if (userJson.has("lastName")) {
				this.lastName = userJson.getString("lastName");
			} else if (userJson.has("firstName")) {
				String[] parts = this.firstName.split(" ");
				if (parts.length > 1) {
					String lastWord = parts[parts.length - 1];
					String restWord = this.firstName.substring(0, this.firstName.indexOf(lastWord)).trim();
					this.firstName = restWord;
					this.lastName = lastWord;
					this.username = this.firstName;
				}
			}
			if (userJson.has("userName")) {
				this.username = userJson.getString("userName");
			}

			if (userJson.has("isGmailLogin")) {
				this.isGmailLogin = userJson.getBoolean("isGmailLogin");
			}
			if (userJson.has("image")) {
				this.image = userJson.getString("image");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "AppUserBean [username=" + username + ", userId=" + userId + ", password=" + password + ", email="
				+ email + ", contactno=" + contactno + ", dob=" + dob + ", firstName=" + firstName + ", lastName="
				+ lastName + ", isGmailLogin=" + isGmailLogin + ", image=" + image + ", deviceRegistrationId="
				+ deviceRegistrationId + "]";
	}
}
