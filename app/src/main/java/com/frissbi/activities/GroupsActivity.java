package com.frissbi.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.Utility;
import com.frissbi.adapters.GroupsAdapter;
import com.frissbi.interfaces.GroupDetailsListener;
import com.frissbi.models.FrissbiGroup;
import com.frissbi.models.Participant;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupsActivity extends AppCompatActivity implements GroupDetailsListener {

    private RecyclerView mGroupsRecyclerView;
    private List<FrissbiGroup> mFrissbiGroupList;
    private ProgressDialog mProgressDialog;
    private GroupDetailsListener mGroupDetailsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        mFrissbiGroupList = new ArrayList<>();
        mProgressDialog = new CustomProgressDialog(this);
        mGroupsRecyclerView = (RecyclerView) findViewById(R.id.groups_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mGroupsRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mGroupsRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mGroupsRecyclerView.addItemDecoration(dividerItemDecoration);
        mGroupDetailsListener=(GroupDetailsListener)this;
        FloatingActionButton addGroupFloatingButton = (FloatingActionButton) findViewById(R.id.add_group_floating_button);

        getAllGroupsFromServer();

        addGroupFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupsActivity.this, CreateGroupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getAllGroupsFromServer() {
        FrissbiGroup.deleteAll(FrissbiGroup.class);
        Participant.deleteAll(Participant.class);
        mProgressDialog.show();
        String url = Utility.REST_URI + Utility.GROUPS + SharedPreferenceHandler.getInstance(this).getUserId();

        TSNetworkHandler.getInstance(this).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
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
                                frissbiGroup.save();

                                Participant adminParticipant = new Participant();
                                adminParticipant.setGroupId(groupJsonObject.getLong("groupId"));
                                adminParticipant.setParticipantId(groupJsonObject.getLong("adminId"));
                                adminParticipant.setFullName(groupJsonObject.getString("fullName"));
                                adminParticipant.setImage(groupJsonObject.getString("adminImage"));
                                adminParticipant.setAdmin(true);
                                adminParticipant.save();

                                JSONArray participantJsonArray = groupJsonObject.getJSONArray("receiptionistArray");
                                for (int j = 0; j < participantJsonArray.length(); j++) {
                                    Participant participant = new Participant();
                                    JSONObject participantJsonObject = participantJsonArray.getJSONObject(j);
                                    participant.setGroupId(groupJsonObject.getLong("groupId"));
                                    participant.setParticipantId(participantJsonObject.getLong("userId"));
                                    participant.setFullName(participantJsonObject.getString("fullName"));
                                    participant.setImage(participantJsonObject.getString("profileImage"));
                                    participant.save();
                                }

                                mFrissbiGroupList.add(frissbiGroup);
                            }
                            GroupsAdapter groupsAdapter = new GroupsAdapter(GroupsActivity.this, mFrissbiGroupList,mGroupDetailsListener);
                            mGroupsRecyclerView.setAdapter(groupsAdapter);
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
            }

        });

    }

    @Override
    public void showGroupDetails(FrissbiGroup group) {

        Intent intent=new Intent(GroupsActivity.this,GroupDetailsActivity.class);
        intent.putExtra("group", group);
        startActivity(intent);

    }
}
