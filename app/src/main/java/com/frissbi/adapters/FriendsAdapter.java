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
import com.frissbi.activities.AddParticipantActivity;
import com.frissbi.interfaces.GroupParticipantListener;
import com.frissbi.models.Friend;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thrymr on 6/2/17.
 */

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> implements Filterable {

    private Context mContext;
    private List<Friend> mFriendList;
    private List<Friend> mOriginalFriendList;
    private FriendsFilter mFriendsFilter;
    private GroupParticipantListener mGroupParticipantListener;
    private boolean isGroupSelection;
    private boolean mIsAddParticipant;

    public FriendsAdapter(Context context, List<Friend> friendList) {
        mContext = context;
        mFriendList = friendList;
        mOriginalFriendList = friendList;
        isGroupSelection = false;
    }

    public FriendsAdapter(Context context, List<Friend> friendList, GroupParticipantListener groupParticipantListener) {
        mContext = context;
        mFriendList = friendList;
        mOriginalFriendList = friendList;
        mGroupParticipantListener = groupParticipantListener;
        isGroupSelection = true;
    }

    public FriendsAdapter(Context context, List<Friend> friendList, boolean isAddParticipant, GroupParticipantListener groupParticipantListener) {
        mContext = context;
        mFriendList = friendList;
        mOriginalFriendList = friendList;
        mGroupParticipantListener = groupParticipantListener;
        mIsAddParticipant = isAddParticipant;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.friends_adapter_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        Friend friend = mFriendList.get(position);

        holder.friendUsernameTv.setText(friend.getFullName());
        holder.friendProfileImage.setImageResource(R.drawable.pic1);

        if (friend.isSelected()) {
            holder.selectedIcon.setVisibility(View.VISIBLE);
        } else {
            holder.selectedIcon.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGroupSelection) {
                    if (mFriendList.get(position).isSelected()) {
                        mFriendList.get(position).setSelected(false);
                        holder.selectedIcon.setVisibility(View.GONE);
                    } else {
                        holder.selectedIcon.setVisibility(View.VISIBLE);
                        mFriendList.get(position).setSelected(true);
                    }
                    mGroupParticipantListener.selectedGroupParticipant(mFriendList.get(position));
                }

                if (mIsAddParticipant){
                    mGroupParticipantListener.selectedGroupParticipant(mFriendList.get(position));
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return mFriendList.size();
    }

    @Override
    public Filter getFilter() {
        if (mFriendsFilter == null) {
            mFriendsFilter = new FriendsFilter();
        }
        return mFriendsFilter;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView friendUsernameTv;
        private ImageView friendProfileImage;
        private ImageView selectedIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            friendProfileImage = (ImageView) itemView.findViewById(R.id.friend_profile_image);
            friendUsernameTv = (TextView) itemView.findViewById(R.id.friend_username_tv);
            selectedIcon = (ImageView) itemView.findViewById(R.id.selected_icon);
        }
    }

    private class FriendsFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                ArrayList<Friend> friendArrayList = new ArrayList<Friend>();
                for (int i = 0; i < mFriendList.size(); i++) {
                    if ((mFriendList.get(i).getFullName().toUpperCase())
                            .startsWith(constraint.toString().toUpperCase())) {
                        friendArrayList.add(mFriendList.get(i));
                    }
                }
                results.count = friendArrayList.size();
                results.values = friendArrayList;
            } else {
                results.count = mOriginalFriendList.size();
                results.values = mOriginalFriendList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFriendList = (List<Friend>) results.values;
            notifyDataSetChanged();
        }
    }
}
