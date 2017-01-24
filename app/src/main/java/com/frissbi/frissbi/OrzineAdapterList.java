package com.frissbi.frissbi;

        import android.content.Context;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.TextView;

        import com.frissbi.R;
        import com.frissbi.locations.Orzine_pojo;

        import java.util.List;

/**
 * Created by KNPL003 on 29-06-2015.
 */


public class OrzineAdapterList extends ArrayAdapter<Orzine_pojo> {
    private List<Orzine_pojo> items;

    public OrzineAdapterList(Context context, List<Orzine_pojo> items) {
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
        ///ImageView imageViewRound;
        if (v == null) {
            LayoutInflater li = LayoutInflater.from(getContext());
            v = li.inflate(R.layout.orzine_iteam, null);
        }

        Orzine_pojo app = items.get(position);

        if (app != null) {
            //ImageView icon = (ImageView)v.findViewById(R.id.appIcon);
            TextView meetingDes = (TextView) v.findViewById(R.id.location_name);
            meetingDes.setText(app.getLocationName());

        }


        return v;
    }


}

