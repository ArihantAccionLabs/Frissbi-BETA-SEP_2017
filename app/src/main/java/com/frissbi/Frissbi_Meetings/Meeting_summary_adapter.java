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

        import com.frissbi.Frissbi_Pojo.Friend_list_Pojo;
import com.frissbi.Frissbi_Pojo.Meeting_summaryPojo;
import com.frissbi.R;


public class Meeting_summary_adapter extends ArrayAdapter<Meeting_summaryPojo> {

    ImageView imageViewRound;
    private List<Meeting_summaryPojo> items;

    public Meeting_summary_adapter(Context context, List<Meeting_summaryPojo> items) {
        super(context, R.layout.meeting_summary_iteam, items);
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
            v = li.inflate(R.layout.meeting_summary_iteam, null);
            imageViewRound = (ImageView) v.findViewById(R.id.imageView_round);
        }

        Meeting_summaryPojo meeting_summaryPojo = items.get(position);

            TextView name = (TextView) v.findViewById(R.id.textname);
            String firstname=meeting_summaryPojo.getFirstName();
            String firstname1=firstname.substring(0, 1);
            String lastname=meeting_summaryPojo.getLastName();
            String lastname1=lastname.substring(0, 1);
            name.setText(firstname1.toUpperCase()+lastname1.toUpperCase());
            String image=meeting_summaryPojo.getAvatarPath();
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
