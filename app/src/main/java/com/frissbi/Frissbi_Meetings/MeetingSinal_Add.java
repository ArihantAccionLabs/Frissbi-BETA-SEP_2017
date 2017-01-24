package com.frissbi.Frissbi_Meetings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by KNPL003 on 29-06-2015.
 */
public class MeetingSinal_Add extends Activity {
    private ProgressDialog pDialog;

    public static String MeetingId;
    public static String requestDateTime;
    public static String scheduledTimeSlot;
    public static String senderFromDateTime;
    public static String senderToDateTime;
    public static String meetingDescription;
    public static String RecipientDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent chat = getIntent();
        if (null != chat) {
            // Friss_Pojo.UserNameTo = intent.getStringExtra(Friss_Pojo.USER_NAME);
            //FristNameTo = intent.getStringExtra(Friss_Pojo.FIRST_NAME);
            //LastNameTo = intent.getStringExtra(Friss_Pojo.LAST_NAME);
            // RecipientDetails = chat.getStringExtra("Useridto_meeting");
            senderFromDateTime = getIntent().getStringExtra("keyName");
            senderToDateTime = getIntent().getStringExtra("keyname1");
            meetingDescription = getIntent().getStringExtra("keyname2");
            requestDateTime = getIntent().getStringExtra("keyname3");
            scheduledTimeSlot = getIntent().getStringExtra("keyname4");
            MeetingId = getIntent().getStringExtra("keyname5");
        }
    }
}
