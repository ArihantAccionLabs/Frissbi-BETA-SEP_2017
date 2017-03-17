package com.frissbi.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.Utility;
import com.frissbi.activities.MeetingDetailsActivity;
import com.frissbi.adapters.OthersActivitiesAdapter;
import com.frissbi.adapters.UpcomingMeetingAdapter;
import com.frissbi.adapters.UserActivitiesAdapter;
import com.frissbi.enums.ActivityType;
import com.frissbi.interfaces.MeetingDetailsListener;
import com.frissbi.interfaces.ViewImageListener;
import com.frissbi.models.Activities;
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
public class TimeLineFragment extends Fragment implements MeetingDetailsListener,ViewImageListener {


    private RecyclerView mUpcomingMeetingRecyclerView;
    private UpcomingMeetingAdapter mUpcomingMeetingAdapter;
    private MeetingDetailsListener mMeetingDetailsListener;
    private List<Meeting> mMeetingList;
    private RecyclerView mOthersActivitiesRecyclerView;
    private Long mUserId;
    private List<Activities> mActivitiesList;
    private ViewImageListener mViewImageListener;
    private OthersActivitiesAdapter mOthersActivitiesAdapter;
    private int mOtherActivityOffSetValue;
    private Button mViewMoreButton;

    public TimeLineFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_time_line, container, false);
        mMeetingDetailsListener = (MeetingDetailsListener) this;
        mViewImageListener = (ViewImageListener) this;
        mUserId = SharedPreferenceHandler.getInstance(getActivity()).getUserId();
        mActivitiesList = new ArrayList<>();
        mUpcomingMeetingRecyclerView = (RecyclerView) view.findViewById(R.id.upcoming_meeting_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mUpcomingMeetingRecyclerView.setLayoutManager(layoutManager);

        mOthersActivitiesRecyclerView = (RecyclerView) view.findViewById(R.id.others_activities_recyclerView);
        RecyclerView.LayoutManager othersLayoutManager = new LinearLayoutManager(getActivity());
        mOthersActivitiesRecyclerView.setLayoutManager(othersLayoutManager);
        mOthersActivitiesRecyclerView.setNestedScrollingEnabled(false);

        return view;
    }

    private void getUpcomingMeetingsFromServer() {
        mMeetingList.clear();
        String url = Utility.REST_URI + Utility.UPCOMING_MEETINGS + mUserId;
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

                            getOthersActivities();

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

    private void getOthersActivities() {
        TSNetworkHandler.getInstance(getActivity()).getResponse(Utility.REST_URI + Utility.FRIENDS_ACTIVITIES + mUserId, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            JSONObject resposeJsonObject = new JSONObject(response.response);
                            FLog.d("TimeLineFragment", "jsonObject" + resposeJsonObject);
                            JSONArray userActivityJsonArray = resposeJsonObject.getJSONArray("userActivityArray");

                            for (int i = 0; i < userActivityJsonArray.length(); i++) {
                                JSONObject userActivityJsonObject = userActivityJsonArray.getJSONObject(i);
                                Activities activities = new Activities();
                                activities.setDate(userActivityJsonObject.getString("date"));
                                activities.setUserProfileImageId(userActivityJsonObject.getString("userProfileImageId"));

                                if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.STATUS_TYPE.toString())) {
                                    activities.setStatusMessage(userActivityJsonObject.getString("status"));
                                    activities.setDate(userActivityJsonObject.getString("date"));
                                    activities.setType(ActivityType.valueOf(ActivityType.STATUS_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.MEETING_TYPE.toString())) {
                                    activities.setMeetingMessage(userActivityJsonObject.getString("meetingMessage"));
                                    activities.setMeetingId(userActivityJsonObject.getLong("meetingId"));
                                    activities.setType(ActivityType.valueOf(ActivityType.MEETING_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.PROFILE_TYPE.toString())) {
                                    activities.setProfileImageId(userActivityJsonObject.getString("profileImageId"));
                                    activities.setType(ActivityType.valueOf(ActivityType.PROFILE_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.COVER_TYPE.toString())) {
                                    activities.setCoverImageId(userActivityJsonObject.getString("coverImageId"));
                                    activities.setType(ActivityType.valueOf(ActivityType.COVER_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.UPLOAD_TYPE.toString())) {
                                    activities.setImageCaption(userActivityJsonObject.getString("imageDescription"));
                                    activities.setUploadedImageId(userActivityJsonObject.getString("imageId"));
                                    activities.setType(ActivityType.valueOf(ActivityType.UPLOAD_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.LOCATION_TYPE.toString())) {
                                    activities.setLocationAddress(userActivityJsonObject.getString("address"));
                                    activities.setType(ActivityType.valueOf(ActivityType.LOCATION_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.FREE_TIME_TYPE.toString())) {
                                    activities.setFreeTimeDate(userActivityJsonObject.getString("freeDate"));
                                    activities.setFreeTimeFromTime(userActivityJsonObject.getString("freeFromTime"));
                                    activities.setFreeTimeToTime(userActivityJsonObject.getString("freeToTime"));
                                    activities.setType(ActivityType.valueOf(ActivityType.FREE_TIME_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.JOIN_DATE_TYPE.toString())) {
                                    activities.setJoinedDate(userActivityJsonObject.getString("registrationDate"));
                                    activities.setType(ActivityType.valueOf(ActivityType.JOIN_DATE_TYPE.toString()).ordinal());
                                }
                                mActivitiesList.add(activities);
                            }

                            FLog.d("TimeLineFragment", "mActivitiesList" + mActivitiesList);

                            mOthersActivitiesAdapter = new OthersActivitiesAdapter(getActivity(), mActivitiesList, mMeetingDetailsListener, mViewImageListener);
                            mOthersActivitiesRecyclerView.setAdapter(mOthersActivitiesAdapter);

                            if (resposeJsonObject.getBoolean("isNextActivityExist")) {
                                mViewMoreButton.setVisibility(View.VISIBLE);
                                mOtherActivityOffSetValue++;
                            } else {
                                mViewMoreButton.setVisibility(View.GONE);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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

    @Override
    public void viewImage(String imageId) {
        ViewImageDialogFragment viewImageDialogFragment = new ViewImageDialogFragment();
        viewImageDialogFragment.setImageId(imageId);
        viewImageDialogFragment.show(getActivity().getSupportFragmentManager(), "ViewImageDialogFragment");
    }
}

