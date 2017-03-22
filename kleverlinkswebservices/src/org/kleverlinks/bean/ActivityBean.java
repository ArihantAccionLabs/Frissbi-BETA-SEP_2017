package org.kleverlinks.bean;

import java.util.Date;

public class ActivityBean {

	private Long userId;
	private Long activityId;
	private java.util.Date date;
	private String profileImage;
	private String coverImage;
	private Long meetingId;
	private String status;
	private String meetingMessage;
	private String meetingUserImageId;
	private String userProfileImageId;
	private String userFullName;
	private String address;
	private String locationDescription;
	private int isPrivate;
	private String imageDescription;
	private String image;
	private String fromDate;
	private String toDate;
	private String registrationDate;

	
	
	public String getMeetingUserImageId() {
		return meetingUserImageId;
	}
	public void setMeetingUserImageId(String meetingUserImageId) {
		this.meetingUserImageId = meetingUserImageId;
	}

	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getProfileImage() {
		return profileImage;
	}
	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}
	public String getCoverImage() {
		return coverImage;
	}
	public void setCoverImage(String coverImage) {
		this.coverImage = coverImage;
	}
	public Long getMeetingId() {
		return meetingId;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Long getActivityId() {
		return activityId;
	}
	public String getUserProfileImageId() {
		return userProfileImageId;
	}
	public void setUserProfileImageId(String userProfileImageId) {
		this.userProfileImageId = userProfileImageId;
	}
	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}
	public void setMeetingId(Long meetingId) {
		this.meetingId = meetingId;
	}
	public String getStatus() {
		return status;
	}
	
	public String getMeetingMessage() {
		return meetingMessage;
	}
	public void setMeetingMessage(String meetingMessage) {
		this.meetingMessage = meetingMessage;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getIsPrivate() {
		return isPrivate;
	}
	public void setIsPrivate(int isPrivate) {
		this.isPrivate = isPrivate;
	}
	
	public String getFromDate() {
		return fromDate;
	}
	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}
	public String getToDate() {
		return toDate;
	}
	public void setToDate(String toDate) {
		this.toDate = toDate;
	}
	
	public String getRegistrationDate() {
		return registrationDate;
	}
	public void setRegistrationDate(String registrationDate) {
		this.registrationDate = registrationDate;
	}
	public String getImageDescription() {
		return imageDescription;
	}
	public void setImageDescription(String imageDescription) {
		this.imageDescription = imageDescription;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getUserFullName() {
		return userFullName;
	}
	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}
	public String getLocationDescription() {
		return locationDescription;
	}
	public void setLocationDescription(String locationDescription) {
		this.locationDescription = locationDescription;
	}
	@Override
	public String toString() {
		return "ActivityBean [userId=" + userId + ", activityId=" + activityId + ", date=" + date + ", profileImage="
				+ profileImage + ", coverImage=" + coverImage + ", meetingId=" + meetingId + ", status=" + status
				+ ", meetingMessage=" + meetingMessage + ", address=" + address + ", isPrivate=" + isPrivate
				+ ", imageDescription=" + imageDescription + ", image=" + image + ", fromDate=" + fromDate + ", toDate="
				+ toDate + ", registrationDate=" + registrationDate + "]";
	}
}
