package com.frissbi.Frissbi_Meetings;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.Frissbi_Pojo.Meetingrequest_Pojo;
import com.frissbi.R;
import com.frissbi.Utility.ServiceHandler;
import com.frissbi.frissbi.OrzineAdapterList;
import com.frissbi.locations.Orzine_pojo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by KNPL003 on 22-08-2015.
 */

public class MeetingendTime extends Activity {
    public static String senderdate;
    EditText endtime;
    TextView setplace;
    OrzineAdapterList adp1;
    List<Orzine_pojo> list = new ArrayList<Orzine_pojo>();
    JSONArray aJson = null;
    ListView orzinelist;
    LinearLayout setonmap, myplace, flexdate;
    Button submit;
    Dialog dialog;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String jsonStr;
    Dialog dialogp;
    private Calendar cal1;
    private int day;
    private int month;
    private int year;
    private ProgressDialog pDialog, progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orzineset);


        submit = (Button) findViewById(R.id.send);
        setonmap = (LinearLayout) findViewById(R.id.setonmap);
        myplace = (LinearLayout) findViewById(R.id.myplace);

        orzinelist = (ListView) findViewById(R.id.orlist);
        setplace = (TextView) findViewById(R.id.setplace);
        preferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        editor = preferences.edit();
        String userid = preferences.getString("USERID_FROM", "editor");
        String user_name = preferences.getString("USERNAME_FROM", "editor");
        Friss_Pojo.UseridFrom = userid;
        Friss_Pojo.UserNameFrom = user_name;
        Log.d("value is", userid);
        editor.commit();


        setonmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setmap = new Intent(getApplicationContext(), com.frissbi.MapLocations.SetLocationMap.class);
                startActivity(setmap);
                Meetingrequest_Pojo.Meeting_DestinationType = "3";


            }

        });
        myplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent setmap=new Intent(getApplicationContext(),com.frissbi.MapLocations.SetLocationMap.class);
                //startActivity(setmap);
                Meetingrequest_Pojo.Meeting_DestinationType = "3";

                if (!(list.isEmpty())) {

                    adp1 = new OrzineAdapterList(getApplicationContext(), list);
                    orzinelist.setAdapter(adp1);
                    orzinelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                            Orzine_pojo item = (Orzine_pojo) adp1.getItem(position);

                            setplace.setText(item.getLocationName());

                            Meetingrequest_Pojo.Meeting_Longitude = Double.parseDouble(item.getLongitude());
                            Meetingrequest_Pojo.Meeting_Latitude = Double.parseDouble(item.getLatitude());
                            Meetingrequest_Pojo.UserPreferredLocationId = item.getUserPreferredLocationID();


                        }
                    });

                }else if (list.isEmpty()) {
                    new From_Orzin().execute();
                }


            }

        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                finish();
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();


    }

    public class From_Orzin extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
          dialogp = new Dialog(MeetingendTime.this);
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

                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.ORZIN_DESTLIST + Friss_Pojo.UseridFrom;
                ServiceHandler sh = new ServiceHandler();

                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

                Log.d("Response: ", "> " + jsonStr);

                aJson = new JSONArray(jsonStr.toString());
                // create apps list


                for (int i = 0; i < aJson.length(); i++) {
                    JSONObject json = aJson.getJSONObject(i);
                    Orzine_pojo orzine = new Orzine_pojo();

                    orzine.setIsDefault(json.getString("IsDefault"));
                    orzine.setLatitude(json.getString("Latitude"));
                    orzine.setLongitude(json.getString("Longitude"));
                    orzine.setLocationType(json.getString("LocationType"));
                    orzine.setUserID(json.getString("UserID"));
                    orzine.setUserPreferredLocationID(json.getString("UserPreferredLocationID"));
                    orzine.setLocationName(json.getString("LocationName"));
                    Log.d("valupedig............", json.getString("LocationName"));

                    Log.d("valupedig............", json.toString());

                    list.add(orzine);

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

          if (!(list.isEmpty())){
              adp1 = new OrzineAdapterList(getApplicationContext(), list);
              orzinelist.setAdapter(adp1);

              orzinelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                  @Override
                  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                      Orzine_pojo item = (Orzine_pojo) adp1.getItem(position);

                      setplace.setText(item.getLocationName());

                      Meetingrequest_Pojo.Meeting_Longitude = Double.parseDouble(item.getLongitude());
                      Meetingrequest_Pojo.Meeting_Latitude = Double.parseDouble(item.getLatitude());
                      Meetingrequest_Pojo.UserPreferredLocationId = item.getUserPreferredLocationID();


                  }
              });
          }else {


              Toast.makeText(getApplicationContext(),"Oops.. Something's not right",Toast.LENGTH_LONG).show();
          }



        }

    }


}
