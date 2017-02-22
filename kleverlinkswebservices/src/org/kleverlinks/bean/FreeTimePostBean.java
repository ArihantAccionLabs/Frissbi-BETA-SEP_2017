package org.kleverlinks.bean;

import java.time.LocalDate;
import java.util.Date;

public class FreeTimePostBean {
	private Long userId;
	private Date freeFromTime;
	private Date freeToTime;
	private String description;
	private LocalDate date;
	
	private String startTime;
	private String endTime;
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
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
