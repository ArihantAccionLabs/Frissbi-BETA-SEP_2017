package com.frissbi.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.ImageCacheHandler;
import com.frissbi.enums.ActivityType;
import com.frissbi.interfaces.MeetingDetailsListener;
import com.frissbi.interfaces.ViewImageListener;
import com.frissbi.models.Activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by thrymr on 15/3/17.
 */

public class UserActivitiesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final SimpleDateFormat mDateFormat;
    private final SimpleDateFormat mDateTimeFormat;
    private final SimpleDateFormat mDateTime12HFormat;
    private Context mContext;
    private List<Activities> mActivitiesList;
    private Calendar mCalendar;
    private String mCurrentDay;
    private MeetingDetailsListener mMeetingDetailsListener;
    private ViewImageListener mViewImageListener;

    public UserActivitiesAdapter(Context context, List<Activities> activitiesList, MeetingDetailsListener meetingDetailsListener, ViewImageListener viewImageListener) {
        mContext = context;
        mActivitiesList = activitiesList;
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        mDateTime12HFormat = new SimpleDateFormat("dd MMM hh:mm a", Locale.ENGLISH);
        mCalendar = Calendar.getInstance(Locale.ENGLISH);
        mCurrentDay = mDateFormat.format(mCalendar.getTime());
        mMeetingDetailsListener = meetingDetailsListener;
        mViewImageListener = viewImageListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        if (viewType == ActivityType.valueOf(ActivityType.STATUS_TYPE.toString()).ordinal() || viewType == ActivityType.valueOf(ActivityType.JOIN_DATE_TYPE.toString()).ordinal()) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.status_message_join_activity_item, parent, false);
            viewHolder = new StatusJoinViewHolder(view);
        } else if (viewType == ActivityType.valueOf(ActivityType.MEETING_TYPE.toString()).ordinal()) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.meeting_activity_item, parent, false);
            viewHolder = new MeetingViewHolder(view);
        } else if (viewType == ActivityType.valueOf(ActivityType.PROFILE_TYPE.toString()).ordinal() || viewType == ActivityType.valueOf(ActivityType.UPLOAD_TYPE.toString()).ordinal()
                || viewType == ActivityType.valueOf(ActivityType.COVER_TYPE.toString()).ordinal()) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.profile_cover_upload_image_activity_item, parent, false);
            viewHolder = new ProfileCoverUploadViewHolder(view);
        } else if (viewType == ActivityType.valueOf(ActivityType.FREE_TIME_TYPE.toString()).ordinal()) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.free_time_activity_item, parent, false);
            viewHolder = new FreeTimeViewHolder(view);
        } else if (viewType == ActivityType.valueOf(ActivityType.LOCATION_TYPE.toString()).ordinal()) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.location_activity_item, parent, false);
            viewHolder = new LocationViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        FLog.d("UserActivitiesAdapter", "position" + position);
        final Activities activities = mActivitiesList.get(position);
        String postedDate = null;
        try {
            if (mDateFormat.parse(activities.getDate()).equals(mDateFormat.parse(mCurrentDay))) {

                Date time2 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(activities.getDate());
                Calendar calendar2 = Calendar.getInstance();
                calendar2.setTime(time2);
                long diff = (mCalendar.getTime().getTime() - calendar2.getTime().getTime());
               /* long diffSeconds = diff / 1000 % 60;
                long diffDays = diff / (24 * 60 * 60 * 1000);*/
                long diffMinutes = diff / (60 * 1000) % 60;
                long diffHours = diff / (60 * 60 * 1000) % 24;

                /*String minText = diffMinutes == 1 ? " minute" : " minutes";
                String hourText = diffHours == 1 ? " hour" : " hours";*/

                if (diffHours == 0) {
                    postedDate = diffMinutes + " min ago";
                } else {
                    postedDate = diffHours + " hr" + " " + diffMinutes + " min ago";
                }
            } else {
                Date date = mDateTimeFormat.parse(activities.getDate());
                postedDate = mDateTime12HFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


        if (holder instanceof StatusJoinViewHolder) {
            if (getItemViewType(position) == ActivityType.valueOf(ActivityType.STATUS_TYPE.toString()).ordinal()) {
                ((StatusJoinViewHolder) holder).statusMessageTv.setText(activities.getStatusMessage());
                ((StatusJoinViewHolder) holder).statusPostedTimeTv.setText(postedDate);

            } else if (getItemViewType(position) == ActivityType.valueOf(ActivityType.JOIN_DATE_TYPE.toString()).ordinal()) {
                ((StatusJoinViewHolder) holder).statusMessageTv.setText("You joined Frissbi on " + activities.getJoinedDate());
                ((StatusJoinViewHolder) holder).statusPostedTimeTv.setText(postedDate);
            }

        } else if (holder instanceof MeetingViewHolder) {
            ((MeetingViewHolder) holder).meetingMessageTv.setText(activities.getMeetingMessage());
            ((MeetingViewHolder) holder).meetingPostedTimeTv.setText(postedDate);

        } else if (holder instanceof ProfileCoverUploadViewHolder) {
            if (getItemViewType(position) == ActivityType.valueOf(ActivityType.PROFILE_TYPE.toString()).ordinal()) {
                ((ProfileCoverUploadViewHolder) holder).profileUpdateMessageTv.setText("Updated profile image");
                ((ProfileCoverUploadViewHolder) holder).profilePostedTimeTv.setText(postedDate);
                if (activities.getProfileImageId() != null) {
                    ImageCacheHandler.getInstance(mContext).setImage(((ProfileCoverUploadViewHolder) holder).activityProfileImageView, activities.getProfileImageId());
                }

            } else if (getItemViewType(position) == ActivityType.valueOf(ActivityType.COVER_TYPE.toString()).ordinal()) {
                ((ProfileCoverUploadViewHolder) holder).profileUpdateMessageTv.setText("Updated cover image");
                ((ProfileCoverUploadViewHolder) holder).profilePostedTimeTv.setText(postedDate);
                if (activities.getCoverImageId() != null) {
                    ImageCacheHandler.getInstance(mContext).setImage(((ProfileCoverUploadViewHolder) holder).activityProfileImageView, activities.getCoverImageId());
                }

            } else if (getItemViewType(position) == ActivityType.valueOf(ActivityType.UPLOAD_TYPE.toString()).ordinal()) {
                ((ProfileCoverUploadViewHolder) holder).profileUpdateMessageTv.setText(activities.getImageCaption());
                ((ProfileCoverUploadViewHolder) holder).profilePostedTimeTv.setText(postedDate);
                if (activities.getProfileImageId() != null) {
                    ImageCacheHandler.getInstance(mContext).setImage(((ProfileCoverUploadViewHolder) holder).activityProfileImageView, activities.getUploadedImageId());
                }
            }

        } else if (holder instanceof FreeTimeViewHolder) {
            ((FreeTimeViewHolder) holder).freeTimeDateTv.setText(activities.getFreeTimeDate());
            ((FreeTimeViewHolder) holder).freeTimeTv.setText(activities.getFreeTimeFromTime() + " to " + activities.getFreeTimeToTime());
            ((FreeTimeViewHolder) holder).freeTimePostedTv.setText(postedDate);
        } else if (holder instanceof LocationViewHolder) {
            ((LocationViewHolder) holder).checkInAddressTv.setText(activities.getLocationAddress());
            ((LocationViewHolder) holder).locationTimeTv.setText(postedDate);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getItemViewType(position) == ActivityType.valueOf(ActivityType.MEETING_TYPE.toString()).ordinal()) {
                    mMeetingDetailsListener.showMeetingDetails(activities.getMeetingId());
                } else if (getItemViewType(position) == ActivityType.valueOf(ActivityType.PROFILE_TYPE.toString()).ordinal()) {
                    mViewImageListener.viewImage(activities.getProfileImageId());
                } else if (getItemViewType(position) == ActivityType.valueOf(ActivityType.COVER_TYPE.toString()).ordinal()) {
                    mViewImageListener.viewImage(activities.getCoverImageId());
                }
            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        return mActivitiesList.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return mActivitiesList.size();
    }


    private class StatusJoinViewHolder extends RecyclerView.ViewHolder {
        private TextView statusPostedTimeTv;
        private TextView statusMessageTv;

        StatusJoinViewHolder(View itemView) {
            super(itemView);
            statusMessageTv = (TextView) itemView.findViewById(R.id.status_message_tv);
            statusPostedTimeTv = (TextView) itemView.findViewById(R.id.status_time_tv);
        }
    }

    private class MeetingViewHolder extends RecyclerView.ViewHolder {
        private TextView meetingMessageTv;
        private TextView meetingPostedTimeTv;

        MeetingViewHolder(View itemView) {
            super(itemView);
            meetingMessageTv = (TextView) itemView.findViewById(R.id.meeting_message_tv);
            meetingPostedTimeTv = (TextView) itemView.findViewById(R.id.meeting_posted_time_tv);
        }
    }

    private class ProfileCoverUploadViewHolder extends RecyclerView.ViewHolder {
        private TextView profilePostedTimeTv;
        private TextView profileUpdateMessageTv;
        private ImageView activityProfileImageView;

        ProfileCoverUploadViewHolder(View itemView) {
            super(itemView);
            activityProfileImageView = (ImageView) itemView.findViewById(R.id.activity_profile_imageView);
            profileUpdateMessageTv = (TextView) itemView.findViewById(R.id.profile_update_message_tv);
            profilePostedTimeTv = (TextView) itemView.findViewById(R.id.profile_time_tv);
        }
    }

    private class FreeTimeViewHolder extends RecyclerView.ViewHolder {

        private TextView freeTimeDateTv;
        private TextView freeTimeTv;
        private TextView freeTimePostedTv;

        FreeTimeViewHolder(View itemView) {
            super(itemView);
            freeTimeDateTv = (TextView) itemView.findViewById(R.id.free_time_date_tv);
            freeTimeTv = (TextView) itemView.findViewById(R.id.free_time_tv);
            freeTimePostedTv = (TextView) itemView.findViewById(R.id.free_time_posted_tv);
        }
    }

    private class LocationViewHolder extends RecyclerView.ViewHolder {
        private TextView checkInAddressTv;
        private TextView locationTimeTv;

        LocationViewHolder(View itemView) {
            super(itemView);
            checkInAddressTv = (TextView) itemView.findViewById(R.id.check_in_address_tv);
            locationTimeTv = (TextView) itemView.findViewById(R.id.location_time_tv);
        }
    }

    public void setMoreActivities(List<Activities> activitiesList) {
        mActivitiesList.addAll(activitiesList);
        notifyDataSetChanged();
    }


}
