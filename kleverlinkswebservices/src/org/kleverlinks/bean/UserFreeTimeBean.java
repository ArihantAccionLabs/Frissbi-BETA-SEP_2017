package org.kleverlinks.bean;

import java.util.Date;

public class UserFreeTimeBean {

	private Integer userId;
	private Date freeFromTime;
	private Date freeToTime;
	private String description;
	
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
	@Override
	public String toString() {
		return "UserFreeTimeBean [userId=" + userId + ", freeFromTime=" + freeFromTime + ", freeToTime=" + freeToTime
				+ ", description=" + description + "]";
	}
}
