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
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.Frissbi_Pojo.Register_pojo;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.ServiceHandler;


import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Created by KNPL003 on 07-05-2015.
 */
public class Test1 extends Activity {
    Button next;
    String email;
    EditText inputFirstName;
    EditText inputLastName;
    String valid_name = null;
    String valid_email = null;
    EditText inputEmail;
   String jsonStr;
    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login2);
        inputFirstName = (EditText) findViewById(R.id.fname);
        inputLastName = (EditText) findViewById(R.id.lname);
        inputEmail = (EditText) findViewById(R.id.email);
        next= (Button) findViewById(R.id.next);
        cd = new ConnectionDetector(getApplicationContext());


        //***** start  Validations for first name******//
        inputFirstName.addTextChangedListener(new TextWatcher() {

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
                Is_Valid_Person_Name(inputFirstName);
            }
        });


        //***** end Validations for first name******//


        //***** Validations for inputLastName******//
        inputLastName.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (inputFirstName.getText().length() > 1) {
                    inputFirstName.setError("good");
                } else if (inputLastName.getText().length() == 0) {
                    inputLastName.setError("should be file ");
                    Is_Valid_Person_Name(inputLastName);
                }

            }
        });

        //***** end Validations inputLastName******//


        //***** Start Validations for email name******//
        inputEmail.addTextChangedListener(new TextWatcher() {

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

                Is_Valid_Email(inputEmail);


                isInternetPresent = cd.isConnectedToInternet();
                // check for Internet status
                if (isInternetPresent) {


                    String data=inputEmail.getText().toString();
                    if(data.contains("@") && (data.contains("."))) {

                        new EmailExist().execute();
                        // new EmailExist().execute();
                    }


                } else {
                    Toast.makeText(getApplicationContext(),"You don't have an internet connection", Toast.LENGTH_SHORT).show();

                }




            }
        });
        //***** end of  Validations for email name******//



        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((!inputFirstName.getText().toString().equals("")) && (!inputLastName.getText().toString().equals("")) && (!inputEmail.getText().toString().equals("")) ){

                  Intent log=new Intent(getApplicationContext(),Register.class);
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
    //UserRegisration Input values

    public void Is_Valid_Person_Name(EditText edt) throws NumberFormatException {
        if (edt.getText().toString().length() <= 0) {
            edt.setError("Accept Alphabets Only.");
            valid_name = null;
        } else if (!edt.getText().toString().matches("[a-zA-Z ]+")) {
            edt.setError("Accept Alphabets Only.");
            valid_name = null;
        } else {
            valid_name = edt.getText().toString();
        }

    }

    public void Is_Valid_Email(EditText edt) {
        if (edt.getText().toString() == null) {
            edt.setError("Invalid email address");
            valid_email = null;
        } else if (isEmailValid(edt.getText().toString()) == false) {
            edt.setError("Invalid email address");
            valid_email = null;
        } else {
            valid_email = edt.getText().toString();
        }
    }
    boolean isEmailValid(CharSequence email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    } // end of email matcher






    public class EmailExist extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Register_pojo.Regi_FirstName=inputFirstName.getText().toString();
            Register_pojo.Regi_LastName=inputLastName.getText().toString();
            Register_pojo.Regi_EmailName=inputEmail.getText().toString();
            email=inputEmail.getText().toString();

            if( email.equals("")){
                email="null";
            }
        }

        @Override
        protected String doInBackground(String... params) {

            try {
             String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.USERNAME_EMAIL_EXIST+ null+"/"+email;
                ServiceHandler sh = new ServiceHandler();
                Log.d("url: ", "> " + url);
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

                inputEmail.setError("Try a different Email-id");
                //biToast.makeText(getApplication(),"UserName alredy Exist",Toast.LENGTH_LONG).show();
            } else {

            }


        }
    }



    @Override
    public void onBackPressed() {

        finish();

    }
}
