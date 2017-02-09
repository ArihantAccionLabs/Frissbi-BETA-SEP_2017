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
import com.frissbi.Utility.FLog;
import com.frissbi.models.Friends;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thrymr on 6/2/17.
 */

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> implements Filterable {

    private Context mContext;
    private List<Friends> mFriendsList;
    private List<Friends> mOriginalFriendsList;
    private FriendsFilter mFriendsFilter;

    public FriendsAdapter(Context context, List<Friends> friendsList) {
        mContext = context;
        mFriendsList = friendsList;
        mOriginalFriendsList = friendsList;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.friends_adapter_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.friendUsernameTv.setText(mFriendsList.get(position).getUserName());
        holder.friendProfileImage.setImageResource(R.drawable.pic1);
    }

    @Override
    public int getItemCount() {
        return mFriendsList.size();
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

        public ViewHolder(View itemView) {
            super(itemView);
            friendProfileImage = (ImageView) itemView.findViewById(R.id.friend_profile_image);
            friendUsernameTv = (TextView) itemView.findViewById(R.id.friend_username_tv);
        }
    }

    private class FriendsFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
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
