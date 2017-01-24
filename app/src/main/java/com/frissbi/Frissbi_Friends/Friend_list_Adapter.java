package com.frissbi.Frissbi_Friends;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.frissbi.Frissbi_Pojo.Friend_list_Pojo;
import com.frissbi.R;
import java.util.List;
    public class Friend_list_Adapter extends ArrayAdapter<Friend_list_Pojo> {
        private List<Friend_list_Pojo> items;

        public Friend_list_Adapter(Context context, List<Friend_list_Pojo> items) {
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
            ImageView imageViewRound = null;
            if (v == null) {
                LayoutInflater li = LayoutInflater.from(getContext());
                v = li.inflate(R.layout.serch_iteam, null);
            }

            Friend_list_Pojo friendlistPojo = items.get(position);
//dhkhjkljdoiodid
            if (friendlistPojo != null) {
                //ImageView icon = (ImageView)v.findViewById(R.id.appIcon);
                TextView name = (TextView) v.findViewById(R.id.text);
                imageViewRound = (ImageView) v.findViewById(R.id.imageView_round);
                name.setText("" + friendlistPojo.getFirstName() + " " + friendlistPojo.getLastName());

                String image = friendlistPojo.getAvatarPath();
                if (!image.equals("")) {

                    byte[] encodeByte = android.util.Base64.decode(image, android.util.Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);


                    imageViewRound.setImageBitmap(bitmap);
                } else if (image.equals("")) {

                    Bitmap bm = BitmapFactory.decodeResource(v.getResources(), R.drawable.pic1);
                    imageViewRound.setImageBitmap(bm);


                }

            }


            return v;
        }


    }
