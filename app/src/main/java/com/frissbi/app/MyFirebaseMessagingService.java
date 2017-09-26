package com.frissbi.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.location.Location;

import com.frissbi.app.Utility.SharedPreferenceHandler;
import com.frissbi.app.Utility.TSLocationManager;
import com.frissbi.app.Utility.Utility;
import com.frissbi.app.activities.GroupDetailsActivity;
import com.frissbi.app.activities.MeetingDetailsActivity;
import com.frissbi.app.activities.ProfileActivity;
import com.frissbi.app.activities.SuggestionsActivity;
import com.frissbi.app.enums.NotificationType;
import com.frissbi.app.networkhandler.TSNetworkHandler;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import android.net.Uri;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;



/**
 * Created by santhoshadigau on 21/09/17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";
    Context context;
    public static int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    String mNotificationName, msg2, msg4;
    Long mMeetingId;

    private boolean isLocationSelected;
    private SharedPreferences mSharedPreferences;
    private Long mUserId;
    private String locationSuggestionJsonString;
    private boolean isLocationUpdate;
    private Long friendUserId;
    private Long groupId;

    private String meetingMessage;
    private Uri soundUri;

    public MyFirebaseMessagingService() {
        ;
        // TODO Auto-generated constructor stub
        soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }


    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub
        Bundle extras = intent.getExtras();

        String msg = intent.getStringExtra("message");
        mSharedPreferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        mUserId = SharedPreferenceHandler.getInstance(this).getUserId();
        mNotificationName = intent.getStringExtra("NotificationName");
        msg2 = intent.getStringExtra("userName");
        if (intent.getExtras().containsKey("meetingId") && intent.getStringExtra("meetingId") != null) {
            mMeetingId = Long.parseLong(intent.getStringExtra("meetingId"));
        }
        msg4 = intent.getStringExtra("userId");

        if (intent.getExtras().containsKey("locationSuggestionJson")) {
            Log.d("GcmIntentService", "locationSuggestionJson" + intent.getExtras().getString("locationSuggestionJson"));
            locationSuggestionJsonString = intent.getExtras().getString("locationSuggestionJson");
            meetingMessage = intent.getExtras().getString("meetingMessage");
            try {
                JSONObject jsonObject = new JSONObject(locationSuggestionJsonString);
                isLocationUpdate = jsonObject.getBoolean("isLocationUpdate");
                Log.d(TAG, "isLocationUpdate" + isLocationUpdate);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        if (intent.getExtras().containsKey("isLocationSelected")) {
            Log.d(TAG, "isLocationSelected" + intent.getExtras().getString("isLocationSelected"));
            isLocationSelected = Boolean.valueOf(intent.getExtras().getString("isLocationSelected"));
        }

        if (intent.getExtras().containsKey("friendUserId")) {
            friendUserId = Long.parseLong(intent.getExtras().getString("friendUserId"));
        }

        if (intent.getExtras().containsKey("groupId")) {
            groupId = Long.parseLong(intent.getExtras().getString("groupId"));
        }



      sendNotification(msg);


    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO: Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
    }

    private void sendNotification(String msg) {
        Log.d("FCM", "mNotificationName" + mNotificationName);
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
        if (mNotificationName.equalsIgnoreCase(NotificationType.FRIEND_PENDING_REQUESTS.toString())) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("friendUserId", friendUserId);
            intent.putExtra("status", "CONFIRM");
            showNotification(intent, msg);

        } else if (mNotificationName.equalsIgnoreCase(NotificationType.FRIEND_REQUEST_ACCEPTANCE.toString())) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("friendUserId", friendUserId);
            intent.putExtra("status", "ACCEPTED");
            showNotification(intent, msg);

        } else if (mNotificationName.equalsIgnoreCase(NotificationType.MEETING_PENDING_REQUESTS.toString())) {
            Intent intent = new Intent(this, MeetingDetailsActivity.class);
            intent.putExtra("meetingId", mMeetingId);
            showNotification(intent, msg);


        } else if (mNotificationName.equals(NotificationType.MEETING_REQUEST_ACCEPTANCE.toString())) {
            Intent intent = new Intent(this, MeetingDetailsActivity.class);
            intent.putExtra("meetingId", mMeetingId);
            showNotification(intent, msg);

        } else if (mNotificationName.equals(NotificationType.MEETING_SUMMARY.toString())) {
            Log.d(TAG, "isLocationSelected--------" + isLocationSelected);
            if (!isLocationSelected) {
                sendUserDetailsForMeetingSummaryToServer();
            } else {
                Intent intent = new Intent(this, MeetingDetailsActivity.class);
                intent.putExtra("meetingId", mMeetingId);
                showNotification(intent, msg);
            }
        } else if (mNotificationName.equals(NotificationType.MEETING_REJECTED.toString())) {
            Intent intent = new Intent(this, MeetingDetailsActivity.class);
            intent.putExtra("meetingId", mMeetingId);
            showNotification(intent, msg);

        } else if (mNotificationName.equals(NotificationType.MEETING_LOCATION_SUGGESTION.toString())) {
            Log.d(TAG, "meetingId" + mMeetingId);
            Log.d(TAG, "isLocationUpdate-----" + isLocationUpdate);
            Intent intent;
            if (isLocationUpdate) {
                intent = new Intent(this, MeetingDetailsActivity.class);
                intent.putExtra("meetingId", mMeetingId);
            } else {
                intent = new Intent(this, SuggestionsActivity.class);
                intent.putExtra("meetingId", mMeetingId);
                intent.putExtra("locationSuggestionJson", locationSuggestionJsonString);
                intent.putExtra("meetingMessage", meetingMessage);
            }
            showNotification(intent, msg);

        } else if (mNotificationName.equals(NotificationType.ADDED_TO_GROUP.toString())) {

            Intent intent = new Intent(this, GroupDetailsActivity.class);
            intent.putExtra("groupId", groupId);
            intent.putExtra("callFrom", "notification");
            showNotification(intent, msg);

        }

    }

    public static void cancelNotification(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel((String) getAppName(context), NOTIFICATION_ID);
    }

    private static String getAppName(Context context) {
        CharSequence appName = context.getPackageManager().getApplicationLabel(context.getApplicationInfo());

        return (String) appName;
    }


    private void sendUserDetailsForMeetingSummaryToServer() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", mUserId);
            jsonObject.put("meetingId", mMeetingId);
            jsonObject.put("isLocationSelected", isLocationSelected);
            Location location = TSLocationManager.getInstance(this).getCurrentLocation();
            Log.d("FCM", "location" + location);
            if (location != null) {
                jsonObject.put("latitude", location.getLatitude());
                jsonObject.put("longitude", location.getLongitude());
                String url = Utility.REST_URI + Utility.MEETING_SUMMARY_BY_LOCATION;
                TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
                    @Override
                    public void handleResponse(TSNetworkHandler.TSResponse response) {

                    }
                });
            } else {
                Toast.makeText(context, "Enable your location", Toast.LENGTH_SHORT).show();
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    private void showNotification(Intent intent, String msg) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.noti)
                .setContentTitle("FRISSBI")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setSound(soundUri)
                .setContentIntent(contentIntent)
                .setContentText(msg)
                .setAutoCancel(true);
        mNotificationManager.notify(NOTIFICATION_ID++, mBuilder.build());
    }
}
