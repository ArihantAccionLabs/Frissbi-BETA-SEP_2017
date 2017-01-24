package com.frissbi.Frissbi_Meetings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.frissbi.R;

import java.util.List;




public class Meeting_Conflictadapter extends ArrayAdapter<Meeting_ConflictPojo> {
    private List<Meeting_ConflictPojo> items;

    public Meeting_Conflictadapter(Context context, List<Meeting_ConflictPojo> items) {
        super(context, R.layout.serch_iteam, items);
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ImageView imageViewRound;
        if (v == null) {
            LayoutInflater li = LayoutInflater.from(getContext());
            v = li.inflate(R.layout.meeting_conflicted_item, null);
        }

        Meeting_ConflictPojo app = items.get(position);

        //ImageView icon = (ImageView)v.findViewById(R.id.appIcon);
        TextView peding = (TextView) v.findViewById(R.id.desc);
        TextView dt = (TextView) v.findViewById(R.id.date);
        TextView dt1 = (TextView) v.findViewById(R.id.time);


        peding.setText("" + app.getMeetingDescription());

        String s=app.getSenderFromDateTime();
        String s1=app.getSenderToDateTime();
        String date=s.substring(0, 10);
        String tm=s.substring(10,16);
        String tm1=s1.substring(10,16);
        dt1.setText("Date :"+ date);

        dt.setText("Time :"+tm+" - "+tm1);




        return v;
    }


}

