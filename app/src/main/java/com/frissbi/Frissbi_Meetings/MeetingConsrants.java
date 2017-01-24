package com.frissbi.Frissbi_Meetings;

/**
 * Created by KNPL003 on 24-06-2015.
 */
public class MeetingConsrants {
    public  String MeetingID;
    public String RequestDateTime;
    public  String ScheduledDateTime;
    public  String LocationID;
    public  String ScheduledTimeSlot;
    public  String MeetingDescription;
    public  String SenderFromDateTime;
    public  String SenderToDateTime;
    public  String googleadrres;

    public String getGoogleadrres() {
        return googleadrres;
    }

    public void setGoogleadrres(String googleadrres) {
        this.googleadrres = googleadrres;
    }

    public String getLocationID() {
        return LocationID;
    }

    public void setLocationID(String locationID) {
        LocationID = locationID;
    }

    public String getMeetingDescription() {
        return MeetingDescription;
    }

    public void setMeetingDescription(String meetingDescription) {
        MeetingDescription = meetingDescription;
    }

    public String getMeetingID() {
        return MeetingID;
    }

    public void setMeetingID(String meetingID) {
        MeetingID = meetingID;
    }

    public String getRequestDateTime() {
        return RequestDateTime;
    }

    public void setRequestDateTime(String requestDateTime) {
        RequestDateTime = requestDateTime;
    }

    public String getScheduledDateTime() {
        return ScheduledDateTime;
    }

    public void setScheduledDateTime(String scheduledDateTime) {
        ScheduledDateTime = scheduledDateTime;
    }

    public String getScheduledTimeSlot() {
        return ScheduledTimeSlot;
    }

    public void setScheduledTimeSlot(String scheduledTimeSlot) {
        ScheduledTimeSlot = scheduledTimeSlot;
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