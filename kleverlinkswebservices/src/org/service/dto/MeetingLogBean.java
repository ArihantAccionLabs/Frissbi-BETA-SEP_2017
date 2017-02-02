package org.service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MeetingLogBean {

	private Integer senderUserId;
	private Integer meetingId;
	private String fullName;
	private String userName;
	private LocalDate date;
	private LocalDateTime fromDate;
	private LocalDateTime toDate;
	private Float startTime;
	private Float endTime;
	private String description;
	private String address;
	private String latitude;
	private String longitude;
	
	

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
	public LocalDateTime getFromDate() {
		return fromDate;
	}
	public void setFromDate(LocalDateTime fromDate) {
		this.fromDate = fromDate;
	}
	public LocalDateTime getToDate() {
		return toDate;
	}
	public void setToDate(LocalDateTime toDate) {
		this.toDate = toDate;
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
	public Integer getSenderUserId() {
		return senderUserId;
	}
	public void setSenderUserId(Integer senderUserId) {
		this.senderUserId = senderUserId;
	}
	public void setEndTime(Float endTime) {
		this.endTime = endTime;
	}
	public void setMeetingStatus(int meetingStatus) {
		this.meetingStatus = meetingStatus;
	}
	@Override
	public String toString() {
		return "MeetingLogBean [senderUserId=" + senderUserId + ", meetingId=" + meetingId + ", fullName=" + fullName
				+ ", userName=" + userName + ", date=" + date + ", fromDate=" + fromDate + ", toDate=" + toDate
				+ ", startTime=" + startTime + ", endTime=" + endTime + ", description=" + description + ", address="
				+ address + ", latitude=" + latitude + ", longitude=" + longitude + ", meetingStatus=" + meetingStatus
				+ "]";
	}

}
