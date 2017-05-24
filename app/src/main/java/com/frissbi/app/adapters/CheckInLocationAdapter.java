package com.frissbi.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frissbi.app.R;
import com.frissbi.app.Utility.ImageCacheHandler;
import com.frissbi.app.interfaces.CheckInLocationListener;
import com.frissbi.app.models.LocationSuggestion;

import java.util.List;

/**
 * Created by thrymr on 22/3/17.
 */

public class CheckInLocationAdapter extends RecyclerView.Adapter<CheckInLocationAdapter.ViewHolder> {

    private Context mContext;
    private List<LocationSuggestion> mLocationSuggestionList;
    private CheckInLocationListener mCheckInLocationListener;

    public CheckInLocationAdapter(Context context, List<LocationSuggestion> locationSuggestionList, CheckInLocationListener checkInLocationListener) {
        mContext = context;
        mLocationSuggestionList = locationSuggestionList;
        mCheckInLocationListener = checkInLocationListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.check_in_location_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final LocationSuggestion locationSuggestion = mLocationSuggestionList.get(position);
        holder.checkInNameTv.setText(locationSuggestion.getName());

        if (locationSuggestion.getImageUrl() != null) {
            ImageCacheHandler.getInstance(mContext).setLocationIcon(holder.checkInImageView, locationSuggestion.getPlaceId(), locationSuggestion.getImageUrl());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCheckInLocationListener.selectedCheckInLocation(locationSuggestion);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLocationSuggestionList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView checkInImageView;
        private final TextView checkInNameTv;

        public ViewHolder(View itemView) {
            super(itemView);
            checkInImageView = (ImageView) itemView.findViewById(R.id.check_in_image);
            checkInNameTv = (TextView) itemView.findViewById(R.id.check_in_name_tv);
        }
    }
}
