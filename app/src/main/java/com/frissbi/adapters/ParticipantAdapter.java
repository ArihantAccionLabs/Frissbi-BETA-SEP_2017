package com.frissbi.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frissbi.Frissbi_img_crop.Util;
import com.frissbi.R;
import com.frissbi.Utility.Utility;
import com.frissbi.models.Participant;

import java.util.List;

/**
 * Created by thrymr on 2/3/17.
 */

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ViewHolder> {

    private Context mContext;
    private List<Participant> mParticipantList;

    public ParticipantAdapter(Context context, List<Participant> participantList) {
        mContext = context;
        mParticipantList = participantList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.group_participant_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Participant participant = mParticipantList.get(position);
        holder.participantUsernameTv.setText(participant.getFullName());
        holder.participantProfileImage.setImageBitmap(Utility.getInstance().getBitmapFromString(participant.getImage()));
        if (participant.isAdmin()) {
            holder.adminTv.setVisibility(View.VISIBLE);
        } else {
            holder.adminTv.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mParticipantList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView participantProfileImage;
        private final TextView participantUsernameTv;
        private final TextView adminTv;

        public ViewHolder(View itemView) {
            super(itemView);
            participantProfileImage = (ImageView) itemView.findViewById(R.id.participant_profile_image);
            participantUsernameTv = (TextView) itemView.findViewById(R.id.participant_username_tv);
            adminTv = (TextView) itemView.findViewById(R.id.admin_tv);
        }
    }
}
