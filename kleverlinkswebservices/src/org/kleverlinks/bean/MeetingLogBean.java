package org.kleverlinks.bean;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MeetingLogBean {

	private Long senderUserId;
	private Long meetingId;
	private String fullName;
	private String userName;
	private LocalDate date;
	private LocalDateTime fromDate;
	private LocalDateTime toDate;
	private String startTime;
	private String endTime;
	private Time meetingDuration;
	private String description;
	private String address;
	private String latitude;
	private String longitude;
	private String profileImageId;

	private int meetingStatus;

	public Long getSenderUserId() {
		return senderUserId;
	}
	public void setSenderUserId(Long senderUserId) {
		this.senderUserId = senderUserId;
	}
	public Long getMeetingId() {
		return meetingId;
	}
	public void setMeetingId(Long meetingId) {
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

	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public void setToDate(LocalDateTime toDate) {
		this.toDate = toDate;
	}

	public void setMeetingStatus(int meetingStatus) {
		this.meetingStatus = meetingStatus;
	}
	public String getProfileImageId() {
		return profileImageId;
	}
	public Time getMeetingDuration() {
		return meetingDuration;
	}
	public void setMeetingDuration(Time meetingDuration) {
		this.meetingDuration = meetingDuration;
	}
	public void setProfileImageId(String profileImageId) {
		this.profileImageId = profileImageId;
	}
}
