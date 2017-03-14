package org.kleverlinks.bean;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.json.JSONObject;

public class UserFreeTimeBean {

	private Long userId;
	private Long userFreeTimeId;
	private LocalDateTime freeFromTime;
	private LocalDateTime freeToTime;
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
	public LocalDateTime getFreeFromTime() {
		return freeFromTime;
	}
	public void setFreeFromTime(LocalDateTime freeFromTime) {
		this.freeFromTime = freeFromTime;
	}
	public LocalDateTime getFreeToTime() {
		return freeToTime;
	}
	public void setFreeToTime(LocalDateTime freeToTime) {
		this.freeToTime = freeToTime;
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
		return "UserFreeTimeBean [userId=" + userId + ", freeFromTime=" + freeFromTime + ", freeToTime=" + freeToTime  +"]";
	}
	public UserFreeTimeBean() {
	}
	public UserFreeTimeBean(JSONObject jsonObject){
		try{
		
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
		String freeDate = jsonObject.getString("freeDateTime");
		String durationTime = jsonObject.getString("duration");
		String[] timeArray = durationTime.split(":");
		LocalDateTime senderFromDateTime = LocalDateTime.ofInstant(formatter.parse(freeDate).toInstant(),ZoneId.systemDefault());
		LocalDateTime senderToDateTime = LocalDateTime.ofInstant(formatter.parse(freeDate).toInstant(), ZoneId.systemDefault()).plusHours(Integer.parseInt(timeArray[0])).plusMinutes(Integer.parseInt(timeArray[1]));
		
		System.out.println(senderToDateTime.toString()+"  ======   "+senderFromDateTime.toString());
		
		this.userId = jsonObject.getLong("userId");
		this.freeFromTime = senderFromDateTime;
		this.freeToTime = senderToDateTime;
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
