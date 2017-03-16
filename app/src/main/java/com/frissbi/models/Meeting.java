package com.frissbi.models;

import com.orm.SugarRecord;

import java.io.Serializable;
import java.util.List;

/**
 * Created by thrymr on 24/1/17.
 */

public class Meeting extends SugarRecord implements Serializable {

    private Long meetingId;
    private String date;
    private String fromTime;
    private String toTime;
    private String description;
    private Double latitude;
    private Double longitude;
    private String address;
    private int meetingStatus;
    private boolean isLocationSelected;
    private List<MeetingFriends> meetingFriendsList;
    private Long meetingSenderId;
    private String userStatus;
    private String month;


    public Meeting() {

    }


    public Long getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(Long meetingId) {
        this.meetingId = meetingId;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFromTime() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public void setToTime(String toTime) {
        this.toTime = toTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getMeetingStatus() {
        return meetingStatus;
    }

    public void setMeetingStatus(int meetingStatus) {
        this.meetingStatus = meetingStatus;
    }

    public List<MeetingFriends> getMeetingFriendsList() {
        return meetingFriendsList;
    }

    public void setMeetingFriendsList(List<MeetingFriends> meetingFriendsList) {
        this.meetingFriendsList = meetingFriendsList;
    }

    public boolean isLocationSelected() {
        return isLocationSelected;
    }

    public void setLocationSelected(boolean locationSelected) {
        isLocationSelected = locationSelected;
    }

    public Long getMeetingSenderId() {
        return meetingSenderId;
    }

    public void setMeetingSenderId(Long meetingSenderId) {
        this.meetingSenderId = meetingSenderId;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    @Override
    public String toString() {
        return "Meeting{" +
                "meetingId=" + meetingId +
                ", date='" + date + '\'' +
                ", fromTime='" + fromTime + '\'' +
                ", toTime='" + toTime + '\'' +
                ", description='" + description + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", address='" + address + '\'' +
                ", meetingStatus=" + meetingStatus +
                ", isLocationSelected=" + isLocationSelected +
                ", meetingFriendsList=" + meetingFriendsList +
                ", meetingSenderId=" + meetingSenderId +
                ", userStatus=" + userStatus +
                '}';
    }
}
