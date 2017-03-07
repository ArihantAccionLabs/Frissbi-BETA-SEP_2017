package org.kleverlinks.bean;

public class GroupInfoBean {
	
	private Long userId;
	
	private Long groupId;
	
	private String groupName;
	
	private String fullName;
	
	private String profileImageId;
	
	private String groupImage;
	
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Long getGroupId() {
		return groupId;
	}
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getProfileImageId() {
		return profileImageId;
	}
	public String getGroupImage() {
		return groupImage;
	}
	public void setGroupImage(String groupImage) {
		this.groupImage = groupImage;
	}
	public void setProfileImageId(String profileImageId) {
		this.profileImageId = profileImageId;
	}
	
	
}
