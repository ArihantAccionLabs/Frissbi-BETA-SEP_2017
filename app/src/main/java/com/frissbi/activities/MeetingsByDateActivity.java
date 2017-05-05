package com.frissbi.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.frissbi.R;
import com.frissbi.Utility.FLog;
import com.frissbi.adapters.MeetingLogAdapter;
import com.frissbi.interfaces.MeetingDetailsListener;
import com.frissbi.models.Meeting;
import com.frissbi.models.MeetingFriends;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MeetingsByDateActivity extends AppCompatActivity implements MeetingDetailsListener {

    private MeetingDetailsListener mMeetingDetailsListener;
    private ActionBar mActionBar;
    private SimpleDateFormat mDateMonthFormat;
    private SimpleDateFormat mDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        mDateMonthFormat = new SimpleDateFormat("dd MMMM", Locale.ENGLISH);
        setContentView(R.layout.activity_meetings_by_date);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        try {
            Date date = mDateFormat.parse(getIntent().getExtras().getString("date"));
            String formattedDate = mDateMonthFormat.format(date);
            mActionBar.setTitle(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mMeetingDetailsListener = (MeetingDetailsListener) this;
        RecyclerView meetingCalendarRecyclerView = (RecyclerView) findViewById(R.id.meeting_calendar_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        meetingCalendarRecyclerView.setLayoutManager(layoutManager);
        List<Meeting> meetingList = Meeting.findWithQuery(Meeting.class, "select * from meeting where date=?", getIntent().getExtras().getString("date"));
        FLog.d("MeetingsByDateActivity", "meetingList" + meetingList);
        for (Meeting meeting : meetingList) {
            List<MeetingFriends> meetingFriendsList = MeetingFriends.findWithQuery(MeetingFriends.class, "select * from meeting_friends where meeting_id=?", meeting.getMeetingId().toString());
            meeting.setMeetingFriendsList(meetingFriendsList);
        }
        MeetingLogAdapter meetingLogAdapter = new MeetingLogAdapter(this, meetingList, mMeetingDetailsListener);
        meetingCalendarRecyclerView.setAdapter(meetingLogAdapter);
    }

    @Override
    public void showMeetingDetails(Long meetingId) {
        Intent intent = new Intent(MeetingsByDateActivity.this, MeetingDetailsActivity.class);
        intent.putExtra("meetingId", meetingId);
        startActivity(intent);
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
