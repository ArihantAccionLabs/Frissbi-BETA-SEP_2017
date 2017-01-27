package com.frissbi.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
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

import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.R;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.adapters.MeetingLogAdapter;
import com.frissbi.models.Meeting;
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
public class MeetingLogFragment extends Fragment {


    private RecyclerView mMeetingLogRecyclerView;
    private SharedPreferences mSharedPreferences;
    private String mUserId;
    private List<Meeting> mMeetingList;
    private ProgressDialog mProgressDialog;
    private ConnectionDetector mConnectionDetector;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MeetingLogAdapter mMeetingLogAdapter;

    public MeetingLogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_meeting_log, container, false);
        mMeetingLogRecyclerView = (RecyclerView) view.findViewById(R.id.meeting_log_recyclerView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        mConnectionDetector = new ConnectionDetector(getActivity());
        mProgressDialog = new CustomProgressDialog(getActivity());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mMeetingLogRecyclerView.setLayoutManager(layoutManager);

        mSharedPreferences = getActivity().getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("USERID_FROM", "editor");



        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMeetingFromServer();
            }
        });


        return view;
    }

    private void getMeetingFromServer() {
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mProgressDialog.show();
        }
        mMeetingList.clear();
        String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.MEETING_PENDINGLIST + mUserId;
        TSNetworkHandler.getInstance(getActivity()).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            JSONObject responseJsonObject = new JSONObject(response.response);
                            JSONArray meetingJsonArray = responseJsonObject.getJSONArray("meeting_array");

                            for (int i = 0; i < meetingJsonArray.length(); i++) {
                                Meeting meeting = new Meeting();
                                JSONObject meetingJsonObject = meetingJsonArray.getJSONObject(i);
                                meeting.setMeetingId(meetingJsonObject.getLong("MeetingID"));
                                meeting.setDate(meetingJsonObject.getString("date"));
                                meeting.setSenderFirstName(meetingJsonObject.getString("FirstName"));
                                meeting.setSenderLastName(meetingJsonObject.getString("LastName"));
                                meeting.setFromTime(meetingJsonObject.getString("from"));
                                meeting.setToTime(meetingJsonObject.getString("to"));
                                meeting.setDescription(meetingJsonObject.getString("description"));
                                meeting.setMeetingStatus(meetingJsonObject.getInt("meetingStatus"));
                                meeting.setAddress(meetingJsonObject.getString("address"));
                                meeting.setLatitude(meetingJsonObject.getDouble("latitude"));
                                meeting.setLongitude(meetingJsonObject.getDouble("longitude"));
                                mMeetingList.add(meeting);
                            }
                            mMeetingLogAdapter = new MeetingLogAdapter(getActivity(), mMeetingList);
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
            Log.d("MeetingLogFragment", "size" + mMeetingList.size());
            if (mMeetingList.size() == 0) {
                getMeetingFromServer();
            }else {
                mMeetingLogAdapter = new MeetingLogAdapter(getActivity(), mMeetingList);
                mMeetingLogRecyclerView.setAdapter(mMeetingLogAdapter);
            }
        }
    }
}
