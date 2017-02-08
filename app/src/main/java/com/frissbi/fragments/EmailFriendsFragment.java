package com.frissbi.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frissbi.R;
import com.frissbi.adapters.EmailAdapter;
import com.frissbi.interfaces.ContactsSelectedListener;
import com.frissbi.models.EmailContacts;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class EmailFriendsFragment extends Fragment {


    private RecyclerView mGmailFriendsRecyclerView;
    private List<EmailContacts> mEmailContactsList;
    private ContactsSelectedListener mContactsSelectedListener;
    private List<Long> mEmailsSelectedIdsList;
    private EmailAdapter mEmailAdapter;

    public EmailFriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gmail_friends, container, false);
        mGmailFriendsRecyclerView = (RecyclerView) view.findViewById(R.id.gmail_friends_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mGmailFriendsRecyclerView.setLayoutManager(layoutManager);
        mEmailContactsList = EmailContacts.listAll(EmailContacts.class);
        mContactsSelectedListener = (ContactsSelectedListener) getActivity();
        mEmailAdapter = new EmailAdapter(getActivity(), mEmailContactsList);
        mGmailFriendsRecyclerView.setAdapter(mEmailAdapter);
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        Log.d("EmailFriendsFragment", "setUserVisibleHint" + isVisibleToUser);
    }

    public void filterEmails(String text) {
        mEmailAdapter.getFilter().filter(text);
    }
}
