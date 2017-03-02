package com.frissbi.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.Utility.Utility;
import com.frissbi.interfaces.GroupDetailsListener;
import com.frissbi.models.FrissbiGroup;

import java.util.List;

/**
 * Created by thrymr on 2/3/17.
 */

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {

    private Context mContext;
    private List<FrissbiGroup> mFrissbiGroupList;
    private GroupDetailsListener mGroupDetailsListener;

    public GroupsAdapter(Context context, List<FrissbiGroup> frissbiGroupList, GroupDetailsListener groupDetailsListener) {
        mContext = context;
        mFrissbiGroupList = frissbiGroupList;
        mGroupDetailsListener=groupDetailsListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.friends_adapter_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final FrissbiGroup frissbiGroup = mFrissbiGroupList.get(position);
        holder.groupNameTv.setText(frissbiGroup.getName());
        holder.groupImage.setImageBitmap(Utility.getInstance().getBitmapFromString(frissbiGroup.getImage()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGroupDetailsListener.showGroupDetails(frissbiGroup);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFrissbiGroupList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView groupNameTv;
        private final ImageView groupImage;

        public ViewHolder(View itemView) {
            super(itemView);
            groupNameTv = (TextView) itemView.findViewById(R.id.friend_username_tv);
            groupImage = (ImageView) itemView.findViewById(R.id.friend_profile_image);
        }
    }
}
