package org.service.dto;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.kleverlinks.bean.MeetingAcceptedUserBean;
import org.kleverlinks.bean.MeetingLogBean;

public class NotificationInfoDTO {

	private Long senderUserId;
	private Long userId;
	private String message;
	private String notificationType;
	private String notificationDescription;
	private Long meetingId;
	private Long groupId;
	private Long friendId;
	private Long userFriendListId;
	private JSONObject jsonObject;
	private List<MeetingAcceptedUserBean> meetingAcceptedUserBeanList;
	
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
	public Long getGroupId() {
		return groupId;
	}
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
	public Long getFriendId() {
		return friendId;
	}
	public void setFriendId(Long friendId) {
		this.friendId = friendId;
	}
	public Long getMeetingId() {
		return meetingId;
	}
	public void setMeetingId(Long meetingId) {
		this.meetingId = meetingId;
	}
	public Long getUserFriendListId() {
		return userFriendListId;
	}
	public void setUserFriendListId(Long userFriendListId) {
		this.userFriendListId = userFriendListId;
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
	public List<MeetingAcceptedUserBean> getMeetingAcceptedUserBeanList() {
		return meetingAcceptedUserBeanList;
	}
	public void setMeetingAcceptedUserBeanList(List<MeetingAcceptedUserBean> meetingAcceptedUserBeanList) {
		this.meetingAcceptedUserBeanList = meetingAcceptedUserBeanList;
	}
	
}
