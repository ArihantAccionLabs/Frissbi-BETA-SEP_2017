package com.frissbi.notifications;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.TSLocationManager;
import com.frissbi.Utility.Utility;
import com.frissbi.activities.GroupDetailsActivity;
import com.frissbi.activities.MeetingDetailsActivity;
import com.frissbi.activities.ProfileActivity;
import com.frissbi.activities.SuggestionsActivity;
import com.frissbi.enums.NotificationType;
import com.frissbi.networkhandler.TSNetworkHandler;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

public class GcmIntentService extends IntentService {
    Context context;
    public static int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    String mNotificationName, msg2, msg4;
    Long mMeetingId;
    public static final String TAG = "GcmIntentService";
    private boolean isLocationSelected;
    private SharedPreferences mSharedPreferences;
    private Long mUserId;
    private String locationSuggestionJsonString;
    private boolean isLocationUpdate;
    private Long friendUserId;
    private Long groupId;
    private Uri soundUri;
    private String meetingMessage;

    //  Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
    public GcmIntentService() {
        super("GcmIntentService");
        // TODO Auto-generated constructor stub
        soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }

    @Override
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
                FLog.d(TAG, "isLocationUpdate" + isLocationUpdate);
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

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty())

        {

            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
                // If it's a1 regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.
               /* for (int i = 0; i < 5; i++) {
                    Log.i(TAG, "Working... " + (i + 1)
                            + "/5 @ " + SystemClock.elapsedRealtime());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                }
                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());*/
                // Post notification of received message.
                //sendNotification("Received: " + extras.toString());
                sendNotification(msg);
                Log.i(TAG, "Received: " + extras.toString());
            }
        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {
        Log.d("GcmIntentService", "mNotificationName" + mNotificationName);
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
            FLog.d(TAG, "isLocationSelected--------" + isLocationSelected);
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
            FLog.d(TAG, "isLocationUpdate-----" + isLocationUpdate);
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
            Log.d("GcmIntentService", "location" + location);
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