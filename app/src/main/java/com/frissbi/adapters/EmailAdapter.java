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
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.SelectedContacts;
import com.frissbi.interfaces.ContactsSelectedListener;
import com.frissbi.models.EmailContacts;

import java.util.List;

/**
 * Created by thrymr on 18/1/17.
 */

public class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.ViewHolder> {

    private Context mContext;
    private List<EmailContacts> mEmailContactsList;
    private List<Long> mEmailsSelectedIdsList;
    private SelectedContacts mSelectedContacts;
    private int lastPosition = -1;

    public EmailAdapter(Context context, List<EmailContacts> emailContactsList) {
        mContext = context;
        mEmailContactsList = emailContactsList;
        mSelectedContacts = SelectedContacts.getInstance();
        mEmailsSelectedIdsList = mSelectedContacts.getEmailsSelectedIdsList();
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

}
