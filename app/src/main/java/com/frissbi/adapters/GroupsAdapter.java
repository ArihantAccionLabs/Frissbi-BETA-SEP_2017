package com.frissbi.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.models.SelectedContacts;
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.Utility;
import com.frissbi.interfaces.GroupDetailsListener;
import com.frissbi.models.FrissbiGroup;
import com.frissbi.models.Participant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thrymr on 2/3/17.
 */

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> implements Filterable {

    private  List<Long> mGroupsSelectedIdsList;
    private  SelectedContacts mSelectedContacts;
    private Context mContext;
    private List<FrissbiGroup> mFrissbiGroupList;
    private GroupDetailsListener mGroupDetailsListener;
    private boolean mIsSelectGroup;
    private GroupsFilter mGroupsFilter;
    private List<FrissbiGroup> mOriginalGroupsList;


    public GroupsAdapter(Context context, List<FrissbiGroup> frissbiGroupList, GroupDetailsListener groupDetailsListener) {
        mContext = context;
        mFrissbiGroupList = frissbiGroupList;
        mGroupDetailsListener = groupDetailsListener;
    }

    public GroupsAdapter(Context context, List<FrissbiGroup> groupList, boolean isSelectGroup) {
        mContext = context;
        mFrissbiGroupList = groupList;
        mIsSelectGroup = isSelectGroup;
        mOriginalGroupsList=groupList;
        mSelectedContacts = SelectedContacts.getInstance();
        mGroupsSelectedIdsList = mSelectedContacts.getGroupSelectedIdsList();

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.friends_adapter_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final FrissbiGroup frissbiGroup = mFrissbiGroupList.get(position);
        holder.groupNameTv.setText(frissbiGroup.getName());
        if (frissbiGroup.getImage() != null) {
            holder.groupImage.setImageBitmap(Utility.getInstance().getBitmapFromString(frissbiGroup.getImage()));
        }

        if (mIsSelectGroup) {
            if (mGroupsSelectedIdsList.contains(mFrissbiGroupList.get(position).getGroupId())) {
                holder.selectedGroupIcon.setVisibility(View.VISIBLE);
            } else {
                holder.selectedGroupIcon.setVisibility(View.GONE);
            }
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSelectGroup) {
                    if (holder.selectedGroupIcon.getVisibility() == View.VISIBLE) {
                        mSelectedContacts.deleteGroupSelectedId(frissbiGroup.getGroupId());
                        holder.selectedGroupIcon.setVisibility(View.GONE);
                        List<Participant> participantList = Participant.findWithQuery(Participant.class, "select * from participant where group_id=?", frissbiGroup.getGroupId().toString());
                        FLog.d("MeetingActivity", "participantList" + participantList);
                        for (int i = 0; i < participantList.size(); i++) {
                            Participant participant = participantList.get(i);
                            if (!participant.getParticipantId().equals(SharedPreferenceHandler.getInstance(mContext).getUserId())) {
                                mSelectedContacts.deleteGroupSelectedId(participant.getParticipantId());
                            }
                        }
                    } else {
                        mSelectedContacts.setGroupSelectedId(frissbiGroup.getGroupId());
                        holder.selectedGroupIcon.setVisibility(View.VISIBLE);
                        List<Participant> participantList = Participant.findWithQuery(Participant.class, "select * from participant where group_id=?", frissbiGroup.getGroupId().toString());
                        FLog.d("MeetingActivity", "participantList" + participantList);
                        for (int i = 0; i < participantList.size(); i++) {
                            Participant participant = participantList.get(i);
                            if (!participant.getParticipantId().equals(SharedPreferenceHandler.getInstance(mContext).getUserId())) {
                                mSelectedContacts.setFriendsSelectedId(participant.getParticipantId());
                            }
                        }
                    }
                }else {
                    mGroupDetailsListener.showGroupDetails(frissbiGroup);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!mIsSelectGroup) {
                    mGroupDetailsListener.viewOrExitGroup(frissbiGroup);
                }
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mFrissbiGroupList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView groupNameTv;
        private final ImageView groupImage;
        private final ImageView selectedGroupIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            groupNameTv = (TextView) itemView.findViewById(R.id.friend_username_tv);
            groupImage = (ImageView) itemView.findViewById(R.id.friend_profile_image);
            selectedGroupIcon=(ImageView)itemView.findViewById(R.id.selected_icon);
        }
    }

    @Override
    public Filter getFilter() {
        if ( mGroupsFilter== null) {
            mGroupsFilter = new GroupsFilter();
        }
        return mGroupsFilter;
    }

    private class GroupsFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                List<FrissbiGroup> frissbiGroupList = new ArrayList<>();
                for (int i = 0; i < mFrissbiGroupList.size(); i++) {
                    if ((mFrissbiGroupList.get(i).getName().toUpperCase())
                            .startsWith(constraint.toString().toUpperCase())) {
                        frissbiGroupList.add(mFrissbiGroupList.get(i));
                    }
                }
                results.count = frissbiGroupList.size();
                results.values = frissbiGroupList;
            } else {
                results.count = mOriginalGroupsList.size();
                results.values = mOriginalGroupsList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFrissbiGroupList = (List<FrissbiGroup>) results.values;
            notifyDataSetChanged();
        }
    }


}
