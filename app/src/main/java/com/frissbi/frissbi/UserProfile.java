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
import android.widget.TextView;
import android.widget.Toast;


import com.frissbi.Frissbi_Meetings.Meetingrequst;
import com.frissbi.R;
import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.ServiceHandler;


public class UserProfile extends Activity {

    TextView username, name, emailname, lastname;
    private ImageView imageViewRound;
    String FristNameTo = "";
    String LastNameTo = "";

    TextView chat, add, meet;

    String jsonStr, jsonStr1;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;


    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        imageViewRound = (ImageView) findViewById(R.id.imageView_round);
        username = (TextView) findViewById(R.id.username);
        lastname = (TextView) findViewById(R.id.lastname);
        name = (TextView) findViewById(R.id.name);
        emailname = (TextView) findViewById(R.id.email);
        add = (TextView) findViewById(R.id.add);
        //chat= (Button) findViewById(R.id.chat);
        cd = new ConnectionDetector(getApplicationContext());
        meet = (TextView) findViewById(R.id.meet);
//        chat.setEnabled(false);
        preferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        editor = preferences.edit();
        String userid = preferences.getString("USERID_FROM", "editor");
        String user_name = preferences.getString("USERNAME_FROM", "editor");
        Friss_Pojo.UseridFrom = userid;
        Friss_Pojo.UserNameFrom = user_name;
        Log.d("value is", userid);
        editor.commit();


        Intent intent = getIntent();
        if (null != intent) {
            Friss_Pojo.UserNameTo = intent.getStringExtra(Friss_Pojo.USER_NAME);
            FristNameTo = intent.getStringExtra(Friss_Pojo.FIRST_NAME);
            LastNameTo = intent.getStringExtra(Friss_Pojo.LAST_NAME);
            Friss_Pojo.UseridTo = intent.getStringExtra(Friss_Pojo.User_Id);
            // Friss_Pojo.AvatarPathTo=intent.getStringExtra(Friss_Pojo.AVATAR_PATH);

            String email = intent.getStringExtra(Friss_Pojo.EMAIL_ADDRESS);
            emailname.setText(email);
            name.setText(FristNameTo);
            lastname.setText(LastNameTo);


            String image = Friss_Pojo.AvatarPathTo;

            if (!image.equals("")) {

                byte[] encodeByte = android.util.Base64.decode(image, android.util.Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                imageViewRound.setImageBitmap(bitmap);
            } else if (image.equals("")) {

                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.pic1);
                imageViewRound.setImageBitmap(bm);


            }
           /* isInternetPresent = cd.isConnectedToInternet();
            // check for Internet status
            if (isInternetPresent) {

                new FriendStastus().execute();


            } else {
                Toast.makeText(getApplicationContext(), "You don't have internet connection", Toast.LENGTH_SHORT).show();

            }*/


        }


        meet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent Accptfrd = new Intent(getApplicationContext(), Meetingrequst.class);
                startActivity(Accptfrd);


            }
        });





/*chat.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent chat=new Intent(getApplicationContext(),Meetingrequst.class);

        chat.putExtra("keyName",  Friss_Pojo.UseridTo);
        startActivity(chat);
    }
});*/

//new approveFriendRequest1().execute();
        username.setText(FristNameTo + LastNameTo);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (("-1".equals("-1"))) {
                    Log.d("valus......", jsonStr1.toString());
                    isInternetPresent = cd.isConnectedToInternet();
                    // check for Internet status
                    if (isInternetPresent) {

                        new SendFrerequst().execute();


                    } else {
                        Toast.makeText(getApplicationContext(), "You don't have an internet connection", Toast.LENGTH_SHORT).show();

                    }

                } else if (jsonStr1.equals("1")) {
                    add.setText("Friend");
                    Log.d("Friend......", jsonStr1.toString());
                } else if (jsonStr1.equals("4")) {
                    Log.d("add friend......", jsonStr1.toString());
                    isInternetPresent = cd.isConnectedToInternet();
                    if (isInternetPresent) {

                        new AcceptFriend().execute();


                    } else {
                        Toast.makeText(getApplicationContext(), "You don't have an internet connection", Toast.LENGTH_SHORT).show();

                    }


                } else if (jsonStr1.equals("0")) {
                    Log.d("Friend Req cancel......", jsonStr1.toString());
                    isInternetPresent = cd.isConnectedToInternet();
                    if (isInternetPresent) {

                        new FriendreqCancel().execute();
                        jsonStr1 = "-1";

                    } else {
                        Toast.makeText(getApplicationContext(), "You don't have an internet connection", Toast.LENGTH_SHORT).show();

                    }

                }

            }
        });
    }

    public class SendFrerequst extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.ADD_FRIEND + Friss_Pojo.UserNameFrom + "/" + Friss_Pojo.UserNameTo;

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
            }
            if (jsonStr.equals("0")) {
                add.setText("Req Sent");
            }
        }
    }


    public class FriendStastus extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String fromusername;
            try {


                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.FRIEND_STASTUS + Friss_Pojo.UseridFrom + "/" + Friss_Pojo.UseridTo;
                ServiceHandler sh = new ServiceHandler();

                // Making a1 request to url and getting response
                Log.d("URL: ", "> " + url);
                jsonStr1 = sh.makeServiceCall(url, ServiceHandler.GET);

                Log.d("Response: ", "> " + jsonStr1);

                Log.d("Response: ", "> " + jsonStr1);

                Log.d("valus......", jsonStr1.toString());


                //frd_sta=Integer.parseInt(jsonStr);
                return jsonStr1.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String jsonStr1) {
            super.onPostExecute(jsonStr1);
            if (jsonStr1 == null) {
                Toast.makeText(getApplication(), "Oops.. Something's not right", Toast.LENGTH_SHORT).show();
            }
            if (jsonStr1.equals("1")) {
                add.setText("Friend");
                Log.d("valus......", jsonStr1.toString());
            } else if (jsonStr1.equals("0")) {
                add.setText("Req Sent");
                Log.d("valus......", jsonStr1.toString());
            } else if (jsonStr1.equals("-1")) {
                Log.d("valus......", jsonStr1.toString());
                add.setText("ADD");
            } else if (jsonStr1.equals("4")) {
                add.setText("Accept");
                Log.d("valus......", jsonStr1.toString());
            }

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
                Log.d("UserProfile", "jsonStr----" + jsonStr);


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
            Log.d("UserProfile", "succes" + succes);
            if (succes == null) {
                Toast.makeText(getApplication(), "Oops.. Something's not right", Toast.LENGTH_SHORT).show();
            } else if (succes.equals("1")) {
                //add.setText("friend request");
                Toast.makeText(getApplication(), "Friend Request Accepted", Toast.LENGTH_LONG).show();

            }

        }
    }


    public class FriendreqCancel extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.FRIEND_CANCEL + Friss_Pojo.UserNameFrom + "/" + Friss_Pojo.UserNameTo;
                ServiceHandler sh = new ServiceHandler();

                // Making a1 request to url and getting response.
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

                Toast.makeText(getApplication(), "Cancel Request", Toast.LENGTH_LONG).show();

            } else {

            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isInternetPresent = cd.isConnectedToInternet();
        // check for Internet status
        if (isInternetPresent) {

            new FriendStastus().execute();


        } else {
            Toast.makeText(getApplicationContext(), "You don't have an internet connection", Toast.LENGTH_SHORT).show();

        }

    }
}
