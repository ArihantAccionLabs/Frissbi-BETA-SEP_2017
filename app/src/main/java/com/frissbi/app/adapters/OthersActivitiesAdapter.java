package com.frissbi.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frissbi.app.R;
import com.frissbi.app.Utility.FLog;
import com.frissbi.app.Utility.ImageCacheHandler;
import com.frissbi.app.Utility.Utility;
import com.frissbi.app.enums.ActivityType;
import com.frissbi.app.fragments.TimeLineFragment;
import com.frissbi.app.interfaces.ShowLocationOnMapListener;
import com.frissbi.app.interfaces.ViewImageListener;
import com.frissbi.app.models.Activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by thrymr on 17/3/17.
 */

public class OthersActivitiesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final SimpleDateFormat mDateFormat;
    private final SimpleDateFormat mDateTimeFormat;
    private final SimpleDateFormat mDateTime12HFormat;
    private final SimpleDateFormat mDateMonthFormat;
    private Context mContext;
    private List<Activities> mActivitiesList;
    private Calendar mCalendar;
    private String mCurrentDay;
    private ViewImageListener mViewImageListener;
    private ShowLocationOnMapListener mShowLocationOnMapListener;

    public OthersActivitiesAdapter(Context context, List<Activities> activitiesList, TimeLineFragment timeLineFragment) {
        mContext = context;
        mActivitiesList = activitiesList;
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        mDateTime12HFormat = new SimpleDateFormat("dd MMM hh:mm a", Locale.ENGLISH);
        mDateMonthFormat = new SimpleDateFormat("dd MMM", Locale.ENGLISH);
        mCalendar = Calendar.getInstance(Locale.ENGLISH);
        mCurrentDay = mDateFormat.format(mCalendar.getTime());
        mViewImageListener = (ViewImageListener) timeLineFragment;
        mShowLocationOnMapListener = (ShowLocationOnMapListener) timeLineFragment;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        if (viewType == ActivityType.valueOf(ActivityType.STATUS_TYPE.toString()).ordinal() || viewType == ActivityType.valueOf(ActivityType.JOIN_DATE_TYPE.toString()).ordinal()) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.status_timeline_activitiy_item, parent, false);
            viewHolder = new StatusJoinViewHolder(view);
        } else if (viewType == ActivityType.valueOf(ActivityType.MEETING_TYPE.toString()).ordinal()) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.meeting_timeline_activity_item, parent, false);
            viewHolder = new MeetingViewHolder(view);
        } else if (viewType == ActivityType.valueOf(ActivityType.PROFILE_TYPE.toString()).ordinal() || viewType == ActivityType.valueOf(ActivityType.UPLOAD_TYPE.toString()).ordinal()
                || viewType == ActivityType.valueOf(ActivityType.COVER_TYPE.toString()).ordinal()) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.profile_cover_upload_timeline_item, parent, false);
            viewHolder = new ProfileCoverUploadViewHolder(view);
        } else if (viewType == ActivityType.valueOf(ActivityType.FREE_TIME_TYPE.toString()).ordinal()) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.free_time_timeline_item, parent, false);
            viewHolder = new FreeTimeViewHolder(view);
        } else if (viewType == ActivityType.valueOf(ActivityType.LOCATION_TYPE.toString()).ordinal()) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.location_timeline_item, parent, false);
            viewHolder = new LocationViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        FLog.d("OthersActivitiesAdapter", "position" + position);
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
                ((StatusJoinViewHolder) holder).profileNameTv.setText(activities.getUserName());
                if (activities.getUserProfileImageId() != null) {
                    ImageCacheHandler.getInstance(mContext).setImage(((StatusJoinViewHolder) holder).timelineItemProfileImage, activities.getUserProfileImageId());
                }

            } else if (getItemViewType(position) == ActivityType.valueOf(ActivityType.JOIN_DATE_TYPE.toString()).ordinal()) {
                ((StatusJoinViewHolder) holder).statusMessageTv.setText("Joined Frissbi on " + activities.getJoinedDate());
                ((StatusJoinViewHolder) holder).statusPostedTimeTv.setText(postedDate);
                ((StatusJoinViewHolder) holder).profileNameTv.setText(activities.getUserName());
                if (activities.getUserProfileImageId() != null) {
                    ImageCacheHandler.getInstance(mContext).setImage(((StatusJoinViewHolder) holder).timelineItemProfileImage, activities.getUserProfileImageId());
                }
            }

        } else if (holder instanceof MeetingViewHolder) {
            ((MeetingViewHolder) holder).meetingMessageTv.setText(activities.getMeetingMessage());
            ((MeetingViewHolder) holder).meetingPostedTimeTv.setText(postedDate);
            ((MeetingViewHolder) holder).profileNameTv.setText(activities.getUserName());
            if (activities.getUserProfileImageId() != null) {
                ImageCacheHandler.getInstance(mContext).setImage(((MeetingViewHolder) holder).timelineItemProfileImage, activities.getUserProfileImageId());
            }

        } else if (holder instanceof ProfileCoverUploadViewHolder) {
            if (getItemViewType(position) == ActivityType.valueOf(ActivityType.PROFILE_TYPE.toString()).ordinal()) {
                ((ProfileCoverUploadViewHolder) holder).profileUpdateMessageTv.setText("Updated profile image");
                ((ProfileCoverUploadViewHolder) holder).profilePostedTimeTv.setText(postedDate);
                if (activities.getProfileImageId() != null) {
                    ImageCacheHandler.getInstance(mContext).setImage(((ProfileCoverUploadViewHolder) holder).activityProfileImageView, activities.getProfileImageId());
                }
                ((ProfileCoverUploadViewHolder) holder).profileNameTv.setText(activities.getUserName());
                if (activities.getProfileImageId() != null) {
                    ImageCacheHandler.getInstance(mContext).setImage(((ProfileCoverUploadViewHolder) holder).timelineItemProfileImage, activities.getProfileImageId());
                }

            } else if (getItemViewType(position) == ActivityType.valueOf(ActivityType.COVER_TYPE.toString()).ordinal()) {
                ((ProfileCoverUploadViewHolder) holder).profileUpdateMessageTv.setText("Updated cover image");
                ((ProfileCoverUploadViewHolder) holder).profilePostedTimeTv.setText(postedDate);
                if (activities.getCoverImageId() != null) {
                    ImageCacheHandler.getInstance(mContext).setImage(((ProfileCoverUploadViewHolder) holder).activityProfileImageView, activities.getCoverImageId());
                }
                ((ProfileCoverUploadViewHolder) holder).profileNameTv.setText(activities.getUserName());
                if (activities.getUserProfileImageId() != null) {
                    ImageCacheHandler.getInstance(mContext).setImage(((ProfileCoverUploadViewHolder) holder).timelineItemProfileImage, activities.getUserProfileImageId());
                }

            } else if (getItemViewType(position) == ActivityType.valueOf(ActivityType.UPLOAD_TYPE.toString()).ordinal()) {
                ((ProfileCoverUploadViewHolder) holder).profileUpdateMessageTv.setText(activities.getImageCaption());
                ((ProfileCoverUploadViewHolder) holder).profilePostedTimeTv.setText(postedDate);
                if (activities.getUploadedImageId() != null) {
                    ImageCacheHandler.getInstance(mContext).setImage(((ProfileCoverUploadViewHolder) holder).activityProfileImageView, activities.getUploadedImageId());
                }
                ((ProfileCoverUploadViewHolder) holder).profileNameTv.setText(activities.getUserName());
                if (activities.getUserProfileImageId() != null) {
                    ImageCacheHandler.getInstance(mContext).setImage(((ProfileCoverUploadViewHolder) holder).timelineItemProfileImage, activities.getUserProfileImageId());
                }
            }

        } else if (holder instanceof FreeTimeViewHolder) {
            Date date = null;
            try {
                date = mDateFormat.parse(activities.getFreeTimeDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String freeDate = mDateMonthFormat.format(date);
            ((FreeTimeViewHolder) holder).freeTimeDateTv.setText(freeDate);
            ((FreeTimeViewHolder) holder).freeTimeTv.setText(Utility.getInstance().convertTime(activities.getFreeTimeFromTime()) + " to " + Utility.getInstance().convertTime(activities.getFreeTimeToTime()));
            ((FreeTimeViewHolder) holder).freeTimePostedTv.setText(postedDate);
            ((FreeTimeViewHolder) holder).profileNameTv.setText(activities.getUserName());
            if (activities.getUserProfileImageId() != null) {
                ImageCacheHandler.getInstance(mContext).setImage(((FreeTimeViewHolder) holder).timelineItemProfileImage, activities.getUserProfileImageId());
            }
        } else if (holder instanceof LocationViewHolder) {
            ((LocationViewHolder) holder).checkInAddressTv.setText("Check In at " + activities.getLocationAddress());
            if (activities.getDescription() != null) {
                ((LocationViewHolder) holder).description.setText(activities.getDescription());
            }
            ((LocationViewHolder) holder).locationTimeTv.setText(postedDate);
            ((LocationViewHolder) holder).profileNameTv.setText(activities.getUserName());
            if (activities.getUserProfileImageId() != null) {
                ImageCacheHandler.getInstance(mContext).setImage(((LocationViewHolder) holder).timelineItemProfileImage, activities.getUserProfileImageId());
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getItemViewType(position) == ActivityType.valueOf(ActivityType.PROFILE_TYPE.toString()).ordinal()) {
                    mViewImageListener.viewImage(activities.getProfileImageId());
                } else if (getItemViewType(position) == ActivityType.valueOf(ActivityType.COVER_TYPE.toString()).ordinal()) {
                    mViewImageListener.viewImage(activities.getCoverImageId());
                } else if (getItemViewType(position) == ActivityType.valueOf(ActivityType.UPLOAD_TYPE.toString()).ordinal()) {
                    mViewImageListener.viewImage(activities.getUploadedImageId());
                } else if (getItemViewType(position) == ActivityType.valueOf(ActivityType.LOCATION_TYPE.toString()).ordinal()) {
                    mShowLocationOnMapListener.showLocation(activities.getLatitude(), activities.getLongitude());
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
        private final ImageView timelineItemProfileImage;
        private final TextView profileNameTv;
        private TextView statusPostedTimeTv;
        private TextView statusMessageTv;


        StatusJoinViewHolder(View itemView) {
            super(itemView);
            statusMessageTv = (TextView) itemView.findViewById(R.id.status_tv);
            statusPostedTimeTv = (TextView) itemView.findViewById(R.id.status_postedTime_tv);
            timelineItemProfileImage = (ImageView) itemView.findViewById(R.id.timeline_item_profile_image);
            profileNameTv = (TextView) itemView.findViewById(R.id.profileName_tv);
        }
    }

    private class MeetingViewHolder extends RecyclerView.ViewHolder {
        private TextView meetingMessageTv;
        private TextView meetingPostedTimeTv;
        private final ImageView timelineItemProfileImage;
        private final TextView profileNameTv;

        MeetingViewHolder(View itemView) {
            super(itemView);
            meetingMessageTv = (TextView) itemView.findViewById(R.id.meeting_tv);
            meetingPostedTimeTv = (TextView) itemView.findViewById(R.id.meeting_postedTime_tv);
            timelineItemProfileImage = (ImageView) itemView.findViewById(R.id.timeline_item_profile_image);
            profileNameTv = (TextView) itemView.findViewById(R.id.profileName_tv);
        }
    }

    private class ProfileCoverUploadViewHolder extends RecyclerView.ViewHolder {
        private TextView profilePostedTimeTv;
        private TextView profileUpdateMessageTv;
        private ImageView activityProfileImageView;
        private final ImageView timelineItemProfileImage;
        private final TextView profileNameTv;

        ProfileCoverUploadViewHolder(View itemView) {
            super(itemView);
            activityProfileImageView = (ImageView) itemView.findViewById(R.id.timeline_activity_imageView);
            profileUpdateMessageTv = (TextView) itemView.findViewById(R.id.profile_update_tv);
            profilePostedTimeTv = (TextView) itemView.findViewById(R.id.profile_postedTime_tv);
            timelineItemProfileImage = (ImageView) itemView.findViewById(R.id.timeline_item_profile_image);
            profileNameTv = (TextView) itemView.findViewById(R.id.profileName_tv);
        }
    }

    private class FreeTimeViewHolder extends RecyclerView.ViewHolder {

        private TextView freeTimeDateTv;
        private TextView freeTimeTv;
        private TextView freeTimePostedTv;
        private final ImageView timelineItemProfileImage;
        private final TextView profileNameTv;

        FreeTimeViewHolder(View itemView) {
            super(itemView);
            freeTimeDateTv = (TextView) itemView.findViewById(R.id.free_date_tv);
            freeTimeTv = (TextView) itemView.findViewById(R.id.free_time_tv);
            freeTimePostedTv = (TextView) itemView.findViewById(R.id.free_postedTime_tv);
            timelineItemProfileImage = (ImageView) itemView.findViewById(R.id.timeline_item_profile_image);
            profileNameTv = (TextView) itemView.findViewById(R.id.profileName_tv);
        }
    }

    private class LocationViewHolder extends RecyclerView.ViewHolder {
        private TextView checkInAddressTv;
        private TextView locationTimeTv;
        private final ImageView timelineItemProfileImage;
        private final TextView profileNameTv;
        private TextView description;

        LocationViewHolder(View itemView) {
            super(itemView);
            checkInAddressTv = (TextView) itemView.findViewById(R.id.check_in_tv);
            locationTimeTv = (TextView) itemView.findViewById(R.id.location_postedTime_tv);
            timelineItemProfileImage = (ImageView) itemView.findViewById(R.id.timeline_item_profile_image);
            profileNameTv = (TextView) itemView.findViewById(R.id.profileName_tv);
            description = (TextView) itemView.findViewById(R.id.description_tv);
        }
    }

    public void setMoreActivities(List<Activities> activitiesList) {
        mActivitiesList.addAll(activitiesList);
        notifyDataSetChanged();
    }
}
