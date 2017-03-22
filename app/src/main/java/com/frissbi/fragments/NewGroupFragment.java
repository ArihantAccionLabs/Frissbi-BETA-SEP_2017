package com.frissbi.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.frissbi.R;
import com.frissbi.Utility.Utility;
import com.frissbi.activities.CreateGroupActivity;
import com.frissbi.adapters.FriendsAdapter;
import com.frissbi.adapters.GroupParticipantAdapter;
import com.frissbi.interfaces.CurrentGroupFragmentListener;
import com.frissbi.interfaces.GroupParticipantListener;
import com.frissbi.models.FrissbiContact;

import java.util.ArrayList;
import java.util.List;

public class NewGroupFragment extends Fragment implements GroupParticipantListener {

    private RelativeLayout mParticipantRLayout;
    private RecyclerView mParticipantRecyclerView;
    private RecyclerView mSelectParticipantRecyclerView;
    private GroupParticipantListener mGroupParticipantListener;
    private List<FrissbiContact> mGroupSelectedFriendList;
    private GroupParticipantAdapter mGroupParticipantAdapter;
    private FriendsAdapter mFriendsAdapter;
    private OnFragmentInteractionListener mListener;
    private Button mCreateGroupButton;
    private List<FrissbiContact> mFrissbiContactList;
    private CurrentGroupFragmentListener mCurrentGroupFragmentListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_group, container, false);
        setUpViews(view);
        return view;
    }

    private void setUpViews(View view) {
        mGroupParticipantListener = (GroupParticipantListener) this;
        mGroupSelectedFriendList = new ArrayList<>();
        mCreateGroupButton = (Button) view.findViewById(R.id.create_group_button);
        mParticipantRLayout = (RelativeLayout) view.findViewById(R.id.participant_rl);
        mParticipantRecyclerView = (RecyclerView) view.findViewById(R.id.participant_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mParticipantRecyclerView.setLayoutManager(layoutManager);
        mSelectParticipantRecyclerView = (RecyclerView) view.findViewById(R.id.select_participant_recyclerView);
        RecyclerView.LayoutManager selectLayoutManager = new LinearLayoutManager(getActivity());
        mSelectParticipantRecyclerView.setLayoutManager(selectLayoutManager);
        mFrissbiContactList = FrissbiContact.findWithQuery(FrissbiContact.class, "select * from frissbi_contact where type=?", Utility.FRIEND_TYPE + "");
        setUpFriendsList();
        mCreateGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onFragmentInteraction(mGroupSelectedFriendList);
            }
        });
    }

    private void setUpFriendsList() {
        mFriendsAdapter = new FriendsAdapter(getActivity(), mFrissbiContactList, mGroupParticipantListener);
        mSelectParticipantRecyclerView.setAdapter(mFriendsAdapter);
    }

    @Override
    public void selectedGroupParticipant(FrissbiContact frissbiContact) {
        mParticipantRLayout.setVisibility(View.VISIBLE);
        if (frissbiContact.isSelected()) {
            mGroupSelectedFriendList.add(frissbiContact);
        } else {
            mGroupSelectedFriendList.remove(frissbiContact);
        }
        if (mGroupSelectedFriendList.size() == 0) {
            mParticipantRLayout.setVisibility(View.GONE);
            mCreateGroupButton.setVisibility(View.GONE);
        } else {
            mCreateGroupButton.setVisibility(View.VISIBLE);
        }
        setUpParticipantsList();
    }

    private void setUpParticipantsList() {
        mGroupParticipantAdapter = new GroupParticipantAdapter(getActivity(), mGroupSelectedFriendList);
        mParticipantRecyclerView.setAdapter(mGroupParticipantAdapter);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCurrentGroupFragmentListener = (CurrentGroupFragmentListener) context;
        mCurrentGroupFragmentListener.setCurrentFragment(CreateGroupActivity.NEW_GROUP);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setFilterText(String text) {
        mFriendsAdapter.getFilter().filter(text);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(List<FrissbiContact> groupSelectedFriendList);
    }


    @Override
    public void onResume() {
        super.onResume();

    }
}
