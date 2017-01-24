package com.frissbi.Frissbi_Meetings;

/**
 * Created by KNPL003 on 21-08-2015.
 */
public class MeetingPending_pojo {

    public String UserMetting_firstname;
    public String UserMetting_lastname;
    public String UserMetting_SenderUserID;
    public String UserMetting_SenderFromDateTime;
    public String UserMetting_SenderToDateTime;
    public String UserMetting_MeetingID;

    public String getUserMetting_firstname() {
        return UserMetting_firstname;
    }

    public void setUserMetting_firstname(String userMetting_firstname) {
        UserMetting_firstname = userMetting_firstname;
    }

    public String getUserMetting_lastname() {
        return UserMetting_lastname;
    }

    public void setUserMetting_lastname(String userMetting_lastname) {
        UserMetting_lastname = userMetting_lastname;
    }

    public String getUserMetting_MeetingID() {
        return UserMetting_MeetingID;
    }

    public void setUserMetting_MeetingID(String userMetting_MeetingID) {
        UserMetting_MeetingID = userMetting_MeetingID;
    }

    public String getUserMetting_SenderFromDateTime() {
        return UserMetting_SenderFromDateTime;
    }

    public void setUserMetting_SenderFromDateTime(String userMetting_SenderFromDateTime) {
        UserMetting_SenderFromDateTime = userMetting_SenderFromDateTime;
    }

    public String getUserMetting_SenderToDateTime() {
        return UserMetting_SenderToDateTime;
    }

    public void setUserMetting_SenderToDateTime(String userMetting_SenderToDateTime) {
        UserMetting_SenderToDateTime = userMetting_SenderToDateTime;
    }

    public String getUserMetting_SenderUserID() {
        return UserMetting_SenderUserID;
    }

    public void setUserMetting_SenderUserID(String userMetting_SenderUserID) {
        UserMetting_SenderUserID = userMetting_SenderUserID;
    }
}