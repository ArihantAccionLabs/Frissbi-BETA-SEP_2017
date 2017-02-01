package com.frissbi.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.models.MeetingFriends;

import java.util.List;


/**
 * Created by thrymr on 30/1/17.
 */

public class MeetingFriendsAdapter extends RecyclerView.Adapter<MeetingFriendsAdapter.ViewHolder> {

    private Context mContext;
    private List<MeetingFriends> mMeetingFriendsList;

    public MeetingFriendsAdapter(Context context, List<MeetingFriends> meetingFriendsList) {
        mContext = context;
        mMeetingFriendsList = meetingFriendsList;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.friends_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.usernameTv.setText(mMeetingFriendsList.get(position).getName());
        if (mMeetingFriendsList.get(position).getType().equalsIgnoreCase("friend")) {
            holder.profileImageView.setImageResource(R.drawable.pic1);

        } else if (mMeetingFriendsList.get(position).getType().equalsIgnoreCase("email")) {
            holder.profileImageView.setImageResource(R.drawable.email_icon);
        } else if (mMeetingFriendsList.get(position).getType().equalsIgnoreCase("contact")) {
            holder.profileImageView.setImageResource(R.drawable.phone_icon);
        }
    }

    @Override
    public int getItemCount() {
        return mMeetingFriendsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView profileImageView;
        private TextView usernameTv;

        public ViewHolder(View itemView) {
            super(itemView);
            profileImageView = (ImageView) itemView.findViewById(R.id.profile_image);
            usernameTv = (TextView) itemView.findViewById(R.id.username_tv);
        }
    }
}
