package com.frissbi.fragments;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frissbi.R;
import com.frissbi.adapters.FrissbiFriendsAdapter;
import com.frissbi.interfaces.ContactsSelectedListener;
import com.frissbi.models.Friend;

import java.util.List;


public class FrissbiFriendsFragment extends Fragment {


    private RecyclerView mFrissbiFriendsRecyclerView;
    private SharedPreferences mSharedPreferences;
    private String mUserId;
    private String mUserName;
    private List<Friend> mFriendList;
    private ProgressDialog mProgressDialog;
    private List<Long> mFriendsSelectedIdList;
    private ContactsSelectedListener mContactsSelectedListener;
    private FrissbiFriendsAdapter mFrissbiFriendsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_frissbi_friends, container, false);

        mFrissbiFriendsRecyclerView = (RecyclerView) view.findViewById(R.id.frissbi_friends_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mFrissbiFriendsRecyclerView.setLayoutManager(layoutManager);
        mFriendList = Friend.listAll(Friend.class);
        mContactsSelectedListener = (ContactsSelectedListener) getActivity();
        mFrissbiFriendsAdapter = new FrissbiFriendsAdapter(getActivity(), mFriendList);
        mFrissbiFriendsRecyclerView.setAdapter(mFrissbiFriendsAdapter);
        return view;
    }


    public void filterFriends(String text) {
        mFrissbiFriendsAdapter.getFilter().filter(text);
    }


}
