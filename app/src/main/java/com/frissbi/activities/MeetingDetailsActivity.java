package com.frissbi.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.R;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.Utility.Utility;
import com.frissbi.models.Meeting;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MeetingDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private Meeting mMeeting;
    private SharedPreferences mSharedPreferences;
    private String mUserId;
    private CustomProgressDialog mProgressDialog;
    private TextView mMeetingDetailsStatusTextView;
    private Button mMeetingAcceptButton;
    private Button mMeetingIgnoreButton;
    private AlertDialog mAlertDialog;
    private AlertDialog mConflictAlertDialog;
    private AlertDialog mConfirmAlertDialog;
    private TextView mMeetingDetailsAtTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mProgressDialog = new CustomProgressDialog(this);
        mMeeting = getIntent().getExtras().getParcelable("meeting");
        mSharedPreferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("USERID_FROM", "editor");
        TextView meetingDetailsTitleTextView = (TextView) findViewById(R.id.meeting_details_title_tv);
        TextView meetingDetailsDateTextView = (TextView) findViewById(R.id.meeting_details_date_tv);
        TextView meetingDetailsTimeTextView = (TextView) findViewById(R.id.meeting_details_time_tv);
        mMeetingDetailsAtTextView = (TextView) findViewById(R.id.meeting_details_at_tv);
        mMeetingDetailsStatusTextView = (TextView) findViewById(R.id.meeting_details_status_tv);
       // meetingDetailsFriendTextView = (TextView) findViewById(R.id.meeting_details_friend_tv);

        mMeetingAcceptButton = (Button) findViewById(R.id.meeting_accept_button);
        mMeetingIgnoreButton = (Button) findViewById(R.id.meeting_ignore_button);

        meetingDetailsTitleTextView.setText(mMeeting.getDescription());
        meetingDetailsDateTextView.setText(mMeeting.getDate());
        meetingDetailsTimeTextView.setText(Utility.getInstance().convertTime(mMeeting.getFromTime()) + " to " + Utility.getInstance().convertTime(mMeeting.getToTime()));

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

        getMeetingDetailsFromServer();

        mMeetingAcceptButton.setOnClickListener(this);
        mMeetingIgnoreButton.setOnClickListener(this);

    }


    private void getMeetingDetailsFromServer() {
        String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.MEETING_SINGALDETAILS + mMeeting.getMeetingId() + "/" + mUserId;
        TSNetworkHandler.getInstance(this).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                //   mMeetingDetailsAtTextView.setText(mMeeting.getAddress());
            }
        });
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
        }

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
