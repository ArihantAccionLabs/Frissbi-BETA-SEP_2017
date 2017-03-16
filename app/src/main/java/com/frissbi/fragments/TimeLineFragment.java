package com.frissbi.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.Utility;
import com.frissbi.activities.MeetingDetailsActivity;
import com.frissbi.adapters.UpcomingMeetingAdapter;
import com.frissbi.interfaces.MeetingDetailsListener;
import com.frissbi.models.Meeting;
import com.frissbi.models.MeetingFriends;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TimeLineFragment extends Fragment implements MeetingDetailsListener{


    private RecyclerView mUpcomingMeetingRecyclerView;
    private UpcomingMeetingAdapter mUpcomingMeetingAdapter;
    private MeetingDetailsListener mMeetingDetailsListener;
    private List<Meeting> mMeetingList;

    public TimeLineFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_time_line, container, false);
        mMeetingDetailsListener=(MeetingDetailsListener) this;
        mUpcomingMeetingRecyclerView = (RecyclerView) view.findViewById(R.id.upcoming_meeting_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mUpcomingMeetingRecyclerView.setLayoutManager(layoutManager);



        return view;
    }

    private void getUpcomingMeetingsFromServer() {
        mMeetingList.clear();
        String url = Utility.REST_URI + Utility.UPCOMING_MEETINGS + SharedPreferenceHandler.getInstance(getActivity()).getUserId();
        TSNetworkHandler.getInstance(getActivity()).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            JSONObject responseJsonObject = new JSONObject(response.response);
                            FLog.d("MeetingLogFragment", "responseJsonObject" + responseJsonObject);
                            JSONArray meetingJsonArray = responseJsonObject.getJSONArray("meetingArray");

                            for (int i = 0; i < meetingJsonArray.length(); i++) {
                                Meeting meeting = new Meeting();
                                List<MeetingFriends> meetingFriendsList = new ArrayList<>();
                                JSONObject meetingJsonObject = meetingJsonArray.getJSONObject(i);
                                meeting.setMeetingId(meetingJsonObject.getLong("meetingId"));
                                meeting.setMeetingSenderId(meetingJsonObject.getLong("meetingSenderId"));

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
                            FLog.d("TimeLineFragment", "mMeetingList" + mMeetingList);
                            mUpcomingMeetingAdapter = new UpcomingMeetingAdapter(getActivity(), mMeetingList, mMeetingDetailsListener);
                            mUpcomingMeetingRecyclerView.setAdapter(mUpcomingMeetingAdapter);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            mMeetingList = new ArrayList<>();
            getUpcomingMeetingsFromServer();
        }
    }


    @Override
    public void showMeetingDetails(Long meetingId) {
        Intent intent = new Intent(getActivity(), MeetingDetailsActivity.class);
        intent.putExtra("meetingId", meetingId);
        startActivity(intent);
    }
}

