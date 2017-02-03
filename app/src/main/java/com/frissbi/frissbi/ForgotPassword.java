package com.frissbi.frissbi;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.frissbi.R;
import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.ServiceHandler;

//import com.frissbi.Webservice.AuthenticateUser;

/**
 * Created by KNPL003 on 09-06-2015.
 */
public class ForgotPassword extends Activity {
    Dialog dialogp;
    EditText email;
    TextView send;
    private ProgressDialog pDialog;
    String jsonStr;
String ForgotPassword="";

    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot);
        email = (EditText) findViewById(R.id.email);
        send = (TextView) findViewById(R.id.send);
        cd = new ConnectionDetector(getApplicationContext());
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInternetPresent = cd.isConnectedToInternet();

                if (isInternetPresent) {

                    if ((!email.getText().toString().equals(""))){

                        new ForgotPass().execute();
                    }else {
                        Toast.makeText(getApplicationContext(), "Enter Password", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Toast.makeText(getApplicationContext(),"You don't have an internet connection", Toast.LENGTH_SHORT).show();

                }


            }
        });

    }

    public class ForgotPass extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialogp = new Dialog(ForgotPassword.this);
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
            ForgotPassword = email.getText().toString();
        }

        @Override
        protected String doInBackground(String... params) {

            try {


                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.FORGOT_PASSWORD + ForgotPassword;
                ServiceHandler sh = new ServiceHandler();
                Log.d("Response: ", "> " + url);
                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

                return jsonStr;


            } catch (Exception e) {
                e.printStackTrace();

                //   Toast.makeText(getActivity(), R.string.connection_error, Toast.LENGTH_SHORT).show();


                return null;


            }
        }
        @Override
        protected void onPostExecute(String sucess) {


            super.onPostExecute(sucess);
            dialogp.dismiss();

            if (jsonStr==null){
                Toast.makeText(getApplication(), "Oops... Something's not right", Toast.LENGTH_SHORT).show();
            }


            if ((jsonStr.equals("0"))) {
                final Dialog  dialogp2 = new Dialog(ForgotPassword.this);


                dialogp2.getWindow();
                dialogp2.setCancelable(false);
                dialogp2.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogp2.setContentView(R.layout.registerdailog_box);
                TextView text= (TextView) dialogp2.findViewById(R.id.msg);
                // ImageButton img= (ImageButton) dialogp2.findViewById(R.id.img);


                text.setText("Password reset link sent to your email.");
                Button cls= (Button) dialogp2.findViewById(R.id.cls);
                cls.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogp2.dismiss();
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        startActivity(intent);

                    }
                });
                dialogp2.show();
                dialogp2.setOnKeyListener(new Dialog.OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialogp2.dismiss();
                            Intent intent = new Intent(getApplicationContext(), Login.class);
                            startActivity(intent);

                        }
                        return true;
                    }
                });



            } else {
                Toast.makeText(getApplication(), "Please enter correct email-id", Toast.LENGTH_SHORT).show();
            }


        }
    }


}

