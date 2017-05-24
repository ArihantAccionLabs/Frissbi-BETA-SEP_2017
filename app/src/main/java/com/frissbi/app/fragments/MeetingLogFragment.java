package com.frissbi.app.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.frissbi.app.R;
import com.frissbi.app.Utility.ConnectionDetector;
import com.frissbi.app.Utility.CustomProgressDialog;
import com.frissbi.app.Utility.SharedPreferenceHandler;
import com.frissbi.app.enums.UserMeetingStatus;
import com.frissbi.app.Utility.Utility;
import com.frissbi.app.activities.MeetingDetailsActivity;
import com.frissbi.app.adapters.MeetingLogAdapter;
import com.frissbi.app.interfaces.MeetingDetailsListener;
import com.frissbi.app.models.Meeting;
import com.frissbi.app.models.MeetingFriends;
import com.frissbi.app.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MeetingLogFragment extends Fragment implements MeetingDetailsListener {


    private RecyclerView mMeetingLogRecyclerView;
    private String mUserId;
    private List<Meeting> mMeetingList;
    private ProgressDialog mProgressDialog;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MeetingLogAdapter mMeetingLogAdapter;
    private MeetingDetailsListener mMeetingDetailsListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_meeting_log, container, false);
        mMeetingLogRecyclerView = (RecyclerView) view.findViewById(R.id.meeting_log_recyclerView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        mProgressDialog = new CustomProgressDialog(getActivity());
        mMeetingDetailsListener = (MeetingDetailsListener) this;
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mMeetingLogRecyclerView.setLayoutManager(layoutManager);


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (ConnectionDetector.getInstance(getActivity()).isConnectedToInternet()) {
                    getMeetingFromServer();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void getMeetingFromServer() {
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mProgressDialog.show();
        }

        String url = Utility.REST_URI + Utility.MEETING_PENDINGLIST + SharedPreferenceHandler.getInstance(getActivity()).getUserId();
        TSNetworkHandler.getInstance(getActivity()).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            mMeetingList.clear();
                            if (mMeetingLogAdapter != null) {
                                mMeetingLogAdapter.notifyDataSetChanged();
                            }
                            JSONObject responseJsonObject = new JSONObject(response.response);
                            Log.d("MeetingLogFragment", "responseJsonObject" + responseJsonObject);
                            JSONArray meetingJsonArray = responseJsonObject.getJSONArray("meeting_array");

                            for (int i = 0; i < meetingJsonArray.length(); i++) {
                                Meeting meeting = new Meeting();
                                List<MeetingFriends> meetingFriendsList = new ArrayList<>();
                                JSONObject meetingJsonObject = meetingJsonArray.getJSONObject(i);
                                meeting.setMeetingId(meetingJsonObject.getLong("meetingId"));
                                meeting.setMeetingSenderId(meetingJsonObject.getLong("meetingSenderId"));
                                if (meetingJsonObject.getLong("meetingSenderId") != SharedPreferenceHandler.getInstance(getActivity()).getUserId()) {
                                    meeting.setMeetingStatus(meetingJsonObject.getInt("status"));
                                    meeting.setUserStatus(UserMeetingStatus.MEETING_RECEIVE.toString());
                                } else {
                                    meeting.setMeetingStatus(1);
                                    meeting.setUserStatus(UserMeetingStatus.MEETING_SENT.toString());
                                }
                                meeting.setDate(meetingJsonObject.getString("date"));
                                meeting.setFromTime(meetingJsonObject.getString("from"));
                                meeting.setToTime(meetingJsonObject.getString("to"));
                                meeting.setDescription(meetingJsonObject.getString("description"));
                                if (meetingJsonObject.getBoolean("isLocationSelected")) {
                                    meeting.setLocationSelected(meetingJsonObject.getBoolean("isLocationSelected"));
                                    meeting.setAddress(meetingJsonObject.getString("address"));
                                    meeting.setLatitude(meetingJsonObject.getDouble("latitude"));
                                    meeting.setLongitude(meetingJsonObject.getDouble("longitude"));
                                } else {
                                    meeting.setLocationSelected(meetingJsonObject.getBoolean("isLocationSelected"));
                                }

                                JSONArray friendsJsonArray = meetingJsonObject.getJSONArray("friendsJsonArray");
                                int friendsLength = friendsJsonArray.length();
                                if (friendsLength > 0) {
                                    for (int j = 0; j < friendsLength; j++) {
                                        MeetingFriends meetingFriends = new MeetingFriends();
                                        meetingFriends.setName(friendsJsonArray.getString(j));
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

                                meeting.setMeetingFriendsList(meetingFriendsList);

                                mMeetingList.add(meeting);
                            }
                            Log.d("MeetingLogFragment", "mMeetingList" + mMeetingList);
                            mMeetingLogAdapter = new MeetingLogAdapter(getActivity(), mMeetingList, mMeetingDetailsListener);
                            mMeetingLogRecyclerView.setAdapter(mMeetingLogAdapter);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
                    }
                }
                mProgressDialog.dismiss();
                mSwipeRefreshLayout.setRefreshing(false);

            }
        });

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (mMeetingList == null) {
                mMeetingList = new ArrayList<>();
            }
            if (mMeetingList.size() == 0) {
                if (ConnectionDetector.getInstance(getActivity()).isConnectedToInternet()) {
                    getMeetingFromServer();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
                }
            } else {
                mMeetingLogAdapter = new MeetingLogAdapter(getActivity(), mMeetingList, mMeetingDetailsListener);
                mMeetingLogRecyclerView.setAdapter(mMeetingLogAdapter);
            }
        }
    }

    @Override
    public void showMeetingDetails(Long meetingId) {
        if (!mSwipeRefreshLayout.isRefreshing()) {
            Intent intent = new Intent(getActivity(), MeetingDetailsActivity.class);
            intent.putExtra("meetingId", meetingId);
            startActivity(intent);
        }
    }
}
