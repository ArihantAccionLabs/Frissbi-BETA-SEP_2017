package com.frissbi.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.R;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.adapters.FriendsAdapter;
import com.frissbi.interfaces.ContactsSelectedListener;
import com.frissbi.models.Friends;
import com.frissbi.networkhandler.TSNetworkHandler;
import com.sun.mail.imap.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class FrissbiFriendsFragment extends Fragment {


    private RecyclerView mFrissbiFriendsRecyclerView;
    private SharedPreferences mSharedPreferences;
    private String mUserId;
    private String mUserName;
    private List<Friends> mFriendsList;
    private ProgressDialog mProgressDialog;
    private List<Long> mFriendsSelectedIdList;
    private ContactsSelectedListener mContactsSelectedListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_frissbi_friends, container, false);

        mFrissbiFriendsRecyclerView = (RecyclerView) view.findViewById(R.id.frissbi_friends_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mFrissbiFriendsRecyclerView.setLayoutManager(layoutManager);
        mFriendsList = Friends.listAll(Friends.class);
        mContactsSelectedListener= (ContactsSelectedListener) getActivity();
        FriendsAdapter friendsAdapter = new FriendsAdapter(getActivity(), mFriendsList);
        mFrissbiFriendsRecyclerView.setAdapter(friendsAdapter);
        return view;
    }

}
