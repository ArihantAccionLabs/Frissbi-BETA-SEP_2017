package com.frissbi.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.frissbi.app.R;
import com.frissbi.app.interfaces.SelectLocationListener;
import com.frissbi.app.models.MyPlaces;

import java.util.List;

/**
 * Created by thrymr on 12/1/17.
 */

public class MyPlacesAdapter extends RecyclerView.Adapter<MyPlacesAdapter.ViewHolder> {
    private Context mContext;
    private List<MyPlaces> mMyPlacesList;
    private SelectLocationListener mSelectLocationListener;

    public MyPlacesAdapter(Context context, List<MyPlaces> myPlacesList, SelectLocationListener selectLocationListener) {
        mContext = context;
        mMyPlacesList = myPlacesList;
        mSelectLocationListener = selectLocationListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.myplace_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.myPlaceTv.setText(mMyPlacesList.get(position).getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MyPlacesAdapter", "onClick");
                mSelectLocationListener.sendSelectLocation(mMyPlacesList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMyPlacesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView myPlaceTv;

        public ViewHolder(View itemView) {
            super(itemView);
            myPlaceTv = (TextView) itemView.findViewById(R.id.myPlace_tv);
        }
    }
}
