package com.frissbi.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.Utility;
import com.frissbi.models.Friend;
import com.frissbi.models.FrissbiContact;

import java.util.List;

/**
 * Created by thrymr on 27/2/17.
 */

public class GroupParticipantAdapter extends RecyclerView.Adapter<GroupParticipantAdapter.ViewHolder> {

    private Context mContext;
    private boolean mIsFromMeetingCreation;
    private List<FrissbiContact> mFrissbiContactList;

    public GroupParticipantAdapter(Context context, List<FrissbiContact> frissbiContactList) {
        mContext = context;
        mFrissbiContactList = frissbiContactList;
    }

    public GroupParticipantAdapter(Context context, List<FrissbiContact> frissbiContactList, boolean isFromMeetingCreation) {
        mContext = context;
        mFrissbiContactList = frissbiContactList;
        FLog.d("GroupParticipantAdapter", "mFrissbiContactList" + mFrissbiContactList);
        mIsFromMeetingCreation = isFromMeetingCreation;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.group_selected_friend_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FrissbiContact frissbiContact = mFrissbiContactList.get(position);
        if (mIsFromMeetingCreation) {
            holder.participantName.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            if (frissbiContact.getType() == 1) {
                holder.participantName.setText(frissbiContact.getName());
                //holder.participantIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.pic1));
                if (frissbiContact.getImageId() != null) {
                    holder.participantIcon.setImageBitmap(Utility.getInstance().getBitmapFromString(frissbiContact.getImageId()));
                } else {
                    holder.participantIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.pic1));
                }
            } else if (frissbiContact.getType() == 2) {
                holder.participantName.setText(frissbiContact.getEmailId());
                holder.participantIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.icon_email));
            } else if (frissbiContact.getType() == 3) {
                holder.participantName.setText(frissbiContact.getName());
                holder.participantIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.icon_phone));
            }
        } else {
            holder.participantName.setText(mFrissbiContactList.get(position).getName());
            if (frissbiContact.getImageId() != null) {
                holder.participantIcon.setImageBitmap(Utility.getInstance().getBitmapFromString(frissbiContact.getImageId()));
            } else {
                holder.participantIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.pic1));
            }
        }
    }

    @Override
    public int getItemCount() {
        /*if (mIsFromMeetingCreation) {
            return mFrissbiContactList.size();
        } else {
            return mFriendList.size();
        }*/
        return mFrissbiContactList.size();
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
