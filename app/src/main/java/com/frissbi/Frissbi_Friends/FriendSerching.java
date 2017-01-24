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

import android.os.Handler;
import android.util.Log;
import android.view.View;


import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.Frissbi_Meetings.Meets;
import com.frissbi.Frissbi_Pojo.Friend_list_Pojo;
import com.frissbi.Frissbi_profilePic.Profile_Pic;
import com.frissbi.R;
import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.ServiceHandler;
import com.frissbi.frissbi.Login;
import com.frissbi.frissbi.Update_profile;
import com.frissbi.frissbi.UserProfile;
import com.navdrawer.SimpleSideDrawer;
import static android.widget.AdapterView.OnItemClickListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FriendSerching extends Activity {
    WebView web, web1;
    private boolean backPressedToExitOnce = false;
    Friend_list_Adapter adp1;
    List<Friend_list_Pojo> list1 = new ArrayList<Friend_list_Pojo>();
    private ListView serch;
    private ProgressDialog progressDialog;
    EditText serchtext = null;
    Button SerchButton, menu, invite;
    private Friend_Serching_adapter adp;
    List<Friend_list_Pojo> list = new ArrayList<Friend_list_Pojo>();
    JSONArray aJson = null;
    String jsonStr, jsonStr1;
    int selectItem;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    private SimpleSideDrawer mNav;
    String value;
    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;
    Dialog dialogp;
    TextView home, meeting, friends, profile, setting, lagout;
    com.frissbi.Utility.MyTextView user_name1;
    LinearLayout home1, meeting1, friends1, seeting1, logout1, profilepic1;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serch);
        serch = (ListView) findViewById(R.id.serch);
        serchtext = (EditText) findViewById(R.id.serchtext);
        SerchButton = (Button) findViewById(R.id.serchbutton);
        invite = (Button) findViewById(R.id.invite);
        menu = (Button) findViewById(R.id.menu);

        cd = new ConnectionDetector(getApplicationContext());
        mNav = new SimpleSideDrawer(this);
        mNav.setLeftBehindContentView(R.layout.activity_behind_left_simple);
        mNav.toggleLeftDrawer();

      /*  serchtext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {




            }

            @Override
            public void afterTextChanged(Editable s) {




                try {
                    if (value.trim().length()>3){
                        new FriendSerch().execute();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }





            }
        });*/


        invite.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
//					share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
                //add a1 subject
                share.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                //build the body of the message to be shared
                String body_details;
                body_details = "Hi, I'd like you to try FRISSBI" + "\n" + getResources().getString(R.string.link) + "\n";
                //add the message
                share.putExtra(android.content.Intent.EXTRA_TEXT, body_details);
                startActivity(Intent.createChooser(share, "body"));

            }
        });


        preferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        editor = preferences.edit();
        Friss_Pojo.UseridFrom = preferences.getString("USERID_FROM", "editor");
        Friss_Pojo.UserNameFrom = preferences.getString("USERNAME_FROM", "editor");
        editor.commit();

        user_name1 = (com.frissbi.Utility.MyTextView) findViewById(R.id.user_name);
        meeting1 = (LinearLayout) findViewById(R.id.meeting1);
        profilepic1 = (LinearLayout) findViewById(R.id.profilepic1);
        seeting1 = (LinearLayout) findViewById(R.id.seeting1);
        logout1 = (LinearLayout) findViewById(R.id.logout1);
        home1 = (LinearLayout) findViewById(R.id.home1);
        friends1 = (LinearLayout) findViewById(R.id.friends1);
        user_name1.setText(Friss_Pojo.UserNameFrom.toUpperCase());
        logout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //  logout1.setBackgroundColor(getResources().getColor(R.color.background_color));
                editor.clear();
                editor.commit();

                System.out.print("value is:" + editor);
                Intent intent = new Intent(getApplicationContext(), Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);


            }
        });

        home1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //home1.setBackgroundColor(getResources().getColor(R.color.background_color));
                Intent intent = new Intent(getApplication(), FriendSerching.class);
                startActivity(intent);


            }
        });

        friends1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  friends1.setBackgroundColor(getResources().getColor(R.color.background_color));
                Intent intent = new Intent(getApplication(), FriendSerching.class);

                startActivity(intent);


            }
        });


        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNav.toggleLeftDrawer();
                mNav.setAnimationDurationLeft(300);
                mNav.setAnimationDurationRight(300);
            }
        });


        meeting1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInternetPresent = cd.isConnectingToInternet();
                // check for Internet status
                if (isInternetPresent) {
                    Intent intent = new Intent(getApplication(), Meets.class);
                    startActivity(intent);


                } else {
                    Toast.makeText(getApplicationContext(), "You don't have an internet connection", Toast.LENGTH_SHORT).show();

                }

                // meeting1.setBackgroundColor(getResources().getColor(R.color.background_color));


            }
        });


        profilepic1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // profilepic1.setBackgroundColor(getResources().getColor(R.color.background_color));
                Intent intent = new Intent(getApplication(), Update_profile.class);
                startActivity(intent);


            }
        });


        seeting1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), Profile_Pic.class);
                startActivity(intent);
                // seeting1.setBackgroundColor(getResources().getColor(R.color.background_color));


            }
        });

        SerchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isInternetPresent = cd.isConnectingToInternet();
                // check for Internet status
                if (isInternetPresent) {

                    new FriendSerch().execute();


                } else {
                    Toast.makeText(getApplicationContext(), "You don't have an internet connection", Toast.LENGTH_SHORT).show();

                }


            }
        });

        isInternetPresent = cd.isConnectingToInternet();
        // check for Internet status
        if (isInternetPresent) {

            new Friendlst().execute();


        } else {
            Toast.makeText(getApplicationContext(), "You don't have an internet connection", Toast.LENGTH_SHORT).show();

        }

    }

    public class FriendSerch extends AsyncTask<String, String, String> {
        String serchtext1 = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            list.clear();
            dialogp = new Dialog(FriendSerching.this);
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
            value = serchtext.getText().toString();
        }

        @Override
        public String doInBackground(String... params) {

            try {
                value = value.replace(" ","%20");
                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.SERCHING_DATABASE + Friss_Pojo.UseridFrom + "/" + value;
                ServiceHandler sh = new ServiceHandler();

                // Making a1 request to url and getting response
                Log.d("Response: ", "> " + url);
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

                Log.d("Response: ", "> " + jsonStr);
                aJson = new JSONArray(jsonStr);
                for (int i = 0; i < aJson.length(); i++) {
                    JSONObject json = aJson.getJSONObject(i);
                    Friend_list_Pojo friendlistPojo = new Friend_list_Pojo();
                    friendlistPojo.setUserName(json.getString("UserName"));
                    friendlistPojo.setFirstName(json.getString("FirstName"));
                    friendlistPojo.setLastName(json.getString("LastName"));
                    friendlistPojo.setAvatarPath(json.getString("AvatarPath"));
                    friendlistPojo.setUserId(json.getString("UserId"));
                    friendlistPojo.setEmail(json.getString("EmailName"));

                    Log.d("valu............", json.getString("UserId"));

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
            dialogp.dismiss();

            if (!(list.isEmpty())) {
                adp = new Friend_Serching_adapter(getApplicationContext(), list);
                serch.setAdapter(adp);

            } else {

                Toast.makeText(getApplicationContext(), "Oops.. Something's not right", Toast.LENGTH_LONG).show();
            }
            serch.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Friend_list_Pojo item = (Friend_list_Pojo) adp.getItem(position);
                    Intent intent = new Intent(getApplicationContext(), UserProfile.class);
                    intent.putExtra(Friss_Pojo.USER_NAME, item.getUserName());
                    intent.putExtra(Friss_Pojo.FIRST_NAME, item.getFirstName());
                    intent.putExtra(Friss_Pojo.LAST_NAME, item.getLastName());
                    intent.putExtra(Friss_Pojo.User_Id, item.getUserId());
                    // intent.putExtra(Friss_Pojo.AVATAR_PATH, item.getAvatarPath());
                    Friss_Pojo.AvatarPathTo = item.getAvatarPath();
                    intent.putExtra(Friss_Pojo.EMAIL_ADDRESS, item.getEmail());
                    startActivity(intent);


                }
            });


        }
    }


    public class Friendlst extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogp = new Dialog(FriendSerching.this);
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
                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.USER_FRIENDSlIST + Friss_Pojo.UserNameFrom;
                ServiceHandler sh = new ServiceHandler();
                Log.d("URL: ", "> " + url);
                // Making a1 request to url and getting response
                jsonStr1 = sh.makeServiceCall(url, ServiceHandler.GET);
                Log.d("Response: ", "> " + jsonStr1);
                aJson = new JSONArray(jsonStr1);
                for (int i = 0; i < aJson.length(); i++) {
                    JSONObject json = aJson.getJSONObject(i);
                    Friend_list_Pojo friendlistPojo = new Friend_list_Pojo();
                    friendlistPojo.setUserName(json.getString("UserName"));
                    friendlistPojo.setFirstName(json.getString("FirstName"));
                    friendlistPojo.setLastName(json.getString("LastName"));
                    friendlistPojo.setAvatarPath(json.getString("AvatarPath"));
                    friendlistPojo.setUserId(json.getString("UserId"));
                    friendlistPojo.setEmail(json.getString("EmailName"));
                    Log.d("valu............", json.getString("UserId"));
                    list1.add(friendlistPojo);

                }
                return jsonStr1;
                //notify the activity that fetch data has been complete*/

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            dialogp.dismiss();
            if (!(list1.isEmpty())) {
                adp1 = new Friend_list_Adapter(getApplicationContext(), list1);
                serch.setAdapter(adp1);

                serch.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Friend_list_Pojo item = (Friend_list_Pojo) adp1.getItem(position);
                        Intent intent = new Intent(FriendSerching.this, UserProfile.class);
                        intent.putExtra(Friss_Pojo.USER_NAME, item.getUserName());
                        intent.putExtra(Friss_Pojo.FIRST_NAME, item.getFirstName());
                        intent.putExtra(Friss_Pojo.LAST_NAME, item.getLastName());
                        intent.putExtra(Friss_Pojo.User_Id, item.getUserId());
                        //  intent.putExtra(Friss_Pojo.AVATAR_PATH, item.getAvatarPath());

                        Friss_Pojo.AvatarPathTo = item.getAvatarPath();

                        intent.putExtra(Friss_Pojo.EMAIL_ADDRESS, item.getEmail());

                        startActivity(intent);
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Let's start making friends", Toast.LENGTH_LONG).show();
            }

        }

    }


  /*  @Override
    public void onBackPressed() {
        if (backPressedToExitOnce) {

            moveTaskToBack(true);
        } else {
            this.backPressedToExitOnce = true;

            Toast.makeText(getApplicationContext(),"Press back twice to exit",Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    backPressedToExitOnce = false;

                }
            }, 2000);
        }
    }*/
}
