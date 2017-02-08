package com.frissbi.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.SelectedContacts;
import com.frissbi.Utility.FLog;
import com.frissbi.models.Friends;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thrymr on 18/1/17.
 */

public class FrissbiFriendsAdapter extends RecyclerView.Adapter<FrissbiFriendsAdapter.ViewHolder> implements Filterable {

    private Context mContext;
    private List<Friends> mFriendsList;
    private List<Long> mFriendsSelectedIdList;
    private SelectedContacts mSelectedContacts;
    private FriendsFilter mFriendsFilter;
    private List<Friends> mOriginalFriendsList;

    public FrissbiFriendsAdapter(Context context, List<Friends> friendsList) {
        mContext = context;
        mFriendsList = friendsList;
        mSelectedContacts = SelectedContacts.getInstance();
        mFriendsSelectedIdList = mSelectedContacts.getFriendsSelectedIdList();
        mOriginalFriendsList = friendsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.frissibi_friends_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.friendUsernameTv.setText(mFriendsList.get(position).getUserName());
        if (mFriendsSelectedIdList.size() > 0) {
            if (mFriendsSelectedIdList.contains(mFriendsList.get(position).getId())) {
                holder.friendCheckbox.setChecked(true);
            } else {
                holder.friendCheckbox.setChecked(false);
            }
        }
        holder.friendCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    mSelectedContacts.setFriendsSelectedId(mFriendsList.get(position).getId());
                } else {
                    mSelectedContacts.deleteFriendsSelectedId(mFriendsList.get(position).getId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFriendsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView friendUsernameTv;
        private CheckBox friendCheckbox;
        public ViewHolder(View itemView) {
            super(itemView);
            friendUsernameTv = (TextView) itemView.findViewById(R.id.friend_username);
            friendCheckbox = (CheckBox) itemView.findViewById(R.id.friend_checkbox);
        }

    }
    @Override
    public Filter getFilter() {
        if (mFriendsFilter == null) {
            mFriendsFilter = new FriendsFilter();
        }
        return mFriendsFilter;
    }

    private class FriendsFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            FLog.d("FriendsAdapter", "constraint" + constraint);
            if (constraint != null && constraint.length() > 0) {
                ArrayList<Friends> friendsArrayList = new ArrayList<Friends>();
                for (int i = 0; i < mFriendsList.size(); i++) {
                    if ((mFriendsList.get(i).getUserName().toUpperCase())
                            .startsWith(constraint.toString().toUpperCase())) {
                        friendsArrayList.add(mFriendsList.get(i));
                    }
                }
                results.count = friendsArrayList.size();
                results.values = friendsArrayList;
            } else {
                results.count = mOriginalFriendsList.size();
                results.values = mOriginalFriendsList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFriendsList = (List<Friends>) results.values;
            notifyDataSetChanged();
        }
    }
}
