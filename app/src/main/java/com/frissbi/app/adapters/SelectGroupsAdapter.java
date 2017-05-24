package com.frissbi.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.frissbi.app.models.FrissbiGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thrymr on 6/3/17.
 */
public class SelectGroupsAdapter extends RecyclerView.Adapter<SelectGroupsAdapter.ViewHolder> implements Filterable {
    private Context mContext;
    List<FrissbiGroup> mFrissbiGroupList;
    private List<FrissbiGroup> mOriginalGroupsList;
    private GroupsFilter mGroupsFilter;

    public SelectGroupsAdapter(Context context, List<FrissbiGroup> frissbiGroupList) {
        mContext = context;
        mFrissbiGroupList = frissbiGroupList;
        mOriginalGroupsList=frissbiGroupList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mFrissbiGroupList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public Filter getFilter() {
        if ( mGroupsFilter== null) {
            mGroupsFilter = new GroupsFilter();
        }
        return mGroupsFilter;
    }

    private class GroupsFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                List<FrissbiGroup> frissbiGroupList = new ArrayList<>();
                for (int i = 0; i < mFrissbiGroupList.size(); i++) {
                    if ((mFrissbiGroupList.get(i).getName().toUpperCase())
                            .startsWith(constraint.toString().toUpperCase())) {
                        frissbiGroupList.add(mFrissbiGroupList.get(i));
                    }
                }
                results.count = frissbiGroupList.size();
                results.values = frissbiGroupList;
            } else {
                results.count = mOriginalGroupsList.size();
                results.values = mOriginalGroupsList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFrissbiGroupList = (List<FrissbiGroup>) results.values;
            notifyDataSetChanged();
        }
    }

}
