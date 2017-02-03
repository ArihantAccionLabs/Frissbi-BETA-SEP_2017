package com.frissbi.Frissbi_Meetings;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.GcmIntentService;
import com.frissbi.R;
import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.Frissbi_Pojo.Meetingrequest_Pojo;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.ServiceHandler;
import com.frissbi.frissbi.OrzineAdapterList;
import com.frissbi.locations.Orzine_pojo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KNPL003 on 17-08-2015.
 */

public class MeetingAcept extends Activity {
    public static String RecipientFromDateTime;
    public static String RecipientToDateTime;
    OrzineAdapterList adp1;
    ListView orzinelist, des_list;
    List<Orzine_pojo> list = new ArrayList<Orzine_pojo>();
    TextView getdate, getdate1, place, textset, anyplace;
    Button done, reject, accept1;
    LinearLayout setonmap, myplace, des_setonmap, flex, defined, flexdate;
    EditText flextime;
    Integer Status;
    Integer update_meet;
    JSONArray aJson = null;
    ImageButton editplace, orzine_placebutton;
    String jsonStr;

    List<Meeting_ConflictPojo> list1 = new ArrayList<Meeting_ConflictPojo>();
    Meeting_Conflictadapter adp;
    ListView conflictlist;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    private ProgressDialog progressDialog;
    private ProgressDialog pDialog;
    Dialog dialogp;
    GcmIntentService srv;
    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;
    private long mMeetingId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meetingaccptpage);
        getdate = (TextView) findViewById(R.id.getdate);
        getdate1 = (TextView) findViewById(R.id.getdate1);
        place = (TextView) findViewById(R.id.place);

        reject = (Button) findViewById(R.id.reject);
        editplace = (ImageButton) findViewById(R.id.editplace);

        accept1 = (Button) findViewById(R.id.accept);
        cd = new ConnectionDetector(getApplicationContext());
        mMeetingId = getIntent().getExtras().getLong("meetingId");
     /*   preferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        editor = preferences.edit();
        String userid = preferences.getString("USERID_FROM", "editor");
        String user_name = preferences.getString("USERNAME_FROM", "editor");
        Friss_Pojo.UseridFrom = userid;
        Friss_Pojo.UserNameFrom = user_name;
        Log.d("value is", userid);
        Log.d("value is", Friss_Pojo.MeetingID);

        editor.commit();*/


      /*  Intent intent = getIntent();
        if (null != intent) {
            Meetingid = intent.getStringExtra(Friss_Pojo.MEETING_ID);
        }*/

        new SingalMeetingDetails().execute();


        accept1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Status = 1;

                if (!(getdate.getText().equals("")) && (!(getdate1.getText().equals(""))) && (!(place.getText().equals("")))) {

                    isInternetPresent = cd.isConnectedToInternet();
                    // check for Internet status
                    if (isInternetPresent) {

                        new Conflict().execute();


                    } else {
                        Toast.makeText(getApplicationContext(), "You don't have an internet connection", Toast.LENGTH_SHORT).show();

                    }


                } else {

                    Toast.makeText(getApplicationContext(), "One or more fields are empty", Toast.LENGTH_SHORT).show();
                }

            }
        });

        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Status = 0;


                if (!(getdate.getText().equals("")) && (!(getdate1.getText().equals(""))) && (!(place.getText().equals("")))) {

                    isInternetPresent = cd.isConnectedToInternet();
                    // check for Internet status
                    if (isInternetPresent) {

                        new MeetingUpdates().execute();


                    } else {
                        Toast.makeText(getApplicationContext(), "You don't have an internet connection", Toast.LENGTH_SHORT).show();

                    }


                } else {

                    Toast.makeText(getApplicationContext(), "One or more fields are empty", Toast.LENGTH_SHORT).show();
                }


            }
        });


        editplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog dialog = new Dialog(MeetingAcept.this);
                // Include dialog.xml file
                dialog.getWindow();
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.setdistancey);
                des_setonmap = (LinearLayout) dialog.findViewById(R.id.des_setonmap);
                flex = (LinearLayout) dialog.findViewById(R.id.flex);
                defined = (LinearLayout) dialog.findViewById(R.id.defined);
                done = (Button) dialog.findViewById(R.id.send);
                textset = (TextView) dialog.findViewById(R.id.textset);

                done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                des_list = (ListView) dialog.findViewById(R.id.des_list);

                flex.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent setmap = new Intent(getApplicationContext(), MeetingendTime.class);
                        startActivity(setmap);
                        Meetingrequest_Pojo.Meeting_Latitude_To = null;
                        Meetingrequest_Pojo.Meeting_Longitude_To = null;
                        place.setText("Anyplace");


                    }
                });

                des_setonmap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent setmap = new Intent(getApplicationContext(), com.frissbi.MapLocations.ToLocationsformap.class);
                        startActivity(setmap);
                        place.setText("Selected Location");


                    }

                });
                defined.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Intent setmap=new Intent(getApplicationContext(),com.frissbi.MapLocations.SetLocationMap.class);
                        // startActivity(setmap);
                        if (!(list.isEmpty())) {

                            adp1 = new OrzineAdapterList(getApplicationContext(), list);
                            des_list.setAdapter(adp1);

                            des_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Orzine_pojo item = (Orzine_pojo) adp1.getItem(position);
                                    textset.setText(item.getLocationName());

                                    Meetingrequest_Pojo.Meeting_Latitude_To = Double.parseDouble(item.getLongitude());
                                    Meetingrequest_Pojo.Meeting_Longitude_To = Double.parseDouble(item.getLatitude());
                                    Meetingrequest_Pojo.UserPreferredLocationId = item.getUserPreferredLocationID();


                                }
                            });
                        } else if (list.isEmpty()) {
                            new Destinaen().execute();
                        }
                    }

                });

                dialog.show();
            }
        });


    }

    public class MeetingUpdates extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogp = new Dialog(MeetingAcept.this);
            // Include dialog.xml file
            dialogp.getWindow();
            dialogp.setCancelable(false);
            dialogp.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogp.setContentView(R.layout.dailog_box);
            ImageView imgView = (ImageView) dialogp.findViewById(R.id.animationImage);
            imgView.setVisibility(ImageView.VISIBLE);
            imgView.setBackgroundResource(R.drawable.frame_animation);

            AnimationDrawable frameAnimation = (AnimationDrawable) imgView.getBackground();

            if (frameAnimation.isRunning()) {
                frameAnimation.stop();
            } else {
                frameAnimation.stop();
                frameAnimation.start();
            }
            dialogp.show();


        }

        @Override
        protected String doInBackground(String... params) {

            try {

                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.MEETING_UPDATE + Friss_Pojo.MeetingID + "/" + Friss_Pojo.UseridFrom + "/" + RecipientFromDateTime + "/" + RecipientToDateTime + "/" + Status + "/"
                        + null + "/" + Meetingrequest_Pojo.Meeting_geoDateTime + "/" + Meetingrequest_Pojo.Meeting_Latitude_To + "/" + Meetingrequest_Pojo.Meeting_Longitude_To + "/" +
                        Meetingrequest_Pojo.Meeting_Latitude + "/" + Meetingrequest_Pojo.Meeting_Longitude + "/" + Meetingrequest_Pojo.Meeting_DestinationType;

                url = url.replace(" ", "%20");
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
            if (jsonStr.equals("0")) {
                if (Status == 1) {
                    Toast.makeText(getApplication(), "You accepted this meeting", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplication(), "You rejected this meeting", Toast.LENGTH_LONG).show();
                }

            } else if (jsonStr.equals("")) {
                Toast.makeText(getApplication(), "You accepted this meeting", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(getApplication(), "Pending Status", Toast.LENGTH_LONG).show();
            }

        }
    }


    public class SingalMeetingDetails extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialogp = new Dialog(MeetingAcept.this);
            // Include dialog.xml file
            dialogp.getWindow();
            dialogp.setCancelable(false);
            dialogp.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogp.setContentView(R.layout.dailog_box);
            ImageView imgView = (ImageView) dialogp.findViewById(R.id.animationImage);
            imgView.setVisibility(ImageView.VISIBLE);
            imgView.setBackgroundResource(R.drawable.frame_animation);

            AnimationDrawable frameAnimation = (AnimationDrawable) imgView.getBackground();

            if (frameAnimation.isRunning()) {
                frameAnimation.stop();
            } else {
                frameAnimation.stop();
                frameAnimation.start();
            }
            dialogp.show();
        }

        @Override
        public String doInBackground(String... params) {


            try {

                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.MEETING_SINGALDETAILS + mMeetingId;
                ServiceHandler sh = new ServiceHandler();
                Log.d("url: ", "> " + url);
                Log.d("Response: ", "> " + jsonStr);
                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

                Log.d("Response: ", "> " + jsonStr);

                //  JSONObject json = new JSONObject(jsonStr);


                // JSONObject json = new JSONObject(getOutputAsString(addService));
                Log.d("valus......", jsonStr.toString());
                // convert json string to json array
                try {
                    aJson = new JSONArray(jsonStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // create apps list


                return jsonStr;
                //notify the activity that fetch data has been complete

            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            dialogp.dismiss();
            try {


                for (int i = 0; i < aJson.length(); i++) {
                    JSONObject json = aJson.getJSONObject(i);
                    MeetingConsrants app = new MeetingConsrants();

                    app.setMeetingDescription(json.getString("MeetingDescription"));
                    app.setMeetingID(json.getString("MeetingID"));
                    //app.setEmailName(json.getString("EmailName"));
                    // app.setScheduledDateTime(json.getString("ScheduledDateTime"));

                    app.setSenderFromDateTime(json.getString("SenderFromDateTime"));
                    app.setSenderToDateTime(json.getString("SenderToDateTime"));
                    app.setRequestDateTime(json.getString("RequestDateTime"));
                    app.setScheduledTimeSlot(json.getString("ScheduledTimeSlot"));
                    Log.d("valupedig............", json.getString("MeetingID"));
                    Log.d("valupedig............", json.toString());
                    String todate = app.getSenderToDateTime();
                    String date = todate.substring(0, 10);
                    String timeto = todate.substring(10, 16);
                    String fromdate = app.getSenderFromDateTime();
                    //String date = fromdate.substring(0, 10);
                    String timefrom = fromdate.substring(10, 16);
                    Log.d("valupedig............", date);
                    getdate.setText(date);
                    getdate1.setText(timefrom + "-" + timeto);
                    RecipientFromDateTime = app.getSenderToDateTime();
                    RecipientToDateTime = app.getSenderFromDateTime();

                    // list.add(app);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }


    public class Destinaen extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogp = new Dialog(MeetingAcept.this);
            // Include dialog.xml file
            dialogp.getWindow();
            dialogp.setCancelable(false);
            dialogp.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogp.setContentView(R.layout.dailog_box);
            ImageView imgView = (ImageView) dialogp.findViewById(R.id.animationImage);
            imgView.setVisibility(ImageView.VISIBLE);
            imgView.setBackgroundResource(R.drawable.frame_animation);

            AnimationDrawable frameAnimation = (AnimationDrawable) imgView.getBackground();

            if (frameAnimation.isRunning()) {
                frameAnimation.stop();
            } else {
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
                aJson = new JSONArray(jsonStr);
                aJson = new JSONArray(jsonStr);
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
            adp1 = new OrzineAdapterList(getApplicationContext(), list);
            des_list.setAdapter(adp1);

            des_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                    Orzine_pojo item = (Orzine_pojo) adp1.getItem(position);
                    textset.setText(item.getLocationName());
                    place.setText(item.getLocationName());
                    Meetingrequest_Pojo.Meeting_Latitude_To = Double.parseDouble(item.getLongitude());
                    Meetingrequest_Pojo.Meeting_Longitude_To = Double.parseDouble(item.getLatitude());
                    Meetingrequest_Pojo.UserPreferredLocationId = item.getUserPreferredLocationID();

                }
            });


        }
    }

    public class Conflict extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogp = new Dialog(MeetingAcept.this);
            // Include dialog.xml file
            dialogp.getWindow();

            dialogp.setCancelable(false);
            dialogp.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogp.setContentView(R.layout.dailog_box);
            ImageView imgView = (ImageView) dialogp.findViewById(R.id.animationImage);
            imgView.setVisibility(ImageView.VISIBLE);
            imgView.setBackgroundResource(R.drawable.frame_animation);

            AnimationDrawable frameAnimation = (AnimationDrawable) imgView.getBackground();

            if (frameAnimation.isRunning()) {
                frameAnimation.stop();
            } else {
                frameAnimation.stop();
                frameAnimation.start();
            }
            dialogp.show();

        }

        @Override
        public String doInBackground(String... params) {
            try {
                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.MEETING_CONFLICT + Friss_Pojo.MeetingID + "/" + Friss_Pojo.UseridFrom + "/" + RecipientFromDateTime + "/" + RecipientToDateTime;
                url = url.replace(" ", "%20");
                ServiceHandler sh = new ServiceHandler();
                Log.d("Response: ", "> " + url);
                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
                aJson = new JSONArray(jsonStr.toString());
                // create apps list
                for (int i = 0; i < aJson.length(); i++) {
                    JSONObject json = aJson.getJSONObject(i);
                    Meeting_ConflictPojo app = new Meeting_ConflictPojo();
                    app.setMeetingDescription(json.getString("MeetingDescription"));
                    app.setSenderFromDateTime(json.getString("SenderFromDateTime"));
                    app.setSenderToDateTime(json.getString("SenderToDateTime"));
                    list1.add(app);

                }
                return jsonStr.toString();
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
            if (list1.isEmpty()) {
                // Toast.makeText(getApplicationContext(), "Data Not Found", Toast.LENGTH_LONG).show();

                new MeetingUpdates().execute();
            } else {
                final Dialog dialog = new Dialog(MeetingAcept.this);
                // Include dialog.xml file
                Button done;
                dialog.getWindow();
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.meeting_conflictedlist);
                conflictlist = (ListView) dialog.findViewById(R.id.orlist);
                Button over = (Button) dialog.findViewById(R.id.over);
                Button ing = (Button) dialog.findViewById(R.id.igno);
                over.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                ing.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Status = 0;
                        new MeetingUpdates().execute();
                    }
                });
                adp = new Meeting_Conflictadapter(getApplicationContext(), list1);
                conflictlist.setAdapter(adp);
            }


        }
    }


}
