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
import com.frissbi.Utility.FLog;
import com.frissbi.adapters.MyContactsAdapter;
import com.frissbi.models.Contacts;
import com.frissbi.models.EmailContacts;
import com.frissbi.models.MyContacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyContactsFragment extends Fragment {


    private List<EmailContacts> mEmailsList;
    private List<Contacts> mContactsList;
    private List<MyContacts> mMyContactsList;
    private RecyclerView mMyContactsRecyclerView;
    private MyContactsAdapter mMyContactsAdapter;

    public MyContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_contacts, container, false);
        mMyContactsRecyclerView = (RecyclerView) view.findViewById(R.id.my_contacts_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mMyContactsRecyclerView.setLayoutManager(layoutManager);
        mEmailsList = EmailContacts.listAll(EmailContacts.class);
        mContactsList = Contacts.listAll(Contacts.class);
        mMyContactsList = new ArrayList<>();
        addEmailsContacts();
        return view;
    }

    private void addEmailsContacts() {
        for (EmailContacts emailContacts : mEmailsList) {
            MyContacts myContacts = new MyContacts();
            myContacts.setName(emailContacts.getEmailId());
            mMyContactsList.add(myContacts);
        }

        FLog.d("MyContactsFragment", "mContactsList" + mContactsList);

        for (Contacts contacts : mContactsList) {
            MyContacts myContacts = new MyContacts();
            myContacts.setName(contacts.getName());
            myContacts.setNumber(contacts.getPhoneNumber());
            mMyContactsList.add(myContacts);
        }
        FLog.d("MyContactsFragment", "mMyContactsList" + mMyContactsList);
        Collections.sort(mMyContactsList);
        mMyContactsAdapter = new MyContactsAdapter(getActivity(), mMyContactsList);
        mMyContactsRecyclerView.setAdapter(mMyContactsAdapter);
    }

    public void filterContacts(String newText) {
        mMyContactsAdapter.getFilter().filter(newText);
    }
}
