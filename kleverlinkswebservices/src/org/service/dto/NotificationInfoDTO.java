package org.service.dto;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class NotificationInfoDTO {

	private Long senderUserId;
	private Long userId;
	private String message;
	private String notificationType;
	private String notificationDescription;
	private Long meetingId;
	private JSONObject jsonObject;
	
	private MeetingLogBean meetingLogBean;
	
	private List<Long> userList = new ArrayList<>();
	
	public Long getSenderUserId() {
		return senderUserId;
	}
	public void setSenderUserId(Long senderUserId) {
		this.senderUserId = senderUserId;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Long getMeetingId() {
		return meetingId;
	}
	public void setMeetingId(Long meetingId) {
		this.meetingId = meetingId;
	}
	public JSONObject getJsonObject() {
		return jsonObject;
	}
	public void setJsonObject(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}
	public List<Long> getUserList() {
		return userList;
	}
	public void setUserList(List<Long> userList) {
		this.userList = userList;
	}
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

	public MeetingLogBean getMeetingLogBean() {
		return meetingLogBean;
	}
	public void setMeetingLogBean(MeetingLogBean meetingLogBean) {
		this.meetingLogBean = meetingLogBean;
	}
}
