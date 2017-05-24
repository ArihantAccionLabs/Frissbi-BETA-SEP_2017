package com.frissbi.app.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.app.R;
import com.frissbi.app.Utility.ConnectionDetector;
import com.frissbi.app.Utility.CustomProgressDialog;
import com.frissbi.app.Utility.SharedPreferenceHandler;
import com.frissbi.app.Utility.Utility;
import com.frissbi.app.adapters.GroupParticipantAdapter;
import com.frissbi.app.adapters.MeetingFriendsAdapter;
import com.frissbi.app.models.FrissbiContact;
import com.frissbi.app.models.Meeting;
import com.frissbi.app.models.MeetingFriends;
import com.frissbi.app.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MeetingDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private Meeting mMeeting;
    private Long mUserId;
    // private TextView mMeetingDetailsStatusTextView;
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
    private View mStatusView;
    //private Button mMoreFriendsButton;
    private TextView mMeetingDetailsFriendTextView;
    private ProgressDialog mProgressDialog;
    private Long mMeetingId;
    private AlertDialog mMeetingCanceledAlertDialog;
    private TextView mChangePlaceButton;
    private TextView mMeetingDateTextView;
    private TextView mMeetingTimeTextView;
    private TextView mMeetingPlaceTextView;
    private TextView mMeetingDurationTextView;
    private TextView mMeetingTitleTextView;
    private RecyclerView mMeetingAttendeesRecyclerView;
    private List<FrissbiContact> mFrissbiContactsList;
    private TextView placeNameTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_details);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mProgressDialog = new CustomProgressDialog(this);
        mFrissbiContactsList = new ArrayList<>();
        Bundle bundle = getIntent().getExtras();

        mUserId = SharedPreferenceHandler.getInstance(this).getUserId();
        // mMoreFriendsButton = (Button) findViewById(R.id.more_friends_button);
       /* mMeetingDetailsFriendTextView = (TextView) findViewById(R.id.meeting_details_friend_tv);

        mMeetingDetailsStatusTextView = (TextView) findViewById(R.id.meeting_details_status_tv);*/

        mChangePlaceButton = (TextView) findViewById(R.id.change_place_tv);


        if (bundle.containsKey("meetingId")) {
            mMeetingId = bundle.getLong("meetingId");
            if (ConnectionDetector.getInstance(this).isConnectedToInternet()) {
                getMeetingDetailsFromServer();
            } else {
                Toast.makeText(this, getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
            }
        }

        setUpViews();


        // mMoreFriendsButton.setOnClickListener(this);
        mChangePlaceButton.setOnClickListener(this);

    }

    private void setUpViews() {
        mMeetingDetailsDateTextView = (TextView) findViewById(R.id.meeting_date_tv);
        mMeetingDetailsTimeTextView = (TextView) findViewById(R.id.meeting_time_tv);
        mMeetingDetailsAtTextView = (TextView) findViewById(R.id.meeting_place_tv);
        mMeetingDurationTextView = (TextView) findViewById(R.id.meeting_duration_tv);
        mMeetingDetailsTitleTextView = (TextView) findViewById(R.id.meeting_title_tv);
        mMeetingAcceptButton = (Button) findViewById(R.id.accept_meeting_button);
        mMeetingIgnoreButton = (Button) findViewById(R.id.ignore_meeting_button);
        placeNameTv = (TextView) findViewById(R.id.place_name_tv);
        mMeetingAttendeesRecyclerView = (RecyclerView) findViewById(R.id.meeting_attendees_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mMeetingAttendeesRecyclerView.setLayoutManager(layoutManager);
        mMeetingAcceptButton.setOnClickListener(this);
        mMeetingIgnoreButton.setOnClickListener(this);
        findViewById(R.id.location_rl).setOnClickListener(this);
    }

    private void setMeetingObject(JSONObject meetingJsonObject) {
        try {
            mMeeting = new Meeting();
            mMeeting.setMeetingId(meetingJsonObject.getLong("meetingId"));
            mMeeting.setMeetingSenderId(meetingJsonObject.getLong("meetingSenderId"));
            if (meetingJsonObject.getLong("meetingSenderId") != mUserId) {
                mMeeting.setMeetingStatus(meetingJsonObject.getInt("meetingStatus"));
            } else {
                mMeeting.setMeetingStatus(1);
            }
            mMeeting.setDate(meetingJsonObject.getString("date"));
            mMeeting.setFromTime(meetingJsonObject.getString("from"));
            mMeeting.setToTime(meetingJsonObject.getString("to"));
            mMeeting.setDuration(meetingJsonObject.getString("meetingDuration"));
            mMeeting.setDescription(meetingJsonObject.getString("description"));
            mMeeting.setMeetingType(meetingJsonObject.getString("meetingType"));
            if (meetingJsonObject.getBoolean("isLocationSelected")) {
                mMeeting.setLocationSelected(meetingJsonObject.getBoolean("isLocationSelected"));
                mMeeting.setAddress(meetingJsonObject.getString("address"));
                if (meetingJsonObject.has("placeName")) {
                    mMeeting.setPlaceName(meetingJsonObject.getString("placeName"));
                }
                mMeeting.setLatitude(meetingJsonObject.getDouble("latitude"));
                mMeeting.setLongitude(meetingJsonObject.getDouble("longitude"));
            } else {
                mMeeting.setLocationSelected(meetingJsonObject.getBoolean("isLocationSelected"));
            }

            JSONArray friendsJsonArray = meetingJsonObject.getJSONArray("friendsJsonArray");
            int friendsLength = friendsJsonArray.length();
            for (int j = 0; j < friendsLength; j++) {
                FrissbiContact frissbiContact = new FrissbiContact();
                JSONObject friendJsonObject = friendsJsonArray.getJSONObject(j);
                frissbiContact.setUserId(friendJsonObject.getLong("userId"));
                frissbiContact.setName(friendJsonObject.getString("fullName"));
                if (friendJsonObject.has("profileImageId")) {
                    frissbiContact.setImageId(friendJsonObject.getString("profileImageId"));
                }
                frissbiContact.setType(Utility.FRIEND_TYPE);
                mFrissbiContactsList.add(frissbiContact);
            }


            JSONArray emailIdJsonArray = meetingJsonObject.getJSONArray("emailIdJsonArray");
            int emailIdsLength = emailIdJsonArray.length();
            if (emailIdsLength > 0) {
                for (int j = 0; j < emailIdsLength; j++) {
                    FrissbiContact frissbiContact = new FrissbiContact();
                    frissbiContact.setName(emailIdJsonArray.getString(j));
                    frissbiContact.setType(Utility.EMAIL_TYPE);
                    mFrissbiContactsList.add(frissbiContact);
                }
            }
            JSONArray contactsJsonArray = meetingJsonObject.getJSONArray("contactsJsonArray");
            int contactsLength = contactsJsonArray.length();
            if (contactsLength > 0) {
                for (int j = 0; j < contactsLength; j++) {
                    FrissbiContact frissbiContact = new FrissbiContact();
                    frissbiContact.setName(contactsJsonArray.getString(j));
                    frissbiContact.setType(Utility.CONTACT_TYPE);
                    mFrissbiContactsList.add(frissbiContact);
                }
            }


            if (meetingJsonObject.has("updateCount")) {
                if (meetingJsonObject.getInt("updateCount") != 0 && meetingJsonObject.getInt("updateCount") != 2) {
                    mChangePlaceButton.setVisibility(View.VISIBLE);
                } else {
                    mChangePlaceButton.setVisibility(View.GONE);
                }
            }


            setData();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setData() {
        Log.d("MeetingDetailsActivity", "mUserId" + mUserId + "mMeeting" + mMeeting);

        GroupParticipantAdapter groupParticipantAdapter = new GroupParticipantAdapter(MeetingDetailsActivity.this, mFrissbiContactsList, true);
        mMeetingAttendeesRecyclerView.setAdapter(groupParticipantAdapter);

        mMeetingDetailsTitleTextView.setText(mMeeting.getDescription());
        mMeetingDetailsDateTextView.setText(mMeeting.getDate());
        mMeetingDurationTextView.setText(mMeeting.getDuration());
        mMeetingDetailsTimeTextView.setText(Utility.getInstance().convertTime(mMeeting.getFromTime()));
        if (!mMeeting.getMeetingType().equalsIgnoreCase("ONLINE")) {
            if (mMeeting.isLocationSelected()) {
                if (mMeeting.getPlaceName() != null) {
                    placeNameTv.setText(mMeeting.getPlaceName());
                }
                mMeetingDetailsAtTextView.setText(mMeeting.getAddress());
            } else {
                mMeetingDetailsAtTextView.setText("Any Place");
            }
        }else {
            mMeetingDetailsAtTextView.setText("Online");
        }


        if (!mMeeting.getMeetingSenderId().equals(mUserId)) {
            if (mMeeting.getMeetingStatus() == Utility.STATUS_PENDING) {
                mMeetingAcceptButton.setVisibility(View.VISIBLE);
                mMeetingIgnoreButton.setVisibility(View.VISIBLE);
            } else if (mMeeting.getMeetingStatus() == Utility.STATUS_ACCEPT) {
                mMeetingAcceptButton.setVisibility(View.GONE);
                mMeetingIgnoreButton.setVisibility(View.GONE);
            } else if (mMeeting.getMeetingStatus() == Utility.STATUS_REJECT) {
                mMeetingAcceptButton.setVisibility(View.GONE);
                mMeetingIgnoreButton.setVisibility(View.GONE);
            }
        } else {
            mMeetingAcceptButton.setVisibility(View.GONE);
            mMeetingIgnoreButton.setVisibility(View.GONE);
        }

    }


    private void getMeetingDetailsFromServer() {
        mProgressDialog.show();
        String url = Utility.REST_URI + Utility.MEETING_SINGALDETAILS + mMeetingId + "/" + mUserId;
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
            case R.id.accept_meeting_button:
                sendMeetingStatusToServer(true, Utility.STATUS_ACCEPT, new JSONArray());
                break;
            case R.id.ignore_meeting_button:
                showRejectConformationAlert();

                break;
            case R.id.location_rl:
                if (mMeeting.getLatitude() != null && mMeeting.getLongitude() != null) {
                    String urlAddress = "http://maps.google.com/maps?q=" + mMeeting.getLatitude() + "," + mMeeting.getLongitude();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress));
                    intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);
                }

                break;
            case R.id.change_place_tv:
                redirectToSuggestPlaces();
                break;
        }

    }

    private void redirectToSuggestPlaces() {
        Intent intent = new Intent(this, SuggestionsActivity.class);
        intent.putExtra("isCallFromSummary", true);
        intent.putExtra("summaryMeetingId", mMeetingId);
        intent.putExtra("title", mMeeting.getDescription());
        startActivity(intent);

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
        String url = Utility.REST_URI + Utility.MEETING_CONFLICT;

        TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                Log.d("MeetingDetailsActivity", "response" + response.response);
                if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {

                    try {
                        JSONObject responseJsonObject = new JSONObject(response.response);
                        if (responseJsonObject.has("isAccepted")) {
                            if (responseJsonObject.getBoolean("isAccepted")) {
                                //mMeetingDetailsStatusTextView.setText("ACCEPTED");
                                // mMeetingDetailsStatusTextView.setTextColor(getResources().getColor(R.color.green));
                                mMeetingAcceptButton.setVisibility(View.GONE);
                                mMeetingIgnoreButton.setVisibility(View.GONE);
                                Toast.makeText(MeetingDetailsActivity.this, responseJsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            } else {
                                showConflictingAlertDialog(responseJsonObject.getString("message"), responseJsonObject.getJSONArray("meetingIdsJsonArray"));
                            }
                        }

                        if (responseJsonObject.has("isRejected")) {
                            if (responseJsonObject.getBoolean("isRejected")) {
                                // mMeetingDetailsStatusTextView.setText("REJECTED");
                                // mMeetingDetailsStatusTextView.setTextColor(getResources().getColor(R.color.red));
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
