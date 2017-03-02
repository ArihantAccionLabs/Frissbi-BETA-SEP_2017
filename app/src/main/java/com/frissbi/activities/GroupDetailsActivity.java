package com.frissbi.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.Frissbi_img_crop.Util;
import com.frissbi.R;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.Utility;
import com.frissbi.adapters.ParticipantAdapter;
import com.frissbi.models.FrissbiGroup;
import com.frissbi.models.Participant;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class GroupDetailsActivity extends AppCompatActivity {

    private static final int ADD_PARTICIPANT = 100;
    private FrissbiGroup mFrissbiGroup;
    private List<Participant> mParticipantList;
    private RecyclerView mGroupParticipantRecyclerView;
    private ProgressDialog mProgressDialog;
    private ImageView mGroupDetailsIcon;
    private TextView mGroupDetailsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFrissbiGroup = (FrissbiGroup) getIntent().getExtras().getSerializable("group");
        mParticipantList = Participant.findWithQuery(Participant.class, "select * from participant where group_id=?", mFrissbiGroup.getGroupId().toString());
        mProgressDialog = new CustomProgressDialog(this);
        mGroupDetailsIcon = (ImageView) findViewById(R.id.group_details_icon);
        mGroupDetailsTextView = (TextView) findViewById(R.id.group_details_tv);
        Button addParticipantButton = (Button) findViewById(R.id.add_participant_button);
        mGroupParticipantRecyclerView = (RecyclerView) findViewById(R.id.group_participant_recyclerView);
        RecyclerView.LayoutManager selectLayoutManager = new LinearLayoutManager(this);
        mGroupParticipantRecyclerView.setLayoutManager(selectLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mGroupParticipantRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mGroupParticipantRecyclerView.addItemDecoration(dividerItemDecoration);


        addParticipantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(GroupDetailsActivity.this, AddParticipantActivity.class), ADD_PARTICIPANT);
            }
        });

        setValues();

    }

    private void setValues() {
        mGroupDetailsIcon.setImageBitmap(Utility.getInstance().getBitmapFromString(mFrissbiGroup.getImage()));
        mGroupDetailsTextView.setText(mFrissbiGroup.getName());
        setUpParticipantList();
    }

    private void setUpParticipantList() {
        ParticipantAdapter participantAdapter = new ParticipantAdapter(this, mParticipantList);
        mGroupParticipantRecyclerView.setAdapter(participantAdapter);
    }


    private void getGroupDetailsById() {
        Participant.deleteAll(Participant.class, "group_id=?", mFrissbiGroup.getGroupId().toString());
        mParticipantList.clear();
        String url = Utility.REST_URI + Utility.GET_GROUP_DETAILS + mFrissbiGroup.getGroupId();
        TSNetworkHandler.getInstance(this).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    try {
                        JSONObject groupJsonObject = new JSONObject(response.response);
                        FrissbiGroup frissbiGroup = new FrissbiGroup();
                        frissbiGroup.setGroupId(groupJsonObject.getLong("groupId"));
                        frissbiGroup.setName(groupJsonObject.getString("groupName"));
                        if (groupJsonObject.has("groupImage")) {
                            frissbiGroup.setImage(groupJsonObject.getString("groupImage"));
                        }
                        frissbiGroup.save();

                        Participant adminParticipant = new Participant();
                        adminParticipant.setGroupId(groupJsonObject.getLong("groupId"));
                        adminParticipant.setParticipantId(groupJsonObject.getLong("adminId"));
                        adminParticipant.setFullName(groupJsonObject.getString("fullName"));
                        adminParticipant.setImage(groupJsonObject.getString("adminImage"));
                        adminParticipant.setAdmin(true);
                        adminParticipant.save();
                        mParticipantList.add(adminParticipant);
                        JSONArray participantJsonArray = groupJsonObject.getJSONArray("receiptionistArray");
                        for (int j = 0; j < participantJsonArray.length(); j++) {
                            Participant participant = new Participant();
                            JSONObject participantJsonObject = participantJsonArray.getJSONObject(j);
                            participant.setGroupId(groupJsonObject.getLong("groupId"));
                            participant.setParticipantId(participantJsonObject.getLong("userId"));
                            participant.setFullName(participantJsonObject.getString("fullName"));
                            participant.setImage(participantJsonObject.getString("profileImage"));
                            participant.save();
                            mParticipantList.add(participant);
                        }
                        setValues();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(GroupDetailsActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }

            }
        });
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_PARTICIPANT && resultCode == RESULT_OK) {
            mProgressDialog.show();
            Long friendId = data.getExtras().getLong("friendId");
            sendParticipantToServer(friendId);
        }
    }

    private void sendParticipantToServer(Long friendId) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", SharedPreferenceHandler.getInstance(this).getUserId());
            jsonObject.put("friendId", friendId);
            jsonObject.put("groupId", mFrissbiGroup.getGroupId());
            String url = Utility.REST_URI + Utility.ADD_PARTICIPANT;
            TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {
                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            try {
                                JSONObject responseJsonObject = new JSONObject(response.response);
                                Toast.makeText(GroupDetailsActivity.this, response.message, Toast.LENGTH_SHORT).show();
                                getGroupDetailsById();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(GroupDetailsActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(GroupDetailsActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                    mProgressDialog.dismiss();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
