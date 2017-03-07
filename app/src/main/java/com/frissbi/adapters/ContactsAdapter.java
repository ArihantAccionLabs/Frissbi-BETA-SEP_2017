package com.frissbi.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.SelectedContacts;
import com.frissbi.interfaces.ContactsSelectedListener;
import com.frissbi.models.Contacts;
import com.frissbi.models.EmailContacts;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thrymr on 18/1/17.
 */

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> implements Filterable {

    private Context mContext;
    private List<Contacts> mContactsList;
    private List<Contacts> mOriginalContactsList;
    private List<Long> mContactsSelectedIdsList;
    private SelectedContacts mSelectedContacts;
    private ContactsFilter mContactsFilter;

    public ContactsAdapter(Context context, List<Contacts> contactsList) {
        mContext = context;
        mContactsList = contactsList;
        mSelectedContacts = SelectedContacts.getInstance();
        mContactsSelectedIdsList = mSelectedContacts.getContactsSelectedIdsList();
        mOriginalContactsList = contactsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.contacts_friends_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.contactNameTv.setText(mContactsList.get(position).getName());
        holder.phoneNumTv.setText(mContactsList.get(position).getPhoneNumber());

        if (mContactsSelectedIdsList.size() > 0) {
            if (mContactsSelectedIdsList.contains(mContactsList.get(position).getId())) {
                holder.contactCheckBox.setChecked(true);
            } else {
                holder.contactCheckBox.setChecked(false);
            }
        }
        holder.contactCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    mSelectedContacts.setContactsSelectedId(mContactsList.get(position).getId());
                } else {
                    mSelectedContacts.deleteContactsSelectedId(mContactsList.get(position).getId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mContactsList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView phoneNumTv;
        private TextView contactNameTv;
        private CheckBox contactCheckBox;

        public ViewHolder(View itemView) {
            super(itemView);
            contactCheckBox = (CheckBox) itemView.findViewById(R.id.contact_checkBox);
            contactNameTv = (TextView) itemView.findViewById(R.id.contact_name_tv);
            phoneNumTv = (TextView) itemView.findViewById(R.id.phone_num_tv);


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
                List<Contacts> contactsList = new ArrayList<>();
                for (int i = 0; i < mContactsList.size(); i++) {
                    if ((mContactsList.get(i).getName().toUpperCase())
                            .startsWith(constraint.toString().toUpperCase())) {
                        contactsList.add(mContactsList.get(i));
                    }
                }
                results.count = contactsList.size();
                results.values = contactsList;
            } else {
                results.count = mOriginalContactsList.size();
                results.values = mOriginalContactsList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mContactsList = (List<Contacts>) results.values;
            notifyDataSetChanged();
        }
    }


}
