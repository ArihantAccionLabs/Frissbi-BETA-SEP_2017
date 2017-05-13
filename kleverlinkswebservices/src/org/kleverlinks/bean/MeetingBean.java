package org.kleverlinks.bean;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class MeetingBean {

	private Long senderUserId;
	private String latitude;
	private String longitude;
	private String address;
	private String meetingTitle;
	private String duration;
	private LocalDateTime senderFromDateTime;
	private LocalDateTime senderToDateTime;
	private String meetingDateTime;
	private Boolean isLocationSelected;
	
	private String meetingType;
	
	private List<Long> friendsIdList = new ArrayList<Long>();
	private List<Long> meetingIdList = new ArrayList<Long>();
	private List<String> emailIdList = new ArrayList<String>();
	private List<String> contactList = new ArrayList<String>();
	
	
	public String getMeetingDateTime() {
		return meetingDateTime;
	}
	
	public void setMeetingDateTime(String meetingDateTime) {
		this.meetingDateTime = meetingDateTime;
	}

	public Long getSenderUserId() {
		return senderUserId;
	}

	public void setSenderUserId(Long senderUserId) {
		this.senderUserId = senderUserId;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getMeetingTitle() {
		return meetingTitle;
	}

	public void setMeetingTitle(String meetingTitle) {
		this.meetingTitle = meetingTitle;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public LocalDateTime getSenderFromDateTime() {
		return senderFromDateTime;
	}

	public void setSenderFromDateTime(LocalDateTime senderFromDateTime) {
		this.senderFromDateTime = senderFromDateTime;
	}

	public LocalDateTime getSenderToDateTime() {
		return senderToDateTime;
	}

	public void setSenderToDateTime(LocalDateTime senderToDateTime) {
		this.senderToDateTime = senderToDateTime;
	}

	public Boolean getIsLocationSelected() {
		return isLocationSelected;
	}

	public void setIsLocationSelected(Boolean isLocationSelected) {
		this.isLocationSelected = isLocationSelected;
	}

	public String getMeetingType() {
		return meetingType;
	}

	public void setMeetingType(String meetingType) {
		this.meetingType = meetingType;
	}

	public List<Long> getFriendsIdList() {
		return friendsIdList;
	}

	public void setFriendsIdList(List<Long> friendsIdList) {
		this.friendsIdList = friendsIdList;
	}

	public List<Long> getMeetingIdList() {
		return meetingIdList;
	}

	public void setMeetingIdList(List<Long> meetingIdList) {
		this.meetingIdList = meetingIdList;
	}

	public List<String> getEmailIdList() {
		return emailIdList;
	}

	public void setEmailIdList(List<String> emailIdList) {
		this.emailIdList = emailIdList;
	}

	public List<String> getContactList() {
		return contactList;
	}

	public void setContactList(List<String> contactList) {
		this.contactList = contactList;
	}
	
	
	public MeetingBean(JSONObject jsonObject) {
		super();
		this.senderUserId = jsonObject.getLong("senderUserId");
		
		this.meetingType = jsonObject.getString("meetingType");

		if (jsonObject.has("latitude")) {

			this.latitude = jsonObject.getString("latitude");
		}
		if (jsonObject.has("longitude")) {

			this.longitude = jsonObject.getString("longitude");
		}
		if (jsonObject.has("address")) {

			this.address = jsonObject.getString("address");
		}
		this.meetingTitle = jsonObject.getString("meetingTitle");
		this.meetingDateTime = jsonObject.getString("meetingDateTime");

		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
		String meetingDate = jsonObject.getString("meetingDateTime");
		String durationTime = jsonObject.getString("duration");
		String[] timeArray = durationTime.split(":");
		try {
			if (timeArray.length > 1) {
				this.duration = timeArray[0] + ":" + timeArray[1] + ":00";
				LocalDateTime senderToDateTime = LocalDateTime
						.ofInstant(formatter.parse(meetingDate).toInstant(), ZoneId.systemDefault())
						.plusHours(Integer.parseInt(timeArray[0])).plusMinutes(Integer.parseInt(timeArray[1]));
				this.senderToDateTime = senderToDateTime;
			} else {
				this.duration = timeArray[0] + ":00:00";
				LocalDateTime senderToDateTime = LocalDateTime
						.ofInstant(formatter.parse(meetingDate).toInstant(), ZoneId.systemDefault())
						.plusHours(Integer.parseInt(timeArray[0]));
				this.senderToDateTime = senderToDateTime;

			}

			LocalDateTime senderFromDateTime = LocalDateTime.ofInstant(formatter.parse(meetingDate).toInstant(),
					ZoneId.systemDefault());
			this.senderFromDateTime = senderFromDateTime;
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.isLocationSelected = jsonObject.getBoolean("isLocationSelected");

		if (jsonObject.has("meetingIdsJsonArray")) {
			if (jsonObject.getJSONArray("meetingIdsJsonArray").length() == 1) {

				this.meetingIdList
						.add(jsonObject.getJSONArray("meetingIdsJsonArray").getJSONObject(0).getLong("meetingId"));
			} else {
				for (int i = 0; i < jsonObject.getJSONArray("meetingIdsJsonArray").length(); i++) {
					this.meetingIdList.add(jsonObject.getJSONArray("meetingIdsJsonArray").getLong(i));
				}

			}
		}
		if (jsonObject.has("friendsIdJsonArray")) {

			for (int i = 0; i < jsonObject.getJSONArray("friendsIdJsonArray").length(); i++) {
				this.friendsIdList.add(jsonObject.getJSONArray("friendsIdJsonArray").getLong(i));
			}
		}
		if (jsonObject.has("emailIdJsonArray")) {

			for (int i = 0; i < jsonObject.getJSONArray("emailIdJsonArray").length(); i++) {
				this.emailIdList.add(jsonObject.getJSONArray("emailIdJsonArray").getString(i));
			}
		}
		if (jsonObject.has("contactsJsonArray")) {

			for (int i = 0; i < jsonObject.getJSONArray("contactsJsonArray").length(); i++) {
				this.contactList.add(jsonObject.getJSONArray("contactsJsonArray").getString(i).replaceAll("\\s", ""));
			}
		}
	}

}
