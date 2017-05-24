
package com.frissbi.app.frissbi;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.app.R;
import com.frissbi.app.Utility.ConnectionDetector;
import com.frissbi.app.Utility.Utility;
import com.frissbi.app.networkhandler.TSNetworkHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by KNPL003 on 09-06-2015.
 */
public class GetTermsandConditions extends Activity {
    Dialog dialogp;

    TextView data;
    Button cls;
    String jsonStr;
    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;
    String data1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terms);

        data = (TextView) findViewById(R.id.send);
        cls = (Button) findViewById(R.id.done);
        TextView textView = (TextView) findViewById(R.id.text1);
        textView.setText("Terms of Use");
        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectedToInternet();

        if (isInternetPresent) {
            getTermsAndConditions();
            //new Privacypolicy_data().execute();
        } else {
            Toast.makeText(getApplicationContext(), "You don't have an internet connection", Toast.LENGTH_SHORT).show();

        }
        cls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void getTermsAndConditions() {
        String url = Utility.REST_URI + Utility.TERMSANDCONDITION;
        TSNetworkHandler.getInstance(this).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {

                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(response.response);
                            data1 = jsonObject.getString("PrivacyPolicyText");
                            data.setText(data1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(GetTermsandConditions.this, response.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(GetTermsandConditions.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*public class Privacypolicy_data extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialogp = new Dialog(GetTermsandConditions.this);
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
        protected String doInBackground(String... params) {

            try {


                ServiceHandler sh = new ServiceHandler();
                Log.d("Response: ", "> " + url);
                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
                JSONObject jsonObject = new JSONObject(jsonStr);
                data1 = jsonObject.getString("PrivacyPolicyText");
                Log.d("Response: ", "> " + data1);
                Log.d("jsonStr: ", "> " + jsonStr);
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

            if (jsonStr == null) {
                Toast.makeText(getApplication(), "Oops.. Something's not right", Toast.LENGTH_SHORT).show();
            }


            if (!(data1.equalsIgnoreCase("0"))) {
                data.setText(data1);

            } else {
                Toast.makeText(getApplication(), "Oops.. Something's not right", Toast.LENGTH_SHORT).show();
            }


        }
    }*/


}

