package com.frissbi.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.models.FrissbiReminder;

import java.util.List;

/**
 * Created by thrymr on 21/3/17.
 */

public class RemindersAdapter extends RecyclerView.Adapter<RemindersAdapter.ViewHolder> {

    private Context mContext;
    private List<FrissbiReminder> mFrissbiReminderList;

    public RemindersAdapter(Context context, List<FrissbiReminder> frissbiReminderList) {
        mContext = context;
        mFrissbiReminderList = frissbiReminderList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.reminder_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.reminderTv.setText(mFrissbiReminderList.get(position).getMessage() + " on " + mFrissbiReminderList.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return mFrissbiReminderList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView reminderTv;

        public ViewHolder(View itemView) {
            super(itemView);
            reminderTv = (TextView) itemView.findViewById(R.id.reminder_tv);
        }
    }
}
