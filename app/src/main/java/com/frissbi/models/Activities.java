package com.frissbi.models;

/**
 * Created by thrymr on 14/3/17.
 */

public class Activities {

    private Long userId;

    private String statusMessage;

    private String profileImageId;

    private String coverImageId;

    private Long meetingId;
    private String meetingMessage;

    private String freeTimeDate;
    private String freeTimeFromTime;
    private String freeTimeToTime;

    private String locationAddress;
    private String description;
    private double latitude;
    private double longitude;

    private String uploadedImageId;
    private String imageCaption;

    private String joinedDate;

    private String date;
    private int type;
    private String userProfileImageId;
    private String userName;

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getProfileImageId() {
        return profileImageId;
    }

    public void setProfileImageId(String profileImageId) {
        this.profileImageId = profileImageId;
    }

    public String getCoverImageId() {
        return coverImageId;
    }

    public void setCoverImageId(String coverImageId) {
        this.coverImageId = coverImageId;
    }

    public Long getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(Long meetingId) {
        this.meetingId = meetingId;
    }

    public String getMeetingMessage() {
        return meetingMessage;
    }

    public void setMeetingMessage(String meetingMessage) {
        this.meetingMessage = meetingMessage;
    }

    public String getFreeTimeDate() {
        return freeTimeDate;
    }

    public void setFreeTimeDate(String freeTimeDate) {
        this.freeTimeDate = freeTimeDate;
    }

    public String getFreeTimeFromTime() {
        return freeTimeFromTime;
    }

    public void setFreeTimeFromTime(String freeTimeFromTime) {
        this.freeTimeFromTime = freeTimeFromTime;
    }

    public String getFreeTimeToTime() {
        return freeTimeToTime;
    }

    public void setFreeTimeToTime(String freeTimeToTime) {
        this.freeTimeToTime = freeTimeToTime;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }


    public String getUploadedImageId() {
        return uploadedImageId;
    }

    public void setUploadedImageId(String uploadedImageId) {
        this.uploadedImageId = uploadedImageId;
    }

    public String getImageCaption() {
        return imageCaption;
    }

    public void setImageCaption(String imageCaption) {
        this.imageCaption = imageCaption;
    }

    public String getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(String joinedDate) {
        this.joinedDate = joinedDate;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserProfileImageId() {
        return userProfileImageId;
    }

    public void setUserProfileImageId(String userProfileImageId) {
        this.userProfileImageId = userProfileImageId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Activities{" +
                "userId=" + userId +
                ", statusMessage='" + statusMessage + '\'' +
                ", profileImageId='" + profileImageId + '\'' +
                ", coverImageId='" + coverImageId + '\'' +
                ", meetingId=" + meetingId +
                ", meetingMessage='" + meetingMessage + '\'' +
                ", freeTimeDate='" + freeTimeDate + '\'' +
                ", freeTimeFromTime='" + freeTimeFromTime + '\'' +
                ", freeTimeToTime='" + freeTimeToTime + '\'' +
                ", locationAddress='" + locationAddress + '\'' +
                ", description='" + description + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", uploadedImageId='" + uploadedImageId + '\'' +
                ", imageCaption='" + imageCaption + '\'' +
                ", joinedDate='" + joinedDate + '\'' +
                ", date='" + date + '\'' +
                ", type=" + type +
                ", userProfileImageId='" + userProfileImageId + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
