package com.frissbi.Frissbi_Meetings;


    import android.content.Context;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageView;
    import android.widget.TextView;



    import java.util.List;

    import android.widget.ArrayAdapter;

    import com.frissbi.Frissbi_Pojo.Image_Pojo;
    import com.frissbi.R;


    public class Meeting_mulltyrequstfrs_Addapter extends ArrayAdapter<Image_Pojo> {

        ImageView imageViewRound;
        private List<Image_Pojo> items;

        public Meeting_mulltyrequstfrs_Addapter(Context context, List<Image_Pojo> items) {
            super(context, R.layout.meeing_requst_img, items);
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if(v == null) {
                LayoutInflater li = LayoutInflater.from(getContext());
                v = li.inflate(R.layout.meeing_requst_img, null);
                imageViewRound = (ImageView) v.findViewById(R.id.imageView_round);
            }

            Image_Pojo meeting_summaryPojo = items.get(position);


             String image=meeting_summaryPojo.getImage_id();

                if (!image.equals("")) {

                    byte[] encodeByte = android.util.Base64.decode(image, android.util.Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                    imageViewRound.setImageBitmap(bitmap);
                } else if (image.equals("")) {

                    Bitmap bm = BitmapFactory.decodeResource(v.getResources(), R.drawable.pic1);
                    imageViewRound.setImageBitmap(bm);


                }




            return v;
        }
    }

