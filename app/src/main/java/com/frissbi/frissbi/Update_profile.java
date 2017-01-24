package com.frissbi.frissbi;

import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.Frissbi_Pojo.Register_pojo;
import com.frissbi.R;
import com.frissbi.Utility.ServiceHandler;

import org.json.JSONObject;

import java.util.Calendar;


/**
 * Created by KNPL003 on 23-09-2015.
 */


public class Update_profile extends Activity {
    private Calendar cal;
    private int day;
    private int month;
    private int year;
    public EditText fn,ln,gn;
    TextView dob;
    private RadioGroup radioSexGroup;
    private RadioButton radioSexButton;
    private ProgressDialog pDialog;
   String jsonStr;
    String FirstName="";

    String LastName="";
    String DOB="";
    String Gender="";
    SharedPreferences preferences1;
    SharedPreferences.Editor editor_gmail;
    RadioButton radioButton,radioButton2;
    Integer selectedId;
    Dialog dialogp;
    SharedPreferences preferences_dob;
    SharedPreferences.Editor editor_dob;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_account);

        fn= (EditText) findViewById(R.id.frstname);
        ln= (EditText) findViewById(R.id.lastname);
        radioSexGroup=(RadioGroup)findViewById(R.id.radioGroup);
        radioButton= (RadioButton) findViewById(R.id.radioButton);
        radioButton2= (RadioButton) findViewById(R.id.radioButton2);

        dob=(TextView) findViewById(R.id.dob1);
        Button  ok= (Button) findViewById(R.id.ok);
        preferences1 = getSharedPreferences("GMAIL_REG", Context.MODE_PRIVATE);
        editor_gmail = preferences1.edit();
        FirstName=preferences1.getString("FirstName","editor");
        LastName=preferences1.getString("lastName", "editor");
        fn.setText(FirstName.toString());
        ln.setText(LastName.toString());

        editor_gmail.commit();

        preferences_dob = getSharedPreferences("DOBDATA", Context.MODE_PRIVATE);
        editor_dob = preferences_dob.edit();
        String dobdata=preferences_dob.getString("dobdata","dob");
       /* String gender=preferences_dob.getString("Gendar","dob");
        if (gender.equals("f")){

            radioButton.setText("M");


        }else if (gender.equals("m")){

            radioButton.setText("m");
        }else {

        }*/


            dob.setText(dobdata);
           DOB=dobdata.toString();
          // Gender=gender;



        radioSexGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {


                if (checkedId == R.id.radioButton) {

                    editor_dob = getApplicationContext().getSharedPreferences("genderdata", MODE_PRIVATE).edit();
                    editor_dob.putString("gender", "m");
                    Gender="m";
                    editor_dob.commit();

                } else if (checkedId == R.id.radioButton2) {

                    editor_dob = getApplicationContext()
                            .getSharedPreferences("genderdata",
                                    MODE_PRIVATE).edit();
                    editor_dob.putString("gender", "f");
                    Gender="f";

                    editor_dob.commit();


                }

            }
        });







        if (getSharedPreferences("genderdata", MODE_PRIVATE) != null) {
            SharedPreferences prefs = getSharedPreferences("genderdata",
                    MODE_PRIVATE);
            try {
                String gender1 = prefs.getString("gender", null);


                if (gender1.equalsIgnoreCase("m")
                        ) {
                    // cbRememberme.setChecked(false);

                    radioButton.setChecked(true);

                    //  male radio button enable

                } else  if (gender1.equalsIgnoreCase("f") ){

                    //  female radio button enable
                    radioButton2.setChecked(true);

                }
            } catch (Exception e) {

            }
        }








        ok.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {


      if((fn.getText().equals(""))&&((ln.getText().equals(""))&&(dob.length()==0))&&(Gender.equals(""))){

          Toast.makeText(getApplication(), "One or more fields are empty", Toast.LENGTH_LONG).show();

        }else {

          new User().execute();
        }

    }
});


//        Toast.makeText(getApplicationContext(), radioSexButton.getText().toString(), Toast.LENGTH_SHORT).show();
        cal = Calendar.getInstance();
        day = cal.get(Calendar.DAY_OF_MONTH);
        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);


       // Gender=radioSexButton.getText().toString();
        Log.d("Gender",Gender);
        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(0);
            }
        });


    }



    public class User extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();




            dialogp = new Dialog(Update_profile.this);
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


          // selectedId=radioSexGroup.getCheckedRadioButtonId();
           // radioSexButton=(RadioButton)findViewById(selectedId);
          //  Gender = radioSexButton.getText().toString();


            Log.d("radioSexButton", Gender);
            FirstName=fn.getText().toString();
            LastName=ln.getText().toString();
        }

        @Override
        public String doInBackground(String... strings) {


            try {

                String url = Friss_Pojo.REST_URI+"/"+"rest"+Friss_Pojo.UPDATE_PROFILE+Friss_Pojo.UseridFrom+"/"+FirstName+"/"+LastName+"/"+Gender+"/"+DOB;
                ServiceHandler sh = new ServiceHandler();

                url=url.replace(" ","%20");
                Log.d("url: ", "> " + url);
                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

                preferences_dob = getSharedPreferences("DOBDATA", Context.MODE_PRIVATE);
                editor_dob = preferences_dob.edit();

                editor_dob.putString("dobdata", DOB);
                editor_dob.putString("Gendar", Gender);
                Log.d("Response: ", "> " + DOB);
                editor_dob.commit();

                Log.d("Response: ", "> " + jsonStr);
               return jsonStr;

            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (jsonStr==null){
                Toast.makeText(getApplication(), "Oops.. Something's not right", Toast.LENGTH_SHORT).show();
            }


            else if (jsonStr.equals("0")) {
                Toast.makeText(getApplication(), "Profile Updated", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getApplication(), "Profile not updated", Toast.LENGTH_LONG).show();
            }

            dialogp.dismiss();

            // int userid=userID;


        }


    }


    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        return new DatePickerDialog(this, datePickerListener, year, month, day);
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            dob.setText(selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay);

              DOB=dob.getText().toString();



            Log.d("DOB",DOB);
        }
    };

}
