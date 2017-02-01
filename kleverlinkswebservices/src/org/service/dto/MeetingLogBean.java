package org.service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MeetingLogBean {

	private Integer userId;
	private Integer meetingId;
	private String fullName;
	private String userName;
	private LocalDate date;
	private LocalDateTime from;
	private LocalDateTime to;
	private String description;
	private String address;
	private String latitude;
	private String longitude;
	
	
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	private int meetingStatus;

	public Integer getMeetingId() {
		return meetingId;
	}
	public void setMeetingId(Integer meetingId) {
		this.meetingId = meetingId;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public LocalDate getDate() {
		return date;
	}
	public LocalDateTime getFrom() {
		return from;
	}
	public void setFrom(LocalDateTime from) {
		this.from = from;
	}
	public LocalDateTime getTo() {
		return to;
	}
	public void setTo(LocalDateTime to) {
		this.to = to;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
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
	public int getMeetingStatus() {
		return meetingStatus;
	}
	public void setMeetingStatus(int meetingStatus) {
		this.meetingStatus = meetingStatus;
	}
	@Override
	public String toString() {
		return "MeetingLogBean [userId=" + userId + ", meetingId=" + meetingId + ", fullName=" + fullName
				+ ", userName=" + userName + ", date=" + date + ", from=" + from + ", to=" + to + ", description="
				+ description + ", address=" + address + ", latitude=" + latitude + ", longitude=" + longitude
				+ ", meetingStatus=" + meetingStatus + "]";
	}
}
