package org.kleverlinks.bean;

import org.json.JSONObject;

public class CredentialBean {
	
	private Long userId;
	private String password;
	private String email;
	
	
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
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
	
	
	public CredentialBean(JSONObject jsonObject) {
		super();
		if(jsonObject.has("userId")){
			this.userId = jsonObject.getLong("userId");
		}
		this.password = jsonObject.getString("password");
		this.email = jsonObject.getString("email");;
	}
}
