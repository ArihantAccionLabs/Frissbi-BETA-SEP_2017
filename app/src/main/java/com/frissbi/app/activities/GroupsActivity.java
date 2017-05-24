package com.frissbi.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.app.R;
import com.frissbi.app.Utility.CustomProgressDialog;
import com.frissbi.app.Utility.SharedPreferenceHandler;
import com.frissbi.app.Utility.Utility;
import com.frissbi.app.adapters.GroupsAdapter;
import com.frissbi.app.interfaces.GroupDetailsListener;
import com.frissbi.app.models.FrissbiGroup;
import com.frissbi.app.models.Participant;
import com.frissbi.app.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupsActivity extends AppCompatActivity implements GroupDetailsListener, SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView mGroupsRecyclerView;
    private List<FrissbiGroup> mFrissbiGroupList;
    private ProgressDialog mProgressDialog;
    private GroupDetailsListener mGroupDetailsListener;
    private GroupsAdapter mGroupsAdapter;
    private Long mUserId;
    private SwipeRefreshLayout mGroupsSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFrissbiGroupList = new ArrayList<>();
        mProgressDialog = new CustomProgressDialog(this);
        mUserId = SharedPreferenceHandler.getInstance(GroupsActivity.this).getUserId();
        mGroupsRecyclerView = (RecyclerView) findViewById(R.id.groups_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mGroupsRecyclerView.setLayoutManager(layoutManager);
        mGroupDetailsListener = (GroupDetailsListener) this;
        FloatingActionButton addGroupFloatingButton = (FloatingActionButton) findViewById(R.id.add_group_floating_button);
        mGroupsSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.groups_swipeRefreshLayout);
        mGroupsSwipeRefreshLayout.setOnRefreshListener(this);
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("isNewGroupCreated")) {
            getAllGroupsFromServer();
        } else {
            mFrissbiGroupList = FrissbiGroup.listAll(FrissbiGroup.class);
            mGroupsAdapter = new GroupsAdapter(GroupsActivity.this, mFrissbiGroupList, mGroupDetailsListener);
            mGroupsRecyclerView.setAdapter(mGroupsAdapter);
        }
        addGroupFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupsActivity.this, CreateGroupActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void getAllGroupsFromServer() {
        if (!mGroupsSwipeRefreshLayout.isRefreshing()) {
            mProgressDialog.show();
        }

        String url = Utility.REST_URI + Utility.GROUPS + mUserId;

        TSNetworkHandler.getInstance(this).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            FrissbiGroup.deleteAll(FrissbiGroup.class);
                            Participant.deleteAll(Participant.class);
                            mFrissbiGroupList.clear();
                            if (mGroupsAdapter != null) {
                                mGroupsAdapter.notifyDataSetChanged();
                            }
                            JSONObject responseJsonObject = new JSONObject(response.response);
                            JSONArray groupJsonArray = responseJsonObject.getJSONArray("groupArray");
                            for (int i = 0; i < groupJsonArray.length(); i++) {
                                JSONObject groupJsonObject = groupJsonArray.getJSONObject(i);
                                FrissbiGroup frissbiGroup = new FrissbiGroup();
                                frissbiGroup.setGroupId(groupJsonObject.getLong("groupId"));
                                frissbiGroup.setName(groupJsonObject.getString("groupName"));
                                if (groupJsonObject.has("groupImage")) {
                                    frissbiGroup.setImage(groupJsonObject.getString("groupImage"));
                                }
                                frissbiGroup.setAdminId(groupJsonObject.getLong("adminId"));
                                frissbiGroup.save();

                                Participant adminParticipant = new Participant();
                                adminParticipant.setGroupId(groupJsonObject.getLong("groupId"));
                                adminParticipant.setParticipantId(groupJsonObject.getLong("adminId"));
                                adminParticipant.setFullName(groupJsonObject.getString("fullName"));
                                if (groupJsonObject.has("adminImage")) {
                                    adminParticipant.setImage(groupJsonObject.getString("adminImage"));
                                }
                                adminParticipant.setAdmin(true);
                                adminParticipant.save();

                                JSONArray participantJsonArray = groupJsonObject.getJSONArray("receiptionistArray");
                                for (int j = 0; j < participantJsonArray.length(); j++) {
                                    Participant participant = new Participant();
                                    JSONObject participantJsonObject = participantJsonArray.getJSONObject(j);
                                    participant.setGroupId(groupJsonObject.getLong("groupId"));
                                    participant.setParticipantId(participantJsonObject.getLong("userId"));
                                    participant.setFullName(participantJsonObject.getString("fullName"));
                                    if (participantJsonObject.has("profileImageId")) {
                                        participant.setImage(participantJsonObject.getString("profileImageId"));
                                    }
                                    participant.save();
                                }

                                mFrissbiGroupList.add(frissbiGroup);
                            }
                            mGroupsAdapter = new GroupsAdapter(GroupsActivity.this, mFrissbiGroupList, mGroupDetailsListener);
                            mGroupsRecyclerView.setAdapter(mGroupsAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(GroupsActivity.this, response.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(GroupsActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
                mProgressDialog.dismiss();
                mGroupsSwipeRefreshLayout.setRefreshing(false);
            }

        });

    }

    @Override
    public void showGroupDetails(FrissbiGroup group) {

        Intent intent = new Intent(GroupsActivity.this, GroupDetailsActivity.class);
        intent.putExtra("group", group);
        startActivity(intent);

    }

    @Override
    public void viewOrExitGroup(FrissbiGroup group) {
        showDialogForViewOrExit(group);
    }

    private void showDialogForViewOrExit(final FrissbiGroup group) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.group_alert_dialog, null);
        builder.setView(view);

        TextView viewGroupTextView = (TextView) view.findViewById(R.id.view_group_tv);
        TextView exitGroupTextView = (TextView) view.findViewById(R.id.exit_group_tv);
        TextView deleteGroupTextView = (TextView) view.findViewById(R.id.delete_group_tv);
        RelativeLayout deleteGroupLayout = (RelativeLayout) view.findViewById(R.id.delete_group_rl);
        if (group.getAdminId().equals(mUserId)) {
            deleteGroupLayout.setVisibility(View.VISIBLE);
        } else {
            deleteGroupLayout.setVisibility(View.GONE);
        }
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        viewGroupTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
                Intent intent = new Intent(GroupsActivity.this, GroupDetailsActivity.class);
                intent.putExtra("group", group);
                startActivity(intent);
            }
        });

        exitGroupTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
                if (group.getAdminId().equals(mUserId)) {
                    sendExitOrDeleteGroupDetailsByAdmin(group, false);
                } else {
                    sendExitGroupDetails(group);
                }
            }
        });

        deleteGroupTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
                sendExitOrDeleteGroupDetailsByAdmin(group, true);
            }
        });

    }

    private void sendExitOrDeleteGroupDetailsByAdmin(FrissbiGroup group, boolean isGroupDeletion) {
        mProgressDialog.show();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", mUserId);
            jsonObject.put("groupId", group.getGroupId());
            jsonObject.put("isGroupDeletion", isGroupDeletion);
            TSNetworkHandler.getInstance(this).getResponse(Utility.REST_URI + Utility.UPDATE_OR_DELETE_BY_GROUP, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {
                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            Toast.makeText(GroupsActivity.this, response.message, Toast.LENGTH_SHORT).show();
                            getAllGroupsFromServer();

                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(GroupsActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(GroupsActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                    mProgressDialog.dismiss();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendExitGroupDetails(FrissbiGroup group) {
        mProgressDialog.show();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", mUserId);
            jsonObject.put("groupId", group.getGroupId());

            TSNetworkHandler.getInstance(this).getResponse(Utility.REST_URI + Utility.EXIT_GROUP, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {
                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            Toast.makeText(GroupsActivity.this, response.message, Toast.LENGTH_SHORT).show();
                            getAllGroupsFromServer();

                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(GroupsActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(GroupsActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                    mProgressDialog.dismiss();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
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
    public void onRefresh() {
        getAllGroupsFromServer();
    }
}
