package com.frissbi.frissbi;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.Gmail.GmailSyncActivity;
import com.frissbi.activities.HomeActivity;
import com.frissbi.Notifications.Config;
import com.frissbi.R;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.ServiceHandler;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONObject;

import java.io.IOException;

public class Login extends Activity {
    public static final String REG_ID = "regId";
    static final String TAG = "Register Activity";
    TextView regi;
    TextView forgot;
    EditText inputEmail;
    EditText inputPassword;
    ImageButton mail, btnLogin;
    //**********variable declaration******************//
    String jsonStr;
    String regId;
    String Usre_ID;
    GoogleCloudMessaging gcm;
    Context context;
    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Dialog dialogp;
    String email_id = "";
    String UserName = "";
    // ************Declaring a1 custom android UI element***********//
    private boolean backPressedToExitOnce = false;
    private Toast toast = null;
    private TextView loginErrorMsg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.pword);
        regi = (TextView) findViewById(R.id.registerbtn);
        btnLogin = (ImageButton) findViewById(R.id.login);
        forgot = (TextView) findViewById(R.id.forgot);
        mail = (ImageButton) findViewById(R.id.gmail);


        mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gamailapi = new Intent(getApplicationContext(), GmailSyncActivity.class);
                startActivity(gamailapi);
            }
        });

        // rem = (ToggleButton) findViewById(R.id.rem);
        // sb = (Switch)findViewById(R.id.switch1);
        forgot.setPaintFlags(forgot.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        loginErrorMsg = (TextView) findViewById(R.id.loginErrorMsg);
        cd = new ConnectionDetector(getApplicationContext());


        if (TextUtils.isEmpty(regId)) {
            regId = registerGCM();
            Log.d("RegisterActivity", "GCM RegId: " + regId);
            Friss_Pojo.REG_ID = regId.toString();
            Log.d("RegId", Friss_Pojo.REG_ID);
        } else {
           /* Toast.makeText(getApplicationContext(),
                    "RegId already available. RegId: " + regId,
                    Toast.LENGTH_LONG).show();*/
        }
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ForgotPassword.class);
                startActivity(intent);
            }
        });


       /* rem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked = editor1 != null) {
                    pref = getSharedPreferences("REM_NAME", Context.MODE_PRIVATE);
                    editor1 = pref.edit();
                    editor1.putString("mail", inputEmail.getText().toString());
                    editor1.putString("Password", inputPassword.getText().toString());
                    editor1.commit();


                } else {
                    pref = getSharedPreferences("REM_NAME", Context.MODE_PRIVATE);
                    editor1 = pref.edit();
                    //editor.putString("mail",inputEmail.getText().toString() );

                    editor1.remove("mail");
                    editor1.remove("Password");
                    editor1.commit();
                }
            }
        });*/


        regi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), Test.class);
                startActivity(myIntent);

            }
        });

        /**
         * Login button click event
         * A Toast is set to alert when the Email and Password field is empty
         **/
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                // get Internet status
                isInternetPresent = cd.isConnectingToInternet();
                // check for Internet status
                if (isInternetPresent) {
                    // Internet Connection is Present
                    // make HTTP requests
                   /* showAlertDialog(Login.this, "Internet Connection",
                            "You have internet connection", true);*/
                    if ((!inputEmail.getText().toString().equals("")) && (!inputPassword.getText().toString().equals(""))) {
                        //   new NetCheck().execute();

                        new UserAthenticat().execute();
                    } else if ((!inputEmail.getText().toString().equals(""))) {
                        Toast.makeText(getApplicationContext(),
                                "Password field is empty", Toast.LENGTH_SHORT).show();
                    } else if ((!inputPassword.getText().toString().equals(""))) {
                        Toast.makeText(getApplicationContext(),
                                "Email field is empty", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Email and Password fields are empty", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    // Internet connection is not present
                    // Ask user to connect to Internet
                    //  showAlertDialog(Login.this, "No Internet Connection",
                    // "You don't have internet connection.", false);

                    Toast.makeText(getApplicationContext(),
                            "You don't have an internet connection", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    public String registerGCM() {

        gcm = GoogleCloudMessaging.getInstance(this);
        regId = getRegistrationId(context);

        if (TextUtils.isEmpty(regId)) {

            registerInBackground();

            Log.d("RegisterActivity",
                    "registerGCM - successfully registered with GCM server - regId: "
                            + regId);
        } else {
           /* Toast.makeText(getApplicationContext(),
                    "RegId already available. RegId: " + regId,
                    Toast.LENGTH_LONG).show();*/
        }
        return regId;
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getSharedPreferences(
                Login.class.getSimpleName(), Context.MODE_PRIVATE);
        String registrationId = prefs.getString(REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";


        }
        return registrationId;
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regId = gcm.register(Config.GOOGLE_PROJECT_ID);
                    Log.d("RegisterActivity", "registerInBackground - regId: "
                            + regId);
                    Friss_Pojo.REG_ID = regId.toString();
                    Log.d("RegId", Friss_Pojo.REG_ID);
                    msg = "Device registered, registration ID=" + regId;

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    Log.d("RegisterActivity", "Error: " + msg);
                }
                Log.d("RegisterActivity", "AsyncTask completed: " + msg);
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
              /*  Toast.makeText(getApplicationContext(),
                        "Registered with GCM Server." + msg, Toast.LENGTH_LONG)
                        .show();*/
                saveRegisterId(context, regId);

            }
        }.execute(null, null, null);
    }


    private void saveRegisterId(Context context, String regId) {
        final SharedPreferences nprefs = getSharedPreferences(Login.class.getSimpleName(), Context.MODE_PRIVATE);
        Log.i(TAG, "Saving regId on app version ");
        SharedPreferences.Editor editor = nprefs.edit();
        editor.putString(REG_ID, regId);
        editor.commit();


    }

    @Override
    public void onBackPressed() {
        if (backPressedToExitOnce) {

            moveTaskToBack(true);
        } else {
            this.backPressedToExitOnce = true;

            Toast.makeText(getApplicationContext(), "Press back twice to exit", Toast.LENGTH_SHORT).show();


            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    backPressedToExitOnce = false;

                }
            }, 2000);
        }
    }

   /* public class Privacypolicy_data extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialogp = new Dialog(Login.this);
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


                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.ISEMAIL_VERIFIED +UserName;
                ServiceHandler sh = new ServiceHandler();
                Log.d("Response: ", "> " + url);
                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

                Log.d("jsonStr: ", "> " + jsonStr);
                return jsonStr;

            } catch (Exception e) {
                e.printStackTrace();

                //   Toast.makeText(getActivity(), R.string.connection_error, Toast.LENGTH_SHORT).show();


                return null;


            }}

        @Override
        protected void onPostExecute(String sucess) {


            super.onPostExecute(sucess);
            dialogp.dismiss();

            if (jsonStr==null){
                Toast.makeText(getApplication(), "Server not responding", Toast.LENGTH_SHORT).show();


            }


            if ((jsonStr.equals("0"))) {

                final Dialog  dialogp2 = new Dialog(Login.this);
                // Include dialog.xml file
                dialogp2.getWindow();
                dialogp2.setCancelable(false);
                dialogp2.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogp2.setContentView(R.layout.meeting_reqdailogbox);
                TextView text= (TextView) dialogp2.findViewById(R.id.msg);
                // ImageButton img= (ImageButton) dialogp2.findViewById(R.id.img);


                text.setText("Please check your inbox to Activate your account.");
                Button cls= (Button) dialogp2.findViewById(R.id.cls);
                cls.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogp2.dismiss();

                    }
                });
                dialogp2.show();
                dialogp2.setOnKeyListener(new Dialog.OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialogp2.dismiss();

                        }
                        return true;
                    }
                });


            } else if(jsonStr.equals("1")) {

               new UserAthenticat().execute();

            }


        }
    }*/

    public class UserAthenticat extends AsyncTask<String, String, String> {

        String UserPass = "";
        String UserName1 = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogp = new Dialog(Login.this);
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
            UserPass = inputPassword.getText().toString();
            UserName1 = inputEmail.getText().toString();
        }

        @Override
        protected String doInBackground(String... params) {

            UserName1.contains("@");
            if (UserName1.contains("@") == true) {
                UserName = null;
                email_id = UserName1;

            } else {
                email_id = null;
                UserName = UserName1;
            }


            WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
            String IPaddress = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

            try {
                preferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
                editor = preferences.edit();

                editor.putString("EMAIL", UserName);
                editor.putString("PASS", UserPass);
                //editor.putString("EMAIL",s1.toString());


                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.USER_AUTHENTICATION + UserName + "/" + UserPass + "/" + IPaddress + "/" + regId + "/" + email_id;
                ServiceHandler sh = new ServiceHandler();
                Log.d("Response: ", "> " + url);
                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
                Log.d("Response: ", "> " + jsonStr);
                JSONObject json = new JSONObject(jsonStr);
                Log.d("Login", "json" + json);
                Log.d("valus......", jsonStr.toString());
                Usre_ID = json.getString("UserId");
                Log.d("valus......", Usre_ID);
                String UserNameFrom = json.getString("UserName");

                Friss_Pojo.UserNameFrom = UserNameFrom.toString();
                editor.putString("USERID_FROM", Usre_ID.toString());

                Friss_Pojo.UseridFrom = Usre_ID.toString();
                editor.putString("USERNAME_FROM", UserNameFrom.toString());
                editor.putString("RegId", regId);
                editor.putString("IPaddress", IPaddress);
                editor.commit();
                Log.d("valus......", UserNameFrom.toString());


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
            if (jsonStr == null) {
                Toast.makeText(getApplication(), "Oops.. Something's not right", Toast.LENGTH_SHORT).show();
            } else if (jsonStr.equals("-2")) {
                final Dialog dialogp2 = new Dialog(Login.this);
                // Include dialog.xml file
                dialogp2.getWindow();
                dialogp2.setCancelable(false);
                dialogp2.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogp2.setContentView(R.layout.registerdailog_box);
                TextView text = (TextView) dialogp2.findViewById(R.id.msg);
                ImageButton img = (ImageButton) dialogp2.findViewById(R.id.img);
                img.setVisibility(View.INVISIBLE);
                text.setText("Please check your inbox to Activate your account.");
                Button cls = (Button) dialogp2.findViewById(R.id.cls);
                cls.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogp2.dismiss();

                    }
                });
                dialogp2.show();
                dialogp2.setOnKeyListener(new Dialog.OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialogp2.dismiss();

                        }
                        return true;
                    }
                });


            } else if (!Usre_ID.equals("0")) {
                Toast.makeText(getApplication(), "Login Successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
            } else {
                loginErrorMsg.setText("Username and/or Password incorrect");
            }


        }
    }
}

