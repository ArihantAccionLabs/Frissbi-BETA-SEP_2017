package com.frissbi.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.activities.CreateGroupActivity;
import com.frissbi.models.Friend;

import java.util.List;

/**
 * Created by thrymr on 27/2/17.
 */

public class GroupParticipantAdapter extends RecyclerView.Adapter<GroupParticipantAdapter.ViewHolder> {

    private Context mContext;
    private List<Friend> mFriendList;

    public GroupParticipantAdapter(Context context, List<Friend> friendList) {
        mContext = context;
        mFriendList = friendList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.group_selected_friend_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.participantName.setText(mFriendList.get(position).getFullName());

    }

    @Override
    public int getItemCount() {
        return mFriendList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView participantIcon;
        private final TextView participantName;

        public ViewHolder(View itemView) {
            super(itemView);
            participantIcon = (ImageView) itemView.findViewById(R.id.participant_icon);
            participantName = (TextView) itemView.findViewById(R.id.participant_name);
        }
    }
}
