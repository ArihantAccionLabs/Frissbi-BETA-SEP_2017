package org.kleverlinks.bean;

import java.util.Date;

public class UserFreeTimeBean {

	private Integer userId;
	private Date freeFromTime;
	private Date freeToTime;
	private String description;
	private String firstName;
	private String lastName;
	
	private Float startTime;
	private Float endTime;
	
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public Date getFreeFromTime() {
		return freeFromTime;
	}
	public void setFreeFromTime(Date freeFromTime) {
		this.freeFromTime = freeFromTime;
	}
	public Date getFreeToTime() {
		return freeToTime;
	}
	public void setFreeToTime(Date freeToTime) {
		this.freeToTime = freeToTime;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	@Override
	public String toString() {
		return "UserFreeTimeBean [userId=" + userId + ", freeFromTime=" + freeFromTime + ", freeToTime=" + freeToTime
				+ ", description=" + description + "]";
	}
}
