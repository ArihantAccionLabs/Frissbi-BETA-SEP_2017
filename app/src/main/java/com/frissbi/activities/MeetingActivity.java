package com.frissbi.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.R;
import com.frissbi.SelectedContacts;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.Utility.Utility;
import com.frissbi.adapters.MeetingTitleAdapter;
import com.frissbi.adapters.SelectedContactsExpandableAdapter;
import com.frissbi.interfaces.MeetingTitleSelectionListener;
import com.frissbi.models.Contacts;
import com.frissbi.models.EmailContacts;
import com.frissbi.models.Friends;
import com.frissbi.models.MyPlaces;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MeetingActivity extends AppCompatActivity implements View.OnClickListener, MeetingTitleSelectionListener {
    private static final int DATE_DIALOG_ID = 100;
    private static final int TIME_DIALOG_ID = 200;
    private static final int PLACE_REQ_CODE = 300;
    private static final int FRIENDS_REQ_CODE = 400;
    private TextView mMeetingDateTextView;
    private TextView mMeetingTimeTextView;
    private TextView mMeetingPlaceTextView;
    private TextView mMeetingDurationTextView;
    private int pickedYear;
    private int picMonth;
    private int picDay;
    private Calendar calendar;
    private int year;
    private int hour;
    private int min;
    private int month;
    private int day;
    String[] values = new String[]{"1:00", "1:30", "2:00", "2:30", "3:00", "3:30", "4:00", "4:30", "5:00", "5:30", "6:00"};
    private SelectedContacts mSelectedContacts;
    private ExpandableListView mSelectedContactsExpandableListView;
    private List<Friends> mFriendsList;
    private List<EmailContacts> mEmailContactsList;
    private List<Contacts> mContactsList;
    private TextView mMeetingTitleTextView;

    private SharedPreferences mSharedPreferences;
    private String mUserId;
    private String mUserName;
    private MyPlaces mMeetingPlace;
    private AlertDialog mAlertDialog;
    private AlertDialog mConflictAlertDialog;
    private AlertDialog mConfirmAlertDialog;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);
        mSelectedContacts = SelectedContacts.getInstance();
        mSelectedContacts.clearContacts();
        mSharedPreferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("USERID_FROM", "editor");
        mUserName = mSharedPreferences.getString("USERNAME_FROM", "editor");
        mProgressDialog = new CustomProgressDialog(this);
        mFriendsList = new ArrayList<>();
        mEmailContactsList = new ArrayList<>();
        mContactsList = new ArrayList<>();
        mMeetingDateTextView = (TextView) findViewById(R.id.meeting_date_tv);
        mMeetingTimeTextView = (TextView) findViewById(R.id.meeting_time_tv);
        mMeetingPlaceTextView = (TextView) findViewById(R.id.meeting_place_tv);
        mMeetingDurationTextView = (TextView) findViewById(R.id.meeting_duration_tv);
        CardView dateCardView = (CardView) findViewById(R.id.date_cardView);
        CardView timeCardView = (CardView) findViewById(R.id.time_cardView);
        CardView durationCardView = (CardView) findViewById(R.id.duration_cardView);
        CardView placeCardView = (CardView) findViewById(R.id.place_cardView);
        Button addfriendsButton = (Button) findViewById(R.id.addFriends);
        RelativeLayout meetingTitleRlayout = (RelativeLayout) findViewById(R.id.meeting_title_rl);
        mSelectedContactsExpandableListView = (ExpandableListView) findViewById(R.id.selected_contacts_expandableListView);
        mMeetingTitleTextView = (TextView) findViewById(R.id.meeting_title_tv);
        Button conformMeetingButton = (Button) findViewById(R.id.conform_meeting);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        min = calendar.get(Calendar.MINUTE);
        showDate(year, month + 1, day);
        mMeetingTimeTextView.setText(updateTime(hour, min));

        dateCardView.setOnClickListener(this);
        timeCardView.setOnClickListener(this);
        durationCardView.setOnClickListener(this);
        placeCardView.setOnClickListener(this);
        addfriendsButton.setOnClickListener(this);
        meetingTitleRlayout.setOnClickListener(this);
        conformMeetingButton.setOnClickListener(this);
    }

    private void showSelectLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.alert_select_meeting_location, null);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        TextView myPlacestv = (TextView) view.findViewById(R.id.my_places_tv);
        TextView anyPlaceTv = (TextView) view.findViewById(R.id.any_place_tv);
        myPlacestv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                Intent intent = new Intent(MeetingActivity.this, MySavedPlacesActivity.class);
                startActivityForResult(intent, PLACE_REQ_CODE);

            }
        });
        anyPlaceTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                mMeetingPlaceTextView.setText(R.string.any_place);
            }
        });
        alertDialog.show();
    }

    private void showDurationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.time, null);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        final ListView listview = (ListView) view.findViewById(R.id.listView);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.orzine_iteam, R.id.location_name, values);


        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String itemValue = (String) listview.getItemAtPosition(position);
                //  scheduledTimeSlot = itemValue + ":" + 00;

                mMeetingDurationTextView.setText(itemValue);
                Log.d("scheduledTimeSlot", itemValue);
                alertDialog.dismiss();

            }

        });
        alertDialog.show();
    }


    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                DatePickerDialog dateDialog = new DatePickerDialog(MeetingActivity.this, myDateListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    dateDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                }

                return dateDialog;
            case TIME_DIALOG_ID:
                // set time picker as current time

                TimePickerDialog timePickerDialog = new TimePickerDialog(MeetingActivity.this, fromTimePickerListener, hour, min, false);
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
            mMeetingTimeTextView.setText(updateTime(hourOfDay, minute));
        }
    };


    private void showDate(int year, int month, int day) {
        pickedYear = year;
        picMonth = month;
        picDay = day;
        mMeetingDateTextView.setText(new StringBuilder().append(year).append("-").append(month).append("-").append(day));
    }


    public static String updateTime(int hours, int mins) {

        String timeSet = "";
        if (hours > 12) {
            hours -= 12;
            timeSet = "PM";
        } else if (hours == 0) {
            hours += 12;
            timeSet = "AM";
        } else if (hours == 12)
            timeSet = "PM";
        else
            timeSet = "AM";
        String minutes = "";
        String _hours = "";

        if (hours < 10) {
            _hours = "0" + hours;
        } else {
            _hours = String.valueOf(hours);
        }

        if (mins < 10)
            minutes = "0" + mins;
        else
            minutes = String.valueOf(mins);

        // Append in a StringBuilder
        String aTime = new StringBuilder().append(_hours).append(':').append(minutes).append(" ").append(timeSet).toString();
        return aTime;

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PLACE_REQ_CODE && resultCode == RESULT_OK) {
            String selectedFrom = data.getStringExtra("selected_from");
            mMeetingPlace = (MyPlaces) data.getSerializableExtra("selected_place");
            if (selectedFrom.equalsIgnoreCase("savedLocation")) {
                mMeetingPlaceTextView.setText(mMeetingPlace.getName());
            } else {
                mMeetingPlaceTextView.setText(R.string.selected_location);
            }
        } else if (requestCode == FRIENDS_REQ_CODE && resultCode == RESULT_OK) {
            mFriendsList.clear();
            mEmailContactsList.clear();
            mContactsList.clear();
            int friendsCount = mSelectedContacts.getFriendsSelectedIdList().size();
            int emailCount = mSelectedContacts.getEmailsSelectedIdsList().size();
            int contactsCount = mSelectedContacts.getContactsSelectedIdsList().size();
            for (int i = 0; i < friendsCount; i++) {
                Friends friends = Friends.findById(Friends.class, mSelectedContacts.getFriendsSelectedIdList().get(i));
                mFriendsList.add(friends);
            }
            for (int i = 0; i < emailCount; i++) {
                EmailContacts emailContacts = EmailContacts.findById(EmailContacts.class, mSelectedContacts.getEmailsSelectedIdsList().get(i));
                mEmailContactsList.add(emailContacts);
            }

            for (int i = 0; i < contactsCount; i++) {
                Contacts contacts = Contacts.findById(Contacts.class, mSelectedContacts.getContactsSelectedIdsList().get(i));
                mContactsList.add(contacts);
            }

            SelectedContactsExpandableAdapter selectedContactsExpandableAdapter = new SelectedContactsExpandableAdapter(MeetingActivity.this, mFriendsList, mEmailContactsList, mContactsList);
            mSelectedContactsExpandableListView.setAdapter(selectedContactsExpandableAdapter);


        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.meeting_title_rl:
                showMeetingTitlesDialog();
                break;
            case R.id.date_cardView:
                onCreateDialog(DATE_DIALOG_ID).show();
                break;
            case R.id.time_cardView:
                onCreateDialog(TIME_DIALOG_ID).show();
                break;
            case R.id.duration_cardView:
                showDurationDialog();
                break;
            case R.id.place_cardView:
                showSelectLocationDialog();
                break;
            case R.id.addFriends:
                Intent intent = new Intent(MeetingActivity.this, AddFriendsToMeetingActivity.class);
                startActivityForResult(intent, FRIENDS_REQ_CODE);
                break;
            case R.id.conform_meeting:
                if (checkFieldsValidation()) {
                    if (ConnectionDetector.getInstance(this).isConnectedToInternet()) {
                        sendMeetingDetailsToServer(new JSONArray());
                    } else {
                        Toast.makeText(this, getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
                    }
                }

                break;
        }

    }

    private boolean checkFieldsValidation() {

        if (mMeetingTitleTextView.getText().toString().equalsIgnoreCase("Meeting for..")) {
            Toast.makeText(this, "Please select meeting title", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if (!mMeetingPlaceTextView.getText().toString().equalsIgnoreCase("Any Place") && mMeetingPlace == null) {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                if (mFriendsList.size() == 0 && mEmailContactsList.size() == 0 && mContactsList.size() == 0) {
                    Toast.makeText(this, "Please select atleast one friend", Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    return true;
                }
            }
        }

    }

    private void showMeetingTitlesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MeetingActivity.this);
        builder.setTitle("Meeting For..");
        View view = LayoutInflater.from(MeetingActivity.this).inflate(R.layout.alert_meeting_tiltes, null);
        RecyclerView meetingTitleRecyclerView = (RecyclerView) view.findViewById(R.id.meeting_title_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MeetingActivity.this);
        meetingTitleRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(meetingTitleRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        meetingTitleRecyclerView.addItemDecoration(dividerItemDecoration);
        builder.setView(view);
        List<String> titleList = new ArrayList<>();
        titleList.add("Breakfast");
        titleList.add("Lunch");
        titleList.add("Coffee");
        titleList.add("Dinner");
        titleList.add("Drinks");
        titleList.add("MEETING");
        MeetingTitleSelectionListener meetingTitleSelectionListener = (MeetingTitleSelectionListener) this;
        mAlertDialog = builder.create();
        MeetingTitleAdapter meetingTitleAdapter = new MeetingTitleAdapter(MeetingActivity.this, titleList, meetingTitleSelectionListener);
        meetingTitleRecyclerView.setAdapter(meetingTitleAdapter);
        mAlertDialog.show();
    }

    private void sendMeetingDetailsToServer(JSONArray meetingIdsJsonArray) {
        mProgressDialog.show();
        JSONArray friendsIdJsonArray = new JSONArray();
        JSONArray emailIdJsonArray = new JSONArray();
        JSONArray contactsJsonArray = new JSONArray();
        if (mFriendsList.size() > 0) {
            for (int i = 0; i < mFriendsList.size(); i++) {
                friendsIdJsonArray.put(mFriendsList.get(i).getFriendId());
            }
        }
        if (mEmailContactsList.size() > 0) {
            for (int i = 0; i < mEmailContactsList.size(); i++) {
                emailIdJsonArray.put(mEmailContactsList.get(i).getEmailId());
            }
        }
        if (mContactsList.size() > 0) {
            for (int i = 0; i < mContactsList.size(); i++) {
                contactsJsonArray.put(mContactsList.get(i).getPhoneNumber());
            }
        }

        JSONObject jsonObject = new JSONObject();
        try {
            if (meetingIdsJsonArray.length() > 0) {
                jsonObject.put("meetingIdsJsonArray", meetingIdsJsonArray);
            }
            jsonObject.put("senderUserId", mUserId);
            jsonObject.put("meetingTitle", mMeetingTitleTextView.getText());
            jsonObject.put("meetingDateTime", mMeetingDateTextView.getText().toString() + "  " + mMeetingTimeTextView.getText().toString());
            jsonObject.put("duration", mMeetingDurationTextView.getText().toString());
            if (mMeetingPlace != null) {
                jsonObject.put("latitude", mMeetingPlace.getLatitude() + "");
                jsonObject.put("longitude", mMeetingPlace.getLongitude() + "");
                jsonObject.put("address", mMeetingPlace.getAddress());
                jsonObject.put("isLocationSelected", true);
            } else {
                jsonObject.put("isLocationSelected", false);
            }
            jsonObject.put("friendsIdJsonArray", friendsIdJsonArray);
            jsonObject.put("emailIdJsonArray", emailIdJsonArray);
            jsonObject.put("contactsJsonArray", contactsJsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        Log.d("MeetingActivity", "jsonObject" + jsonObject);
        String url = Utility.REST_URI + Utility.MEETING_INSERT;
        TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {

                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            final JSONObject responseJsonObject = new JSONObject(response.response);
                            if (responseJsonObject.getBoolean("isInserted")) {
                                Toast.makeText(MeetingActivity.this, responseJsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                /*MeetingAlarmManager.getInstance(MeetingActivity.this).setMeetingAlarm(responseJsonObject.getLong("meetingId"), responseJsonObject.getBoolean("isLocationSelected"),
                                        mMeetingDateTextView.getText().toString() + "  " + mMeetingTimeTextView.getText().toString());*/
                                Intent intent = new Intent(MeetingActivity.this, HomeActivity.class);
                                startActivity(intent);
                            } else {
                                showConflictingAlertDialog(responseJsonObject.getString("message"), responseJsonObject.getJSONArray("meetingIdsJsonArray"));
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(MeetingActivity.this, response.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MeetingActivity.this, "Something went wrong at server end", Toast.LENGTH_SHORT).show();
                }

                mProgressDialog.dismiss();

            }
        });


    }

    private void showConflictingAlertDialog(String message, final JSONArray meetingIdsJsonArray) {
        String fromTime = "";
        String toTime = "";
        if (meetingIdsJsonArray.length() == 1) {
            try {
                JSONObject conflictJsonObject = meetingIdsJsonArray.getJSONObject(0);
                fromTime = Utility.getInstance().convertTime(conflictJsonObject.getString("from"));
                toTime = Utility.getInstance().convertTime(conflictJsonObject.getString("to"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert!");
        if (meetingIdsJsonArray.length() > 1) {
            builder.setMessage(message);
        } else {
            builder.setMessage(message + " from " + fromTime + " to " + toTime + "." + " Do you wish to accept new meeting request? (Former Meeting will be cancelled)");
        }
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mConflictAlertDialog.dismiss();
                conformationAlert(meetingIdsJsonArray);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mConflictAlertDialog.dismiss();
            }
        });
        mConflictAlertDialog = builder.create();
        mConflictAlertDialog.show();

    }


    private void conformationAlert(final JSONArray meetingIdsJsonArray) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert!");
        builder.setMessage("Are you sure?");
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mConfirmAlertDialog.dismiss();

            }
        });
        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mConfirmAlertDialog.dismiss();
                sendMeetingDetailsToServer(meetingIdsJsonArray);
            }
        });
        mConfirmAlertDialog = builder.create();
        mConfirmAlertDialog.show();
    }


    @Override
    public void selectedMeetingTitle(String title) {
        mMeetingTitleTextView.setText(title);
        mAlertDialog.dismiss();
    }


}
