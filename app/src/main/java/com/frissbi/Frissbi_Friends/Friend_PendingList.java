package com.frissbi.Frissbi_Friends;

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
import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.R;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.ServiceHandler;
import com.frissbi.frissbi.UserProfileAccept;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KNPL003 on 11-06-2015.
 */
public class Friend_PendingList extends Activity implements Friend_PedinglistAdapter.customButtonListener {
    List<Friend_list_Pojo> list = new ArrayList<Friend_list_Pojo>();
    Friend_PedinglistAdapter adp;
    JSONArray aJson = null;
    ListView pendinglist;
    String jsonStr;
    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;
    private ProgressDialog progressDialog;
    Dialog dialogp;
    private SharedPreferences mSharedPreferences;
    private String mUserId;
    private String mUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pendinglist);
        pendinglist = (ListView) findViewById(R.id.pendinglist);
        mSharedPreferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("USERID_FROM", "editor");
        mUserName = mSharedPreferences.getString("USERNAME_FROM", "editor");
        cd = new ConnectionDetector(getApplicationContext());

        isInternetPresent = cd.isConnectingToInternet();
        // check for Internet status
        if (isInternetPresent) {
            new Pendinglist().execute();


        } else {
            Toast.makeText(getApplicationContext(), "You don't have an internet connection", Toast.LENGTH_SHORT).show();

        }


    }

    @Override
    public void onButtonClickListner(int position, String name) {
        //  Toast.makeText(Friend_PendingList.this, "Button click " + name,
        //  Toast.LENGTH_SHORT).show();

        Friss_Pojo.UserNameTo = name.toString();


        isInternetPresent = cd.isConnectingToInternet();
        // check for Internet status
        if (isInternetPresent) {

            new AcceptFriend().execute();


        } else {
            Toast.makeText(getApplicationContext(), "You don't have an internet connection", Toast.LENGTH_SHORT).show();

        }


    }

    @Override
    public void onButtonClickListner1(int position) {

        Friend_list_Pojo item = (Friend_list_Pojo) adp.getItem(position);
        String userNameFriend = item.getUserName();
        //  Toast.makeText(Friend_PendingList.this, "Button click " + Friss_Pojo.UserNameTo,
        //  Toast.LENGTH_SHORT).show();

        isInternetPresent = cd.isConnectingToInternet();
        // check for Internet status
        if (isInternetPresent) {

            new RejectFriend().execute();


        } else {
            Toast.makeText(getApplicationContext(), "You don't have an internet connection", Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onTextViewonClickListner(int position) {

        Friend_list_Pojo item = (Friend_list_Pojo) adp.getItem(position);

        Intent intent = new Intent(getApplicationContext(), UserProfileAccept.class);
        intent.putExtra(Friss_Pojo.USER_NAME, item.getUserName());
        intent.putExtra(Friss_Pojo.FIRST_NAME, item.getFirstName());
        intent.putExtra(Friss_Pojo.LAST_NAME, item.getLastName());
        intent.putExtra(Friss_Pojo.User_Id, item.getUserId());

        startActivity(intent);
    }

    public class Pendinglist extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogp = new Dialog(Friend_PendingList.this);
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
                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.Peding_List +mUserName;
                ServiceHandler sh = new ServiceHandler();

                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

                Log.d("Response: ", "> " + url);
                Log.d("Response: ", "> " + jsonStr);
                Log.d("valus......", jsonStr.toString());

                aJson = new JSONArray(jsonStr);
                // create apps list


                for (int i = 0; i < aJson.length(); i++) {
                    JSONObject json = aJson.getJSONObject(i);
                    Friend_list_Pojo friendlistPojo = new Friend_list_Pojo();
                    friendlistPojo.setUserName(json.getString("UserName"));
                    friendlistPojo.setFirstName(json.getString("FirstName"));
                    friendlistPojo.setLastName(json.getString("LastName"));
                    friendlistPojo.setEmail(json.getString("EmailName"));
                    friendlistPojo.setAvatarPath(json.getString("AvatarPath"));
                    friendlistPojo.setUserId(json.getString("UserId"));
                    Log.d("valupedig............", json.getString("UserId"));

                    list.add(friendlistPojo);

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

            if (list.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Oops.. Something's not right", Toast.LENGTH_LONG).show();

            } else {

                adp = new Friend_PedinglistAdapter(getApplicationContext(), list);
                pendinglist.setAdapter(adp);
                adp.setCustomButtonListner(Friend_PendingList.this);
            }
           /* pendinglist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                    Friend_list_Pojo item = (Friend_list_Pojo) adp.getItem(position);

                    Intent intent = new Intent(getApplicationContext(), UserProfileAccept.class);
                    intent.putExtra(Friss_Pojo.USER_NAME, item.getUserName());
                    intent.putExtra(Friss_Pojo.FIRST_NAME, item.getFirstName());
                    intent.putExtra(Friss_Pojo.LAST_NAME, item.getLastName());
                    intent.putExtra(Friss_Pojo.User_Id, item.getUserId());
                    intent.putExtra(Friss_Pojo.EMAIL_ADDRESS, item.getEmail());
                    Friss_Pojo.AvatarPathTo = item.getAvatarPath();

                    startActivity(intent);


                }
            });*/
            dialogp.dismiss();

        }
    }

    public class AcceptFriend extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.APPROVE_FRIEND + Friss_Pojo.UserNameTo + "/" + Friss_Pojo.UserNameFrom;
                ServiceHandler sh = new ServiceHandler();

                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

                Log.d("URL: ", "> " + url);
                Log.d("Response: ", "> " + jsonStr);

                Log.d("valus......", jsonStr.toString());


                //add_frd=Integer.parseInt(jsonStr);
                return jsonStr.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String succes) {
            super.onPostExecute(succes);

            if (jsonStr == null) {
                Toast.makeText(getApplication(), "Oops.. Something's not right", Toast.LENGTH_SHORT).show();
            } else if (jsonStr.equals("1")) {
                //add.setText("friend request");
                // Toast.makeText(getApplication(), "Friend Accept", Toast.LENGTH_LONG).show();

            }

        }
    }

    public class RejectFriend extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.REJCT_FRIEND + Friss_Pojo.UserNameFrom + "/" + Friss_Pojo.UserNameTo;
                ServiceHandler sh = new ServiceHandler();

                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

                Log.d("URL: ", "> " + url);
                Log.d("Response: ", "> " + jsonStr);

                Log.d("valus......", jsonStr.toString());


                //add_frd=Integer.parseInt(jsonStr);
                return jsonStr.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String succes) {
            super.onPostExecute(succes);

            if (jsonStr == null) {
                Toast.makeText(getApplication(), "Oops.. Something's not right", Toast.LENGTH_SHORT).show();
            } else if (jsonStr.equals("1")) {
                //add.setText("friend request");
                //Toast.makeText(getApplication(), "REJCT FRIEND", Toast.LENGTH_LONG).show();

            }

        }
    }


}
