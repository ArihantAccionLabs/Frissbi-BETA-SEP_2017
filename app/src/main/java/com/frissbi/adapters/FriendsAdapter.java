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
import com.frissbi.Utility.ImageCacheHandler;
import com.frissbi.interfaces.FriendProfileListener;
import com.frissbi.interfaces.GroupParticipantListener;
import com.frissbi.models.FrissbiContact;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thrymr on 6/2/17.
 */

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> implements Filterable {

    private Context mContext;
    private List<FrissbiContact> mFrissbiContactList;
    private List<FrissbiContact> mOriginalFrissbiContactList;
    private FriendsFilter mFriendsFilter;
    private GroupParticipantListener mGroupParticipantListener;
    private FriendProfileListener mFriendProfileListener;
    private boolean mIsGroupList;
    private boolean mIsAddParticipantList;
    private boolean mIsFriendList;

    public FriendsAdapter(Context context, List<FrissbiContact> frissbiContactList, FriendProfileListener friendProfileListener) {
        mContext = context;
        mFrissbiContactList = frissbiContactList;
        mOriginalFrissbiContactList = frissbiContactList;
        mFriendProfileListener = friendProfileListener;
        mIsFriendList = true;
    }

    public FriendsAdapter(Context context, List<FrissbiContact> frissbiContactList, GroupParticipantListener groupParticipantListener) {
        mContext = context;
        mFrissbiContactList = frissbiContactList;
        mOriginalFrissbiContactList = frissbiContactList;
        mGroupParticipantListener = groupParticipantListener;
        mIsGroupList = true;
    }

    public FriendsAdapter(Context context, List<FrissbiContact> frissbiContactList, boolean isAddParticipant, GroupParticipantListener groupParticipantListener) {
        mContext = context;
        mFrissbiContactList = frissbiContactList;
        mOriginalFrissbiContactList = frissbiContactList;
        mGroupParticipantListener = groupParticipantListener;
        mIsAddParticipantList = isAddParticipant;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.friends_adapter_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final FrissbiContact frissbiContact = mFrissbiContactList.get(position);

        holder.friendUsernameTv.setText(frissbiContact.getName());
        if (frissbiContact.getImageId() != null) {
            ImageCacheHandler.getInstance(mContext).setImage(holder.friendProfileImage, frissbiContact.getImageId());
        }

        if (frissbiContact.isSelected()) {
            holder.selectedIcon.setVisibility(View.VISIBLE);
        } else {
            holder.selectedIcon.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsGroupList) {
                    if (frissbiContact.isSelected()) {
                        frissbiContact.setSelected(false);
                        holder.selectedIcon.setVisibility(View.GONE);
                    } else {
                        holder.selectedIcon.setVisibility(View.VISIBLE);
                        frissbiContact.setSelected(true);
                    }
                    mGroupParticipantListener.selectedGroupParticipant(frissbiContact);
                }

                if (mIsAddParticipantList) {
                    mGroupParticipantListener.selectedGroupParticipant(frissbiContact);
                }

                if (mIsFriendList) {
                    mFriendProfileListener.viewFriendProfile(frissbiContact.getUserId());
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return mFrissbiContactList.size();
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
                List<FrissbiContact> frissbiContactList = new ArrayList<>();
                for (int i = 0; i < mFrissbiContactList.size(); i++) {
                    if ((mFrissbiContactList.get(i).getName().toUpperCase())
                            .startsWith(constraint.toString().toUpperCase())) {
                        frissbiContactList.add(mFrissbiContactList.get(i));
                    }
                }
                results.count = frissbiContactList.size();
                results.values = frissbiContactList;
            } else {
                results.count = mOriginalFrissbiContactList.size();
                results.values = mOriginalFrissbiContactList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFrissbiContactList = (List<FrissbiContact>) results.values;
            notifyDataSetChanged();
        }
    }
}
