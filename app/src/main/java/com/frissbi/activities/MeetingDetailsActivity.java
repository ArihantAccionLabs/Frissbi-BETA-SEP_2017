package com.frissbi.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.R;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.Utility.Utility;
import com.frissbi.adapters.MeetingFriendsAdapter;
import com.frissbi.models.Meeting;
import com.frissbi.models.MeetingFriends;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MeetingDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private Meeting mMeeting;
    private SharedPreferences mSharedPreferences;
    private String mUserId;
    private TextView mMeetingDetailsStatusTextView;
    private Button mMeetingAcceptButton;
    private Button mMeetingIgnoreButton;
    private AlertDialog mAlertDialog;
    private AlertDialog mConflictAlertDialog;
    private AlertDialog mConfirmAlertDialog;
    private AlertDialog mFriendsAlertDialog;
    private List<MeetingFriends> mMeetingFriendsList;
    private JSONObject mMeetingJsonObject;
    private TextView mMeetingDetailsTitleTextView;
    private TextView mMeetingDetailsDateTextView;
    private TextView mMeetingDetailsTimeTextView;
    private TextView mMeetingDetailsAtTextView;
    private LinearLayout mMeetingStatusLayout;
    private View mStatusView;
    private Button mMoreFriendsButton;
    private TextView mMeetingDetailsFriendTextView;
    private ProgressDialog mProgressDialog;
    private Long mMeetingId;
    private AlertDialog mMeetingCanceledAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_details);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mProgressDialog = new CustomProgressDialog(this);

        Bundle bundle = getIntent().getExtras();

        mSharedPreferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("USERID_FROM", "editor");
        mMeetingDetailsTitleTextView = (TextView) findViewById(R.id.meeting_details_title_tv);
        mMeetingDetailsDateTextView = (TextView) findViewById(R.id.meeting_details_date_tv);
        mMeetingDetailsTimeTextView = (TextView) findViewById(R.id.meeting_details_time_tv);
        mMeetingDetailsAtTextView = (TextView) findViewById(R.id.meeting_details_at_tv);
        mMeetingStatusLayout = (LinearLayout) findViewById(R.id.status_ll);
        mStatusView = findViewById(R.id.view3);
        mMoreFriendsButton = (Button) findViewById(R.id.more_friends_button);
        mMeetingDetailsFriendTextView = (TextView) findViewById(R.id.meeting_details_friend_tv);

        mMeetingDetailsStatusTextView = (TextView) findViewById(R.id.meeting_details_status_tv);
        mMeetingAcceptButton = (Button) findViewById(R.id.meeting_accept_button);
        mMeetingIgnoreButton = (Button) findViewById(R.id.meeting_ignore_button);


        if (bundle.getString("callFrom").equalsIgnoreCase("meetingLog")) {
            if (bundle.getSerializable("meeting") != null) {
                mMeeting = (Meeting) bundle.getSerializable("meeting");
                mMeetingId = mMeeting.getMeetingId();
                getMeetingDetailsFromServer();
            }
        } else if (bundle.getString("callFrom").equalsIgnoreCase("notification")) {
            if (bundle.getString("meetingId") != null) {
                mMeetingId = Long.parseLong(bundle.getString("meetingId"));
                if (ConnectionDetector.getInstance(this).isConnectedToInternet()) {
                    getMeetingDetailsFromServer();
                } else {
                    Toast.makeText(this, getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
                }
            }
        }


        mMeetingAcceptButton.setOnClickListener(this);
        mMeetingIgnoreButton.setOnClickListener(this);
        mMoreFriendsButton.setOnClickListener(this);

    }

    private void setMeetingObject(JSONObject meetingJsonObject) {
        List<MeetingFriends> meetingFriendsList = new ArrayList<>();
        try {
            mMeeting = new Meeting();
            mMeeting.setMeetingId(meetingJsonObject.getLong("meetingId"));
            mMeeting.setMeetingSenderId(meetingJsonObject.getLong("meetingSenderId"));
            if (meetingJsonObject.getLong("meetingSenderId") != Long.parseLong(mUserId)) {
                mMeeting.setMeetingStatus(meetingJsonObject.getInt("meetingStatus"));
            } else {
                mMeeting.setMeetingStatus(1);
            }
            mMeeting.setDate(meetingJsonObject.getString("date"));
            mMeeting.setFromTime(meetingJsonObject.getString("from"));
            mMeeting.setToTime(meetingJsonObject.getString("to"));
            mMeeting.setDescription(meetingJsonObject.getString("description"));
            if (meetingJsonObject.getBoolean("isLocationSelected")) {
                mMeeting.setLocationSelected(meetingJsonObject.getBoolean("isLocationSelected"));
                mMeeting.setAddress(meetingJsonObject.getString("address"));
                mMeeting.setLatitude(meetingJsonObject.getDouble("latitude"));
                mMeeting.setLongitude(meetingJsonObject.getDouble("longitude"));
            } else {
                mMeeting.setLocationSelected(meetingJsonObject.getBoolean("isLocationSelected"));
            }

            JSONArray friendsJsonArray = meetingJsonObject.getJSONArray("friendsJsonArray");
            int friendsLength = friendsJsonArray.length();
            if (friendsLength > 0) {
                for (int j = 0; j < friendsLength; j++) {
                    MeetingFriends meetingFriends = new MeetingFriends();
                    JSONObject jsonObject = friendsJsonArray.getJSONObject(j);
                    meetingFriends.setName(jsonObject.getString("fullName"));
                    meetingFriends.setStatus(jsonObject.getInt("status"));
                    meetingFriends.setType("friend");
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
                    meetingFriendsList.add(meetingFriends);
                }
            }


            mMeeting.setMeetingFriendsList(meetingFriendsList);
            setViewWithValues();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setViewWithValues() {
        mMeetingFriendsList = mMeeting.getMeetingFriendsList();
        if (mMeetingFriendsList.size() > 1) {
            mMeetingDetailsFriendTextView.setText(mMeetingFriendsList.get(0).getName());
            mMoreFriendsButton.setVisibility(View.VISIBLE);
        } else {
            mMeetingDetailsFriendTextView.setText(mMeetingFriendsList.get(0).getName());
            mMoreFriendsButton.setVisibility(View.GONE);
        }
        mMeetingDetailsTitleTextView.setText(mMeeting.getDescription());
        mMeetingDetailsDateTextView.setText(mMeeting.getDate());
        mMeetingDetailsTimeTextView.setText(Utility.getInstance().convertTime(mMeeting.getFromTime()) + " to " + Utility.getInstance().convertTime(mMeeting.getToTime()));
        if (mMeeting.isLocationSelected()) {
            mMeetingDetailsAtTextView.setText(mMeeting.getAddress());
        } else {
            mMeetingDetailsAtTextView.setText("Any Place");
        }

        if (mMeeting.getMeetingSenderId() != Long.parseLong(mUserId)) {
            mMeetingStatusLayout.setVisibility(View.VISIBLE);
            mStatusView.setVisibility(View.VISIBLE);
            if (mMeeting.getMeetingStatus() == Utility.STATUS_PENDING) {
                mMeetingDetailsStatusTextView.setText("PENDING");
                mMeetingDetailsStatusTextView.setTextColor(getResources().getColor(R.color.light_orange));
            } else if (mMeeting.getMeetingStatus() == Utility.STATUS_ACCEPT) {
                mMeetingDetailsStatusTextView.setText("ACCEPTED");
                mMeetingDetailsStatusTextView.setTextColor(getResources().getColor(R.color.green));
                mMeetingAcceptButton.setVisibility(View.GONE);
            } else if (mMeeting.getMeetingStatus() == Utility.STATUS_REJECT) {
                mMeetingDetailsStatusTextView.setText("REJECTED");
                mMeetingAcceptButton.setVisibility(View.GONE);
                mMeetingIgnoreButton.setVisibility(View.GONE);
                mMeetingDetailsStatusTextView.setTextColor(getResources().getColor(R.color.red));
            } else if (mMeeting.getMeetingStatus() == Utility.STATUS_ACCEPT) {
                mMeetingDetailsStatusTextView.setText("COMPLETED");
                mMeetingAcceptButton.setVisibility(View.GONE);
                mMeetingIgnoreButton.setVisibility(View.GONE);
                mMeetingDetailsStatusTextView.setTextColor(getResources().getColor(R.color.blue));
            }
        } else {
            mMeetingAcceptButton.setVisibility(View.GONE);
            mMeetingStatusLayout.setVisibility(View.GONE);
            mStatusView.setVisibility(View.GONE);
        }
    }


    private void getMeetingDetailsFromServer() {
        mProgressDialog.show();
        String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.MEETING_SINGALDETAILS + mMeetingId + "/" + mUserId;
        TSNetworkHandler.getInstance(this).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    try {
                        JSONObject responseJsonObject = new JSONObject(response.response);
                        if (responseJsonObject.getBoolean("isMeetingExisted")) {
                            setMeetingObject(responseJsonObject);
                        } else {
                            setMeetingCanceledAlert();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MeetingDetailsActivity.this, "Something went wrong at server end", Toast.LENGTH_SHORT).show();
                }
                mProgressDialog.dismiss();
            }
        });
    }

    private void setMeetingCanceledAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert!");
        builder.setMessage("Meeting has been cancelled by creator");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mMeetingCanceledAlertDialog.dismiss();
                onBackPressed();
            }
        });
        mMeetingCanceledAlertDialog = builder.create();
        mMeetingCanceledAlertDialog.setCancelable(false);
        mMeetingCanceledAlertDialog.setCanceledOnTouchOutside(false);
        mMeetingCanceledAlertDialog.show();

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.meeting_accept_button:
                sendMeetingStatusToServer(true, Utility.STATUS_ACCEPT, new JSONArray());
                break;
            case R.id.meeting_ignore_button:
                showRejectConformationAlert();

                break;
            case R.id.more_friends_button:
                showFriendsListAlertDialog();
                break;
        }

    }

    private void showFriendsListAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(MeetingDetailsActivity.this).inflate(R.layout.alert_meeting_tiltes, null);
        builder.setView(view);
        RecyclerView meetingFriendsRecyclerView = (RecyclerView) view.findViewById(R.id.meeting_title_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MeetingDetailsActivity.this);
        meetingFriendsRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(meetingFriendsRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        meetingFriendsRecyclerView.addItemDecoration(dividerItemDecoration);
        mFriendsAlertDialog = builder.create();
        MeetingFriendsAdapter meetingFriendsAdapter = new MeetingFriendsAdapter(MeetingDetailsActivity.this, mMeetingFriendsList);
        meetingFriendsRecyclerView.setAdapter(meetingFriendsAdapter);
        mFriendsAlertDialog.show();

    }

    private void showRejectConformationAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MeetingDetailsActivity.this);
        builder.setTitle("Alert!");
        builder.setMessage("Are you sure. You want to reject meeting..");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAlertDialog.dismiss();
                sendMeetingStatusToServer(false, Utility.STATUS_REJECT, new JSONArray());
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAlertDialog.dismiss();
            }
        });

        mAlertDialog = builder.create();
        mAlertDialog.show();


    }


    private void sendMeetingStatusToServer(boolean isAccepted, int status, JSONArray meetingIdsJsonArray) {
        mProgressDialog.show();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("meetingId", mMeeting.getMeetingId());
            jsonObject.put("userId", mUserId);
            jsonObject.put("meetingDate", mMeeting.getDate());
            if (isAccepted) {
                jsonObject.put("isAccepted", true);
            } else {
                jsonObject.put("isRejected", true);
            }
            jsonObject.put("meetingStatus", status);
            if (meetingIdsJsonArray.length() > 0) {
                jsonObject.put("meetingIdsJsonArray", meetingIdsJsonArray);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.MEETING_CONFLICT;
        TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                Log.d("MeetingDetailsActivity", "response" + response.response);
                if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {

                    try {
                        JSONObject responseJsonObject = new JSONObject(response.response);
                        if (responseJsonObject.has("isAccepted")) {
                            if (responseJsonObject.getBoolean("isAccepted")) {
                                mMeetingDetailsStatusTextView.setText("ACCEPTED");
                                mMeetingDetailsStatusTextView.setTextColor(getResources().getColor(R.color.green));
                                mMeetingAcceptButton.setVisibility(View.GONE);
                                Toast.makeText(MeetingDetailsActivity.this, responseJsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            } else {
                                showConflictingAlertDialog(responseJsonObject.getString("message"), responseJsonObject.getJSONArray("meetingIdsJsonArray"));
                            }
                        }

                        if (responseJsonObject.has("isRejected")) {
                            if (responseJsonObject.getBoolean("isRejected")) {
                                mMeetingDetailsStatusTextView.setText("REJECTED");
                                mMeetingDetailsStatusTextView.setTextColor(getResources().getColor(R.color.red));
                                mMeetingAcceptButton.setVisibility(View.GONE);
                                mMeetingIgnoreButton.setVisibility(View.GONE);
                                Toast.makeText(MeetingDetailsActivity.this, responseJsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MeetingDetailsActivity.this, responseJsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if ((response.status == TSNetworkHandler.TSResponse.STATUS_FAIL)) {
                    Toast.makeText(MeetingDetailsActivity.this, response.message, Toast.LENGTH_SHORT).show();
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
                sendMeetingStatusToServer(true, Utility.STATUS_ACCEPT, meetingIdsJsonArray);
            }
        });
        mConfirmAlertDialog = builder.create();
        mConfirmAlertDialog.show();
    }

}
