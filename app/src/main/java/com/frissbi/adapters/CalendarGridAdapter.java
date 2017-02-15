package com.frissbi.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.frissbi.R;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by thrymr on 15/2/17.
 */

public class CalendarGridAdapter extends ArrayAdapter {

    private Context mContext;
    private static final String TAG = CalendarGridAdapter.class.getSimpleName();
    private List<Date> monthlyDates;
    private Calendar currentDate;
    private int currentDay;

    public CalendarGridAdapter(Context context, List<Date> dayValueInCells, Calendar cal, int currentDay) {
        super(context, R.layout.calendar_item);
        monthlyDates = dayValueInCells;
        currentDate = cal;
        this.currentDay = currentDay;
        mContext = context;
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Date mDate = monthlyDates.get(position);
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(mDate);
        int dayValue = dateCal.get(Calendar.DAY_OF_MONTH);
        int displayMonth = dateCal.get(Calendar.MONTH) + 1;
        int displayYear = dateCal.get(Calendar.YEAR);
        int currentMonth = currentDate.get(Calendar.MONTH) + 1;
        int currentYear = currentDate.get(Calendar.YEAR);

        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.calendar_item, parent, false);
        }
        if (displayMonth == currentMonth && displayYear == currentYear) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
            // view.setBackgroundColor(Color.parseColor("#cccccc"));
        }
        //Add day to calendar
        TextView cellNumber = (TextView) view.findViewById(R.id.calendar_date_id);
        cellNumber.setText(String.valueOf(dayValue));
        if (currentDay != 0) {
            if (dayValue == currentDay) {
                cellNumber.setTextColor(ContextCompat.getColor(mContext,R.color.orange));
            }
        }
        //Add events to the calendar
        /*TextView eventIndicator = (TextView) view.findViewById(R.id.event_id);
        Calendar eventCalendar = Calendar.getInstance();*/
       /* for (int i = 0; i < allEvents.size(); i++) {
            eventCalendar.setTime(allEvents.get(i).getDate());
            if (dayValue == eventCalendar.get(Calendar.DAY_OF_MONTH) && displayMonth == eventCalendar.get(Calendar.MONTH) + 1
                    && displayYear == eventCalendar.get(Calendar.YEAR)) {
                eventIndicator.setBackgroundColor(Color.parseColor("#FF4081"));
            }
        }*/
        return view;
    }

    @Override
    public int getCount() {
        return monthlyDates.size();
    }

    @Nullable
    @Override
    public Date getItem(int position) {
        return monthlyDates.get(position);
    }

    @Override
    public int getPosition(Object item) {
        return monthlyDates.indexOf(item);
    }


}
