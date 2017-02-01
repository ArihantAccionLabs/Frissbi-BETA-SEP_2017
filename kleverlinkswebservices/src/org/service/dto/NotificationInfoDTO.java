package org.service.dto;

public class NotificationInfoDTO {

	private Integer senderUserId;
	private Integer userId;
	private String message;
	private String notificationType;
	private String notificationDescription;
	private Integer meetingId;
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

}
