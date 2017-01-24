package com.frissbi.Frissbi_Friends;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.Frissbi_Meetings.Meeting_mulltyrequstfrs_Addapter;
import com.frissbi.Frissbi_Meetings.Meetingrequst;
import com.frissbi.Frissbi_Pojo.Friend_list_Pojo;
import com.frissbi.R;
import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.Utility.HorizontalView;
import com.frissbi.Utility.ServiceHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * Created by KNPL003 on 30-06-2015.
 */
public class FriendList_CheckBox extends Activity {
    private ProgressDialog progressDialog;
    FriendListaddapater adp;
    List<Friend_list_Pojo> list = new ArrayList<Friend_list_Pojo>();
    JSONArray aJson = null;

    ListView friend_list;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String jsonStr;

    SharedPreferences userids_prf;
    SharedPreferences.Editor userids_editor;
    StringBuffer user_ids, img_ids, user_ids1;
    String img1;
    String user_ids2;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friendlist);
        friend_list = (ListView) findViewById(R.id.friend_list);

        Button ok = (Button) findViewById(R.id.ok);

        userids_prf = getSharedPreferences("USERIDS", Context.MODE_PRIVATE);
        userids_editor = userids_prf.edit();
        img1 = userids_prf.getString("IMGIDS", "");
        user_ids2 = userids_prf.getString("USERID", "");
        Log.d("Image_ids_requst", img1);
        userids_editor.commit();
        userName = Friss_Pojo.UserNameFrom;
        Log.d("FriendList_CheckBox", "userName" + userName);

        new Friendlst_box().execute();
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), Meetingrequst.class);
                startActivity(intent);
                finish();
            }
        });

        //String user_name = preferences.getString("USERNAME_FROM", "editor");
        //Friss_Pojo.UseridFrom = userid;
        // Friss_Pojo.UserNameFrom = user_name;
        // Log.d("value is", userid);
        // editor.commit();
    }

    public class Friendlst_box extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(FriendList_CheckBox.this);
            progressDialog.setTitle("Loading Frissbi Data........................");

            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        public String doInBackground(String... params) {
            try {
                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.USER_FRIENDSlIST + userName;
                Log.d("FriendList_CheckBox", "URL" + url);
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
                    friendlistPojo.setAvatarPath(json.getString("AvatarPath"));
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

            adp = new FriendListaddapater(getApplicationContext(), list);
            friend_list.setAdapter(adp);


        }
    }

    private class ViewHolder {
        TextView firstlastname;
        CheckBox friend_chekbox;
        ImageView imageViewRound;
        //LinearLayout change_clr;
    }

    public class FriendListaddapater extends ArrayAdapter<Friend_list_Pojo> {
        private List<Friend_list_Pojo> items;
        ViewHolder holder = null;

        public FriendListaddapater(Context context, List<Friend_list_Pojo> items) {
            super(context, R.layout.friendlist_items, items);
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;


            if (v == null) {
                LayoutInflater li = LayoutInflater.from(getContext());
                v = li.inflate(R.layout.friendlist_items, null);
                holder = new ViewHolder();
                holder.firstlastname = (TextView) v.findViewById(R.id.firstlastname);
                holder.friend_chekbox = (CheckBox) v.findViewById(R.id.friend_chekbox);
                holder.imageViewRound = (ImageView) v.findViewById(R.id.imageView_round);


                v.setTag(holder);

                holder.friend_chekbox.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        CheckBox cb = (CheckBox) v;
                        // boolean checked = ((CheckBox) v).isChecked();
                        if (cb.isChecked()) {
                            Friend_list_Pojo _state = (Friend_list_Pojo) cb.getTag();
                            Toast.makeText(getApplicationContext(), "Clicked on Checkbox: " + cb.getText() + " is " + cb.isChecked(),
                                    Toast.LENGTH_LONG).show();
                            _state.setSelected(cb.isChecked());

                            user_ids = new StringBuffer();
                            img_ids = new StringBuffer();


                            List<Friend_list_Pojo> stateList = adp.items;
                            for (int i = 0; i < stateList.size(); i++) {
                                Friend_list_Pojo friendlistPojo = stateList.get(i);
                                if (friendlistPojo.isSelected()) {
                                    user_ids.append("," + friendlistPojo.getUserId());
                                    img_ids.append("," + friendlistPojo.getAvatarPath());

                                }
                            }
                            Toast.makeText(getApplicationContext(), img_ids, Toast.LENGTH_LONG).show();
                            Toast.makeText(getApplicationContext(), user_ids, Toast.LENGTH_LONG).show();
                            //cb.mask("Disabled");
                        } else {

                            Friend_list_Pojo _state = (Friend_list_Pojo) cb.getTag();

                            if (_state.isSelected()) {
                                user_ids.append("," + _state.getUserId());
                                img_ids.append("," + _state.getAvatarPath());
                                Toast.makeText(getApplicationContext(), "Clicked on Checkbox: " + cb.getText() + " is " + cb.isChecked(),
                                        Toast.LENGTH_LONG).show();
                                _state.setSelected(cb.isChecked());

                            }
                        }
                        Toast.makeText(getApplicationContext(), img_ids, Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), user_ids, Toast.LENGTH_LONG).show();
                      /*  if (((CheckBox)v).isChecked()) {
                            holder.change_clr.setBackgroundColor(Color.BLUE);
                            Placs_pojo _state = (Placs_pojo) cb.getTag();

                            Toast.makeText(getApplicationContext(), "Clicked on Checkbox: " + cb.getText() + " is " + cb.isChecked(),
                                    Toast.LENGTH_LONG).show();

                            _state.setSelected(checked);
                        } else {
                            holder.change_clr.setBackgroundColor(Color.GREEN);
                        }*/


                    }
                });


            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Friend_list_Pojo state = items.get(position);

            holder.firstlastname.setText("  " + state.getFirstName() + " " + state.getLastName());


            String image = state.getAvatarPath();

            if (!image.equals("")) {

                byte[] encodeByte = android.util.Base64.decode(image, android.util.Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                holder.imageViewRound.setImageBitmap(bitmap);
            } else if (image.equals("")) {

                Bitmap bm = BitmapFactory.decodeResource(v.getResources(), R.drawable.pic1);
                holder.imageViewRound.setImageBitmap(bm);
            }
            holder.friend_chekbox.setChecked(state.isSelected());

            holder.friend_chekbox.setTag(state);


            return v;
        }

    }

}
