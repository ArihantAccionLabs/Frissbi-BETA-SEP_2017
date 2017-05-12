package com.frissbi.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.Utility;
import com.frissbi.adapters.CalendarGridAdapter;
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

public class MeetingCalendarActivity extends AppCompatActivity {

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
    private List<Meeting> mMeetingList;
    private Integer mCurrentYear;
    private Integer mCurrentMonth;
    private String mCurrentDay;
    private ProgressDialog mProgressDialog;
    private Integer mSelectedYear;
    private Integer mSelectedMonth;
    private String mSelectedDay;
    private List<MeetingDate> mMeetingDateList;

    MyGestureListener myGestureListener;
    private GestureDetectorCompat mDetector;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_calendar);
        mActionBar = getSupportActionBar();
        mDetector = new GestureDetectorCompat(this, new MyGestureListener());
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mMonthFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        mCalendar = Calendar.getInstance(Locale.ENGLISH);
        mCurrentDay = mDateFormat.format(mCalendar.getTime());
        mCurrentMonth = mCalendar.get(Calendar.MONTH) + 1;
        mCurrentYear = mCalendar.get(Calendar.YEAR);
        mMeetingList = new ArrayList<>();
        mProgressDialog = new CustomProgressDialog(this);
        mCalendarGridView = (GridView) findViewById(R.id.calendar_grid);


        mCalendarGridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mDetector.onTouchEvent(event)) {
                    FLog.d("MeetingCalendarActivity", "onTouchEvent--FALSE" + event);
                    return false;
                }
                FLog.d("MeetingCalendarActivity", "onTouchEvent--TRUE");
                return false;
            }
        });

        /*meetingCalendarLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mDetector.onTouchEvent(event)){
                    return false;
                }
                return true;
            }
        });*/
        myGestureListener = new MyGestureListener();

        mSelectedYear = mCurrentYear;
        mSelectedMonth = mCurrentMonth;
        mSelectedDay = mCurrentDay;
        checkMeetingInLocalDB(mSelectedYear, mSelectedMonth);
        // getMeetingLogByDate(mDateFormat.format(mCalendar.getTime()));

       /* mPreviousButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);*/

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
            Intent intent = new Intent(this, MeetingsByDateActivity.class);
            intent.putExtra("date", date);
            startActivity(intent);
        } else {
            Toast.makeText(MeetingCalendarActivity.this, "No meeting available...", Toast.LENGTH_SHORT).show();
        }
    }


    private void getMeetingCountByMonth(final Integer year, final Integer month) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", SharedPreferenceHandler.getInstance(this).getUserId());
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
                                Meeting.deleteAll(Meeting.class);
                                FLog.d("MeetingCalendarActivity", "meetingList----" + Meeting.listAll(Meeting.class));
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
                                mMeetingDateList = new ArrayList<>();
                                MeetingDate.deleteAll(MeetingDate.class);
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
            jsonObject.put("userId", SharedPreferenceHandler.getInstance(this).getUserId());
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
                               /* if (mMeetingList.size() > 0) {
                                    mMeetingCalendarRecyclerView.setVisibility(View.VISIBLE);
                                    mMeetingLogAdapter = new MeetingLogAdapter(MeetingCalendarActivity.this, mMeetingList, mMeetingDetailsListener);
                                    mMeetingCalendarRecyclerView.setAdapter(mMeetingLogAdapter);
                                } else {
                                    mMeetingCalendarRecyclerView.setVisibility(View.GONE);
                                    Toast.makeText(MeetingCalendarActivity.this, "No meeting available...", Toast.LENGTH_SHORT).show();
                                }*/
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
        //mCurrentDateTextView.setText(sDate);
        mActionBar.setTitle(sDate);
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
            getMeetingCountByMonth(year, month);
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
    public boolean onTouchEvent(MotionEvent event) {
        FLog.d("MeetingCalendarActivity", "onTouchEvent" + event);
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

   /* class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener implements
            View.OnTouchListener {
        Context context;
        GestureDetector gDetector;
        static final int SWIPE_MIN_DISTANCE = 120;
        static final int SWIPE_MAX_OFF_PATH = 250;
        static final int SWIPE_THRESHOLD_VELOCITY = 200;


        public SwipeGestureListener(Context context) {
            this(context, null);
        }

        public SwipeGestureListener(Context context, GestureDetector gDetector) {

            if (gDetector == null)
                gDetector = new GestureDetector(context, this);

            this.context = context;
            this.gDetector = gDetector;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            FLog.d("MeetingCalendarActivity", "onFling-----------");
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH
                        || Math.abs(velocityY) < SWIPE_THRESHOLD_VELOCITY) {
                    return false;
                }
                if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE) {
                    Toast.makeText(MeetingCalendarActivity.this, "bottomToTop",
                            Toast.LENGTH_SHORT).show();
                } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE) {
                    Toast.makeText(MeetingCalendarActivity.this,
                            "topToBottom  ", Toast.LENGTH_SHORT)
                            .show();
                }
            } else {
                if (Math.abs(velocityX) < SWIPE_THRESHOLD_VELOCITY) {
                    return false;
                }
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
                    Toast.makeText(MeetingCalendarActivity.this, "swipe rightLeft ", Toast.LENGTH_SHORT).show();
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
                    Toast.makeText(MeetingCalendarActivity.this,
                            "swipe LeftToright  ", Toast.LENGTH_SHORT).show();
                }
            }

            return super.onFling(e1, e2, velocityX, velocityY);

        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            return gDetector.onTouchEvent(event);
        }

        public GestureDetector getDetector() {
            return gDetector;
        }

    }*/


    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDown(MotionEvent event) {
            Log.d(DEBUG_TAG, "onDown: " + event.toString());
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
          /*  if (event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Log.d(DEBUG_TAG, "onDown: " + "Right to left");
                return false; // Right to left

            } else if (event2.getX() - event1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Log.d(DEBUG_TAG, "onDown: " + "Left to right");
                return false; // Left to right
            }*/

            if (event1.getY() - event2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                Log.d(DEBUG_TAG, "onDown: " + "Bottom to top");
                mCalendar.add(Calendar.MONTH, 1);
                mSelectedYear = mCalendar.get(Calendar.YEAR);
                mSelectedMonth = mCalendar.get(Calendar.MONTH);
                FLog.d(TAG, "next_month" + mCalendar.get(Calendar.MONTH) + "year--" + mCalendar.get(Calendar.YEAR));
                checkMeetingInLocalDB(mSelectedYear, mSelectedMonth + 1);
                return false; // Bottom to top
            } else if (event2.getY() - event1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                Log.d(DEBUG_TAG, "onDown: " + "Top to bottom");
                mCalendar.add(Calendar.MONTH, -1);
                mSelectedYear = mCalendar.get(Calendar.YEAR);
                mSelectedMonth = mCalendar.get(Calendar.MONTH);
                FLog.d(TAG, "previous_month" + mCalendar.get(Calendar.MONTH) + "year--" + mCalendar.get(Calendar.YEAR));
                checkMeetingInLocalDB(mSelectedYear, mSelectedMonth + 1);
                return false; // Top to bottom
            }
            return false;
        }
    }


}
