package com.frissbi.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.Utility;
import com.frissbi.activities.ProfileActivity;
import com.frissbi.adapters.FriendsAdapter;
import com.frissbi.interfaces.FriendProfileListener;
import com.frissbi.models.Friend;
import com.frissbi.models.FrissbiContact;
import com.frissbi.models.Profile;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyFriendsFragment extends Fragment implements FriendProfileListener {
    private RecyclerView mFriendsRecyclerView;
    private FriendsAdapter mFriendsAdapter;
    private List<FrissbiContact> mFrissbiContactList;
    private FriendProfileListener mFriendProfileListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_friends, container, false);
        mFriendProfileListener = (FriendProfileListener) this;
        mFriendsRecyclerView = (RecyclerView) view.findViewById(R.id.my_friends_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mFriendsRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mFriendsRecyclerView.addItemDecoration(dividerItemDecoration);
        mFriendsRecyclerView.setLayoutManager(layoutManager);

        mFrissbiContactList = FrissbiContact.findWithQuery(FrissbiContact.class, "select * from frissbi_contact where type=?", Utility.FRIEND_TYPE + "");
        FLog.d("MyFriendsFragment", "mFrissbiContactList" + mFrissbiContactList);
        mFriendsAdapter = new FriendsAdapter(getActivity(), mFrissbiContactList, mFriendProfileListener);
        mFriendsRecyclerView.setAdapter(mFriendsAdapter);
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
}
