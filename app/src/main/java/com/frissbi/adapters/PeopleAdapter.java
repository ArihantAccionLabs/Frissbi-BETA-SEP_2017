package com.frissbi.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.Utility.FriendStatus;
import com.frissbi.interfaces.FriendRequestListener;
import com.frissbi.models.Friend;

import java.util.List;

/**
 * Created by thrymr on 10/2/17.
 */

public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.ViewHolder> {

    private Context mContext;
    private List<Friend> mFriendList;
    private FriendRequestListener mFriendRequestListener;

    public PeopleAdapter(Context context, List<Friend> friendList, FriendRequestListener friendRequestListener) {
        mContext = context;
        mFriendList = friendList;
        mFriendRequestListener = friendRequestListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.people_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.peopleNameTv.setText(mFriendList.get(position).getFullName());
        if (mFriendList.get(position).getStatus().equalsIgnoreCase(FriendStatus.UNFRIEND.toString())){
            holder.addPeopleButton.setText("Add");
        }else if (mFriendList.get(position).getStatus().equalsIgnoreCase(FriendStatus.WAITING.toString())){
            holder.addPeopleButton.setText("Req Sent");
        }else if (mFriendList.get(position).getStatus().equalsIgnoreCase(FriendStatus.CONFIRM.toString())){
            holder.addPeopleButton.setText("Accept");
        }
        holder.addPeopleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFriendRequestListener.sendFriendRequest(mFriendList.get(position));
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFriendRequestListener.viewProfile(mFriendList.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return mFriendList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView peopleNameTv;
        private final ImageView peopleImageView;
        private final Button addPeopleButton;

        public ViewHolder(View itemView) {
            super(itemView);
            peopleNameTv = (TextView) itemView.findViewById(R.id.people_name_tv);
            peopleImageView = (ImageView) itemView.findViewById(R.id.people_image);
            addPeopleButton = (Button) itemView.findViewById(R.id.add_people_button);
        }
    }
}
