package com.frissbi.Utility;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.frissbi.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by thrymr on 24/1/17.
 */
public class Utility {
    public static final int PROFILE_IMAGE = 0;
    public static final int COVER_IMAGE = 1;
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_ACCEPT = 1;
    public static final int STATUS_REJECT = 2;
    public static final int STATUS_COMPLETED = 3;
    public static final int CAMERA_REQUEST = 100;
    public static final int SELECT_FILE = 200;
    public static final int FRIEND_TYPE = 1;
    public static final int EMAIL_TYPE = 2;
    public static final int CONTACT_TYPE = 3;
    public static final String GROUP_NOTIFICATION_TYPE = "GROUP_TYPE";
    public static final String FRIEND_NOTIFICATION_TYPE = "FRIEND_TYPE";
    //public static final String REST_URI ="http://13.76.99.32/kleverlinkswebservices";
    public static final String REST_URI = "http://139.59.32.89:8080/kleverlinkswebservices/rest";
    //public static final String REST_URI = "http://192.168.2.148:9090/kleverlinkswebservices/rest";//Sunil
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
    public static final String REJECT_FRIEND = "/FriendListService/rejectFriendRequest";
    public static final String UNFRIEND = "/FriendListService/unFriendRequest";
    public static final String VIEW_OTHER_PROFILE = "/FriendListService/seeOtherProfile/";
    public static final String MEETING_LOG_BY_DATE = "/MeetingDetailsService/getMeetingDetailsByUserID";
    public static final String MEETING_COUNT_BY_MONTH = "/CalendarService/getMeetingMonthWise";
    public static final String USER_REGISTRATION = "/UserRegistrationService/registerUser";
    public static final String CREATE_GROUP = "/GroupCreationService/create";
    public static final String GROUPS = "/GroupCreationService/getGroupInfo/";
    public static final String ADD_PARTICIPANT = "/GroupCreationService/addMember";
    public static final String GET_GROUP_DETAILS = "/GroupCreationService/getGroupInfoByGroupId/";
    public static final String EXIT_GROUP = "/GroupCreationService/removeOrExitGroup";
    public static final String UPDATE_OR_DELETE_BY_GROUP = "/GroupCreationService/updateOrDeleteGroupByAdmin";
    public static final String VIEW_PROFILE = "/FriendListService/viewProfile/";
    public static final String SAVED_LOCATIONS = "/UserSettingsService/getUserPreferredLocations/";
    public static final String LOCATION_INSERT = "/UserSettingsService/insertUserPreferredLocations/";
    public static final String UPCOMING_MEETINGS = "/TimeLineService/getOneWeekMeetingInfo/";
    public static final String POST_FREE_TIME = "/TimeLineService/postTime";
    public static final String INSERT_PROFILE_IMAGE = "/UserRegistrationService/insertProfileImage";
    public static final String INSERT_COVER_IMAGE = "/UserActivityService/insertCoverImage";
    public static final String GET_IMAGE = "/UserActivityService/getImage/";
    public static final String STATUS_MESSAGE = "/UserActivityService/insertUserStatus";
    public static final String USER_ACTIVITIES = "/UserActivityService/getUserActivity/";
    public static final String FRIENDS_ACTIVITIES = "/FriendActivityService/getFriendActivity/";
    public static final String INVITE_CONTACTS = "/FriendListService/adviseFissbiAppInstall";
    public static final String PEOPLE_YOU_MAY_KNOW = "/FriendActivityService/getPeopleYouMayKnow/";
    public static final String NOTIFICATION_LOG = "/FriendListService/getNotificationLogByUserId/";
    public static final String UPLOAD_PHOTO = "/UserActivityService/insertUserPhotos";
    public static final String CHECK_IN_LOCATION = "/UserActivityService/insertUserLocation";
    public static final String NOTIFICATION_AS_READ = "/FriendListService/updateNotificationAsRead/";
    public static final String REGISTRATION = "/UserRegistrationService/registerUser";
    public static final String LOGIN = "/UserRegistrationService/user-login";
    public static final String FORGOT = "/AuthenticateUserService/forgotPassword";

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 100;
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

    public static boolean checkPermission(Activity context, String actionName) {
        if (ContextCompat.checkSelfPermission(context,
                actionName)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(context,
                    actionName)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(context,
                        new String[]{actionName},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(context,
                        new String[]{actionName},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    public static void hideKeyboard(View view, Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    public Bitmap rotateImage(Bitmap bitmap, String mPictureImagePath) {
        int rotateAngle = 0;
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(mPictureImagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotateAngle = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotateAngle = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotateAngle = 270;
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                break;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotateAngle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                true);
    }

    public String updateTime(int hours, int mins) {

        String timeSet = "";
        if (hours > 12) {
            hours -= 12;
            timeSet = "PM";
        } else if (hours == 0) {
            hours += 12;
            timeSet = "AM";
        } else if (hours == 12)
            timeSet = "PM";
        else
            timeSet = "AM";
        String minutes = "";
        String _hours = "";

        if (hours < 10) {
            _hours = "0" + hours;
        } else {
            _hours = String.valueOf(hours);
        }

        if (mins < 10)
            minutes = "0" + mins;
        else
            minutes = String.valueOf(mins);

        // Append in a StringBuilder
        String aTime = new StringBuilder().append(_hours).append(':').append(minutes).append(" ").append(timeSet).toString();
        return aTime;

    }


    public void setImageDialog(final Context context, String imageId) {

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        // it set dialogue hole page
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);


        // fo no title
        //   dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image);
        dialog.setCanceledOnTouchOutside(true);
        final ImageView localImage = (ImageView) dialog.findViewById(R.id.img);

        // ImageCacheHandler.getInstance(context).setImage(localImage, documentId, url);

        ImageCacheHandler.getInstance(context).setImage(localImage, imageId);
        dialog.show();

    }


}
