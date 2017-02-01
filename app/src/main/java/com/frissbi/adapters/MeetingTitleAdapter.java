package com.frissbi.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.interfaces.MeetingTitleSelectionListener;

import java.util.List;

/**
 * Created by thrymr on 20/1/17.
 */

public class MeetingTitleAdapter extends RecyclerView.Adapter<MeetingTitleAdapter.ViewHolder> {
    private Context mContext;
    private List<String> mTitleList;
    private MeetingTitleSelectionListener mMeetingTitleSelectionListener;

    public MeetingTitleAdapter(Context context, List<String> titleList, MeetingTitleSelectionListener meetingTitleSelectionListener) {
        mContext = context;
        mTitleList = titleList;
        mMeetingTitleSelectionListener = meetingTitleSelectionListener;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.meeting_title_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.titleItemTv.setText(mTitleList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMeetingTitleSelectionListener.selectedMeetingTitle(mTitleList.get(position));

            }
        });
    }

    @Override
    public int getItemCount() {
        return mTitleList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView titleItemTv;

        public ViewHolder(View itemView) {
            super(itemView);
            titleItemTv = (TextView) itemView.findViewById(R.id.title_item_tv);
        }
    }
}
