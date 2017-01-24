package com.frissbi.Frissbi_Friends;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.frissbi.Frissbi_Pojo.Friend_list_Pojo;
import com.frissbi.R;

import java.util.List;

/**
 * Created by KNPL003 on 24-06-2015.
 */

import android.widget.ImageView;


public class Friend_PedinglistAdapter extends ArrayAdapter<Friend_list_Pojo> {
    Button accept;
    Button ignore;
    TextView peding;
    private List<Friend_list_Pojo> items;
    customButtonListener customListner;

    public interface customButtonListener {

        public void onButtonClickListner(int position,String name);
        public void onButtonClickListner1(int position);
        public void onTextViewonClickListner(int position);
    }

    public void setCustomButtonListner(customButtonListener listener) {
        this.customListner = listener;
    }
  public Friend_PedinglistAdapter(Context context, List<Friend_list_Pojo> items) {
        super(context, R.layout.serch_iteam, items);
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
         ImageView imageViewRound;
        if (v == null) {
            LayoutInflater li = LayoutInflater.from(getContext());
            v = li.inflate(R.layout.pending_iteam, null);
        }

        Friend_list_Pojo friendlistPojo = items.get(position);

        if (friendlistPojo != null) {
            //ImageView icon = (ImageView)v.findViewById(R.id.appIcon);
             peding = (TextView) v.findViewById(R.id.peding);

             accept=(Button)v.findViewById(R.id.accept);
              ignore=(Button)v.findViewById(R.id.ignore);
             peding.setText("" + friendlistPojo.getFirstName() + " " + friendlistPojo.getLastName());


            String image=friendlistPojo.getAvatarPath();
            imageViewRound = (ImageView) v.findViewById(R.id.imageView_round);

            if(!image.equals("")){

                byte [] encodeByte= android.util.Base64.decode(image, android.util.Base64.DEFAULT);
                Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);



                imageViewRound.setImageBitmap(bitmap);
            }
            else  if(image.equals("")){

                Bitmap bm = BitmapFactory.decodeResource(v.getResources(), R.drawable.pic1);
                imageViewRound.setImageBitmap(bm);



            }


        }

        peding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Friend_list_Pojo friendlistPojo = items.get(position);
                String temp= friendlistPojo.getUserId();

                if (customListner != null) {
                    customListner.onTextViewonClickListner(position);
                }

            }
        });
        accept.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Friend_list_Pojo friendlistPojo = items.get(position);
                String temp= friendlistPojo.getUserName();
                if (customListner != null) {
                    customListner.onButtonClickListner(position,temp);
                }

            }
        });
        ignore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Friend_list_Pojo friendlistPojo = items.get(position);
                String temp= friendlistPojo.getUserName();
                if (customListner != null) {
                    customListner.onButtonClickListner1(position);
                }

            }
        });

        return v;
    }


}

