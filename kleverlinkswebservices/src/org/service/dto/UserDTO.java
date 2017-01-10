package org.service.dto;

public class UserDTO {

	private Integer userId;
	private String emailId;
	private String fullName;
	public String getEmailId() {
		return emailId;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	@Override
	public String toString() {
		return "UserDTO [userId=" + userId + ", emailId=" + emailId + ", fullName=" + fullName + "]";
	}
	
}
