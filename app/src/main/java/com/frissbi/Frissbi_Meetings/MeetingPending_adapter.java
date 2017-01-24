package com.frissbi.Frissbi_Meetings;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.frissbi.R;

import java.util.List;


public class MeetingPending_adapter extends ArrayAdapter<MeetingPending_pojo> {
    private List<MeetingPending_pojo> items;

    public MeetingPending_adapter(Context context, List<MeetingPending_pojo> items) {
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

        if (v == null) {
            LayoutInflater li = LayoutInflater.from(getContext());
            v = li.inflate(R.layout.meetingpending_iteams, null);
        }

        MeetingPending_pojo app = items.get(position);

        //ImageView icon = (ImageView)v.findViewById(R.id.appIcon);
        TextView peding = (TextView) v.findViewById(R.id.peding);
        TextView dt = (TextView) v.findViewById(R.id.dt);
        TextView dt1 = (TextView) v.findViewById(R.id.dt1);
        peding.setText("" + app.getUserMetting_firstname() + " " + app.getUserMetting_lastname());

        String s = app.getUserMetting_SenderFromDateTime();
        String s1 = app.getUserMetting_SenderToDateTime();
        String date = s.substring(0, 10);
        String tm = s.substring(10, 16);
        String tm1 = s1.substring(10, 16);
        dt1.setText("Date :" + date);

        dt.setText("Time :" + tm + " - " + tm1);


        return v;
    }


}

