package com.frissbi.frissbi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.R;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.ServiceHandler;

/**
 * Created by KNPL003 on 24-06-2015.
 */


public class UserProfileAccept extends Activity {

    TextView username, name, emailname, lastname, add;
    String FristNameTo = "";
    String LastNameTo = "";
    Integer frd_sta;
    Integer add_frd;
    LinearLayout acceptlist;
    TextView approve, rejct;
    String jsonStr, jsonStr1;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;
    private ImageView imageViewRound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acceptuserprofile);
        imageViewRound = (ImageView) findViewById(R.id.imageView_round);
        username = (TextView) findViewById(R.id.username);
        add = (TextView) findViewById(R.id.add);
        lastname = (TextView) findViewById(R.id.lastname);
        name = (TextView) findViewById(R.id.name);
        emailname = (TextView) findViewById(R.id.email);
        acceptlist = (LinearLayout) findViewById(R.id.accptlist);
        approve = (TextView) findViewById(R.id.approv);
        rejct = (TextView) findViewById(R.id.rejct);
        cd = new ConnectionDetector(getApplicationContext());
//SharedPreferences Start
        preferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        editor = preferences.edit();
        String userid = preferences.getString("USERID_FROM", "editor");
        String user_name = preferences.getString("USERNAME_FROM", "editor");
        Friss_Pojo.UseridFrom = userid;
        Friss_Pojo.UserNameFrom = user_name;
        Log.d("value is", userid);
        editor.commit();
//SharedPreferences end
        Intent intent = getIntent();
        if (null != intent) {
            Friss_Pojo.UserNameTo = intent.getStringExtra(Friss_Pojo.USER_NAME);
            FristNameTo = intent.getStringExtra(Friss_Pojo.FIRST_NAME);
            LastNameTo = intent.getStringExtra(Friss_Pojo.LAST_NAME);
            Friss_Pojo.UseridTo = intent.getStringExtra(Friss_Pojo.User_Id);

            String email = intent.getStringExtra(Friss_Pojo.EMAIL_ADDRESS);
            emailname.setText(email);
            name.setText(FristNameTo);
            lastname.setText(LastNameTo);
            String image=Friss_Pojo.AvatarPathTo;

            if (!image.equals("")) {

                byte[] encodeByte = android.util.Base64.decode(image, android.util.Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                imageViewRound.setImageBitmap(bitmap);
            } else if (image.equals("")) {

                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.pic1);
                imageViewRound.setImageBitmap(bm);


            }

        }

        username.setText(FristNameTo + LastNameTo);
        approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptlist.setVisibility(View.INVISIBLE);
                isInternetPresent = cd.isConnectingToInternet();
                // check for Internet status
                if (isInternetPresent) {

                    new Approve().execute();


                } else {
                    Toast.makeText(getApplicationContext(), "You don't have internet connection", Toast.LENGTH_SHORT).show();

                }


            }
        });

        rejct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptlist.setVisibility(View.INVISIBLE);
                isInternetPresent = cd.isConnectingToInternet();
                // check for Internet status
                if (isInternetPresent) {

                    new Rejct().execute();


                } else {
                    Toast.makeText(getApplicationContext(), "You don't have internet connection", Toast.LENGTH_SHORT).show();

                }

            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptlist.setVisibility(View.VISIBLE);
            }
        });
    }

    public class Approve extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {


                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.USER_FRIENDSlIST + Friss_Pojo.UserNameFrom + "/" + Friss_Pojo.UserNameTo;

                ServiceHandler sh = new ServiceHandler();

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
        protected void onPostExecute(String succes) {
            super.onPostExecute(succes);
            if (jsonStr == null) {
                Toast.makeText(getApplication(), "Server not responding", Toast.LENGTH_SHORT).show();
            } else if (jsonStr.equals("1")) {
                add.setText("friends");


            }

        }

    }


    public class Rejct extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {

                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.REJCT_FRIEND + Friss_Pojo.UserNameFrom + "/" + Friss_Pojo.UserNameTo;

                ServiceHandler sh = new ServiceHandler();

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
        protected void onPostExecute(String succes) {
            super.onPostExecute(succes);
            if (jsonStr == null) {
                Toast.makeText(getApplication(), "Server not responding", Toast.LENGTH_SHORT).show();
            } else if (jsonStr.equals("1")) {
                add.setText("REJCT FRIEND");


            }


        }
    }

}
