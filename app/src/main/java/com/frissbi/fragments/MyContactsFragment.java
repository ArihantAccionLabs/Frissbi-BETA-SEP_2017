package com.frissbi.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frissbi.R;
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.Utility;
import com.frissbi.adapters.MyContactsAdapter;
import com.frissbi.models.FrissbiContact;
import com.frissbi.models.MyContacts;
import com.orm.query.Select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyContactsFragment extends Fragment {

    private List<FrissbiContact> mFrissbiContactList;
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
        mFrissbiContactList = FrissbiContact.findWithQuery(FrissbiContact.class, "select * from frissbi_contact where type=?", Utility.EMAIL_TYPE + "");
        mFrissbiContactList.addAll(FrissbiContact.findWithQuery(FrissbiContact.class, "select * from frissbi_contact where type=?", Utility.CONTACT_TYPE + ""));
        Collections.sort(mFrissbiContactList);
        mMyContactsAdapter = new MyContactsAdapter(getActivity(), mFrissbiContactList);
        mMyContactsRecyclerView.setAdapter(mMyContactsAdapter);
        return view;
    }
    public void filterContacts(String newText) {
        mMyContactsAdapter.getFilter().filter(newText);
    }
}
