package com.frissbi.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.SelectedContacts;
import com.frissbi.interfaces.ContactsSelectedListener;
import com.frissbi.models.Friends;

import java.util.List;

/**
 * Created by thrymr on 18/1/17.
 */

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private Context mContext;
    private List<Friends> mFriendsList;
    private List<Long> mFriendsSelectedIdList;
    private SelectedContacts mSelectedContacts;

    public FriendsAdapter(Context context, List<Friends> friendsList) {
        mContext = context;
        mFriendsList = friendsList;
        mSelectedContacts = SelectedContacts.getInstance();
        mFriendsSelectedIdList = mSelectedContacts.getFriendsSelectedIdList();
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
}
