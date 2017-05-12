package com.frissbi.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.Utility;
import com.frissbi.adapters.MyContactsAdapter;
import com.frissbi.interfaces.InviteFriendListener;
import com.frissbi.models.FrissbiContact;
import com.frissbi.models.MyContacts;
import com.frissbi.networkhandler.TSNetworkHandler;
import com.orm.query.Select;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyContactsFragment extends Fragment implements InviteFriendListener {

    private List<FrissbiContact> mFrissbiContactList;
    private RecyclerView mMyContactsRecyclerView;
    private MyContactsAdapter mMyContactsAdapter;
    private InviteFriendListener mInviteFriendListener;

    public MyContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_contacts, container, false);
        mInviteFriendListener = (InviteFriendListener) this;
        mMyContactsRecyclerView = (RecyclerView) view.findViewById(R.id.my_contacts_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mMyContactsRecyclerView.setLayoutManager(layoutManager);
        mFrissbiContactList = FrissbiContact.findWithQuery(FrissbiContact.class, "select * from frissbi_contact where type=?", Utility.EMAIL_TYPE + "");
        mFrissbiContactList.addAll(FrissbiContact.findWithQuery(FrissbiContact.class, "select * from frissbi_contact where type=?", Utility.CONTACT_TYPE + ""));
        Collections.sort(mFrissbiContactList);
        mMyContactsAdapter = new MyContactsAdapter(getActivity(), mFrissbiContactList, mInviteFriendListener);
        mMyContactsRecyclerView.setAdapter(mMyContactsAdapter);
        return view;
    }

    public void filterContacts(String newText) {
        mMyContactsAdapter.getFilter().filter(newText);
    }

    @Override
    public void inviteFriend(FrissbiContact frissbiContact) {
        FLog.d("MyContactsFragment", "frissbiContact" + frissbiContact);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", SharedPreferenceHandler.getInstance(getActivity()).getUserId());
            if (frissbiContact.getType() == Utility.EMAIL_TYPE) {
                jsonObject.put("emailId", frissbiContact.getEmailId());
            } else {
                jsonObject.put("phoneNumber", frissbiContact.getPhoneNumber());
            }

            TSNetworkHandler.getInstance(getActivity()).getResponse(Utility.REST_URI + Utility.INVITE_CONTACTS, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {
                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
