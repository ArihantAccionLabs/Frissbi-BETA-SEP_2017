package com.frissbi.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.Utility;
import com.frissbi.activities.MeetingCalendarActivity;
import com.frissbi.interfaces.MeetingDetailsListener;
import com.frissbi.models.Meeting;
import com.frissbi.models.MeetingFriends;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by thrymr on 7/3/17.
 */

public class UpcomingMeetingAdapter extends RecyclerView.Adapter<UpcomingMeetingAdapter.ViewHolder> {

    private final SimpleDateFormat mDateFormat;
    private final Calendar mCalendar;
    private final String mCurrentDay;
    private Context mContext;
    private List<Meeting> mMeetingList;
    private MeetingDetailsListener mMeetingDetailsListener;
    private int counter;

    public UpcomingMeetingAdapter(Context context, List<Meeting> meetingList, MeetingDetailsListener meetingDetailsListener) {
        mContext = context;
        mMeetingList = meetingList;
        mMeetingDetailsListener = meetingDetailsListener;
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        mCalendar = Calendar.getInstance(Locale.ENGLISH);
        mCurrentDay = mDateFormat.format(mCalendar.getTime());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mMeetingList.size() > 0) {
            FLog.d("UpcomingMeetingAdapter", "counter" + counter);
            if (counter == mMeetingList.size()) {
                return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.view_more_meetings, parent, false));
            } else {
                return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.upcoming_meeting_item, parent, false));
            }
        } else {
            return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.view_more_meetings, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position != 0) {
            counter = position;
        }
        counter++;
        if (mMeetingList.size() > 0) {
            FLog.d("UpcomingMeetingAdapter", "position" + position);
            if (position == mMeetingList.size()) {
                holder.viewMoreTv.setText("View more");
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // mMeetingDetailsListener.showMeetingDetails(meeting);
                        Intent intent = new Intent(mContext, MeetingCalendarActivity.class);
                        mContext.startActivity(intent);
                    }
                });
            } else {
                int color = position % 2 == 0 ? R.color.brown : R.color.bg_color;
                holder.upcomingMeetingCardView.setCardBackgroundColor(ContextCompat.getColor(mContext, color));

                final Meeting meeting = mMeetingList.get(position);
                List<MeetingFriends> meetingFriendsList = meeting.getMeetingFriendsList();
                if (meetingFriendsList.size() != 0) {
                    if (meetingFriendsList.size() > 2) {
                        holder.meetingDescriptionTv.setText(meeting.getDescription() + " with " + meeting.getMeetingFriendsList().get(0).getName() + " and " + meetingFriendsList.size() + " others");
                    } else if (meetingFriendsList.size() == 2) {
                        holder.meetingDescriptionTv.setText(meeting.getDescription() + " with " + meeting.getMeetingFriendsList().get(0).getName() + " and " + meetingFriendsList.size() + " other");
                    } else {
                        holder.meetingDescriptionTv.setText(meeting.getDescription() + " with " + meeting.getMeetingFriendsList().get(0).getName());
                    }
                }

                try {
                    if (mDateFormat.parse(meeting.getDate()).equals(mDateFormat.parse(mCurrentDay))) {

                        Date time2 = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(meeting.getDate() + " " + meeting.getFromTime());
                        Calendar calendar2 = Calendar.getInstance();
                        calendar2.setTime(time2);
                        long diff = (calendar2.getTime().getTime() - mCalendar.getTime().getTime());
               /* long diffSeconds = diff / 1000 % 60;
                long diffDays = diff / (24 * 60 * 60 * 1000);*/
                        long diffMinutes = diff / (60 * 1000) % 60;
                        long diffHours = diff / (60 * 60 * 1000) % 24;

                        String minText = diffMinutes == 1 ? " minute" : " minutes";
                        String hourText = diffMinutes == 1 ? " hour" : " hours";


                        if (diffHours == 0) {
                            holder.upcomingMeetingTimeTv.setText(" in" + " " + diffMinutes + minText);
                        } else {
                            holder.upcomingMeetingTimeTv.setText(" in" + " " + diffHours + hourText + " " + diffMinutes + minText);
                        }


                    } else {
                        holder.upcomingMeetingTimeTv.setText("On " + meeting.getDate() + " at " + Utility.getInstance().convertTime(meeting.getFromTime()));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMeetingDetailsListener.showMeetingDetails(meeting.getMeetingId());
                    }
                });
            }
        } else {
            holder.viewMoreTv.setText("Click plus button to add meeting");
        }


    }

    @Override
    public int getItemCount() {
        if (mMeetingList.size() > 0) {
            return mMeetingList.size() + 1;
        } else {
            return 1;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView meetingDescriptionTv;
        private final TextView upcomingMeetingTimeTv;
        private final TextView viewMoreTv;
        private final CardView upcomingMeetingCardView;

        public ViewHolder(View itemView) {
            super(itemView);
            meetingDescriptionTv = (TextView) itemView.findViewById(R.id.meeting_description_tv);
            upcomingMeetingTimeTv = (TextView) itemView.findViewById(R.id.upcoming_meeting_time_tv);
            viewMoreTv = (TextView) itemView.findViewById(R.id.view_more_tv);
            upcomingMeetingCardView = (CardView) itemView.findViewById(R.id.upcoming_meeting_cardView);
        }
    }
}
