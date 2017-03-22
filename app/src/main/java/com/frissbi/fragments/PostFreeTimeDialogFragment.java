package com.frissbi.fragments;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.frissbi.R;
import com.frissbi.Utility.Utility;
import com.frissbi.activities.MeetingActivity;
import com.frissbi.interfaces.PostFreeTimeListener;

import java.util.Calendar;



public class PostFreeTimeDialogFragment extends DialogFragment implements View.OnClickListener {

    private static final int DATE_DIALOG_ID = 100;
    private static final int TIME_DIALOG_ID = 200;
    private TextView mFreeTimeDateTextView;
    private TextView mFreeTimeTimeTextView;
    private TextView mFreeTimeDurationTextView;
    private Calendar calendar;
    private int year;
    private int hour;
    private int min;
    private int month;
    private int day;
    String[] values = new String[]{"1:00", "1:30", "2:00", "2:30", "3:00", "3:30", "4:00", "4:30", "5:00", "5:30", "6:00"};
    private String mFreeTime;
    private PostFreeTimeListener mPostFreeTimeListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post_free_time_dialog, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setUpViews(view);
        return view;
    }

    private void setUpViews(View view) {
        mFreeTimeDateTextView = (TextView) view.findViewById(R.id.free_time_date_tv);
        mFreeTimeTimeTextView = (TextView) view.findViewById(R.id.free_time_time_tv);
        mFreeTimeDurationTextView = (TextView) view.findViewById(R.id.free_time_duration_tv);
        view.findViewById(R.id.confirm_free_time_imageView).setOnClickListener(this);
        view.findViewById(R.id.free_time_date_rl).setOnClickListener(this);
        view.findViewById(R.id.free_time_time_rl).setOnClickListener(this);
        view.findViewById(R.id.free_time_duration_rl).setOnClickListener(this);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        min = calendar.get(Calendar.MINUTE);
        showDate(year, month + 1, day);
        mFreeTimeTimeTextView.setText(Utility.getInstance().updateTime(hour, min));
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.free_time_date_rl:
                onCreateDialog(DATE_DIALOG_ID).show();
                break;
            case R.id.free_time_time_rl:
                onCreateDialog(TIME_DIALOG_ID).show();
                break;
            case R.id.free_time_duration_rl:
                showDurationDialog();
                break;
            case R.id.confirm_free_time_imageView:
                dismiss();
                mPostFreeTimeListener.sendFreeTime(mFreeTimeDateTextView.getText().toString() + " " + mFreeTimeTimeTextView.getText().toString(), mFreeTimeDurationTextView.getText().toString());
                break;
        }

    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                DatePickerDialog dateDialog = new DatePickerDialog(getActivity(), myDateListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    dateDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                }

                return dateDialog;
            case TIME_DIALOG_ID:
                // set time picker as current time
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), fromTimePickerListener, hour, min, false);
                return timePickerDialog;

        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(android.widget.DatePicker arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day

            int year, month, day;
            year = arg1;
            month = arg2;
            day = arg3;

            Calendar cal = Calendar.getInstance();
            int tyear = cal.get(Calendar.YEAR);
            int tmonth = cal.get(Calendar.MONTH);
            int tday = cal.get(Calendar.DAY_OF_MONTH);

            if (tyear == year) {
                if (month == tmonth) {
                    if (tday > day) {
                        day = 1;
                    }
                    showDate(year, tmonth + 1, day);
                } else {
                    showDate(year, month + 1, day);
                }
            } else {
                showDate(arg1, month + 1, day);
            }

        }
    };


    private TimePickerDialog.OnTimeSetListener fromTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            hour = hourOfDay;
            min = minute;
            mFreeTimeTimeTextView.setText(Utility.getInstance().updateTime(hourOfDay, minute));
        }
    };

    private void showDate(int year, int month, int day) {
        mFreeTimeDateTextView.setText(new StringBuilder().append(year).append("-").append(month).append("-").append(day));
    }

    private void showDurationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.time, null);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        final ListView listview = (ListView) view.findViewById(R.id.listView);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.orzine_iteam, R.id.location_name, values);


        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String itemValue = (String) listview.getItemAtPosition(position);
                mFreeTimeDurationTextView.setText(itemValue);
                alertDialog.dismiss();
            }

        });
        alertDialog.show();
    }

    public void setFreeTimeListener(PostFreeTimeListener postFreeTimeListener) {
        mPostFreeTimeListener = postFreeTimeListener;
    }
}
