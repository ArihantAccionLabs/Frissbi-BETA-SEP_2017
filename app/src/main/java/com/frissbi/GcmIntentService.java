package com.frissbi;

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

import com.frissbi.Frissbi_Friends.FriendSerching;
import com.frissbi.Frissbi_Friends.Friend_PendingList;
import com.frissbi.Frissbi_Meetings.Meeting_StatusPage;
import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.NotificationType;
import com.frissbi.Utility.TSLocationManager;
import com.frissbi.activities.GroupDetailsActivity;
import com.frissbi.activities.MeetingDetailsActivity;
import com.frissbi.activities.ProfileActivity;
import com.frissbi.activities.SuggestionsActivity;
import com.frissbi.locations.NearByPlacess;
import com.frissbi.networkhandler.TSNetworkHandler;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

public class GcmIntentService extends IntentService {
    Context context;
    public static int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    String mNotificationName, msg2, mMeetingId, msg4;
    public static final String TAG = "GcmIntentService";
    private boolean isLocationSelected;
    private SharedPreferences mSharedPreferences;
    private String mUserId;
    private String locationSuggestionJsonString;
    private boolean isLocationUpdate;
    private Long friendUserId;
    private Long groupId;
    private Uri soundUri;

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
        mUserId = mSharedPreferences.getString("USERID_FROM", "editor");
        mNotificationName = intent.getStringExtra("NotificationName");
        msg2 = intent.getStringExtra("userName");
        mMeetingId = intent.getStringExtra("meetingId");
        msg4 = intent.getStringExtra("userId");

        if (intent.getExtras().containsKey("locationSuggestionJson")) {
            locationSuggestionJsonString = intent.getExtras().getString("locationSuggestionJson");
            try {
                JSONObject jsonObject = new JSONObject(locationSuggestionJsonString);
                isLocationUpdate = jsonObject.getBoolean("isLocationUpdate");
                FLog.d(TAG, "isLocationUpdate" + isLocationUpdate);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        if (intent.getExtras().containsKey("isLocationSelected")) {
            Log.d(TAG, "isLocationSelected---String" + intent.getExtras().getString("isLocationSelected"));
            isLocationSelected = Boolean.valueOf(intent.getExtras().getString("isLocationSelected"));
            FLog.d(TAG, "isLocationSelected" + isLocationSelected);
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


        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
        if (mNotificationName.equalsIgnoreCase(NotificationType.FRIEND_PENDING_REQUESTS.toString())) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("friendUserId", friendUserId);
            showNotification(intent, msg);

        } else if (mNotificationName.equalsIgnoreCase(NotificationType.FRIEND_REQUEST_ACCEPTANCE.toString())) {

            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("friendUserId", friendUserId);
            showNotification(intent, msg);


        } else if (mNotificationName.equalsIgnoreCase(NotificationType.MEETING_PENDING_REQUESTS.toString())) {
            Intent intent = new Intent(this, MeetingDetailsActivity.class);
            intent.putExtra("meetingId", mMeetingId);
            intent.putExtra("callFrom", "notification");
            showNotification(intent, msg);


        } else if (mNotificationName.equals(NotificationType.MEETING_REQUEST_ACCEPTANCE.toString())) {
            Intent intent = new Intent(this, MeetingDetailsActivity.class);
            intent.putExtra("meetingId", mMeetingId);
            intent.putExtra("callFrom", "notification");
            showNotification(intent, msg);

        } else if (mNotificationName.equals("Meeeting Voting Request")) {
            Intent myintent = new Intent(this, NearByPlacess.class);
            myintent.putExtra("message", msg);
            myintent.putExtra("meetingId", mMeetingId);
            Friss_Pojo.MeetingID = mMeetingId;
            myintent.putExtra("userId ", msg4);
            myintent.putExtra("NotificationName", mNotificationName);
            showNotification(myintent, msg);


        } else if (mNotificationName.equals("Place Change")) {
            Intent myintent = new Intent(this, Meeting_StatusPage.class);
            myintent.putExtra("message", msg);
            myintent.putExtra("meetingId", mMeetingId);
            Friss_Pojo.MeetingID = mMeetingId;
            myintent.putExtra("userId ", msg4);
            myintent.putExtra("NotificationName", mNotificationName);

            showNotification(myintent, msg);


        } else if (mNotificationName.equals(NotificationType.MEETING_SUMMARY.toString())) {
            FLog.d(TAG, "isLocationSelected--------" + isLocationSelected);
            if (!isLocationSelected) {
                sendUserDetailsForMeetingSummaryToServer();
            } else {

                Intent intent = new Intent(this, MeetingDetailsActivity.class);
                intent.putExtra("meetingId", mMeetingId);
                intent.putExtra("callFrom", "notification");

                showNotification(intent, msg);
            }
        } else if (mNotificationName.equals(NotificationType.MEETING_REJECTED.toString())) {
            Intent intent = new Intent(this, MeetingDetailsActivity.class);
            intent.putExtra("meetingId", mMeetingId);
            intent.putExtra("callFrom", "notification");
            showNotification(intent, msg);

        } else if (mNotificationName.equals(NotificationType.MEETING_LOCATION_SUGGESTION.toString())) {
            Log.d(TAG, "meetingId" + mMeetingId);
            FLog.d(TAG, "isLocationUpdate-----" + isLocationUpdate);
            Intent intent;
            if (isLocationUpdate) {
                intent = new Intent(this, MeetingDetailsActivity.class);
                intent.putExtra("meetingId", mMeetingId);
                intent.putExtra("callFrom", "notification");
            } else {
                intent = new Intent(this, SuggestionsActivity.class);
                intent.putExtra("meetingId", mMeetingId);
                intent.putExtra("locationSuggestionJson", locationSuggestionJsonString.toString());
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
            if (location != null) {
                jsonObject.put("latitude", location.getLatitude());
                jsonObject.put("longitude", location.getLongitude());
                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.MEETING_SUMMARY_BY_LOCATION;
                TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
                    @Override
                    public void handleResponse(TSNetworkHandler.TSResponse response) {
                        if (response != null) {
                            Log.d(TAG, "response" + response.response);
                            Toast.makeText(GcmIntentService.this, response.message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
                .addAction(R.drawable.noti, "View", contentIntent)
                .addAction(0, "Remind", contentIntent)
                .setContentIntent(contentIntent)
                .setContentText(msg)
                .setAutoCancel(true);
        mNotificationManager.notify(NOTIFICATION_ID++, mBuilder.build());
    }

}