package com.frissbi.fragments;


import android.content.Intent;
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
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.Utility;
import com.frissbi.activities.ProfileActivity;
import com.frissbi.adapters.PeopleAdapter;
import com.frissbi.adapters.PeopleMayKnowAdapter;
import com.frissbi.enums.FriendStatus;
import com.frissbi.interfaces.FriendRequestListener;
import com.frissbi.models.Friend;
import com.frissbi.models.FrissbiContact;
import com.frissbi.networkhandler.TSNetworkHandler;
import com.orm.query.Select;

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
    private Long mUserId;
    private List<FrissbiContact> mFrissbiContactList;
    private List<FrissbiContact> mMayKnowFrissbiContactList;
    private FriendRequestListener mFriendRequestListener;
    private RecyclerView mMayKnowRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_people, container, false);
        mUserId = SharedPreferenceHandler.getInstance(getActivity()).getUserId();
        mFrissbiContactList = new ArrayList<>();
        mFriendRequestListener = (FriendRequestListener) this;
        mMayKnowFrissbiContactList = new ArrayList<>();
        mMayKnowRecyclerView = (RecyclerView) view.findViewById(R.id.mayKnow_recyclerView);
        RecyclerView.LayoutManager mayKnowLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mMayKnowRecyclerView.setLayoutManager(mayKnowLayoutManager);
        mPeopleRecyclerView = (RecyclerView) view.findViewById(R.id.people_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mPeopleRecyclerView.setLayoutManager(layoutManager);
        mSearchPeopleTextView = (TextView) view.findViewById(R.id.search_people_tv);
        getPeopleMayKnowFromServer();
        return view;
    }

    private void getPeopleMayKnowFromServer() {

        TSNetworkHandler.getInstance(getActivity()).getResponse(Utility.REST_URI + Utility.PEOPLE_YOU_MAY_KNOW + mUserId, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            JSONObject responseJsonObject = new JSONObject(response.response);

                            JSONArray peopleJsonArray = responseJsonObject.getJSONArray("userJsonArray");

                            for (int i = 0; i < peopleJsonArray.length(); i++) {
                                JSONObject peopleJsonObject = peopleJsonArray.getJSONObject(i);
                                FrissbiContact frissbiContact = new FrissbiContact();
                                frissbiContact.setUserId(peopleJsonObject.getLong("userId"));
                                frissbiContact.setName(peopleJsonObject.getString("fullName"));
                                frissbiContact.setEmailId(peopleJsonObject.getString("emailId"));
                                if (peopleJsonObject.has("profileImageId")) {
                                    frissbiContact.setImageId(peopleJsonObject.getString("profileImageId"));
                                }
                                frissbiContact.setStatus(peopleJsonObject.getString("status"));
                                frissbiContact.setType(Utility.FRIEND_TYPE);
                                mMayKnowFrissbiContactList.add(frissbiContact);
                            }

                            PeopleMayKnowAdapter peopleMayKnowAdapter = new PeopleMayKnowAdapter(getActivity(), mMayKnowFrissbiContactList, mFriendRequestListener);
                            mMayKnowRecyclerView.setAdapter(peopleMayKnowAdapter);
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

    public void searchFriends(String query) {
        searchForPeopleInServer(query);
    }

    private void searchForPeopleInServer(String query) {
        mFrissbiContactList.clear();
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
                                JSONObject peopleJsonObject = peopleJsonArray.getJSONObject(i);
                                FrissbiContact frissbiContact = new FrissbiContact();
                                frissbiContact.setUserId(peopleJsonObject.getLong("userId"));
                                frissbiContact.setName(peopleJsonObject.getString("fullName"));
                                frissbiContact.setEmailId(peopleJsonObject.getString("emailId"));
                                if (peopleJsonObject.has("profileImageId")) {
                                    frissbiContact.setImageId(peopleJsonObject.getString("profileImageId"));
                                }
                                if (peopleJsonObject.has("phoneNumber")) {
                                    frissbiContact.setPhoneNumber(peopleJsonObject.getString("phoneNumber"));
                                }
                                frissbiContact.setStatus(peopleJsonObject.getString("status"));
                                frissbiContact.setType(Utility.FRIEND_TYPE);
                                mFrissbiContactList.add(frissbiContact);
                            }

                            FLog.d("mFrissbiContactList", "----" + mFrissbiContactList);

                            if (mFrissbiContactList.size() > 0) {
                                mSearchPeopleTextView.setVisibility(View.GONE);
                                PeopleAdapter peopleAdapter = new PeopleAdapter(getActivity(), mFrissbiContactList, mFriendRequestListener);
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
    public void sendFriendRequest(FrissbiContact frissbiContact) {
        if (frissbiContact.getStatus().equalsIgnoreCase(FriendStatus.UNFRIEND.toString())) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("userId", mUserId);
                jsonObject.put("friendId", frissbiContact.getUserId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String url = Utility.REST_URI + Utility.ADD_FRIEND;
            TSNetworkHandler.getInstance(getActivity()).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {

                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }

                }
            });
        } else if (frissbiContact.getStatus().equalsIgnoreCase(FriendStatus.CONFIRM.toString())) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("userId", mUserId);
                jsonObject.put("friendId", frissbiContact.getUserId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String url = Utility.REST_URI + Utility.APPROVE_FRIEND;
            TSNetworkHandler.getInstance(getActivity()).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {

                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }

    }

    @Override
    public void viewProfile(FrissbiContact frissbiContact) {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra("friendUserId", frissbiContact.getUserId());
        intent.putExtra("isFriend", false);
        intent.putExtra("status", frissbiContact.getStatus());
        startActivity(intent);

    }
}
