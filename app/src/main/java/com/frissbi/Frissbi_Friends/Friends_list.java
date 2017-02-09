package com.frissbi.Frissbi_Friends;
import android.app.Activity;
        import android.app.ProgressDialog;
        import android.content.Context;
        import android.content.SharedPreferences;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

        import com.frissbi.Frissbi_Pojo.Friend_list_Pojo;
        import com.frissbi.R;
        import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.ServiceHandler;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.io.InputStream;

        import java.util.ArrayList;
        import java.util.List;

/**
 * Created by KNPL003 on 30-06-2015.
 */
public class Friends_list extends Activity {
    private ProgressDialog progressDialog;
    Friend_list_Adapter adp;
    List<Friend_list_Pojo> list = new ArrayList<Friend_list_Pojo>();
    JSONArray aJson = null;
    InputStream is = null;
    String result = null;
    String line = null;
    ListView friend_list;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String jsonStr;


    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friendlist);
        friend_list = (ListView) findViewById(R.id.friend_list);
        Button ok = (Button) findViewById(R.id.ok);

        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectedToInternet();
        // check for Internet status
        if (isInternetPresent) {

            new Friendlst().execute();


        } else {
            Toast.makeText(getApplicationContext(),"You don't have an internet connection", Toast.LENGTH_SHORT).show();

        }
    ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();


            }
        });
        preferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        editor = preferences.edit();
        String userid = preferences.getString("USERID_FROM", "editor");
        String user_name = preferences.getString("USERNAME_FROM", "editor");
        Friss_Pojo.UseridFrom = userid;
        Friss_Pojo.UserNameFrom = user_name;
        Log.d("value is", userid);
        editor.commit();
    }

    public class Friendlst extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(Friends_list.this);
            progressDialog.setTitle("Loading Frissbi Data........................");

            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        public String doInBackground(String... params) {
            try {
                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.USER_FRIENDSLIST +Friss_Pojo.UserNameFrom;
                ServiceHandler sh = new ServiceHandler();

                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

                Log.d("Response: ", "> " + jsonStr);
                aJson = new JSONArray(jsonStr);
                for (int i = 0; i < aJson.length(); i++) {
                    JSONObject json = aJson.getJSONObject(i);
                    Friend_list_Pojo friendlistPojo = new Friend_list_Pojo();
                    friendlistPojo.setUserName(json.getString("UserName"));
                    friendlistPojo.setFirstName(json.getString("FirstName"));
                    friendlistPojo.setLastName(json.getString("LastName"));
                    //friendlistPojo.setEmailName(json.getString("EmailName"));
                    friendlistPojo.setUserId(json.getString("UserId"));
                    Log.d("valu............", json.getString("UserId"));

                    list.add(friendlistPojo);

                }
                return jsonStr;
                //notify the activity that fetch data has been complete*/


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            progressDialog.dismiss();
            // adapter = new ArrayAdapter<String>(getApplicationContext(),
            // android.R.layout.simple_list_item_1, android.R.id.text1, list);

            adp = new Friend_list_Adapter(getApplicationContext(), list);
            friend_list.setAdapter(adp);


        }
    }





}