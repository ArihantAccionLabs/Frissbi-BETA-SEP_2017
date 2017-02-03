package com.frissbi.locations;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.R;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.ServiceHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.widget.AdapterView.OnItemClickListener;

/**
 * Created by KNPL003 on 14-07-2015.
 */

public class NearByPlacess extends Activity {
    public int count1 = 1;
    public int count = 5;

    ListView places;
    List<Placs_pojo> list = new ArrayList<Placs_pojo>();
    JSONArray aJson = null;
    NearByPlacessAdapter adp1;
    Button loadmore, send;
   String Userlocationid;
    String jsonStr;
String MeetingId;
    String userId;
    Dialog dialogp;

    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearbyplaces);
        places = (ListView) findViewById(R.id.places);
        loadmore = (Button) findViewById(R.id.loadmore);
        send = (Button) findViewById(R.id.send);
        cd = new ConnectionDetector(getApplicationContext());
        //Intent intent=getIntent();
//         MeetingId=intent.getExtras().getString("msg3");
        // userId=intent.getExtras().getString("msg4");

       // Intent intent = getIntent();
       /* if (null != intent) {
            MeetingId=intent.getExtras().getString("msg3");
            Friss_Pojo.MeetingID=MeetingId;
            userId=intent.getExtras().getString("msg4");
        }*/

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           isInternetPresent = cd.isConnectedToInternet();
                // check for Internet status
                if (isInternetPresent) {

                    if(!(Userlocationid.equals(""))) {
                        new UserLocationVotingIDes().execute();
                    }else {

                        Toast.makeText(getApplicationContext(),"Select One Place ", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(),"You don't have an internet connection", Toast.LENGTH_SHORT).show();

                }




            }
        });

        loadmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count1 += 5;
                count += 5;
                if (count == 20) {
                   // Toast.makeText(getApplicationContext(), "Places over", Toast.LENGTH_LONG).show();
                    loadmore.setVisibility(view.INVISIBLE);
                }

                isInternetPresent = cd.isConnectedToInternet();
                // check for Internet status
                if (isInternetPresent) {

                    new GetMeetingDetails().execute();


                } else {
                    Toast.makeText(getApplicationContext(),"You don't have an internet connection", Toast.LENGTH_SHORT).show();

                }

            }
        });

        isInternetPresent = cd.isConnectedToInternet();
        // check for Internet status
        if (isInternetPresent) {

            new GetMeetingDetails().execute();


        } else {
            Toast.makeText(getApplicationContext(),"You don't have an internet connection", Toast.LENGTH_SHORT).show();

        }

    }

    public class GetMeetingDetails extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           dialogp = new Dialog(NearByPlacess.this);
            // Include dialog.xml file
            dialogp.getWindow();

            dialogp.setCancelable(false);
            dialogp.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogp.setContentView(R.layout.dailog_box);


            ImageView imgView = (ImageView)dialogp.findViewById(R.id.animationImage);
            imgView.setVisibility(ImageView.VISIBLE);
            imgView.setBackgroundResource(R.drawable.frame_animation);

            AnimationDrawable frameAnimation =(AnimationDrawable) imgView.getBackground();

            if (frameAnimation.isRunning()) {
                frameAnimation.stop();
            }
            else {
                frameAnimation.stop();
                frameAnimation.start();
            }
            dialogp.show();
        }

        @Override
        public String doInBackground(String... params) {


            try {


                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.NEARBY_PLACES + Friss_Pojo.MeetingID + "/" + Friss_Pojo.UseridFrom + "/" + count1 + "/" + count;
                ServiceHandler sh = new ServiceHandler();

                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
                Log.d("url......", url);

                aJson = new JSONArray(jsonStr);
                // create apps list


                for (int i = 0; i < aJson.length(); i++) {
                    JSONObject json = aJson.getJSONObject(i);
                    Placs_pojo app = new Placs_pojo();
                    app.setPlacesname(json.getString("name"));
                    app.setPlacesvicinity(json.getString("formatted_address"));
                    app.setUserLocationVotingID(json.getString("UserLocationVotingID"));
                    app.setDistance(json.getString("distance"));
                    app.setEnabled(json.getString("Enabled"));
                    Log.d("valus......", json.toString());
                    Log.d("valupedig............", json.getString("Enabled"));

                    list.add(app);

                    //swipeRefreshLayout.setRefreshing(false);
                }
                return jsonStr;
                //notify the activity that fetch data has been complete

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }


        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            dialogp.dismiss();
            if (!(list.isEmpty())) {

                adp1 = new NearByPlacessAdapter(getApplicationContext(), list);
                places.setAdapter(adp1);
                places.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        places.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                        Placs_pojo _state = adp1.getItem(position);
                        Userlocationid = _state.getUserLocationVotingID();
                        Toast.makeText(getApplicationContext(), Userlocationid, Toast.LENGTH_LONG).show();

                    }
                });
            }


        }
    }

    private class ViewHolder {
        TextView plename, placesdes, dist;

    }


    public class NearByPlacessAdapter extends ArrayAdapter<Placs_pojo> {
        ViewHolder holder = null;
        private List<Placs_pojo> items;
        public NearByPlacessAdapter(Context context, List<Placs_pojo> items) {
            super(context, R.layout.place_iteams, items);
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater li = LayoutInflater.from(getContext());
                v = li.inflate(R.layout.place_iteams, null);
                holder = new ViewHolder();
                holder.plename = (TextView) v.findViewById(R.id.placesname);
                holder.placesdes = (TextView) v.findViewById(R.id.placesname1);
                holder.dist = (TextView) v.findViewById(R.id.dist);
                v.setTag(holder);

            } else {
                holder = (ViewHolder) v.getTag();
            }
            Placs_pojo state = items.get(position);
            holder.plename.setText(state.getPlacesname());
            holder.placesdes.setText(state.getPlacesvicinity());
            holder.dist.setText(state.getDistance());
            return v;
        }

    }

    public class UserLocationVotingIDes extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialogp = new Dialog(NearByPlacess.this);
            // Include dialog.xml file
            dialogp.getWindow();

            dialogp.setCancelable(false);
            dialogp.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogp.setContentView(R.layout.dailog_box);


            ImageView imgView = (ImageView)dialogp.findViewById(R.id.animationImage);
            imgView.setVisibility(ImageView.VISIBLE);
            imgView.setBackgroundResource(R.drawable.frame_animation);

            AnimationDrawable frameAnimation =(AnimationDrawable) imgView.getBackground();

            if (frameAnimation.isRunning()) {
                frameAnimation.stop();
            }
            else {
                frameAnimation.stop();
                frameAnimation.start();
            }
            dialogp.show();

        }

        @Override
        protected String doInBackground(String... params) {


            try {

                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.USERLOCTION_VOTING +   Friss_Pojo.MeetingID + "/" + Userlocationid;
                ServiceHandler sh = new ServiceHandler();
                Log.d("URL", url);
                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

                Log.d("Response: ", "> " + jsonStr);

                return jsonStr;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String S) {


            super.onPostExecute(S);
            dialogp.dismiss();
            if (jsonStr==null){
                Toast.makeText(getApplication(), "Oops.. Something's not right", Toast.LENGTH_SHORT).show();
            }

           else if (jsonStr.equals("0")) {
                Toast.makeText(getApplication(), "Place selected", Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(getApplicationContext(), Friend_PendingList.class);
                // startActivity(intent);
            } else {

            }

        }
    }

}