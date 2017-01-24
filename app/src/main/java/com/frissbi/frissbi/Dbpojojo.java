package com.frissbi.frissbi;

/**
 * Created by KNPL003 on 30-06-2015.
 */
public class Dbpojojo {

   public static  Integer sessionid;
    public static  Integer touserid;
    public static  Integer fromuserid;
    public static  Integer groupid;

    public static  Integer chatmessageid;
    public static  Integer status;

    public static  String sentmsgdatetime;
    public static  String receivemsgdatetime;
    public static  String textmassege;


    // tabl_frissbigroup - column names
    public static  String groupname;
    public static  String groupadmin;
    public static  String recorddatetime;
    public static  String players;

    public static Integer getChatmessageid() {
        return chatmessageid;
    }

    public static void setChatmessageid(Integer chatmessageid) {
        Dbpojojo.chatmessageid = chatmessageid;
    }

    public static Integer getFromuserid() {
        return fromuserid;
    }

    public static void setFromuserid(Integer fromuserid) {
        Dbpojojo.fromuserid = fromuserid;
    }

    public static String getGroupadmin() {
        return groupadmin;
    }

    public static void setGroupadmin(String groupadmin) {
        Dbpojojo.groupadmin = groupadmin;
    }

    public static Integer getGroupid() {
        return groupid;
    }

    public static void setGroupid(Integer groupid) {
        Dbpojojo.groupid = groupid;
    }

    public static String getGroupname() {
        return groupname;
    }

    public static void setGroupname(String groupname) {
        Dbpojojo.groupname = groupname;
    }

    public static String getPlayers() {
        return players;
    }

    public static void setPlayers(String players) {
        Dbpojojo.players = players;
    }

    public static String getReceivemsgdatetime() {
        return receivemsgdatetime;
    }

    public static void setReceivemsgdatetime(String receivemsgdatetime) {
        Dbpojojo.receivemsgdatetime = receivemsgdatetime;
    }

    public static String getRecorddatetime() {
        return recorddatetime;
    }

    public static void setRecorddatetime(String recorddatetime) {
        Dbpojojo.recorddatetime = recorddatetime;
    }

    public static String getSentmsgdatetime() {
        return sentmsgdatetime;
    }

    public static void setSentmsgdatetime(String sentmsgdatetime) {
        Dbpojojo.sentmsgdatetime = sentmsgdatetime;
    }

    public static Integer getSessionid() {
        return sessionid;
    }

    public static void setSessionid(Integer sessionid) {
        Dbpojojo.sessionid = sessionid;
    }

    public static Integer getStatus() {
        return status;
    }

    public static void setStatus(Integer status) {
        Dbpojojo.status = status;
    }

    public static String getTextmassege() {
        return textmassege;
    }

    public static void setTextmassege(String textmassege) {
        Dbpojojo.textmassege = textmassege;
    }

    public static Integer getTouserid() {
        return touserid;
    }

    public static void setTouserid(Integer touserid) {
        Dbpojojo.touserid = touserid;
    }
}
