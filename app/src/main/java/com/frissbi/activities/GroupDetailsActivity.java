package com.frissbi.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.Utility;
import com.frissbi.adapters.ParticipantAdapter;
import com.frissbi.interfaces.GroupMemberDeleteListener;
import com.frissbi.models.FrissbiGroup;
import com.frissbi.models.Participant;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupDetailsActivity extends AppCompatActivity implements GroupMemberDeleteListener {

    private static final int ADD_PARTICIPANT = 100;
    private FrissbiGroup mFrissbiGroup;
    private List<Participant> mParticipantList;
    private RecyclerView mGroupParticipantRecyclerView;
    private ProgressDialog mProgressDialog;
    private ImageView mGroupDetailsIcon;
    private TextView mGroupDetailsTextView;
    private Button mAddParticipantButton;
    private GroupMemberDeleteListener mGroupMemberDeleteListener;
    private AlertDialog mAlertDialog;
    private Long mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle bundle = getIntent().getExtras();
        mUserId = SharedPreferenceHandler.getInstance(this).getUserId();
        mGroupMemberDeleteListener = (GroupMemberDeleteListener) this;

        mProgressDialog = new CustomProgressDialog(this);
        mGroupDetailsIcon = (ImageView) findViewById(R.id.group_details_icon);
        mGroupDetailsTextView = (TextView) findViewById(R.id.group_details_tv);
        mAddParticipantButton = (Button) findViewById(R.id.add_participant_button);
        mGroupParticipantRecyclerView = (RecyclerView) findViewById(R.id.group_participant_recyclerView);
        RecyclerView.LayoutManager selectLayoutManager = new LinearLayoutManager(this);
        mGroupParticipantRecyclerView.setLayoutManager(selectLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mGroupParticipantRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mGroupParticipantRecyclerView.addItemDecoration(dividerItemDecoration);


        mAddParticipantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(GroupDetailsActivity.this, AddParticipantActivity.class), ADD_PARTICIPANT);
            }
        });

        if (bundle != null) {
            if (bundle.containsKey("groupId")) {
                getGroupDetailsById(bundle.getLong("groupId"));
            } else if (bundle.containsKey("group")) {
                mFrissbiGroup = (FrissbiGroup) getIntent().getExtras().getSerializable("group");
                if (mFrissbiGroup.getAdminId().equals(SharedPreferenceHandler.getInstance(this).getUserId())) {
                    mAddParticipantButton.setVisibility(View.VISIBLE);
                } else {
                    mAddParticipantButton.setVisibility(View.GONE);
                }
                mParticipantList = Participant.findWithQuery(Participant.class, "select * from participant where group_id=?", mFrissbiGroup.getGroupId().toString());
                setValues();
            }
        }


    }

    private void setValues() {
        if (mFrissbiGroup.getImage() != null) {
            mGroupDetailsIcon.setImageBitmap(Utility.getInstance().getBitmapFromString(mFrissbiGroup.getImage()));
        }
        mGroupDetailsTextView.setText(mFrissbiGroup.getName());
        setUpParticipantList();
    }

    private void setUpParticipantList() {
        ParticipantAdapter participantAdapter = new ParticipantAdapter(this, mParticipantList, mGroupMemberDeleteListener);
        mGroupParticipantRecyclerView.setAdapter(participantAdapter);
    }


    private void getGroupDetailsById(Long groupId) {
        mProgressDialog.show();
        Participant.deleteAll(Participant.class, "group_id=?", groupId.toString());
        if (mParticipantList != null && mParticipantList.size() > 0) {
            mParticipantList.clear();
        } else {
            mParticipantList = new ArrayList<>();
        }
        String url = Utility.REST_URI + Utility.GET_GROUP_DETAILS + groupId;
        TSNetworkHandler.getInstance(this).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    try {
                        JSONObject groupJsonObject = new JSONObject(response.response);
                        mFrissbiGroup = new FrissbiGroup();
                        mFrissbiGroup.setGroupId(groupJsonObject.getLong("groupId"));
                        mFrissbiGroup.setName(groupJsonObject.getString("groupName"));
                        if (groupJsonObject.has("groupImage")) {
                            mFrissbiGroup.setImage(groupJsonObject.getString("groupImage"));
                        }
                        mFrissbiGroup.save();

                        Participant adminParticipant = new Participant();
                        adminParticipant.setGroupId(groupJsonObject.getLong("groupId"));
                        adminParticipant.setParticipantId(groupJsonObject.getLong("adminId"));
                        adminParticipant.setFullName(groupJsonObject.getString("fullName"));
                        if (groupJsonObject.has("adminImage")) {
                            adminParticipant.setImage(groupJsonObject.getString("adminImage"));
                        }
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
                            if (groupJsonObject.has("profileImage")) {
                                participant.setImage(participantJsonObject.getString("profileImage"));
                            }
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
                mProgressDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.group_meet:
                Intent intent = new Intent(GroupDetailsActivity.this, MeetingActivity.class);
                intent.putExtra("groupId", mFrissbiGroup.getGroupId());
                startActivity(intent);
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
        mProgressDialog.show();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", mUserId);
            jsonObject.put("friendId", friendId);
            jsonObject.put("groupId", mFrissbiGroup.getGroupId());
            String url = Utility.REST_URI + Utility.ADD_PARTICIPANT;
            TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {
                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            Toast.makeText(GroupDetailsActivity.this, response.message, Toast.LENGTH_SHORT).show();
                            getGroupDetailsById(mFrissbiGroup.getGroupId());

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

    @Override
    public void viewDeleteGroupMember(final Participant participant) {
        if (!participant.isAdmin() && mFrissbiGroup.getAdminId().equals(mUserId)) {
            showDialogForViewOrDelete(participant);
        }
    }


    private void showDialogForViewOrDelete(final Participant participant) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.group_alert_dialog, null);
        builder.setView(view);

        TextView viewGroupTextView = (TextView) view.findViewById(R.id.view_group_tv);
        TextView exitGroupTextView = (TextView) view.findViewById(R.id.exit_group_tv);
        viewGroupTextView.setText("View " + participant.getFullName());
        exitGroupTextView.setText("Remove " + participant.getFullName());
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        viewGroupTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
                Intent intent = new Intent(GroupDetailsActivity.this, ProfileActivity.class);
                intent.putExtra("friendUserId", participant.getParticipantId());
                startActivity(intent);
            }
        });

        exitGroupTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
                showDeleteParticipantAlert(participant);
            }
        });

    }

    private void showDeleteParticipantAlert(final Participant participant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert!");
        builder.setMessage("Do you want to delete " + participant.getFullName());

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAlertDialog.dismiss();
                sendDeleteGroupMember(participant);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAlertDialog.dismiss();

            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }


    private void sendDeleteGroupMember(Participant participant) {
        mProgressDialog.show();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", participant.getParticipantId());
            jsonObject.put("groupId", participant.getGroupId());

            TSNetworkHandler.getInstance(this).getResponse(Utility.REST_URI + Utility.EXIT_GROUP, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {
                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            Toast.makeText(GroupDetailsActivity.this, response.message, Toast.LENGTH_SHORT).show();
                            getGroupDetailsById(mFrissbiGroup.getGroupId());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_meet, menu);
        return true;

    }


}
