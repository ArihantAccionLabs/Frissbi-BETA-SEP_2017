package com.frissbi.models;

/**
 * Created by thrymr on 14/3/17.
 */

public class Activities {

    private String statusMessage;

    private String profileImageId;

    private String coverImageId;

    private Long meetingId;
    private String meetingMessage;

    private String freeTimeDate;
    private String freeTimeFromTime;
    private String freeTimeToTime;

    private String locationAddress;

    private String uploadedImageId;
    private String imageCaption;

    private String joinedDate;

    private String date;
    private int  type;
    private String userProfileImageId;


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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    @Override
    public String toString() {
        return "Activities{" +
                "statusMessage='" + statusMessage + '\'' +
                ", profileImageId='" + profileImageId + '\'' +
                ", coverImageId='" + coverImageId + '\'' +
                ", meetingId=" + meetingId +
                ", meetingMessage='" + meetingMessage + '\'' +
                ", freeTimeDate='" + freeTimeDate + '\'' +
                ", freeTimeFromTime='" + freeTimeFromTime + '\'' +
                ", freeTimeToTime='" + freeTimeToTime + '\'' +
                ", locationAddress='" + locationAddress + '\'' +
                ", uploadedImageId='" + uploadedImageId + '\'' +
                ", imageCaption='" + imageCaption + '\'' +
                ", joinedDate='" + joinedDate + '\'' +
                ", date='" + date + '\'' +
                ", type=" + type +
                ", userProfileImageId='" + userProfileImageId + '\'' +
                '}';
    }
}
