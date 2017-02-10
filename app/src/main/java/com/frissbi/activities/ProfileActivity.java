package com.frissbi.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Utility.FriendStatus;
import com.frissbi.Utility.Utility;
import com.frissbi.models.Friend;
import com.frissbi.networkhandler.TSNetworkHandler;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mProfileUserImageView;
    private TextView mProfileUserNameTextView;
    private Button mAddFriendButton;
    private TextView mProfileUserEmail;
    private Friend mFriend;
    private SharedPreferences mSharedPreferences;
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("friend")) {
            mFriend = (Friend) bundle.getSerializable("friend");
        }
        mSharedPreferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("USERID_FROM", "editor");
        mProfileUserImageView = (ImageView) findViewById(R.id.profile_user_image);
        mProfileUserNameTextView = (TextView) findViewById(R.id.profile_user_name);
        mAddFriendButton = (Button) findViewById(R.id.add_friend_button);
        mProfileUserEmail = (TextView) findViewById(R.id.profile_user_email);

        mProfileUserNameTextView.setText(mFriend.getFullName());
        mProfileUserEmail.setText(mFriend.getEmailId());

        if (mFriend.getStatus().equalsIgnoreCase(FriendStatus.UNFRIEND.toString())) {
            mAddFriendButton.setText("Add");
        } else if (mFriend.getStatus().equalsIgnoreCase(FriendStatus.WAITING.toString())) {
            mAddFriendButton.setText("Req Sent");
        } else if (mFriend.getStatus().equalsIgnoreCase(FriendStatus.CONFIRM.toString())) {
            mAddFriendButton.setText("Accept");
        }


        mAddFriendButton.setOnClickListener(this);
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
                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(ProfileActivity.this, "Something went wrong at server side", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else if (mFriend.getStatus().equalsIgnoreCase(FriendStatus.CONFIRM.toString())) {

        }
    }


}
