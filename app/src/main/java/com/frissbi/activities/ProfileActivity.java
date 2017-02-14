package com.frissbi.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.Frissbi_img_crop.Util;
import com.frissbi.R;
import com.frissbi.Utility.FriendStatus;
import com.frissbi.Utility.Utility;
import com.frissbi.models.Friend;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mProfileUserImageView;
    private TextView mProfileUserNameTextView;
    private Button mAddFriendButton;
    private TextView mProfileUserEmail;
    private Friend mFriend;
    private SharedPreferences mSharedPreferences;
    private String mUserId;
    private TextView mDobTextView;
    private TextView mGenderTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSharedPreferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("USERID_FROM", "editor");
        mProfileUserImageView = (ImageView) findViewById(R.id.profile_user_image);
        mProfileUserNameTextView = (TextView) findViewById(R.id.profile_user_name);
        mAddFriendButton = (Button) findViewById(R.id.add_friend_button);
        mProfileUserEmail = (TextView) findViewById(R.id.profile_user_email);
        mDobTextView = (TextView) findViewById(R.id.dob_tv);
        mGenderTextView = (TextView) findViewById(R.id.gender_tv);
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("friend")) {
            mFriend = (Friend) bundle.getSerializable("friend");
            setValues();
        }

        if (bundle.containsKey("friendUserId")) {
            getProfileDetailsFromServer(bundle.getLong("friendUserId"));
        }

        mAddFriendButton.setOnClickListener(this);

    }

    private void setValues() {
        mProfileUserNameTextView.setText(mFriend.getFullName());
        mProfileUserEmail.setText(mFriend.getEmailId());
        mDobTextView.setText("DOB : " + mFriend.getDob());
        if (mFriend.getGender() != null) {
            mGenderTextView.setVisibility(View.VISIBLE);
            mGenderTextView.setText("Gender : " + mFriend.getGender());
        } else {
            mGenderTextView.setVisibility(View.GONE);
        }

        if (mFriend.getStatus().equalsIgnoreCase(FriendStatus.UNFRIEND.toString())) {
            mAddFriendButton.setText("Add");
        } else if (mFriend.getStatus().equalsIgnoreCase(FriendStatus.WAITING.toString())) {
            mAddFriendButton.setText("Req Sent");
        } else if (mFriend.getStatus().equalsIgnoreCase(FriendStatus.CONFIRM.toString())) {
            mAddFriendButton.setText("Accept");
        } else if (mFriend.getStatus().equalsIgnoreCase(FriendStatus.FRIENDS.toString())) {
            mAddFriendButton.setText("Friends");
        }
    }

    private void getProfileDetailsFromServer(long friendUserId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", mUserId);
            jsonObject.put("friendUserId", friendUserId);
            String url = Utility.REST_URI + Utility.VIEW_PROFILE;
            TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {
                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            try {
                                JSONObject responseJsonObject = new JSONObject(response.response);
                                JSONObject profileJsonObject = responseJsonObject.getJSONObject("viewProfile");
                                mFriend = new Friend();
                                mFriend.setUserId(profileJsonObject.getLong("userId"));
                                mFriend.setFullName(profileJsonObject.getString("fullName"));
                                mFriend.setEmailId(profileJsonObject.getString("emailId"));
                                mFriend.setStatus(profileJsonObject.getString("status"));
                                mFriend.setDob(profileJsonObject.getString("dob"));
                                if (profileJsonObject.has("gender")) {
                                    mFriend.setGender(profileJsonObject.getString("gender"));
                                }
                                Log.d("ProfileActivity", "mFriend" + mFriend);
                                setValues();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "Something went wrong at server side", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_friend_button:
                sendFriendRequest();
                break;
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

    public void sendFriendRequest() {
        if (mFriend.getStatus().equalsIgnoreCase(FriendStatus.UNFRIEND.toString())) {
            String url = Utility.REST_URI + Utility.ADD_FRIEND + mUserId + "/" + mFriend.getUserId();
            TSNetworkHandler.getInstance(this).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {
                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                            mAddFriendButton.setText("Req Sent");
                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(ProfileActivity.this, "Something went wrong at server side", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else if (mFriend.getStatus().equalsIgnoreCase(FriendStatus.CONFIRM.toString())) {
            String url = Utility.REST_URI + Utility.APPROVE_FRIEND + mUserId + "/" + mFriend.getUserId();
            TSNetworkHandler.getInstance(this).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {

                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                            mAddFriendButton.setText("Friends");
                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(ProfileActivity.this, "Something went wrong at server side", Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }
    }
}
