package com.frissbi.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Utility.FriendStatus;
import com.frissbi.Utility.Utility;
import com.frissbi.activities.ProfileActivity;
import com.frissbi.adapters.PeopleAdapter;
import com.frissbi.interfaces.FriendRequestListener;
import com.frissbi.models.Friend;
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
public class PeopleFragment extends Fragment implements FriendRequestListener {


    private RecyclerView mPeopleRecyclerView;
    private TextView mSearchPeopleTextView;
    private SharedPreferences mSharedPreferences;
    private String mUserId;
    private List<Friend> mFriendList;
    private FriendRequestListener mFriendRequestListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_people, container, false);
        mSharedPreferences = getActivity().getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("USERID_FROM", "editor");
        mFriendList = new ArrayList<>();
        mFriendRequestListener = (FriendRequestListener) this;
        mPeopleRecyclerView = (RecyclerView) view.findViewById(R.id.people_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mPeopleRecyclerView.setLayoutManager(layoutManager);
        mSearchPeopleTextView = (TextView) view.findViewById(R.id.search_people_tv);
        return view;
    }

    public void searchFriends(String query) {
        searchForPeopleInServer(query);
    }

    private void searchForPeopleInServer(String query) {
        mFriendList.clear();
        String url = Utility.REST_URI + Utility.PEOPLE_SEARCH + mUserId + "/" + query;
        TSNetworkHandler.getInstance(getActivity()).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {

                        try {
                            JSONObject responseJsonObject = new JSONObject(response.response);

                            JSONArray peopleJsonArray = responseJsonObject.getJSONArray("unfriendList");

                            for (int i = 0; i < peopleJsonArray.length(); i++) {
                                Friend friend = new Friend();
                                JSONObject peopleJsonObject = peopleJsonArray.getJSONObject(i);
                                friend.setUserId(peopleJsonObject.getLong("userId"));
                                friend.setFullName(peopleJsonObject.getString("fullName"));
                                friend.setEmailId(peopleJsonObject.getString("emailId"));
                                friend.setStatus(peopleJsonObject.getString("status"));
                                mFriendList.add(friend);
                            }

                            if (mFriendList.size() > 0) {
                                mSearchPeopleTextView.setVisibility(View.GONE);
                                PeopleAdapter peopleAdapter = new PeopleAdapter(getActivity(), mFriendList, mFriendRequestListener);
                                mPeopleRecyclerView.setAdapter(peopleAdapter);
                            } else {
                                mSearchPeopleTextView.setVisibility(View.VISIBLE);
                                Toast.makeText(getActivity(), "No people found with search..", Toast.LENGTH_SHORT).show();
                            }

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
    public void sendFriendRequest(Friend friend) {
        if (friend.getStatus().equalsIgnoreCase(FriendStatus.UNFRIEND.toString())) {
            String url = Utility.REST_URI + Utility.ADD_FRIEND + mUserId + "/" + friend.getUserId();
            TSNetworkHandler.getInstance(getActivity()).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {

                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(getActivity(), "Something went wrong at server side", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }else  if (friend.getStatus().equalsIgnoreCase(FriendStatus.CONFIRM.toString())){



        }

    }

    @Override
    public void viewProfile(Friend friend) {

        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra("friend", friend);
        startActivity(intent);

    }
}
