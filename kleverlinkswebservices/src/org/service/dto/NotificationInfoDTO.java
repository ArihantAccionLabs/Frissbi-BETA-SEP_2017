package org.service.dto;

public class NotificationInfoDTO {

	private String message;
	private String notificationType;
	private int meetingId;
	private int receiverId;
	private int senderUserId;
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getNotificationType() {
		return notificationType;
	}
	public void setNotificationType(String notificationType) {
		this.notificationType = notificationType;
	}
	public int getMeetingId() {
		return meetingId;
	}
	public void setMeetingId(int meetingId) {
		this.meetingId = meetingId;
	}
	public int getReceiverId() {
		return receiverId;
	}
	public void setReceiverId(int receiverId) {
		this.receiverId = receiverId;
	}
	public int getSenderUserId() {
		return senderUserId;
	}
	public void setSenderUserId(int senderUserId) {
		this.senderUserId = senderUserId;
	}
	@Override
	public String toString() {
		return "NotificationInfoDTO [message=" + message + ", notificationType=" + notificationType + ", meetingId="
				+ meetingId + ", receiverId=" + receiverId + ", senderUserId=" + senderUserId + "]";
	}
	
	
}
