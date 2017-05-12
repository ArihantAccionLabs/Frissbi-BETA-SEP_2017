package com.frissbi.Utility;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.models.FrissbiReminder;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by thrymr on 30/1/17.
 */
public class ReminderAlarmReceiver extends BroadcastReceiver {
    private Context mContext;
    private Uri soundUri;
    public static int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ReminderAlarmReceiver", "remainderMessage" + intent.getExtras().getString("remainderMessage"));
        Log.d("ReminderAlarmReceiver", "date" + intent.getExtras().getString("date"));
        mContext = context;
        soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        FrissbiReminder frissbiReminder = FrissbiReminder.findById(FrissbiReminder.class, intent.getExtras().getLong("id"));
        frissbiReminder.delete();
        FLog.d("ReminderAlarmManager", "FrissbiReminderList" + FrissbiReminder.listAll(FrissbiReminder.class));
        showNotification(intent.getExtras().getString("remainderMessage"), intent.getExtras().getString("date"));
    }

    private void showNotification(/*Intent intent,*/ String msg, String date) {

        //PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.noti)
                .setContentTitle("FRISSBI REMINDER")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg + " now"))
                .setSound(soundUri)
                /*.addAction(R.drawable.noti, "View", contentIntent)
                .addAction(0, "Remind", contentIntent)
                .setContentIntent(contentIntent)*/
                .setContentText(msg + " now")
                .setAutoCancel(true);
        mNotificationManager.notify(NOTIFICATION_ID++, mBuilder.build());
    }
}
