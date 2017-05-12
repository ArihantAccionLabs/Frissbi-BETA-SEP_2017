package com.frissbi.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.models.SelectedContacts;
import com.frissbi.Utility.ImageCacheHandler;
import com.frissbi.models.FrissbiContact;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thrymr on 18/1/17.
 */

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> implements Filterable {

    private Context mContext;
    private List<FrissbiContact> mFrissbiContactList;
    private List<FrissbiContact> mOriginalFrissbiContactList;
    private List<FrissbiContact> mContactsFrissbiContactList;
    private SelectedContacts mSelectedContacts;
    private ContactsFilter mContactsFilter;
    private List<Long> mSelectedIds;

    public ContactsAdapter(Context context, List<FrissbiContact> contactsList) {
        mContext = context;
        mFrissbiContactList = contactsList;
        mSelectedContacts = SelectedContacts.getInstance();
        mContactsFrissbiContactList = mSelectedContacts.getFrissbiContactList();
        mOriginalFrissbiContactList = contactsList;
        mSelectedIds = new ArrayList<>();
        for (FrissbiContact frissbiContact : mContactsFrissbiContactList) {
            mSelectedIds.add(frissbiContact.getId());
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.contacts_friends_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final FrissbiContact frissbiContact = mFrissbiContactList.get(position);

        if (frissbiContact.getType() == 1) {
            holder.contactNameTv.setText(frissbiContact.getName());
            holder.phoneNumTv.setVisibility(View.GONE);
            if (frissbiContact.getImageId() != null) {
                ImageCacheHandler.getInstance(mContext).setImage(holder.profileImageView, frissbiContact.getImageId());
            }
        } else if (frissbiContact.getType() == 2) {
            holder.contactNameTv.setText(frissbiContact.getEmailId());
            holder.phoneNumTv.setVisibility(View.GONE);
            holder.profileImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.email_icon));
        } else if (frissbiContact.getType() == 3) {
            holder.contactNameTv.setText(frissbiContact.getName());
            holder.phoneNumTv.setVisibility(View.VISIBLE);
            holder.phoneNumTv.setText(frissbiContact.getPhoneNumber());
            holder.profileImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.phone_icon));
        }
        if (mSelectedIds.size() > 0) {
            if (mSelectedIds.contains(frissbiContact.getId())) {
                holder.selectedIconImageView.setVisibility(View.VISIBLE);
            } else {
                holder.selectedIconImageView.setVisibility(View.GONE);
            }
        } else {
            holder.selectedIconImageView.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.selectedIconImageView.getVisibility() == View.VISIBLE) {
                    holder.selectedIconImageView.setVisibility(View.GONE);
                    mSelectedContacts.deleteFrissbiContact(frissbiContact);
                } else {
                    holder.selectedIconImageView.setVisibility(View.VISIBLE);
                    mSelectedContacts.setFrissbiContact(frissbiContact);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mFrissbiContactList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView profileImageView;
        private final ImageView selectedIconImageView;
        private TextView phoneNumTv;
        private TextView contactNameTv;

        public ViewHolder(View itemView) {
            super(itemView);
            profileImageView = (ImageView) itemView.findViewById(R.id.profile_imageView);
            contactNameTv = (TextView) itemView.findViewById(R.id.contact_name_tv);
            phoneNumTv = (TextView) itemView.findViewById(R.id.phone_num_tv);
            selectedIconImageView = (ImageView) itemView.findViewById(R.id.selected_icon_imageView);
        }
    }

    @Override
    public Filter getFilter() {
        if (mContactsFilter == null) {
            mContactsFilter = new ContactsFilter();
        }
        return mContactsFilter;
    }

    private class ContactsFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                List<FrissbiContact> contactsList = new ArrayList<>();
                for (int i = 0; i < mFrissbiContactList.size(); i++) {
                    if ((mFrissbiContactList.get(i).getName().toUpperCase())
                            .startsWith(constraint.toString().toUpperCase())) {
                        contactsList.add(mFrissbiContactList.get(i));
                    }
                }
                results.count = contactsList.size();
                results.values = contactsList;
            } else {
                results.count = mOriginalFrissbiContactList.size();
                results.values = mOriginalFrissbiContactList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFrissbiContactList = (List<FrissbiContact>) results.values;
            notifyDataSetChanged();
        }
    }


}
