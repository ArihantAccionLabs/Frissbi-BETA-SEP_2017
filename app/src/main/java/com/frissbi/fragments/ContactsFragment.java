package com.frissbi.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frissbi.R;
import com.frissbi.adapters.ContactsAdapter;
import com.frissbi.interfaces.ContactsSelectedListener;
import com.frissbi.models.Contacts;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {


    private RecyclerView mContactsRecyclerView;
    List<Contacts> mContactsList;
    private List<Long> mContactsSelectedIdsList;
    private ContactsSelectedListener mContactsSelectedListener;
    private ContactsAdapter mContactsAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        mContactsRecyclerView = (RecyclerView) view.findViewById(R.id.contacts_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mContactsRecyclerView.setLayoutManager(layoutManager);
        mContactsList = Contacts.listAll(Contacts.class);
        mContactsSelectedListener = (ContactsSelectedListener) getActivity();
        mContactsAdapter = new ContactsAdapter(getActivity(), mContactsList);
        mContactsRecyclerView.setAdapter(mContactsAdapter);
        return view;
    }

    public void filterContacts(String text) {
        mContactsAdapter.getFilter().filter(text);
    }

}
