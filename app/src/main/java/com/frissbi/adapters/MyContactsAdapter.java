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

import com.frissbi.R;
import com.frissbi.models.MyContacts;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thrymr on 9/2/17.
 */

public class MyContactsAdapter extends RecyclerView.Adapter<MyContactsAdapter.ViewHolder> implements Filterable {

    private Context mContext;
    private List<MyContacts> mMyContactsList;
    private List<MyContacts> mOriginalMyContactsList;
    private MyContactsFilter mMyContactsFilter;

    public MyContactsAdapter(Context context, List<MyContacts> myContactsList) {
        mContext = context;
        mMyContactsList = myContactsList;
        mOriginalMyContactsList = myContactsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.my_contacts_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.myContactsNameTv.setText(mMyContactsList.get(position).getName());
        if (mMyContactsList.get(position).getNumber() == null) {
            holder.myContactsNumTv.setVisibility(View.GONE);
        } else {
            holder.myContactsNumTv.setVisibility(View.VISIBLE);
            holder.myContactsNumTv.setText(mMyContactsList.get(position).getNumber());
        }
    }

    @Override
    public int getItemCount() {
        return mMyContactsList.size();
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
                List<MyContacts> myContactsArrayList = new ArrayList<MyContacts>();
                for (int i = 0; i < mMyContactsList.size(); i++) {
                    if ((mMyContactsList.get(i).getName().toUpperCase())
                            .startsWith(constraint.toString().toUpperCase())) {
                        myContactsArrayList.add(mMyContactsList.get(i));
                    }
                }
                results.count = myContactsArrayList.size();
                results.values = myContactsArrayList;
            } else {
                results.count = mOriginalMyContactsList.size();
                results.values = mOriginalMyContactsList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mMyContactsList = (List<MyContacts>) results.values;
            notifyDataSetChanged();
        }
    }


}
