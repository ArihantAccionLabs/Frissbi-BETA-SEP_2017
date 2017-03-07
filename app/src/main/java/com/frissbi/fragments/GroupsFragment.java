package com.frissbi.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frissbi.R;
import com.frissbi.adapters.GroupsAdapter;
import com.frissbi.models.FrissbiGroup;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

private List<FrissbiGroup> mGroupList;
    private RecyclerView mGroupsRecyclerView;
    private GroupsAdapter mGroupsAdapter;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        mGroupsRecyclerView = (RecyclerView) view.findViewById(R.id.groups_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mGroupsRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mGroupsRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mGroupsRecyclerView.addItemDecoration(dividerItemDecoration);
        mGroupList = FrissbiGroup.listAll(FrissbiGroup.class);
        mGroupsAdapter = new GroupsAdapter(getActivity(), mGroupList,true);
        mGroupsRecyclerView.setAdapter(mGroupsAdapter);
        return view;
    }

    public void filterGroups(String text) {
        mGroupsAdapter.getFilter().filter(text);
    }

}
