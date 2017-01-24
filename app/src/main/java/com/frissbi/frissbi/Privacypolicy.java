package com.frissbi.frissbi;

        import android.app.Activity;
        import android.app.Dialog;
        import android.app.ProgressDialog;
        import android.content.Intent;
        import android.graphics.drawable.AnimationDrawable;
        import android.os.AsyncTask;
        import android.os.Bundle;
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

        import org.json.JSONObject;

/**
 * Created by KNPL003 on 09-06-2015.
 */
public class Privacypolicy extends Activity {
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
        cls=(Button)findViewById(R.id.done);
        TextView textView =(TextView)findViewById(R.id.text1);
        textView.setText("Privacy Policy");
        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();

        if (isInternetPresent) {



            new Privacypolicy_data().execute();



        } else {
            Toast.makeText(getApplicationContext(),"You don't have an internet connection", Toast.LENGTH_SHORT).show();

        }
        cls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

          finish();

            }
        });

    }

    public class Privacypolicy_data extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialogp = new Dialog(Privacypolicy.this);
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


                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.Privacy_policy + "1";
                ServiceHandler sh = new ServiceHandler();
                Log.d("Response: ", "> " + url);
                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);


                JSONObject jsonObject=new JSONObject(jsonStr);

                    data1=jsonObject.getString("PrivacyPolicyText");
                Log.d("Response: ", "> " + data1);

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
                Toast.makeText(getApplication(), "Oops.. Something's not right", Toast.LENGTH_SHORT).show();
            }


            if (!(jsonStr.equals("0"))) {
                data.setText(data1);

            } else {
                Toast.makeText(getApplication(), "Oops.. Something's not right", Toast.LENGTH_SHORT).show();
            }


        }
    }


}

