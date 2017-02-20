package com.frissbi.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.models.MeetingDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Created by thrymr on 15/2/17.
 */

public class CalendarGridAdapter extends ArrayAdapter {

    private final SimpleDateFormat mDateFormat;
    private Date mCurrentDate;
    private Context mContext;
    private static final String TAG = CalendarGridAdapter.class.getSimpleName();
    private List<Date> mMonthlyDates;
    private Calendar mCalendar;
    private String mCurrentDay;
    private String mSelectedDay;
    private List<MeetingDate> mMeetingDateList;

    public CalendarGridAdapter(Context context, List<Date> dayValueInCells, Calendar calendar, String currentDay, String selectedDay, List<MeetingDate> meetingDateList) {
        super(context, R.layout.calendar_item);
        mContext = context;
        mMonthlyDates = dayValueInCells;
        mCalendar = calendar;
        mCurrentDay = currentDay;
        mSelectedDay = selectedDay;
        mMeetingDateList = meetingDateList;
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        try {
            mCurrentDate = mDateFormat.parse(mCurrentDay);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Date mDate = mMonthlyDates.get(position);
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(mDate);
        int dayValue = dateCal.get(Calendar.DAY_OF_MONTH);
        int displayMonth = dateCal.get(Calendar.MONTH) + 1;
        int displayYear = dateCal.get(Calendar.YEAR);
        int currentMonth = mCalendar.get(Calendar.MONTH) + 1;
        int currentYear = mCalendar.get(Calendar.YEAR);

        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.calendar_item, parent, false);
        }

        if (displayMonth == currentMonth && displayYear == currentYear) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
        //Add day to calendar
        TextView cellNumber = (TextView) view.findViewById(R.id.calendar_date_id);
        View currentDayView = view.findViewById(R.id.current_day_view);
        LinearLayout calendarLayout = (LinearLayout) view.findViewById(R.id.calendar_ll);

        if (mDateFormat.format(dateCal.getTime()).equalsIgnoreCase(mCurrentDay)) {
            currentDayView.setVisibility(View.VISIBLE);
        } else {
            currentDayView.setVisibility(View.GONE);
        }

        if (mDateFormat.format(dateCal.getTime()).equalsIgnoreCase(mSelectedDay)) {
            calendarLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.gray));
        }

        cellNumber.setText(String.valueOf(dayValue));

        for (MeetingDate meetingDate : mMeetingDateList) {
            try {
                if (mDateFormat.parse(meetingDate.getDate()).after(mCurrentDate) || mDateFormat.parse(meetingDate.getDate()).equals(mCurrentDate)) {
                    if (meetingDate.getDate().equalsIgnoreCase(mDateFormat.format(dateCal.getTime()))) {
                        cellNumber.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                        if (meetingDate.getCount() <= 2) {
                            GradientDrawable shape = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.circle_shape);
                            shape.setColor(ContextCompat.getColor(mContext, R.color.light_orange));
                            cellNumber.setBackground(ContextCompat.getDrawable(mContext, R.drawable.circle_clip));
                            cellNumber.getBackground().setLevel(2500);
                        } else if (meetingDate.getCount() > 2 && meetingDate.getCount() <= 4) {
                            GradientDrawable shape = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.circle_shape);
                            shape.setColor(ContextCompat.getColor(mContext, R.color.green));
                            cellNumber.setBackground(ContextCompat.getDrawable(mContext, R.drawable.circle_clip));
                            cellNumber.getBackground().setLevel(5000);
                        } else if (meetingDate.getCount() >= 5) {
                            GradientDrawable shape = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.circle_shape);
                            shape.setColor(ContextCompat.getColor(mContext, R.color.red));
                            cellNumber.setBackground(ContextCompat.getDrawable(mContext, R.drawable.circle_clip));
                            cellNumber.getBackground().setLevel(10000);
                        }
                    }
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return view;
    }

    @Override
    public int getCount() {
        return mMonthlyDates.size();
    }

    @Nullable
    @Override
    public Date getItem(int position) {
        return mMonthlyDates.get(position);
    }

    @Override
    public int getPosition(Object item) {
        return mMonthlyDates.indexOf(item);
    }


}
