package com.frissbi.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.frissbi.Frissbi_img_crop.Util;
import com.frissbi.R;
import com.frissbi.Utility.Utility;
import com.frissbi.models.FrissbiContact;
import com.frissbi.models.MyContacts;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thrymr on 9/2/17.
 */

public class MyContactsAdapter extends RecyclerView.Adapter<MyContactsAdapter.ViewHolder> implements Filterable {

    private Context mContext;
    private List<FrissbiContact> mFrissbiContactList;
    private List<FrissbiContact> mOriginalFrissbiContactList;
    private MyContactsFilter mMyContactsFilter;

    public MyContactsAdapter(Context context, List<FrissbiContact> frissbiContactList) {
        mContext = context;
        mFrissbiContactList = frissbiContactList;
        mOriginalFrissbiContactList = frissbiContactList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.my_contacts_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        FrissbiContact frissbiContact = mFrissbiContactList.get(position);

        if (frissbiContact.getType() == Utility.EMAIL_TYPE) {
            holder.myContactsNameTv.setText(frissbiContact.getEmailId());
            holder.myContactsNumTv.setVisibility(View.GONE);
        } else if (frissbiContact.getType() == Utility.CONTACT_TYPE) {
            holder.myContactsNameTv.setText(frissbiContact.getName());
            holder.myContactsNumTv.setVisibility(View.VISIBLE);
            holder.myContactsNumTv.setText(frissbiContact.getPhoneNumber());
        }
    }

    @Override
    public int getItemCount() {
        return mFrissbiContactList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private Button inviteButton;
        private TextView myContactsNameTv;
        private TextView myContactsNumTv;

        public ViewHolder(View itemView) {
            super(itemView);
            myContactsNameTv = (TextView) itemView.findViewById(R.id.my_contacts_name);
            myContactsNumTv = (TextView) itemView.findViewById(R.id.my_contacts_num);
            inviteButton = (Button) itemView.findViewById(R.id.invite_button);
        }
    }


    @Override
    public Filter getFilter() {
        if (mMyContactsFilter == null) {
            mMyContactsFilter = new MyContactsFilter();
        }
        return mMyContactsFilter;
    }

    private class MyContactsFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                List<FrissbiContact> frissbiContactList = new ArrayList<FrissbiContact>();
                for (int i = 0; i < mFrissbiContactList.size(); i++) {
                    if ((mFrissbiContactList.get(i).getName().toUpperCase())
                            .startsWith(constraint.toString().toUpperCase())) {
                        frissbiContactList.add(mFrissbiContactList.get(i));
                    }
                }
                results.count = frissbiContactList.size();
                results.values = frissbiContactList;
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
