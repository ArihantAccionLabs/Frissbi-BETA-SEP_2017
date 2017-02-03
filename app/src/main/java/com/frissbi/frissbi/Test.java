package com.frissbi.frissbi;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.Frissbi_Pojo.Register_pojo;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.ServiceHandler;


/**
 * Created by KNPL003 on 07-05-2015.
 */
public class Test extends Activity {
    public static String UsernameTest;
    public static String PasswordTest;
    Button next;
    EditText inputPassword;
    EditText inputUsername,inputcPassword;
    TextView black, green;
    String low;
    TextView Low,Normal,Strong;
    String jsonStr;
    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    setContentView(R.layout.login1);

        inputUsername = (EditText) findViewById(R.id.uname);
        black = (TextView) findViewById(R.id.black);
        green = (TextView) findViewById(R.id.green);
        inputPassword = (EditText) findViewById(R.id.pword);
        inputcPassword = (EditText) findViewById(R.id.cpword);
        next= (Button) findViewById(R.id.next);

        Low = (TextView) findViewById(R.id.low);
        Normal = (TextView) findViewById(R.id.nor);
        Strong = (TextView) findViewById(R.id.strong);
        low=inputPassword.getText().toString();
        cd = new ConnectionDetector(getApplicationContext());



        //***** Start Validations User Name******//
        inputUsername.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub


            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub


            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                // new UsernameValidat().execute();


                isInternetPresent = cd.isConnectedToInternet();
                // check for Internet status
                if (isInternetPresent) {

                    new UserExist().execute();


                } else {
                    Toast.makeText(getApplicationContext(),"You don't have an internet connection", Toast.LENGTH_SHORT).show();

                }


            }

        });

        //***** Start  on Validations for ConformPassword******//
        inputPassword.addTextChangedListener(new TextWatcher() {

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
                String low=inputPassword.getText().toString();
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
                    inputPassword.setError("Six characters or more");
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

                if (inputPassword.getText().toString().equals(inputcPassword.getText().toString())) {
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

                if((!inputUsername.getText().toString().equals("")) && (!inputPassword.getText().toString().equals("")) ){

                    Register_pojo.Regi_UserName=inputUsername.getText().toString();
                    Register_pojo.Regi_UserPassword=inputcPassword.getText().toString();
                    Log.d("Password",Register_pojo.Regi_UserPassword);

                    Intent log=new Intent(getApplicationContext(),Test1.class);
                    startActivity(log);
                    overridePendingTransition(R.anim.lift, R.anim.rght);

                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "One or more fields are empty", Toast.LENGTH_SHORT).show();

                }

            }
        });
    }
    public class UserExist extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            UsernameTest=inputUsername.getText().toString();
            if(UsernameTest.equals("")){
                UsernameTest="null";
            }
        }

        @Override
        protected String doInBackground(String... params) {

            try {
           String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.USERNAME_EMAIL_EXIST+ UsernameTest+"/"+null;
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
            super.onPostExecute(ifExists);
            if (jsonStr==null){
                Toast.makeText(getApplication(), "Oops.. Something's not right", Toast.LENGTH_SHORT).show();
            }

           else if (jsonStr.equals("1")) {

                inputUsername.setError("Try a different Username");
                //biToast.makeText(getApplication(),"UserName alredy Exist",Toast.LENGTH_LONG).show();
            } else {

            }


        }

    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);

    }
}
