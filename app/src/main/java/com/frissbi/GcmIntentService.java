package com.frissbi;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.frissbi.Frissbi_Friends.FriendSerching;
import com.frissbi.Frissbi_Friends.Friend_PendingList;
import com.frissbi.Frissbi_Meetings.MeetingPendingList;
import com.frissbi.Frissbi_Meetings.Meeting_StatusPage;
import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.locations.NearByPlacess;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.Random;

public class GcmIntentService extends IntentService {
    Context context;
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    String msg1, msg2, msg3, msg4;
    public static final String TAG = "GCM Demo";

    //  Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
    public GcmIntentService() {
        super("GcmIntentService");
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub
        Bundle extras = intent.getExtras();
        String msg = intent.getStringExtra("message");
        msg1 = intent.getStringExtra("NotificationName");
        msg2 = intent.getStringExtra("userName");
        msg3 = intent.getStringExtra("meetingId");
        msg4 = intent.getStringExtra("userId");
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {

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
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
        if (msg1.equals("Friend Pending Requests")) {
            Intent myintent = new Intent(this, Friend_PendingList.class);
            myintent.putExtra("message", msg);
            myintent.putExtra("NotificationName", msg1);
            myintent.putExtra("userName", msg2);


            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, myintent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.noti)
                    .setContentTitle("FRISSBI")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                    .setSound(soundUri)
                    .addAction(R.drawable.noti, "View", contentIntent)
                    .addAction(0, "Remind", contentIntent)
                    .setContentIntent(contentIntent)
                    .setContentText(msg);

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            mBuilder.setAutoCancel(true);

        } else if (msg1.equals("Friend Request Acceptance")) {


            Intent myintent = new Intent(this, FriendSerching.class);
            myintent.putExtra("message", msg);
            myintent.putExtra("NotificationName", msg1);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, myintent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.noti)
                    .setContentTitle("FRISSBI")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                    .setSound(soundUri)
                    .addAction(R.drawable.noti, "View", contentIntent)
                    .addAction(0, "Remind", contentIntent)
                    .setContentIntent(contentIntent)
                    .setContentText(msg);

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            mBuilder.setAutoCancel(true);

        } else if (msg1.equals("Meeting Pending Requests")) {
            Intent myintent = new Intent(this, MeetingPendingList.class);
            myintent.putExtra("message", msg);
            myintent.putExtra("NotificationName", msg1);
            myintent.putExtra("meetingId", msg2);


            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, myintent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.noti)
                    .setContentTitle("FRISSBI")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                    .setSound(soundUri)
                    .addAction(R.drawable.noti, "View", contentIntent)
                    .addAction(0, "Remind", contentIntent)
                    .setContentIntent(contentIntent)
                    .setContentText(msg);

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            mBuilder.setAutoCancel(true);

        } else if (msg1.equals("Meeting Request Acceptance")) {
            Intent myintent = new Intent(this, FriendSerching.class);
            myintent.putExtra("message", msg);
            myintent.putExtra("NotificationName", msg1);


            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, myintent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.noti)
                    .setContentTitle("FRISSBI")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                    .setSound(soundUri)
                    .addAction(R.drawable.noti, "View", contentIntent)
                    .addAction(0, "Remind", contentIntent)
                    .setContentIntent(contentIntent)
                    .setContentText(msg);

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            mBuilder.setAutoCancel(true);

        } else if (msg1.equals("Meeeting Voting Request")) {
            Intent myintent = new Intent(this, NearByPlacess.class);
            myintent.putExtra("message", msg);
            myintent.putExtra("meetingId", msg3);
            Friss_Pojo.MeetingID = msg3;
            myintent.putExtra("userId ", msg4);
            myintent.putExtra("NotificationName", msg1);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, myintent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.noti)
                    .setContentTitle("FRISSBI")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                    .setSound(soundUri)
                    .addAction(R.drawable.noti, "View", contentIntent)
                    .addAction(0, "Remind", contentIntent)
                    .setContentIntent(contentIntent)
                    .setContentText(msg);

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            mBuilder.setAutoCancel(true);

        } else if (msg1.equals("Place Change")) {
            Intent myintent = new Intent(this, Meeting_StatusPage.class);
            myintent.putExtra("message", msg);
            myintent.putExtra("meetingId", msg3);
            Friss_Pojo.MeetingID = msg3;
            myintent.putExtra("userId ", msg4);
            myintent.putExtra("NotificationName", msg1);


            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, myintent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.noti)
                    .setContentTitle("FRISSBI")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                    .setSound(soundUri)
                    .addAction(R.drawable.noti, "View", contentIntent)
                    .addAction(0, "Remind", contentIntent)
                    .setContentIntent(contentIntent)
                    .setContentText(msg);

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            mBuilder.setAutoCancel(true);

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

}