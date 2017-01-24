package org.service.dto;

import java.time.LocalDateTime;

public class UserDTO {

	private Integer userId;
	private Integer meetingId;
	private String emailId;
	private String fullName;
	private String userName;
	private Float startTime;
	private Float endTime;
	private LocalDateTime meetingFromTime;
	private LocalDateTime meetingToTime;
	private String description;
	
	
	private String latitude ;
	private String longitude ;
	
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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Float getStartTime() {
		return startTime;
	}

	public void setStartTime(Float startTime) {
		this.startTime = startTime;
	}

	public Float getEndTime() {
		return endTime;
	}

	public void setEndTime(Float endTime) {
		this.endTime = endTime;
	}

	public Integer getMeetingId() {
		return meetingId;
	}

	public void setMeetingId(Integer meetingId) {
		this.meetingId = meetingId;
	}

	public LocalDateTime getMeetingFromTime() {
		return meetingFromTime;
	}

	public void setMeetingFromTime(LocalDateTime meetingFromTime) {
		this.meetingFromTime = meetingFromTime;
	}

	public LocalDateTime getMeetingToTime() {
		return meetingToTime;
	}

	public void setMeetingToTime(LocalDateTime meetingToTime) {
		this.meetingToTime = meetingToTime;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "UserDTO [userId=" + userId + ", emailId=" + emailId + ", fullName=" + fullName + ", userName="
				+ userName + ", startTime=" + startTime + ", endTime=" + endTime + "]";
	}

}
