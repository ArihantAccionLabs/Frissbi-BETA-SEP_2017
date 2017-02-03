package com.frissbi.Settings;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

public class ChangePassword extends Activity {
    public static String UsernameTest;
    public static String PasswordTest;
    Button next;
    EditText inputoldPassword;
    EditText inputnewPassword,inputcPassword;
    TextView black, green;
    String low;
    TextView Low,Normal,Strong;
    String jsonStr;
    String newpassword;
    String oldpassword;
    Dialog dialogp;
    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.changepassword);

        inputoldPassword = (EditText) findViewById(R.id.uname);
        black = (TextView) findViewById(R.id.black);
        green = (TextView) findViewById(R.id.green);
        inputnewPassword = (EditText) findViewById(R.id.pword);
        inputcPassword = (EditText) findViewById(R.id.cpword);
        next= (Button) findViewById(R.id.done);

        Low = (TextView) findViewById(R.id.low);
        Normal = (TextView) findViewById(R.id.nor);
        Strong = (TextView) findViewById(R.id.strong);
        low=inputnewPassword.getText().toString();

        cd = new ConnectionDetector(getApplicationContext());





        //***** Start  on Validations for ConformPassword******//
        inputnewPassword.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {


                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {




                if (low.length()==0) {
                    Low.setVisibility(View.GONE);
                    Normal.setVisibility(View.GONE);
                    Strong.setVisibility(View.GONE);

                } else if (low.length()<3){
                    Low.setVisibility(View.INVISIBLE);
                }else if (low.length()<5){
                    Normal.setVisibility(View.INVISIBLE);
                }else if (low.length()>6){
                    Strong.setVisibility(View.INVISIBLE);
                }
            }
            // TODO Auto-generated method stub



            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                String low=inputnewPassword.getText().toString();
                if (low.length()==0){
                    Low.setVisibility(View.GONE);
                    Normal.setVisibility(View.GONE);
                    Strong.setVisibility(View.GONE);
                }

                else if (low.length()<=3) {
                    Low.setVisibility(View.VISIBLE);

                } else if (low.length()<=5){
                    Low.setVisibility(View.VISIBLE);
                    Normal.setVisibility(View.VISIBLE);
                }else if (low.length()>=6){
                    Strong.setVisibility(View.VISIBLE);
                    Low.setVisibility(View.VISIBLE);
                    Normal.setVisibility(View.VISIBLE);
                }
            }

        });

        //***** end  of Validations for ConformPassword******//


        //***** Start  on Validations for ConformPassword******//
        inputcPassword.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub
                if (low.length()<=5){
                    inputnewPassword.setError("Six characters or more");
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

                if (inputnewPassword.getText().toString().equals(inputcPassword.getText().toString())) {
                    //Toast.makeText(getApplicationContext(), "ok done",1).show();
                    green.setVisibility(View.VISIBLE);
                    black.setVisibility(View.INVISIBLE);
                    // btnRegister.setVisibility(View.INVISIBLE);

                } else {
                    // Toast.makeText(getApplicationContext(), "ok error", Toast.LENGTH_SHORT).show();
                    black.setVisibility(View.VISIBLE);
                    green.setVisibility(View.INVISIBLE);
                }
            }

        });

        //***** end  of Validations for ConformPassword******//




        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isInternetPresent = cd.isConnectedToInternet();
                // check for Internet status
                if (isInternetPresent) {

                    if((!inputoldPassword.getText().toString().equals("")) && (!inputnewPassword.getText().toString().equals("")) ){


                        new ChangePWD().execute();


                    }
                    else {
                        Toast.makeText(getApplicationContext(),
                                "One or more fields are empty", Toast.LENGTH_SHORT).show();

                    }


                } else {
                    Toast.makeText(getApplicationContext(),"You don't have an internet connection", Toast.LENGTH_SHORT).show();

                }





            }
        });
    }
    public class ChangePWD extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();




            dialogp = new Dialog(ChangePassword.this);
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



           oldpassword=inputnewPassword.getText().toString();
            newpassword=inputnewPassword.getText().toString();
        }

        @Override
        protected String doInBackground(String... params) {


            try {

            String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.CHANGE_PASSWORD+ Friss_Pojo.UseridFrom+"/"+oldpassword+"/"+newpassword;
                ServiceHandler sh = new ServiceHandler();

                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

                Log.d("Response: ", "> " + jsonStr);
                Log.d("valus......", jsonStr.toString());

                return  jsonStr.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String ifExists) {
            super.onPostExecute(ifExists);dialogp.dismiss();
            if (jsonStr.equals("0")) {

                Toast.makeText(getApplicationContext(), "Change Password", Toast.LENGTH_LONG);

                //biToast.makeText(getApplication(),"UserName alredy Exist",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(),"Old Password is invalid",Toast.LENGTH_LONG);
            }


        }

    }


}
