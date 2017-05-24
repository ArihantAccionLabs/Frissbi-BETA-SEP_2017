package com.frissbi.app.Utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.frissbi.app.models.FrissbiReminder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by thrymr on 30/1/17.
 */
public class ReminderAlarmManager {
    private Context mContext;
    static ReminderAlarmManager ourInstance;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private Date remainderTime;

    private ReminderAlarmManager(Context context) {
        mContext = context;
    }


    public static ReminderAlarmManager getInstance(Context context) {
        if (ourInstance == null)
            ourInstance = new ReminderAlarmManager(context);
        return ourInstance;
    }


    public void setRemainderAlarm(String remainderMessage, String time) {
        FLog.d("ReminderAlarmManager", "date" + time + "remainderMessage" + remainderMessage);
        FrissbiReminder frissbiReminder = new FrissbiReminder();
        frissbiReminder.setMessage(remainderMessage);
        frissbiReminder.setTime(time);
        frissbiReminder.setReminderId(frissbiReminder.getId());
        frissbiReminder.save();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.ENGLISH);
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(formatter.parse(time));
            FLog.d("AlarmManager", "cal" + cal.getTime());
            remainderTime = cal.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        FLog.d("ReminderAlarmManager", "remainderTime" + remainderTime);
        alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, ReminderAlarmReceiver.class);
        intent.putExtra("remainderMessage", remainderMessage);
        intent.putExtra("date", time);
        intent.putExtra("id", frissbiReminder.getId());
        alarmIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), alarmIntent);
        } else {
            alarmMgr.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), alarmIntent);
        }
    }
}
