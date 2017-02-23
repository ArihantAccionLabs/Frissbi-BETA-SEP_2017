package org.kleverlinks.bean;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

import org.json.JSONObject;

public class UserFreeTimeBean {

	private Long userId;
	private Long userFreeTimeId;
	private Date freeFromTime;
	private Date freeToTime;
	private String description;
	private String firstName;
	private String lastName;
	private LocalDate date;
	
	private Float startTime;
	private Float endTime;
	private Boolean isConflicted;
	
	public Boolean getIsConflicted() {
		return isConflicted;
	}
	public void setIsConflicted(Boolean isConflicted) {
		this.isConflicted = isConflicted;
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
	public UserFreeTimeBean() {
	}
	public UserFreeTimeBean(JSONObject jsonObject){
		try{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
		this.userId = jsonObject.getLong("userId");
		this.freeFromTime = dateFormat.parse(jsonObject.getString("freeFromTime"));
		this.freeToTime = dateFormat.parse(jsonObject.getString("freeToTime"));
		this.description = jsonObject.getString("description");
		this.isConflicted = jsonObject.getBoolean("isConflicted");
		if(jsonObject.has("userFreeTimeId")){
		this.userFreeTimeId = jsonObject.getLong("userFreeTimeId");
		}
		this.isConflicted = jsonObject.getBoolean("isConflicted");
	}catch (Exception e) {
		e.printStackTrace();
	}
	}
}
