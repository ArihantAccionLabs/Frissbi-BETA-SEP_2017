package com.frissbi.app.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.frissbi.app.R;
import com.frissbi.app.Utility.Utility;
import com.frissbi.app.interfaces.NewReminderListener;

import java.util.Calendar;


public class AddReminderDialogFragment extends DialogFragment implements View.OnClickListener {


    private static final int DATE_DIALOG_ID = 100;
    private static final int TIME_DIALOG_ID = 200;
    private TextView mRemainderDateTextView;
    private TextView mRemainderTimeTextView;
    private Calendar mCalendar;
    private int year, hour;
    private int month;
    private int day;
    private int min;
    private NewReminderListener mNewReminderListener;
    private EditText mReminderMessageEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_reminder_dialog, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setUpViews(view);
        return view;
    }

    private void setUpViews(View view) {
        mReminderMessageEditText = (EditText) view.findViewById(R.id.reminder_message_et);
        mRemainderDateTextView = (TextView) view.findViewById(R.id.remainder_date_tv);
        mRemainderTimeTextView = (TextView) view.findViewById(R.id.remainder_time_tv);
        view.findViewById(R.id.confirm_remainder_imageView).setOnClickListener(this);
        view.findViewById(R.id.remainder_date_rl).setOnClickListener(this);
        view.findViewById(R.id.remainder_time_rl).setOnClickListener(this);
        mCalendar = Calendar.getInstance();
        year = mCalendar.get(Calendar.YEAR);
        month = mCalendar.get(Calendar.MONTH);
        day = mCalendar.get(Calendar.DAY_OF_MONTH);
        hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        min = mCalendar.get(Calendar.MINUTE);
        showDate(year, month + 1, day);
        mRemainderTimeTextView.setText(Utility.getInstance().updateTime(hour, min));
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.remainder_date_rl:
                onCreateDialog(DATE_DIALOG_ID).show();
                break;
            case R.id.remainder_time_rl:
                onCreateDialog(TIME_DIALOG_ID).show();
                break;
            case R.id.confirm_remainder_imageView:
                dismiss();
                mNewReminderListener.addReminder(mReminderMessageEditText.getText().toString(), mRemainderDateTextView.getText().toString() + " " + mRemainderTimeTextView.getText().toString());
                break;
        }

    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                DatePickerDialog dateDialog = new DatePickerDialog(getActivity(), myDateListener,
                        mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
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
            mRemainderTimeTextView.setText(Utility.getInstance().updateTime(hourOfDay, minute));
        }
    };

    private void showDate(int year, int month, int day) {
        mRemainderDateTextView.setText(new StringBuilder().append(year).append("-").append(month).append("-").append(day));
    }


    public void setReminderListener(NewReminderListener newReminderListener) {
        mNewReminderListener = newReminderListener;
    }
}
