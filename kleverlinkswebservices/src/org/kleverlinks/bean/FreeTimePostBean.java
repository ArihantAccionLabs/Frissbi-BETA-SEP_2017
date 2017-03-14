package org.kleverlinks.bean;

import java.time.LocalDate;

public class FreeTimePostBean {
	private Long userId;
	private Long userFreeTimeId;
	private LocalDate date;
	private String startTime;
	private String endTime;
	
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Long getUserFreeTimeId() {
		return userFreeTimeId;
	}
	public void setUserFreeTimeId(Long userFreeTimeId) {
		this.userFreeTimeId = userFreeTimeId;
	}
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
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
}
