package com.frissbi.app.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frissbi.app.R;
import com.frissbi.app.adapters.ContactsAdapter;
import com.frissbi.app.interfaces.ContactsSelectedListener;
import com.frissbi.app.models.FrissbiContact;
import com.orm.query.Select;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    private RecyclerView mContactsRecyclerView;
    List<FrissbiContact> mFrissbiContactList;
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
        mFrissbiContactList = Select.from(FrissbiContact.class).orderBy("type").list();
        //mFrissbiContactList = FrissbiContact.listAll(FrissbiContact.class);
        mContactsSelectedListener = (ContactsSelectedListener) getActivity();
        mContactsAdapter = new ContactsAdapter(getActivity(), mFrissbiContactList);
        mContactsRecyclerView.setAdapter(mContactsAdapter);
        return view;
    }

    public void filterContacts(String text) {
        mContactsAdapter.getFilter().filter(text);
    }

}
