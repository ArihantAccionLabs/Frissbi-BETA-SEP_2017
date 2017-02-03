package com.frissbi.Utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by thrymr on 30/1/17.
 */
public class MeetingAlarmManager {
    private Context mContext;
    static MeetingAlarmManager ourInstance;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private Date twoHourBack;

    private MeetingAlarmManager(Context context) {
        mContext = context;
    }


    public static MeetingAlarmManager getInstance(Context context) {
        if (ourInstance == null)
            ourInstance = new MeetingAlarmManager(context);
        return ourInstance;
    }


    public void setMeetingAlarm(long meetingId, boolean isLocationSelected, String date) {
        Log.d("MeetingAlarmManager", "date" + date + "meetingId" + meetingId + "isLocationSelected" + isLocationSelected);
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(formatter.parse(date));
            Log.d("AlarmManager", "cal" + cal.getTime());
            if (isLocationSelected) {
                cal.add(Calendar.HOUR, -1);
            } else {
                cal.add(Calendar.HOUR, -2);
            }
            twoHourBack = cal.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.d("MeetingAlarmManager", "twoHourBack" + twoHourBack);
        alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, MeetingAlarmReceiver.class);
        intent.putExtra("meetingId", meetingId);
        intent.putExtra("isLocationSelected", isLocationSelected);
        alarmIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.d("MeetingAlarmManager", "getTimeInMillis" + cal.getTimeInMillis());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), alarmIntent);
        }
    }
}
