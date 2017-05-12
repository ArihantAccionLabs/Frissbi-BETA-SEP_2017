package com.frissbi.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.Utility.EndlessScrollView;
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.Utility;
import com.frissbi.activities.FriendsListActivity;
import com.frissbi.activities.MeetingDetailsActivity;
import com.frissbi.adapters.OthersActivitiesAdapter;
import com.frissbi.adapters.UpcomingMeetingAdapter;
import com.frissbi.enums.ActivityType;
import com.frissbi.interfaces.EndlessScrollListener;
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
public class TimeLineFragment extends Fragment implements MeetingDetailsListener, ViewImageListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, EndlessScrollListener {


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
    private FloatingActionButton mTimelineScrollTopFloatingButton;
    // private Button mViewMoreActivitiesButton;
    private EndlessScrollView mTimelineEndlessScrollView;
    private ProgressDialog mProgressDialog;
    private SwipeRefreshLayout mTimelineSwipeRefreshLayout;
    private RelativeLayout mNofriendsLayout;
    private boolean mIsNextActivityExist;


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
        mProgressDialog = new CustomProgressDialog(getActivity());
        mMeetingList = new ArrayList<>();
        mUserId = SharedPreferenceHandler.getInstance(getActivity()).getUserId();
        mActivitiesList = new ArrayList<>();
        mUpcomingMeetingRecyclerView = (RecyclerView) view.findViewById(R.id.upcoming_meeting_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mUpcomingMeetingRecyclerView.setLayoutManager(layoutManager);

        mOthersActivitiesRecyclerView = (RecyclerView) view.findViewById(R.id.others_activities_recyclerView);
        RecyclerView.LayoutManager othersLayoutManager = new LinearLayoutManager(getActivity());
        mOthersActivitiesRecyclerView.setLayoutManager(othersLayoutManager);
        mOthersActivitiesRecyclerView.setNestedScrollingEnabled(false);
        // mViewMoreActivitiesButton = (Button) view.findViewById(R.id.view_more_activities_button);
        mTimelineScrollTopFloatingButton = (FloatingActionButton) view.findViewById(R.id.timeline_scroll_top_floating_button);
        mTimelineEndlessScrollView = (EndlessScrollView) view.findViewById(R.id.timeline_nestedScrollView);
        mTimelineSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.timeline_swipeRefreshLayout);
        mNofriendsLayout = (RelativeLayout) view.findViewById(R.id.no_friends_rl);
        mTimelineSwipeRefreshLayout.setOnRefreshListener(this);
        // mViewMoreActivitiesButton.setOnClickListener(this);
        mTimelineScrollTopFloatingButton.setOnClickListener(this);
        mTimelineEndlessScrollView.setScrollViewListener(this);
        view.findViewById(R.id.make_friends_button).setOnClickListener(this);
        getUpcomingMeetingsFromServer();
        return view;
    }

    private void getUpcomingMeetingsFromServer() {
        if (!mTimelineSwipeRefreshLayout.isRefreshing()) {
            mProgressDialog.show();
        }
        String url = Utility.REST_URI + Utility.UPCOMING_MEETINGS + mUserId;
        TSNetworkHandler.getInstance(getActivity()).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            mMeetingList.clear();
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
                                    if (meetingJsonObject.has("address")) {
                                        meeting.setAddress(meetingJsonObject.getString("address"));
                                    }
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
                } else {
                    Toast.makeText(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
                mProgressDialog.dismiss();
            }

        });

    }

    private void getOthersActivities() {
        if (!mTimelineSwipeRefreshLayout.isRefreshing()) {
            mProgressDialog.show();
        }
        TSNetworkHandler.getInstance(getActivity()).getResponse(Utility.REST_URI + Utility.FRIENDS_ACTIVITIES + mUserId, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            mActivitiesList.clear();
                            JSONObject resposeJsonObject = new JSONObject(response.response);
                            FLog.d("TimeLineFragment", "jsonObject" + resposeJsonObject);

                            if (resposeJsonObject.has("userActivityArray")) {
                                mNofriendsLayout.setVisibility(View.GONE);
                                JSONArray userActivityJsonArray = resposeJsonObject.getJSONArray("userActivityArray");

                                for (int i = 0; i < userActivityJsonArray.length(); i++) {
                                    JSONObject userActivityJsonObject = userActivityJsonArray.getJSONObject(i);
                                    Activities activities = new Activities();
                                    activities.setDate(userActivityJsonObject.getString("date"));
                                    if (userActivityJsonObject.has("userProfileImageId")) {
                                        activities.setUserProfileImageId(userActivityJsonObject.getString("userProfileImageId"));
                                    }
                                    activities.setUserName(userActivityJsonObject.getString("userFullName"));

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

                                mOthersActivitiesAdapter = new OthersActivitiesAdapter(getActivity(), mActivitiesList, mViewImageListener);
                                mOthersActivitiesRecyclerView.setAdapter(mOthersActivitiesAdapter);

                                if (resposeJsonObject.getBoolean("isNextActivityExist")) {
                                    //mViewMoreActivitiesButton.setVisibility(View.VISIBLE);
                                    mIsNextActivityExist = true;
                                    mOtherActivityOffSetValue++;
                                } else {
                                    mIsNextActivityExist = false;
                                    // mViewMoreActivitiesButton.setVisibility(View.GONE);
                                }
                            } else {
                                mNofriendsLayout.setVisibility(View.VISIBLE);
                                mTimelineScrollTopFloatingButton.setVisibility(View.GONE);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                }
                mProgressDialog.dismiss();
                mTimelineSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    private void getMoreOtherActivities() {
        TSNetworkHandler.getInstance(getActivity()).getResponse(Utility.REST_URI + Utility.FRIENDS_ACTIVITIES + mUserId + "/" + mOtherActivityOffSetValue, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            JSONObject resposeJsonObject = new JSONObject(response.response);
                            FLog.d("ProfileActivity", "jsonObject" + resposeJsonObject);
                            JSONArray userActivityJsonArray = resposeJsonObject.getJSONArray("userActivityArray");
                            List<Activities> activitiesList = new ArrayList<Activities>();

                            for (int i = 0; i < userActivityJsonArray.length(); i++) {
                                JSONObject userActivityJsonObject = userActivityJsonArray.getJSONObject(i);
                                Activities activities = new Activities();
                                activities.setDate(userActivityJsonObject.getString("date"));
                                if (userActivityJsonObject.has("userProfileImageId")) {
                                    activities.setUserProfileImageId(userActivityJsonObject.getString("userProfileImageId"));
                                }
                                activities.setUserProfileImageId(userActivityJsonObject.getString("userFullName"));
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
                                activitiesList.add(activities);
                            }

                            FLog.d("TimeLineFragment", "MORE=====activitiesList" + activitiesList);
                            mOthersActivitiesAdapter.setMoreActivities(activitiesList);

                            if (resposeJsonObject.getBoolean("isNextActivityExist")) {
                                mIsNextActivityExist = true;
                                //  mViewMoreActivitiesButton.setVisibility(View.VISIBLE);
                                mOtherActivityOffSetValue++;
                            } else {
                                mIsNextActivityExist = false;
                                // mViewMoreActivitiesButton.setVisibility(View.GONE);
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
    public void showMeetingDetails(Long meetingId) {
        Intent intent = new Intent(getActivity(), MeetingDetailsActivity.class);
        intent.putExtra("meetingId", meetingId);
        startActivity(intent);
    }

    @Override
    public void viewImage(String imageId) {
       /* ViewImageDialogFragment viewImageDialogFragment = new ViewImageDialogFragment();
        viewImageDialogFragment.setImageId(imageId);
        viewImageDialogFragment.show(getActivity().getSupportFragmentManager(), "ViewImageDialogFragment");*/
        Utility.getInstance().setImageDialog(getActivity(), imageId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_more_activities_button:
                // getMoreOtherActivities();
                break;
            case R.id.timeline_scroll_top_floating_button:
                mTimelineEndlessScrollView.fullScroll(ScrollView.FOCUS_UP);
                break;
            case R.id.make_friends_button:
                Intent intent = new Intent(getActivity(), FriendsListActivity.class);
                intent.putExtra("callFrom", "makeFriends");
                startActivity(intent);
                break;

        }
    }

    @Override
    public void onRefresh() {
        getUpcomingMeetingsFromServer();
    }


    @Override
    public void onScrollChanged(EndlessScrollView scrollView, int x, int y, int oldx, int oldy) {
        // We take the last son in the scrollview
        View view = scrollView.getChildAt(scrollView.getChildCount() - 1);

        if (y == 0) {
            mTimelineSwipeRefreshLayout.setEnabled(true);
            mTimelineScrollTopFloatingButton.setVisibility(View.GONE);
        } else {
            mTimelineSwipeRefreshLayout.setEnabled(false);
            mTimelineScrollTopFloatingButton.setVisibility(View.VISIBLE);
        }

        int distanceToEnd = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
        // if diff is zero, then the bottom has been reached
        if (distanceToEnd == 0) {
            // do stuff your load more stuff
            if (mIsNextActivityExist) {
                getMoreOtherActivities();
            }
        }
    }
}

