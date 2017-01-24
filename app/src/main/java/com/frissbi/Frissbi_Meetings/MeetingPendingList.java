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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.frissbi.Frissbi_Pojo.Friend_list_Pojo;
import com.frissbi.R;
import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.Utility.ServiceHandler;
import com.frissbi.frissbi.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.widget.AdapterView.OnItemClickListener;
/**
 * Created by KNPL003 on 21-08-2015.
 */

/**
 * Created by KNPL003 on 11-06-2015.
 */
public class MeetingPendingList extends Activity {
    private ProgressDialog progressDialog;
    List<MeetingPending_pojo> list = new ArrayList<MeetingPending_pojo>();
    JSONArray aJson = null;
    MeetingPending_adapter adp;
    ListView pendinglist;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String jsonStr;
    Dialog dialogp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_pendinglist);
        pendinglist = (ListView) findViewById(R.id.pendinglist);
        new MeetingPending().execute();

        preferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        editor = preferences.edit();
        String userid = preferences.getString("USERID_FROM", "editor");
        String user_name = preferences.getString("USERNAME_FROM", "editor");
        Friss_Pojo.UseridFrom = userid;
        Friss_Pojo.UserNameFrom = user_name;
        Log.d("value is", userid);
        editor.commit();
    }

    public class MeetingPending extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogp = new Dialog(MeetingPendingList.this);
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
                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.MEETING_PENDINGLIST + Friss_Pojo.UseridFrom;
                ServiceHandler sh = new ServiceHandler();

                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
                aJson = new JSONArray(jsonStr.toString());
                // create apps list
                for (int i = 0; i < aJson.length(); i++) {
                    JSONObject json = aJson.getJSONObject(i);
                    MeetingPending_pojo app = new MeetingPending_pojo();
                    app.setUserMetting_firstname(json.getString("FirstName"));
                    app.setUserMetting_lastname(json.getString("LastName"));
                    app.setUserMetting_MeetingID(json.getString("MeetingID"));
                    //app.setEmailName(json.getString("EmailName"));
                    app.setUserMetting_SenderFromDateTime(json.getString("SenderFromDateTime"));
                    app.setUserMetting_SenderToDateTime(json.getString("SenderToDateTime"));
                    Log.d("valupedig............", json.getString("MeetingID"));
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
            dialogp.dismiss();
            if (list.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Oops.. Something's not right", Toast.LENGTH_LONG).show();


            } else {

                adp = new MeetingPending_adapter(getApplicationContext(), list);
                pendinglist.setAdapter(adp);


            }
            pendinglist.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MeetingPending_pojo item = (MeetingPending_pojo) adp.getItem(position);
                    Intent intent = new Intent(getApplicationContext(), MeetingAcept.class);

                    // intent.putExtra(Friss_Pojo.MEETING_ID,item.getUserMetting_MeetingID());
                    Friss_Pojo.MeetingID = item.getUserMetting_MeetingID();
                    startActivity(intent);


                }
            });


        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
