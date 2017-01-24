package com.frissbi.Frissbi_Meetings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.Utility.ServiceHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by KNPL003 on 21-08-2015.
 */

    /**
     * Created by KNPL003 on 11-06-2015.
     */
    public class ConflictedMeetingDetails extends Activity {
        private ProgressDialog progressDialog;
        List<Meeting_ConflictPojo> list=new ArrayList<Meeting_ConflictPojo>();
        JSONArray aJson=null;
        Meeting_Conflictadapter adp;
        ListView conflictlist;
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        String jsonStr;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.meeting_conflictedlist);
            conflictlist= (ListView) findViewById(R.id.conflict);

            new ConflictedMeetingDetailsList().execute();

            preferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
            editor = preferences.edit();
            String userid = preferences.getString("USERID_FROM", "editor");
            String user_name = preferences.getString("USERNAME_FROM", "editor");
            Friss_Pojo.UseridFrom= userid;
            Friss_Pojo.UserNameFrom=user_name;
            Log.d("value is", userid);
            editor.commit();
        }
        public class ConflictedMeetingDetailsList extends AsyncTask<String, String, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                progressDialog = new ProgressDialog(ConflictedMeetingDetails.this);
                progressDialog.setTitle("Loading Frissbi Data........................");
                progressDialog.setIndeterminate(false);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            public String doInBackground(String... params) {
                   try{


                    String url = Friss_Pojo.REST_URI+"/"+"rest"+Friss_Pojo.MEETING_CONFLICT + Friss_Pojo.UseridFrom;
                    ServiceHandler sh = new ServiceHandler();

                    // Making a1 request to url and getting response
                    jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
                   aJson = new JSONArray(jsonStr.toString());
                    // create apps list



                    for (int i = 0; i < aJson.length(); i++) {
                        JSONObject json = aJson.getJSONObject(i);
                        Meeting_ConflictPojo  app = new Meeting_ConflictPojo();

                        app.setMeetingDescription(json.getString("MeetingDescription"));
                        app.setSenderFromDateTime(json.getString("SenderFromDateTime"));
                        app.setSenderToDateTime(json.getString("SenderToDateTime"));

                         list.add(app);

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

                if(list.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Data Not Found", Toast.LENGTH_LONG).show();


                }else {

                    adp = new Meeting_Conflictadapter(getApplicationContext(), list);
                    conflictlist.setAdapter(adp);
                }

                progressDialog.dismiss();

            }
        }

    }
