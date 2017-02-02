package org.service.dto;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class NotificationInfoDTO {

	private Integer senderUserId;
	private Integer userId;
	private String message;
	private String notificationType;
	private String notificationDescription;
	private Integer meetingId;
	private JSONObject jsonObject;
	
	private List<Integer> userList = new ArrayList<>();
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getNotificationType() {
		return notificationType;
	}
	public String getNotificationDescription() {
		return notificationDescription;
	}
	public void setNotificationDescription(String notificationDescription) {
		this.notificationDescription = notificationDescription;
	}
	public void setNotificationType(String notificationType) {
		this.notificationType = notificationType;
	}
	public Integer getMeetingId() {
		return meetingId;
	}
	public void setMeetingId(Integer meetingId) {
		this.meetingId = meetingId;
	}

	public Integer getSenderUserId() {
		return senderUserId;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public void setSenderUserId(Integer senderUserId) {
		this.senderUserId = senderUserId;
	}
	public JSONObject getJsonObject() {
		return jsonObject;
	}
	public void setJsonObject(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	public List<Integer> getUserList() {
		return userList;
	}
	public void setUserList(List<Integer> userList) {
		this.userList = userList;
	}
	@Override
	public String toString() {
		return "NotificationInfoDTO [senderUserId=" + senderUserId + ", userId=" + userId + ", message=" + message
				+ ", notificationType=" + notificationType + ", notificationDescription=" + notificationDescription
				+ ", meetingId=" + meetingId + "]";
	}

}
