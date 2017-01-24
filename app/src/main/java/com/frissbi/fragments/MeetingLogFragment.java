package com.frissbi.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.R;
import com.frissbi.networkhandler.TSNetworkHandler;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class MeetingLogFragment extends Fragment {


    private RecyclerView mMeetingLogRecyclerView;
    private SharedPreferences mSharedPreferences;
    private String mUserId;

    public MeetingLogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_meeting_log, container, false);
        mMeetingLogRecyclerView = (RecyclerView) view.findViewById(R.id.meeting_log_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mMeetingLogRecyclerView.setLayoutManager(layoutManager);
        mSharedPreferences = getActivity().getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("USERID_FROM", "editor");
        getMeetingFromServer();
        return view;
    }

    private void getMeetingFromServer() {
        String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.MEETING_PENDINGLIST + mUserId;
        TSNetworkHandler.getInstance(getActivity()).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {

            }
        });

    }

}
