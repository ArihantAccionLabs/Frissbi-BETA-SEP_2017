package com.frissbi.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.Utility.Utility;
import com.frissbi.activities.MeetingDetailsActivity;
import com.frissbi.models.Meeting;
import com.frissbi.models.MeetingFriends;

import java.util.List;

/**
 * Created by thrymr on 24/1/17.
 */

public class MeetingLogAdapter extends RecyclerView.Adapter<MeetingLogAdapter.ViewHolder> {
    private Context mContext;
    private List<Meeting> mMeetingsList;

    public MeetingLogAdapter(Context context, List<Meeting> meetingList) {
        mContext = context;
        mMeetingsList = meetingList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.meeting_log_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        List<MeetingFriends> meetingFriendsList = mMeetingsList.get(position).getMeetingFriendsList();
        if (meetingFriendsList.size() > 1) {
            holder.meetingLogDescriptionTv.setText(mMeetingsList.get(position).getDescription() + " with " + mMeetingsList.get(position).getMeetingFriendsList().get(0).getName() + " and " + meetingFriendsList.size() + " others");
        } else {
            holder.meetingLogDescriptionTv.setText(mMeetingsList.get(position).getDescription() + " with " + mMeetingsList.get(position).getMeetingFriendsList().get(0).getName());
        }
        holder.meetingLogDateTv.setText("Date : " + mMeetingsList.get(position).getDate());
        String time = "Time : " + Utility.getInstance().convertTime(mMeetingsList.get(position).getFromTime()) + " to " + Utility.getInstance().convertTime(mMeetingsList.get(position).getToTime());
        holder.meetingLogTimeTv.setText(time);
        if (mMeetingsList.get(position).getMeetingStatus() == Utility.STATUS_PENDING) {
            holder.meetingCardView.setBackgroundColor(mContext.getResources().getColor(R.color.light_orange));
        } else if (mMeetingsList.get(position).getMeetingStatus() == Utility.STATUS_ACCEPT) {
            holder.meetingCardView.setBackgroundColor(mContext.getResources().getColor(R.color.green));
        } else if (mMeetingsList.get(position).getMeetingStatus() == Utility.STATUS_REJECT) {
            holder.meetingCardView.setBackgroundColor(mContext.getResources().getColor(R.color.red));
        } else if (mMeetingsList.get(position).getMeetingStatus() == Utility.STATUS_ACCEPT) {
            holder.meetingCardView.setBackgroundColor(mContext.getResources().getColor(R.color.blue));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MeetingDetailsActivity.class);
                intent.putExtra("meeting", mMeetingsList.get(position));
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mMeetingsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CardView meetingCardView;
        private TextView meetingLogTimeTv;
        private TextView meetingLogDescriptionTv;
        private TextView meetingLogDateTv;

        public ViewHolder(View itemView) {
            super(itemView);
            meetingLogDescriptionTv = (TextView) itemView.findViewById(R.id.meeting_log_description_tv);
            meetingLogDateTv = (TextView) itemView.findViewById(R.id.meeting_log_date_tv);
            meetingLogTimeTv = (TextView) itemView.findViewById(R.id.meeting_log_time_tv);
            meetingCardView = (CardView) itemView.findViewById(R.id.meeting_cardView);
        }
    }
}
