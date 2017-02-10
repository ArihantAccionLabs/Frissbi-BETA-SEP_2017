package com.frissbi.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.SelectedContacts;
import com.frissbi.Utility.FLog;
import com.frissbi.models.EmailContacts;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thrymr on 18/1/17.
 */

public class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.ViewHolder> implements Filterable {

    private Context mContext;
    private List<EmailContacts> mEmailContactsList;
    private List<EmailContacts> mOriginalEmailContactsList;
    private List<Long> mEmailsSelectedIdsList;
    private SelectedContacts mSelectedContacts;
    private int lastPosition = -1;
    private EmailsFilter mEmailsFilter;

    public EmailAdapter(Context context, List<EmailContacts> emailContactsList) {
        mContext = context;
        mEmailContactsList = emailContactsList;
        mSelectedContacts = SelectedContacts.getInstance();
        mEmailsSelectedIdsList = mSelectedContacts.getEmailsSelectedIdsList();
        mOriginalEmailContactsList = emailContactsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.email_friend_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.emailIdTv.setText(mEmailContactsList.get(position).getEmailId());
        if (mEmailsSelectedIdsList.size() > 0) {
            if (mEmailsSelectedIdsList.contains(mEmailContactsList.get(position).getId())) {
                holder.emailCheckbox.setChecked(true);
            } else {
                holder.emailCheckbox.setChecked(false);
            }
        }
        holder.emailCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    mSelectedContacts.setEmailsSelectedId(mEmailContactsList.get(position).getId());
                } else {
                    mSelectedContacts.deleteEmailsSelectedId(mEmailContactsList.get(position).getId());
                }
            }
        });
        // Here you apply the animation when the view is bound
        //  setAnimation(holder.itemView, position);
        // animate(holder);
    }

    @Override
    public int getItemCount() {
        return mEmailContactsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView emailIdTv;
        private CheckBox emailCheckbox;

        public ViewHolder(View itemView) {
            super(itemView);
            emailCheckbox = (CheckBox) itemView.findViewById(R.id.email_checkbox);
            emailIdTv = (TextView) itemView.findViewById(R.id.email_id_tv);


        }
    }


    /**
     * Here is the key method to apply the animation
     */
    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.bounce_interpolator);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(mContext, R.anim.bounce_interpolator);
        viewHolder.itemView.setAnimation(animAnticipateOvershoot);
    }


    @Override
    public Filter getFilter() {
        if (mEmailsFilter == null) {
            mEmailsFilter = new EmailsFilter();
        }
        return mEmailsFilter;
    }

    private class EmailsFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            FLog.d("FriendsAdapter", "constraint" + constraint);
            if (constraint != null && constraint.length() > 0) {
                List<EmailContacts> emailContactsArrayList = new ArrayList<>();
                for (int i = 0; i < mEmailContactsList.size(); i++) {
                    if ((mEmailContactsList.get(i).getEmailId().toUpperCase())
                            .startsWith(constraint.toString().toUpperCase())) {
                        emailContactsArrayList.add(mEmailContactsList.get(i));
                    }
                }
                results.count = emailContactsArrayList.size();
                results.values = emailContactsArrayList;
            } else {
                results.count = mOriginalEmailContactsList.size();
                results.values = mOriginalEmailContactsList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mEmailContactsList = (List<EmailContacts>) results.values;
            notifyDataSetChanged();
        }
    }


}
