package com.frissbi.Frissbi_Meetings;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.frissbi.R;


import java.util.List;


/**
 * Created by KNPL003 on 24-06-2015.
 */


public class Meetingdetailsadapter extends ArrayAdapter<MeetingConsrants> {
    private List<MeetingConsrants> items;

    public Meetingdetailsadapter(Context context, List<MeetingConsrants> items) {
        super(context, R.layout.meetingdetails_iteam, items);
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
            v = li.inflate(R.layout.meetingdetails_iteam, null);
        }

        MeetingConsrants app = items.get(position);

        if (app != null) {
            //ImageView icon = (ImageView)v.findViewById(R.id.appIcon);
            TextView meetingDes = (TextView) v.findViewById(R.id.meetingdes);
            TextView date = (TextView) v.findViewById(R.id.date);
            TextView place1 = (TextView) v.findViewById(R.id.place1);
            meetingDes.setText(app.getMeetingDescription());
            place1.setText(app.getGoogleadrres());

            String SenderDate = app.getSenderFromDateTime();
            Log.d("Value", SenderDate);
            String SenderDate1 = SenderDate.substring(0, 10);
            String SenderDate2 = SenderDate.substring(10, 16);

            String Timesender = app.getSenderToDateTime();
            String Timesender1 = Timesender.substring(0, 10);
            String Timesender2 = Timesender.substring(10, 16);
            date.setText(SenderDate2 + "-" + Timesender2);



    }


        return v;
    }


}

