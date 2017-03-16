package org.kleverlinks.bean;

public class AppUserFriendBean {
	
	private Long userId;
	private String coverImageId;
	private String profileImageId;
	private String registrationDate;
	
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getProfileImageId() {
		return profileImageId;
	}
	public void setProfileImageId(String profileImageId) {
		this.profileImageId = profileImageId;
	}
	public String getCoverImageId() {
		return coverImageId;
	}
	public void setCoverImageId(String coverImageId) {
		this.coverImageId = coverImageId;
	}
	
	public String getRegistrationDate() {
		return registrationDate;
	}
	public void setRegistrationDate(String registrationDate) {
		this.registrationDate = registrationDate;
	}

}
