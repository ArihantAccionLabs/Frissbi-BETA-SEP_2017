package com.frissbi.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.Utility.FriendStatus;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.Utility;
import com.frissbi.models.Friend;
import com.frissbi.models.Profile;
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
    private Long mUserId;
    private TextView mDobTextView;
    private TextView mGenderTextView;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mProgressDialog = new CustomProgressDialog(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSharedPreferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        mUserId = SharedPreferenceHandler.getInstance(this).getUserId();
        mProfileUserImageView = (ImageView) findViewById(R.id.profile_user_image);
        mProfileUserNameTextView = (TextView) findViewById(R.id.profile_user_name);
        mAddFriendButton = (Button) findViewById(R.id.add_friend_button);
        mProfileUserEmail = (TextView) findViewById(R.id.profile_user_email);
        mDobTextView = (TextView) findViewById(R.id.dob_tv);
        mGenderTextView = (TextView) findViewById(R.id.gender_tv);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mAddFriendButton.setVisibility(View.VISIBLE);
            if (bundle.containsKey("friend")) {
                mFriend = (Friend) bundle.getSerializable("friend");
                setValues();
            } else if (bundle.containsKey("friendUserId")) {
                getProfileDetailsFromServer(bundle.getLong("friendUserId"));
            }
        } else {
            mAddFriendButton.setVisibility(View.GONE);
            getProfileDetails();
        }
        mAddFriendButton.setOnClickListener(this);

    }

    private void getProfileDetails() {
        mProgressDialog.show();
        String url = Utility.REST_URI + Utility.VIEW_PROFILE + mUserId;
        TSNetworkHandler.getInstance(this).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {

                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            JSONObject responseJsonObject = new JSONObject(response.response);
                            Profile profile = new Profile();
                            JSONObject profileJsonObject = responseJsonObject.getJSONObject("viewProfile");
                            profile.setUserName(profileJsonObject.getString("userName"));
                            profile.setFirstName(profileJsonObject.getString("firstName"));
                            profile.setLastName(profileJsonObject.getString("lastName"));
                            profile.setEmail(profileJsonObject.getString("email"));
                            if (profileJsonObject.has("contactNumber")) {
                                profile.setContactNumber(profileJsonObject.getString("contactNumber"));
                            }

                            if (profileJsonObject.has("profileImage")) {
                                String imageString = profileJsonObject.getString("profileImage");
                                byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                profile.setImageBitmap(bitmap);
                            }

                            if (profileJsonObject.has("gender")) {
                                profile.setGender(profileJsonObject.getString("gender"));
                            }

                            if (profileJsonObject.has("dob")) {
                                profile.setDob(profileJsonObject.getString("dob"));
                            }
                            setProfileDetails(profile);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                    }
                }
                mProgressDialog.dismiss();
            }
        });
    }

    private void setProfileDetails(Profile profile) {
        mProfileUserNameTextView.setText(profile.getUserName());
        mProfileUserEmail.setText(profile.getEmail());
        if (profile.getDob() != null) {
            mDobTextView.setVisibility(View.VISIBLE);
            mDobTextView.setText("DOB : " + profile.getDob());
        } else {
            mDobTextView.setVisibility(View.GONE);
        }
        if (profile.getGender() != null) {
            mGenderTextView.setVisibility(View.VISIBLE);
            mGenderTextView.setText("Gender : " + profile.getGender());
        } else {
            mGenderTextView.setVisibility(View.GONE);
        }
        if (profile.getImageBitmap() != null) {
            mProfileUserImageView.setImageBitmap(profile.getImageBitmap());
        }
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
        mProgressDialog.show();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", mUserId);
            jsonObject.put("friendId", friendUserId);
            String url = Utility.REST_URI + Utility.VIEW_OTHER_PROFILE;
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
                                if (profileJsonObject.has("dob")) {
                                    mFriend.setDob(profileJsonObject.getString("dob"));
                                }
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
                    mProgressDialog.dismiss();
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
        mProgressDialog.show();
        if (mFriend.getStatus().equalsIgnoreCase(FriendStatus.UNFRIEND.toString())) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("userId", mUserId);
                jsonObject.put("friendId", mFriend.getUserId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String url = Utility.REST_URI + Utility.ADD_FRIEND;
            TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
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
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("userId", mUserId);
                jsonObject.put("friendId", mFriend.getUserId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String url = Utility.REST_URI + Utility.APPROVE_FRIEND;
            TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
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
        mProgressDialog.dismiss();
    }
}
