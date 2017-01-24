package com.frissbi.Frissbi_Pojo;

/**
 * Created by KNPL003 on 21-08-2015.
 */
public class Meeting_summaryPojo {
    public String MeetingDescription;
    public String SenderFromDateTime;
    public String SenderToDateTime;
    public String Latitude;
    public String Longitude;
    public String DestinationAddress;
   public String avatarPath;
   public String FirstName;
    public String LastName;

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public String getDestinationAddress() {
        return DestinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        DestinationAddress = destinationAddress;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getMeetingDescription() {
        return MeetingDescription;
    }

    public void setMeetingDescription(String meetingDescription) {
        MeetingDescription = meetingDescription;
    }

    public String getSenderFromDateTime() {
        return SenderFromDateTime;
    }

    public void setSenderFromDateTime(String senderFromDateTime) {
        SenderFromDateTime = senderFromDateTime;
    }

    public String getSenderToDateTime() {
        return SenderToDateTime;
    }

    public void setSenderToDateTime(String senderToDateTime) {
        SenderToDateTime = senderToDateTime;
    }
}