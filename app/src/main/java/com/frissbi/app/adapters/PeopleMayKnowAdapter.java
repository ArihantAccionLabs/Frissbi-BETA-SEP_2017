package com.frissbi.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.frissbi.app.R;
import com.frissbi.app.Utility.ImageCacheHandler;
import com.frissbi.app.interfaces.FriendRequestListener;
import com.frissbi.app.models.FrissbiContact;

import java.util.List;

/**
 * Created by thrymr on 20/3/17.
 */

public class PeopleMayKnowAdapter extends RecyclerView.Adapter<PeopleMayKnowAdapter.ViewHolder> {

    private Context mContext;
    private List<FrissbiContact> mFrissbiContactList;
    private FriendRequestListener mFriendRequestListener;

    public PeopleMayKnowAdapter(Context context, List<FrissbiContact> frissbiContactList, FriendRequestListener friendRequestListener) {
        mContext = context;
        mFrissbiContactList = frissbiContactList;
        mFriendRequestListener = friendRequestListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.people_you_may_know_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final FrissbiContact frissbiContact = mFrissbiContactList.get(position);
        if (frissbiContact.getImageId() != null) {
            ImageCacheHandler.getInstance(mContext).setImage(holder.pepoleProfileImage, frissbiContact.getImageId());
        }
        holder.peopleUserNameTv.setText(frissbiContact.getName());
        holder.pepoleAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFriendRequestListener.sendFriendRequest(frissbiContact);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFriendRequestListener.viewProfile(frissbiContact);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mFrissbiContactList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView pepoleProfileImage;
        private final TextView peopleUserNameTv;
        private final Button pepoleAddFriendButton;

        public ViewHolder(View itemView) {
            super(itemView);
            pepoleProfileImage = (ImageView) itemView.findViewById(R.id.pepole_profile_image);
            peopleUserNameTv = (TextView) itemView.findViewById(R.id.people_userName_tv);
            pepoleAddFriendButton = (Button) itemView.findViewById(R.id.pepole_add_friend_button);
        }
    }
}
