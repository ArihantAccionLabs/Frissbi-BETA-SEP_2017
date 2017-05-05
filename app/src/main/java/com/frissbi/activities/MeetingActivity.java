package com.frissbi.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.SelectedContacts;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.Utility;
import com.frissbi.adapters.GroupParticipantAdapter;
import com.frissbi.adapters.MeetingTitleAdapter;
import com.frissbi.interfaces.MeetingTitleSelectionListener;
import com.frissbi.models.FrissbiContact;
import com.frissbi.models.FrissbiGroup;
import com.frissbi.models.MyPlaces;
import com.frissbi.models.Participant;
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
    /*private List<Friend> mFriendList;
    private List<EmailContacts> mEmailContactsList;
    private List<Contacts> mContactsList;*/
    private List<FrissbiContact> mFrissbiContactList;
    private List<FrissbiGroup> mFrissbiGroupList;
    private TextView mMeetingTitleTextView;

    private Long mUserId;
    private MyPlaces mMeetingPlace;
    private AlertDialog mAlertDialog;
    private AlertDialog mConflictAlertDialog;
    private TextView mAddAttendeeTextView;
    private RecyclerView mMeetingAttendeesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSelectedContacts = SelectedContacts.getInstance();
        mSelectedContacts.clearContacts();
        mUserId = SharedPreferenceHandler.getInstance(this).getUserId();
        mProgressDialog = new CustomProgressDialog(this);
       /* mFriendList = new ArrayList<>();
        mEmailContactsList = new ArrayList<>();
        mContactsList = new ArrayList<>();*/
        mFrissbiContactList = new ArrayList<>();
        mFrissbiGroupList = new ArrayList<>();
        mMeetingDateTextView = (TextView) findViewById(R.id.meeting_date_tv);
        mMeetingTimeTextView = (TextView) findViewById(R.id.meeting_time_tv);
        mMeetingPlaceTextView = (TextView) findViewById(R.id.meeting_place_tv);
        mMeetingDurationTextView = (TextView) findViewById(R.id.meeting_duration_tv);
        mAddAttendeeTextView = (TextView) findViewById(R.id.add_attendee_tv);
        RelativeLayout dateLayout = (RelativeLayout) findViewById(R.id.date_rl);
        RelativeLayout timeLayout = (RelativeLayout) findViewById(R.id.time_rl);
        RelativeLayout durationLayout = (RelativeLayout) findViewById(R.id.duration_rl);
        RelativeLayout locationLayout = (RelativeLayout) findViewById(R.id.location_rl);
        RelativeLayout descriptionLayout = (RelativeLayout) findViewById(R.id.description_rl);
        // mSelectedContactsExpandableListView = (ExpandableListView) findViewById(R.id.selected_contacts_expandableListView);
        ImageView confirmMeetingImageView = (ImageView) findViewById(R.id.confirm_meeting);
        Button addAttendeeButton = (Button) findViewById(R.id.add_attendee_button);
        mMeetingTitleTextView = (TextView) findViewById(R.id.meeting_title_tv);
        mMeetingAttendeesRecyclerView = (RecyclerView) findViewById(R.id.meeting_attendees_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mMeetingAttendeesRecyclerView.setLayoutManager(layoutManager);
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("groupId")) {
            mSelectedContacts.setGroupSelectedId(getIntent().getExtras().getLong("groupId"));
            setUpFriendsAdapter();
        }
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        min = calendar.get(Calendar.MINUTE);
        showDate(year, month + 1, day);
        mMeetingTimeTextView.setText(Utility.getInstance().updateTime(hour, min));

        dateLayout.setOnClickListener(this);
        timeLayout.setOnClickListener(this);
        durationLayout.setOnClickListener(this);
        locationLayout.setOnClickListener(this);
        descriptionLayout.setOnClickListener(this);
        confirmMeetingImageView.setOnClickListener(this);
        addAttendeeButton.setOnClickListener(this);
    }

    private AlertDialog mConfirmAlertDialog;

    private ProgressDialog mProgressDialog;

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


        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.orzine_iteam, R.id.location_name, values);


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
            mMeetingTimeTextView.setText(Utility.getInstance().updateTime(hourOfDay, minute));
        }
    };


    private void showDate(int year, int month, int day) {
        pickedYear = year;
        picMonth = month;
        picDay = day;
        mMeetingDateTextView.setText(new StringBuilder().append(year).append("-").append(month).append("-").append(day));
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
            setUpFriendsAdapter();
        }
    }

   /* private void setUpFriendsAdapter() {
        mFriendList.clear();
        mEmailContactsList.clear();
        mContactsList.clear();
        mFrissbiGroupList.clear();

        int emailCount = mSelectedContacts.getEmailsSelectedIdsList().size();
        int contactsCount = mSelectedContacts.getContactsSelectedIdsList().size();
        int groupsCount = mSelectedContacts.getGroupSelectedIdsList().size();

        for (int i = 0; i < groupsCount; i++) {
            List<FrissbiGroup> frissbiGroupList = FrissbiGroup.findWithQuery(FrissbiGroup.class, "select * from participant where group_id=?", mSelectedContacts.getGroupSelectedIdsList().get(i).toString());
            List<Participant> participantList = Participant.findWithQuery(Participant.class, "select * from participant where group_id=?", frissbiGroupList.get(0).getGroupId().toString());
            FLog.d("MeetingActivity", "participantList" + participantList);
            for (int j = 0; j < participantList.size(); j++) {
                Participant participant = participantList.get(j);
                if (!participant.getParticipantId().equals(SharedPreferenceHandler.getInstance(this).getUserId())) {
                    FLog.d("MeetingActivity", "participantId" + participant.getParticipantId());
                    mSelectedContacts.setFriendsSelectedId(participant.getParticipantId());
                   *//* List<Friend> friendList = Friend.findWithQuery(Friend.class, "select * from friend where user_id=?", participant.getParticipantId().toString());
                    mFriendList.add(friendList.get(0));*//*
                }
            }
        }

        //   int friendsCount = mSelectedContacts.getFriendsSelectedIdList().size();
        Set<Long> selectedFriendIdSet = new HashSet<>();
        FLog.d("MeetingActivity", "mSelectedContacts======" + mSelectedContacts.getFriendsSelectedIdList());
        selectedFriendIdSet.addAll(mSelectedContacts.getFriendsSelectedIdList());
        FLog.d("MeetingActivity", "selectedFriendIdSet-----" + selectedFriendIdSet);
        List<Long> selectedFriendIdList = new ArrayList<>();
        selectedFriendIdList.addAll(selectedFriendIdSet);
        FLog.d("MeetingActivity", "selectedFriendIdList++++++" + selectedFriendIdList);
        for (int i = 0; i < selectedFriendIdList.size(); i++) {
            List<Friend> friendList = Friend.findWithQuery(Friend.class, "select * from friend where user_id=?", String.valueOf(selectedFriendIdList.get(i)));
            mFriendList.add(friendList.get(0));
        }
        for (int i = 0; i < emailCount; i++) {
            //List<EmailContacts> emailContactsList = EmailContacts.findWithQuery(EmailContacts.class, "select * from email_contacts where email_id=?", String.valueOf(mSelectedContacts.getEmailsSelectedIdsList().get(i)));
            EmailContacts emailContacts = EmailContacts.findById(EmailContacts.class, mSelectedContacts.getEmailsSelectedIdsList().get(i));
            mEmailContactsList.add(emailContacts);
        }

        for (int i = 0; i < contactsCount; i++) {
            Contacts contacts = Contacts.findById(Contacts.class, mSelectedContacts.getContactsSelectedIdsList().get(i));
            mContactsList.add(contacts);
        }


        SelectedContactsExpandableAdapter selectedContactsExpandableAdapter = new SelectedContactsExpandableAdapter(MeetingActivity.this, mFriendList, mEmailContactsList, mContactsList);
        mSelectedContactsExpandableListView.setAdapter(selectedContactsExpandableAdapter);
    }*/


    private void setUpFriendsAdapter() {
        mFrissbiContactList.clear();
        // mFrissbiGroupList.clear();

        //int emailCount = mSelectedContacts.getEmailsSelectedIdsList().size();

        int groupsCount = mSelectedContacts.getGroupSelectedIdsList().size();

        for (int i = 0; i < groupsCount; i++) {
            List<FrissbiGroup> frissbiGroupList = FrissbiGroup.findWithQuery(FrissbiGroup.class, "select * from participant where group_id=?", mSelectedContacts.getGroupSelectedIdsList().get(i).toString());
            List<Participant> participantList = Participant.findWithQuery(Participant.class, "select * from participant where group_id=?", frissbiGroupList.get(0).getGroupId().toString());

            for (int j = 0; j < participantList.size(); j++) {
                Participant participant = participantList.get(j);
                FLog.d("MeetingActivity", "participant" + participant);
                if (!participant.getParticipantId().equals(SharedPreferenceHandler.getInstance(this).getUserId())) {
                    String[] userIds = new String[1];
                    userIds[0] = participant.getParticipantId().toString();
                    FLog.d("MeetingActivity", "frissbiContactList" + FrissbiContact.listAll(FrissbiContact.class));
                    //FrissbiContact frissbiContact = Select.from(FrissbiContact.class).where("user_id", userIds).first();
                    FrissbiContact frissbiContact = FrissbiContact.findWithQuery(FrissbiContact.class, "select * from frissbi_contact where user_id=?", participant.getParticipantId().toString()).get(0);

                    mSelectedContacts.setFrissbiContact(frissbiContact);
                }
            }
        }

        mFrissbiContactList.addAll(mSelectedContacts.getFrissbiContactList());

        if (mFrissbiContactList.size() > 0) {
            mAddAttendeeTextView.setVisibility(View.GONE);
            GroupParticipantAdapter groupParticipantAdapter = new GroupParticipantAdapter(MeetingActivity.this, mFrissbiContactList, true);
            mMeetingAttendeesRecyclerView.setAdapter(groupParticipantAdapter);
        } else {
            mAddAttendeeTextView.setVisibility(View.VISIBLE);
        }

    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.description_rl:
                showMeetingTitlesDialog();
                break;
            case R.id.date_rl:
                onCreateDialog(DATE_DIALOG_ID).show();
                break;
            case R.id.time_rl:
                onCreateDialog(TIME_DIALOG_ID).show();
                break;
            case R.id.duration_rl:
                showDurationDialog();
                break;
            case R.id.location_rl:
                showSelectLocationDialog();
                break;
            case R.id.add_attendee_button:
                Intent intent = new Intent(MeetingActivity.this, AddFriendsToMeetingActivity.class);
                startActivityForResult(intent, FRIENDS_REQ_CODE);
                break;
            case R.id.confirm_meeting:
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
                if (mFrissbiContactList.size() == 0) {
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
        builder.setTitle("Purpose..");
        View view = LayoutInflater.from(MeetingActivity.this).inflate(R.layout.alert_meeting_tiltes, null);
        RecyclerView meetingTitleRecyclerView = (RecyclerView) view.findViewById(R.id.meeting_title_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MeetingActivity.this);
        meetingTitleRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(meetingTitleRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        meetingTitleRecyclerView.addItemDecoration(dividerItemDecoration);
        builder.setView(view);
        List<String> titleList = new ArrayList<>();
        titleList.add("BREAKFAST");
        titleList.add("LUNCH");
        titleList.add("COFFEE");
        titleList.add("DINNER");
        titleList.add("DRINKS");
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
       /* if (mFriendList.size() > 0) {
            for (int i = 0; i < mFriendList.size(); i++) {
                friendsIdJsonArray.put(mFriendList.get(i).getUserId());
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
        }*/

        for (FrissbiContact frissbiContact : mFrissbiContactList) {

            if (frissbiContact.getType() == 1) {
                friendsIdJsonArray.put(frissbiContact.getUserId());
            } else if (frissbiContact.getType() == 2) {
                emailIdJsonArray.put(frissbiContact.getEmailId());
            } else if (frissbiContact.getType() == 3) {
                contactsJsonArray.put(frissbiContact.getPhoneNumber());
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
                                /*ReminderAlarmManager.getInstance(MeetingActivity.this).setMeetingAlarm(responseJsonObject.getLong("meetingId"), responseJsonObject.getBoolean("isLocationSelected"),
                                        mMeetingDateTextView.getText().toString() + "  " + mMeetingTimeTextView.getText().toString());*/
                              /*  Intent intent = new Intent(MeetingActivity.this, HomeActivity.class);
                                startActivity(intent);*/
                                onBackPressed();
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
                    Toast.makeText(MeetingActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

}
