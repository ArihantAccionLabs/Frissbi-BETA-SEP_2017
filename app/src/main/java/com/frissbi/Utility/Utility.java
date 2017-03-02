package com.frissbi.Utility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by thrymr on 24/1/17.
 */
public class Utility {
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_ACCEPT = 1;
    public static final int STATUS_REJECT = 2;
    public static final int STATUS_COMPLETED = 3;
    //public static final String REST_URI ="http://13.76.99.32/kleverlinkswebservices";
    public static final String REST_URI = "http://192.168.2.94:9090/kleverlinkswebservices/rest";//Sunil
    public static final String USER_FRIENDSLIST = "/FriendListService/friendsList/";
    public static final String MEETING_INSERT = "/MeetingDetailsService/insertMeetingDetails/";
    public static final String MEETING_SINGALDETAILS = "/MeetingDetailsService/getUserDetailsByMeetingID/";
    public static final String MEETING_PENDINGLIST = "/MeetingDetailsService/getPendingMeetingRequests/";
    public static final String MEETING_CONFLICT = "/MeetingDetailsService/getConflictedMeetingDetails/";
    public static final String MORE_LOCATIONS = "/MeetingDetailsService/getFrissbiLocations/";
    public static final String SUBMIT_MEETING_LOCATION = "/MeetingDetailsService/updateMeetingAddress/";
    public static final String PEOPLE_SEARCH = "/FriendListService/search/";
    public static final String ADD_FRIEND = "/FriendListService/sendFriendRequest/";
    public static final String APPROVE_FRIEND = "/FriendListService/approveFriendRequest/";
    public static final String VIEW_PROFILE = "/FriendListService/seeOtherProfile/";
    public static final String MEETING_LOG_BY_DATE = "/MeetingDetailsService/getMeetingDetailsByUserID";
    public static final String MEETING_COUNT_BY_MONTH = "/CalendarService/getMeetingMonthWise";
    public static final String USER_REGISTRATION = "/UserRegistrationService/registerUser";
    public static final String CREATE_GROUP = "/GroupCreationService/create";
    public static final String GROUPS = "/GroupCreationService/getGroupInfo/";
    public static final String  ADD_PARTICIPANT= "/GroupCreationService/addMember";
    public static final String  GET_GROUP_DETAILS= "/GroupCreationService/getGroupInfoByGroupId/";
    private static Utility ourInstance = new Utility();

    public static Utility getInstance() {
        return ourInstance;
    }

    private Utility() {

    }

    public String convertTime(String time) {
        String convertedTime = null;


        Date _24HourDt = null;
        try {
            SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm");
            SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a");
            _24HourDt = _24HourSDF.parse(time);
            convertedTime = _12HourSDF.format(_24HourDt);

        } catch (ParseException e) {
            e.printStackTrace();
        }


        return convertedTime;
    }


    public Bitmap decodeFile(File f) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            final int REQUIRED_SIZE = 200;

            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Bitmap getBitmapFromString(String imageString) {
        byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}
