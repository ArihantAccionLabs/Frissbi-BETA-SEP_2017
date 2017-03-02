package com.frissbi.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.Utility;
import com.frissbi.adapters.FriendsAdapter;
import com.frissbi.models.Friend;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyFriendsFragment extends Fragment {


    private List<Friend> mFriendList;
    private RecyclerView mFriendsRecyclerView;
    private FriendsAdapter mFriendsAdapter;
    private ProgressDialog mProgressDialog;
    private Long mUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_friends, container, false);
        mProgressDialog = new CustomProgressDialog(getActivity());
        mUserId = SharedPreferenceHandler.getInstance(getActivity()).getUserId();
        mFriendsRecyclerView = (RecyclerView) view.findViewById(R.id.my_friends_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mFriendsRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mFriendsRecyclerView.addItemDecoration(dividerItemDecoration);
        mFriendsRecyclerView.setLayoutManager(layoutManager);

        mFriendList = Friend.listAll(Friend.class);

        if (mFriendList.size() == 0) {
            if (ConnectionDetector.getInstance(getActivity()).isConnectedToInternet()) {
                getFriendsFromServer();
            } else {
                Toast.makeText(getActivity(), getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
            }
        } else {
            mFriendsAdapter = new FriendsAdapter(getActivity(), mFriendList);
            mFriendsRecyclerView.setAdapter(mFriendsAdapter);
        }
        return view;
    }


    private void getFriendsFromServer() {
        Friend.deleteAll(Friend.class);
        mFriendList.clear();
        mProgressDialog.show();
        String url = Utility.REST_URI + Utility.USER_FRIENDSLIST + mUserId;
        TSNetworkHandler.getInstance(getActivity()).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            JSONObject responseJsonObject = new JSONObject(response.response);
                            JSONArray friendsListJsonArray = responseJsonObject.getJSONArray("friends_array");
                            for (int index = 0; index < friendsListJsonArray.length(); index++) {
                                JSONObject friendJsonObject = friendsListJsonArray.getJSONObject(index);
                                Friend friend = new Friend();
                                friend.setUserId(friendJsonObject.getLong("userId"));
                                friend.setFullName(friendJsonObject.getString("fullName"));
                                friend.setEmailId(friendJsonObject.getString("email"));
                                friend.save();
                                mFriendList.add(friend);
                            }
                            mFriendsAdapter = new FriendsAdapter(getActivity(), mFriendList);
                            mFriendsRecyclerView.setAdapter(mFriendsAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Something went wrong at server end", Toast.LENGTH_SHORT).show();
                }
                mProgressDialog.dismiss();
            }
        });
    }


    public void filterFriends(String newText) {
        mFriendsAdapter.getFilter().filter(newText);
    }
}
