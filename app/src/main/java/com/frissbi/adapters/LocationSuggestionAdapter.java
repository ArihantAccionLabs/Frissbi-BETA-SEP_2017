package com.frissbi.adapters;

import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.Utility.ImageCacheHandler;
import com.frissbi.models.LocationSuggestion;

import java.util.List;

/**
 * Created by thrymr on 7/2/17.
 */

public class LocationSuggestionAdapter extends RecyclerView.Adapter<LocationSuggestionAdapter.ViewHolder> {

    private Context mContext;
    private int mSelectedPosition = -1;
    private List<LocationSuggestion> mLocationSuggestionList;

    public LocationSuggestionAdapter(Context context, List<LocationSuggestion> locationSuggestionList) {
        mContext = context;
        mLocationSuggestionList = locationSuggestionList;

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.location_suggestion_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        holder.locNameTv.setText(mLocationSuggestionList.get(position).getName());
        holder.locAddressTv.setText(mLocationSuggestionList.get(position).getAddress());
        if (mLocationSuggestionList.get(position).getImageUrl() != null) {
            //ImageCacheHandler.getInstance(mContext).setImageId(holder.locImageView, mLocationSuggestionList.get(position).getLocationId() + "", mLocationSuggestionList.get(position).getImageUrl());
        }
        if (mSelectedPosition == position) {
            holder.locCheckbox.setChecked(true);
        } else {
            holder.locCheckbox.setChecked(false);
        }


        holder.locRating.setRating(Float.parseFloat(mLocationSuggestionList.get(position).getRating()));

        holder.locCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mSelectedPosition = position;
                    notifyDataSetChanged();
                } else {
                    mSelectedPosition = -1;
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mLocationSuggestionList.size();
    }

    public LocationSuggestion getSelectedLocation() {
        if (mSelectedPosition != -1) {
            return mLocationSuggestionList.get(mSelectedPosition);
        } else {
            return null;
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private RatingBar locRating;
        private CheckBox locCheckbox;
        private TextView locAddressTv;
        private TextView locNameTv;
        private ImageView locImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            locImageView = (ImageView) itemView.findViewById(R.id.loc_image);
            locNameTv = (TextView) itemView.findViewById(R.id.loc_name_tv);
            locAddressTv = (TextView) itemView.findViewById(R.id.loc_address_tv);
            locCheckbox = (CheckBox) itemView.findViewById(R.id.loc_checkbox);
            locRating = (RatingBar) itemView.findViewById(R.id.loc_rating);
        }
    }
}
