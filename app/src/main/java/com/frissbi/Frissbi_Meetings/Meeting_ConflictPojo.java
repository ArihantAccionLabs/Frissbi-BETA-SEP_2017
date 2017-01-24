package com.frissbi.Frissbi_Meetings;

/**
 * Created by KNPL003 on 01-09-2015.
 */
public class Meeting_ConflictPojo {
    public  String MeetingDescription;
    public  String SenderFromDateTime;
    public  String SenderToDateTime;

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
