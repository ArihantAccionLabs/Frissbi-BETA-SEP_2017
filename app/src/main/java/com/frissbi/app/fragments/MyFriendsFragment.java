package com.frissbi.app.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.frissbi.app.R;
import com.frissbi.app.Utility.FLog;
import com.frissbi.app.Utility.SharedPreferenceHandler;
import com.frissbi.app.Utility.Utility;
import com.frissbi.app.activities.ProfileActivity;
import com.frissbi.app.adapters.FriendsAdapter;
import com.frissbi.app.interfaces.FriendProfileListener;
import com.frissbi.app.models.FrissbiContact;
import com.frissbi.app.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyFriendsFragment extends Fragment implements FriendProfileListener, SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView mFriendsRecyclerView;
    private FriendsAdapter mFriendsAdapter;
    private List<FrissbiContact> mFrissbiContactList;
    private FriendProfileListener mFriendProfileListener;
    private SwipeRefreshLayout mFriendsSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_friends, container, false);
        mFriendProfileListener = (FriendProfileListener) this;
        mFriendsRecyclerView = (RecyclerView) view.findViewById(R.id.my_friends_recyclerView);
        mFriendsSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.friends_swipeRefreshLayout);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mFriendsRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mFriendsRecyclerView.addItemDecoration(dividerItemDecoration);
        mFriendsRecyclerView.setLayoutManager(layoutManager);

        mFrissbiContactList = FrissbiContact.findWithQuery(FrissbiContact.class, "select * from frissbi_contact where type=?", Utility.FRIEND_TYPE + "");
        FLog.d("MyFriendsFragment", "mFrissbiContactList" + mFrissbiContactList);
        mFriendsAdapter = new FriendsAdapter(getActivity(), mFrissbiContactList, mFriendProfileListener);
        mFriendsRecyclerView.setAdapter(mFriendsAdapter);
        mFriendsSwipeRefreshLayout.setOnRefreshListener(this);
        return view;
    }

    public void filterFriends(String newText) {
        mFriendsAdapter.getFilter().filter(newText);
    }

    @Override
    public void viewFriendProfile(Long userId) {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra("friendUserId", userId);
        intent.putExtra("isFriend", true);
        startActivity(intent);
    }


    private void getFriendsFromServer() {
        FrissbiContact.deleteAll(FrissbiContact.class);
        String url = Utility.REST_URI + Utility.USER_FRIENDSLIST + SharedPreferenceHandler.getInstance(getActivity()).getUserId();
        TSNetworkHandler.getInstance(getActivity()).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            JSONObject responseJsonObject = new JSONObject(response.response);
                            if (responseJsonObject.has("friends_array")) {
                                mFrissbiContactList.clear();
                                JSONArray friendsListJsonArray = responseJsonObject.getJSONArray("friends_array");
                                for (int index = 0; index < friendsListJsonArray.length(); index++) {
                                    JSONObject friendJsonObject = friendsListJsonArray.getJSONObject(index);
                                    FrissbiContact frissbiContact = new FrissbiContact();
                                    frissbiContact.setUserId(friendJsonObject.getLong("userId"));
                                    frissbiContact.setName(friendJsonObject.getString("fullName"));
                                    frissbiContact.setEmailId(friendJsonObject.getString("emailId"));
                                    if (friendJsonObject.has("profileImageId")) {
                                        frissbiContact.setImageId(friendJsonObject.getString("profileImageId"));
                                    }
                                    if (friendJsonObject.has("phoneNumber")) {
                                        frissbiContact.setPhoneNumber(friendJsonObject.getString("phoneNumber"));
                                    }
                                    frissbiContact.setType(Utility.FRIEND_TYPE);
                                    frissbiContact.save();
                                    mFrissbiContactList.add(frissbiContact);
                                }
                                mFriendsAdapter = new FriendsAdapter(getActivity(), mFrissbiContactList, mFriendProfileListener);
                                mFriendsRecyclerView.setAdapter(mFriendsAdapter);
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
                mFriendsSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    @Override
    public void onRefresh() {
        getFriendsFromServer();
    }
}
