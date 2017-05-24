package com.frissbi.app.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frissbi.app.R;
import com.frissbi.app.Utility.ImageCacheHandler;
import com.frissbi.app.Utility.Utility;
import com.frissbi.app.interfaces.NotificationListener;
import com.frissbi.app.models.Notification;

import java.util.List;

/**
 * Created by thrymr on 20/3/17.
 */

public class NotificationLogAdapter extends RecyclerView.Adapter<NotificationLogAdapter.ViewHolder> {

    private Context mContext;
    private List<Notification> mNotificationList;
    private NotificationListener mNotificationListener;

    public NotificationLogAdapter(Context context, List<Notification> notificationList, NotificationListener notificationListener) {
        mContext = context;
        mNotificationList = notificationList;
        mNotificationListener = notificationListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Notification notification = mNotificationList.get(position);
        holder.notificationMessageTv.setText(notification.getMessage());

        if (!notification.isRead()) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.lightgray));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
        }

        if (notification.getType().equalsIgnoreCase(Utility.GROUP_NOTIFICATION_TYPE)) {
            if (notification.getGroupImageId() != null) {
                ImageCacheHandler.getInstance(mContext).setImage(holder.notificationImageView, notification.getGroupImageId());
            }
            holder.notificationMessageTv.setText(notification.getGroupAdmin() + " added you to " + notification.getGroupName());
        }

        if (notification.getType().equalsIgnoreCase(Utility.FRIEND_NOTIFICATION_TYPE)) {
            if (notification.getFriendImageId() != null) {
                ImageCacheHandler.getInstance(mContext).setImage(holder.notificationImageView, notification.getFriendImageId());
            }
            holder.notificationMessageTv.setText(notification.getFriendName() + " wants to be friend with you ");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNotificationListener.selectedNotification(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNotificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView notificationMessageTv;
        private final ImageView notificationImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            notificationMessageTv = (TextView) itemView.findViewById(R.id.notification_message_tv);
            notificationImageView = (ImageView) itemView.findViewById(R.id.notification_imageView);
        }
    }
}
