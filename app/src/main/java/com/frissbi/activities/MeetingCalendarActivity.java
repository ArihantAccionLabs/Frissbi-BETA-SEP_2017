package com.frissbi.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.Utility;
import com.frissbi.adapters.CalendarGridAdapter;
import com.frissbi.adapters.MeetingLogAdapter;
import com.frissbi.interfaces.MeetingDetailsListener;
import com.frissbi.models.Meeting;
import com.frissbi.models.MeetingDate;
import com.frissbi.models.MeetingFriends;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MeetingCalendarActivity extends AppCompatActivity implements View.OnClickListener, MeetingDetailsListener {

    private static final String TAG = "MeetingCalendarActivity";
    private ImageView mPreviousButton;
    private ImageView mNextButton;
    private TextView mCurrentDateTextView;
    private GridView mCalendarGridView;
    private static final int MAX_CALENDAR_COLUMN = 42;
    private Calendar mCalendar;
    private SimpleDateFormat mMonthFormat;
    private SimpleDateFormat mDateFormat;
    private CalendarGridAdapter mAdapter;
    private List<Date> mDayValueInCells;
    private SharedPreferences mSharedPreferences;
    private String mUserId;
    private List<Meeting> mMeetingList;
    private MeetingDetailsListener mMeetingDetailsListener;
    private RecyclerView mMeetingCalendarRecyclerView;
    private Integer mCurrentYear;
    private Integer mCurrentMonth;
    private String mCurrentDay;
    private ProgressDialog mProgressDialog;
    private MeetingLogAdapter mMeetingLogAdapter;
    private Integer mSelectedYear;
    private Integer mSelectedMonth;
    private String mSelectedDay;
    private List<MeetingDate> mMeetingDateList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_calendar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mMonthFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        mCalendar = Calendar.getInstance(Locale.ENGLISH);
        mCurrentDay = mDateFormat.format(mCalendar.getTime());
        mCurrentMonth = mCalendar.get(Calendar.MONTH) + 1;
        mCurrentYear = mCalendar.get(Calendar.YEAR);
        mMeetingList = new ArrayList<>();
        mMeetingDetailsListener = (MeetingDetailsListener) this;
        mProgressDialog = new CustomProgressDialog(this);
        mSharedPreferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("USERID_FROM", "editor");
        mPreviousButton = (ImageView) findViewById(R.id.previous_month);
        mNextButton = (ImageView) findViewById(R.id.next_month);
        mCurrentDateTextView = (TextView) findViewById(R.id.display_current_date);
        mCalendarGridView = (GridView) findViewById(R.id.calendar_grid);
        mMeetingCalendarRecyclerView = (RecyclerView) findViewById(R.id.meeting_calendar_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mMeetingCalendarRecyclerView.setLayoutManager(layoutManager);

        mSelectedYear = mCurrentYear;
        mSelectedMonth = mCurrentMonth;
        mSelectedDay = mCurrentDay;
        checkMeetingInLocalDB(mSelectedYear, mSelectedMonth);
        // getMeetingLogByDate(mDateFormat.format(mCalendar.getTime()));

        mPreviousButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);

        mCalendarGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Date mDate = mAdapter.getItem(position);
                mCalendar.setTime(mDate);
                mSelectedDay = mDateFormat.format(mCalendar.getTime());
                getMeetingsByDateFromLocalDB(mDateFormat.format(mCalendar.getTime()));
                setUpCalendarAdapter(mCurrentDay, mSelectedDay, mMeetingDateList);
            }
        });
    }

    private void getMeetingsByDateFromLocalDB(String date) {
        List<Meeting> meetingList = Meeting.findWithQuery(Meeting.class, "select * from meeting where date=?", date);
        FLog.d(TAG, "meetingList" + meetingList);
        for (Meeting meeting : meetingList) {
            List<MeetingFriends> meetingFriendsList = MeetingFriends.findWithQuery(MeetingFriends.class, "select * from meeting_friends where meeting_id=?", meeting.getMeetingId().toString());
            meeting.setMeetingFriendsList(meetingFriendsList);
        }

        if (meetingList.size() > 0) {
            mMeetingCalendarRecyclerView.setVisibility(View.VISIBLE);
            mMeetingLogAdapter = new MeetingLogAdapter(MeetingCalendarActivity.this, meetingList, mMeetingDetailsListener);
            mMeetingCalendarRecyclerView.setAdapter(mMeetingLogAdapter);
        } else {
            mMeetingCalendarRecyclerView.setVisibility(View.GONE);
            Toast.makeText(MeetingCalendarActivity.this, "No meeting available...", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previous_month:
                mCalendar.add(Calendar.MONTH, -1);
                mSelectedYear = mCalendar.get(Calendar.YEAR);
                mSelectedMonth = mCalendar.get(Calendar.MONTH);
                FLog.d(TAG, "previous_month" + mCalendar.get(Calendar.MONTH) + "year--" + mCalendar.get(Calendar.YEAR));
                checkMeetingInLocalDB(mSelectedYear, mSelectedMonth + 1);
                /*if (mCalendar.get(Calendar.MONTH) == mCurrentMonth) {
                    setUpCalendarAdapter(mCurrentDay);
                } else {
                    setUpCalendarAdapter(0);
                }*/
                break;
            case R.id.next_month:
                mCalendar.add(Calendar.MONTH, 1);
                mSelectedYear = mCalendar.get(Calendar.YEAR);
                mSelectedMonth = mCalendar.get(Calendar.MONTH);
                FLog.d(TAG, "next_month" + mCalendar.get(Calendar.MONTH) + "year--" + mCalendar.get(Calendar.YEAR));
                checkMeetingInLocalDB(mSelectedYear, mSelectedMonth + 1);
              /*  if (mCalendar.get(Calendar.MONTH) == mCurrentMonth) {
                    setUpCalendarAdapter(mCurrentDay);
                } else {
                    setUpCalendarAdapter(0);
                }*/
                break;
        }
    }


    private void getMeetingCountByMonth(final Integer year, final Integer month) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", mUserId);
            jsonObject.put("date", year + "-" + month);

            String url = Utility.REST_URI + Utility.MEETING_COUNT_BY_MONTH;

            TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {
                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            try {
                                JSONObject responseJsonObject = new JSONObject(response.response);
                                JSONArray meetingJsonArray = responseJsonObject.getJSONArray("meetingArray");
                                for (int i = 0; i < meetingJsonArray.length(); i++) {
                                    JSONObject meetingJsonObject = meetingJsonArray.getJSONObject(i);
                                    Meeting meeting = new Meeting();
                                    List<MeetingFriends> meetingFriendsList = new ArrayList<>();
                                    meeting.setMeetingId(meetingJsonObject.getLong("meetingId"));
                                    meeting.setDate(meetingJsonObject.getString("date"));
                                    meeting.setFromTime(meetingJsonObject.getString("from"));
                                    meeting.setToTime(meetingJsonObject.getString("to"));
                                    meeting.setDescription(meetingJsonObject.getString("description"));
                                    meeting.setMonth(meetingJsonObject.getString("month"));
                                    if (meetingJsonObject.getBoolean("isLocationSelected")) {
                                        meeting.setLocationSelected(meetingJsonObject.getBoolean("isLocationSelected"));
                                        meeting.setAddress(meetingJsonObject.getString("address"));
                                        meeting.setLatitude(meetingJsonObject.getDouble("latitude"));
                                        meeting.setLongitude(meetingJsonObject.getDouble("longitude"));
                                    } else {
                                        meeting.setLocationSelected(meetingJsonObject.getBoolean("isLocationSelected"));
                                    }

                                    JSONArray friendsJsonArray = meetingJsonObject.getJSONArray("friendsJsonArray");
                                    int friendsLength = friendsJsonArray.length();
                                    if (friendsLength > 0) {
                                        for (int j = 0; j < friendsLength; j++) {
                                            MeetingFriends meetingFriends = new MeetingFriends();
                                            meetingFriends.setName(friendsJsonArray.getString(j));
                                            meetingFriends.setType("friend");
                                            meetingFriends.setMeetingId(meetingJsonObject.getLong("meetingId"));
                                            meetingFriends.save();
                                            meetingFriendsList.add(meetingFriends);
                                        }
                                    }


                                    JSONArray emailIdJsonArray = meetingJsonObject.getJSONArray("emailIdJsonArray");
                                    int emailIdsLength = emailIdJsonArray.length();
                                    if (emailIdsLength > 0) {
                                        for (int j = 0; j < emailIdsLength; j++) {
                                            MeetingFriends meetingFriends = new MeetingFriends();
                                            meetingFriends.setName(emailIdJsonArray.getString(j));
                                            meetingFriends.setType("email");
                                            meetingFriends.setMeetingId(meetingJsonObject.getLong("meetingId"));
                                            meetingFriends.save();
                                            meetingFriendsList.add(meetingFriends);
                                        }
                                    }
                                    JSONArray contactsJsonArray = meetingJsonObject.getJSONArray("contactsJsonArray");
                                    int contactsLength = contactsJsonArray.length();
                                    if (contactsLength > 0) {
                                        for (int j = 0; j < contactsLength; j++) {
                                            MeetingFriends meetingFriends = new MeetingFriends();
                                            meetingFriends.setName(contactsJsonArray.getString(j));
                                            meetingFriends.setType("contact");
                                            meetingFriends.setMeetingId(meetingJsonObject.getLong("meetingId"));
                                            meetingFriends.save();
                                            meetingFriendsList.add(meetingFriends);
                                        }
                                    }

                                    meeting.setMeetingFriendsList(meetingFriendsList);
                                    meeting.save();
                                }

                                JSONArray dateCountJsonArray = responseJsonObject.getJSONArray("dateCountArray");
                                mMeetingDateList = new ArrayList<MeetingDate>();
                                for (int j = 0; j < dateCountJsonArray.length(); j++) {
                                    MeetingDate meetingDate = new MeetingDate();
                                    JSONObject countJsonObject = dateCountJsonArray.getJSONObject(j);
                                    meetingDate.setMonth(year + "-" + month);
                                    meetingDate.setDate(countJsonObject.getString("date"));
                                    meetingDate.setCount(countJsonObject.getInt("count"));
                                    meetingDate.save();
                                    mMeetingDateList.add(meetingDate);
                                }

                                setUpCalendarAdapter(mCurrentDay, mSelectedDay, mMeetingDateList);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(MeetingCalendarActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getMeetingLogByDate(String date) {
        mProgressDialog.show();
        JSONObject jsonObject = new JSONObject();
        mMeetingList.clear();

        try {
            jsonObject.put("userId", mUserId);
            jsonObject.put("date", date);
            String url = Utility.REST_URI + Utility.MEETING_LOG_BY_DATE;
            TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {
                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            try {
                                JSONObject responseJsonObject = new JSONObject(response.response);
                                JSONArray meetingJsonArray = responseJsonObject.getJSONArray("meetingArray");
                                for (int i = 0; i < meetingJsonArray.length(); i++) {
                                    JSONObject meetingJsonObject = meetingJsonArray.getJSONObject(i);
                                    Meeting meeting = new Meeting();
                                    List<MeetingFriends> meetingFriendsList = new ArrayList<>();
                                    meeting.setMeetingId(meetingJsonObject.getLong("meetingId"));
                                    meeting.setDate(meetingJsonObject.getString("date"));
                                    meeting.setFromTime(meetingJsonObject.getString("from"));
                                    meeting.setToTime(meetingJsonObject.getString("to"));
                                    meeting.setDescription(meetingJsonObject.getString("description"));
                                    if (meetingJsonObject.getBoolean("isLocationSelected")) {
                                        meeting.setLocationSelected(meetingJsonObject.getBoolean("isLocationSelected"));
                                        meeting.setAddress(meetingJsonObject.getString("address"));
                                        meeting.setLatitude(meetingJsonObject.getDouble("latitude"));
                                        meeting.setLongitude(meetingJsonObject.getDouble("longitude"));
                                    } else {
                                        meeting.setLocationSelected(meetingJsonObject.getBoolean("isLocationSelected"));
                                    }

                                    JSONArray friendsJsonArray = meetingJsonObject.getJSONArray("friendsJsonArray");
                                    int friendsLength = friendsJsonArray.length();
                                    if (friendsLength > 0) {
                                        for (int j = 0; j < friendsLength; j++) {
                                            MeetingFriends meetingFriends = new MeetingFriends();
                                            meetingFriends.setName(friendsJsonArray.getString(j));
                                            meetingFriends.setType("friend");
                                            meetingFriends.setMeetingId(meetingJsonObject.getLong("meetingId"));
                                            meetingFriendsList.add(meetingFriends);
                                        }
                                    }


                                    JSONArray emailIdJsonArray = meetingJsonObject.getJSONArray("emailIdJsonArray");
                                    int emailIdsLength = emailIdJsonArray.length();
                                    if (emailIdsLength > 0) {
                                        for (int j = 0; j < emailIdsLength; j++) {
                                            MeetingFriends meetingFriends = new MeetingFriends();
                                            meetingFriends.setName(emailIdJsonArray.getString(j));
                                            meetingFriends.setType("email");
                                            meetingFriends.setMeetingId(meetingJsonObject.getLong("meetingId"));
                                            meetingFriendsList.add(meetingFriends);
                                        }
                                    }
                                    JSONArray contactsJsonArray = meetingJsonObject.getJSONArray("contactsJsonArray");
                                    int contactsLength = contactsJsonArray.length();
                                    if (contactsLength > 0) {
                                        for (int j = 0; j < contactsLength; j++) {
                                            MeetingFriends meetingFriends = new MeetingFriends();
                                            meetingFriends.setName(contactsJsonArray.getString(j));
                                            meetingFriends.setType("contact");
                                            meetingFriends.setMeetingId(meetingJsonObject.getLong("meetingId"));
                                            meetingFriendsList.add(meetingFriends);
                                        }
                                    }

                                    meeting.setMeetingFriendsList(meetingFriendsList);
                                    mMeetingList.add(meeting);
                                }
                                if (mMeetingList.size() > 0) {
                                    mMeetingCalendarRecyclerView.setVisibility(View.VISIBLE);
                                    mMeetingLogAdapter = new MeetingLogAdapter(MeetingCalendarActivity.this, mMeetingList, mMeetingDetailsListener);
                                    mMeetingCalendarRecyclerView.setAdapter(mMeetingLogAdapter);
                                } else {
                                    mMeetingCalendarRecyclerView.setVisibility(View.GONE);
                                    Toast.makeText(MeetingCalendarActivity.this, "No meeting available...", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    mProgressDialog.dismiss();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void setUpCalendarAdapter(String currentDay, String selectedDay, List<MeetingDate> meetingDateList) {
        mDayValueInCells = new ArrayList<Date>();
        Calendar mCal = (Calendar) mCalendar.clone();
        mCal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfTheMonth = mCal.get(Calendar.DAY_OF_WEEK) - 1;
        mCal.add(Calendar.DAY_OF_MONTH, -firstDayOfTheMonth);
        while (mDayValueInCells.size() < MAX_CALENDAR_COLUMN) {
            mDayValueInCells.add(mCal.getTime());
            mCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        String sDate = mMonthFormat.format(mCalendar.getTime());
        mCurrentDateTextView.setText(sDate);
        mAdapter = new CalendarGridAdapter(MeetingCalendarActivity.this, mDayValueInCells, mCalendar, currentDay, selectedDay, meetingDateList);
        mCalendarGridView.setAdapter(mAdapter);
    }


    private void checkMeetingInLocalDB(Integer year, Integer month) {
        List<Meeting> meetingList = Meeting.findWithQuery(Meeting.class, "Select * from Meeting where month = ?", year.toString() + "-" + month.toString());
        FLog.d(TAG, "meetingList" + meetingList);
        if (meetingList.size() > 0) {
            List<MeetingDate> meetingDateList = MeetingDate.findWithQuery(MeetingDate.class, "Select * from Meeting_Date where month = ?", year.toString() + "-" + month.toString());
            FLog.d(TAG, "meetingDateList" + meetingDateList);
            mMeetingDateList = meetingDateList;
            setUpCalendarAdapter(mCurrentDay, mSelectedDay, meetingDateList);
        } else {
            getMeetingCountByMonth(year, month);
        }
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

    @Override
    public void showMeetingDetails(Meeting meeting) {
        Intent intent = new Intent(MeetingCalendarActivity.this, MeetingDetailsActivity.class);
        intent.putExtra("callFrom", "meetingLog");
        intent.putExtra("meeting", meeting);
        startActivity(intent);
    }
}
