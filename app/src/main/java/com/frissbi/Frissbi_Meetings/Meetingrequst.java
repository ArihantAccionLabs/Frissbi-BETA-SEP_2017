package com.frissbi.Frissbi_Meetings;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.frissbi.Frissbi_Friends.FriendSerching;
import com.frissbi.Frissbi_Pojo.Friend_list_Pojo;
import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.Frissbi_Pojo.Image_Pojo;
import com.frissbi.Frissbi_Pojo.Meetingrequest_Pojo;
import com.frissbi.R;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.HorizontalView;
import com.frissbi.Utility.ServiceHandler;
import com.frissbi.frissbi.OrzineAdapterList;
import com.frissbi.locations.Orzine_pojo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by KNPL003 on 25-06-2015.
 */
public class Meetingrequst extends Activity {


    public static Integer senderUserId;
    public static String requestDateTime;
    public static String scheduledTimeSlot;
    public static String senderFromDateTime;
    public static String senderToDateTime;
    public static String meetingDescription;
    public static String RecipientDetails;
    public static String senderdate;
    public String senderdateandtime;
    public static int hour;
    public static int mm;
    public static int seconds;
    public static int mel;
    public String img1;
    public EditText meetingdesp;
    ImageView animation_img;
    TextView loading_text;


    TextView meeting_duration, meeting_time, textset;
    HorizontalView horizontalView;
    OrzineAdapterList adp1;
    List<Orzine_pojo> list = new ArrayList<Orzine_pojo>();
    Meeting_mulltyrequstfrs_Addapter adp;
    List<Image_Pojo> list1 = new ArrayList<Image_Pojo>();
    StringBuffer UserIds;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String MeetingID;
    LinearLayout setonmap, myplace, des_setonmap, flex, defined;
    TextView Scduldatetime, from, to, flexdate;
    Button done, addfriends;
    int horsfrom;
    int mintsfrom;
    Dialog dialogp, dialogp1;
    String img;
    Integer useridto;
    ListView orzinelist, des_list;
    String jsonStr;
    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;

    FriendListaddapater adp3;
    List<Friend_list_Pojo> list3 = new ArrayList<Friend_list_Pojo>();
    JSONArray aJson = null;
    ListView friend_list;
    ListView listview1;
    LinearLayout to2;
    String[] values = new String[]{
            "1:00", "1:30", "2:00", "2:30", "3:00", "3:30", "4:00", "4:30", "5:00", "5:30", "6:00", "6:30", "7:00", "7:30", "8:00", "8:30", "9:00", "9:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "20:00", "20:30", "21:00", "21:30", "22:00", "22:30", "23:00", "23:30", "24:00"

    };
    String[] values1 = new String[]{
            "1:00", "1:30", "2:00", "2:30", "3:00", "3:30", "4:00", "4:30", "5:00", "5:30", "6:00",

    };


    private Calendar cal1;
    private int day;
    private int month;
    private int year;

    private Calendar calendar;
    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {


            String s1 = (selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay);
            // String DateTime = (s1 + " " + hour + ":" + mm + ":" + seconds + "." + mel);
            // String scheduledDateTime = DateTime;
            Scduldatetime.setText(s1);
            senderdate = s1;
            Log.d("DATE", s1);
            // cal.setText(DateTime);


        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meetinginsert1);
        done = (Button) findViewById(R.id.done);
        addfriends = (Button) findViewById(R.id.addfriends);
        meetingdesp = (EditText) findViewById(R.id.meetingdesp);
        Scduldatetime = (TextView) findViewById(R.id.datetime);
        meeting_time = (TextView) findViewById(R.id.meeting_time);
        meeting_duration = (TextView) findViewById(R.id.meeting_duration);

        horizontalView = (HorizontalView) findViewById(R.id.hor_list);
        to2 = (LinearLayout) findViewById(R.id.to2);
        calendar = Calendar.getInstance();
        cd = new ConnectionDetector(getApplicationContext());
        meeting_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogp = new Dialog(Meetingrequst.this);
                // Include dialog.xml file
                dialogp.getWindow();
                dialogp.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogp.setContentView(R.layout.time);
                listview1 = (ListView) dialogp.findViewById(R.id.listView);
                Button ok = (Button) dialogp.findViewById(R.id.ok);
                TextView text1 = (TextView) dialogp.findViewById(R.id.tex1);

                text1.setText("Meeting Time");
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.orzine_iteam, R.id.location_name, values);


                listview1.setAdapter(adapter);
                listview1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        // ListView Clicked item index
                        int itemPosition = position;

                        // ListView Clicked item value
                        String itemValue = (String) listview1.getItemAtPosition(position);

                        senderdateandtime = itemValue;
                        senderFromDateTime = (senderdate + " " + itemValue + ":" + "00" + "." + "000");

                        meeting_time.setText(senderdateandtime);
                        Log.d("senderFromDateTime", senderFromDateTime);


                        dialogp.dismiss();

                    }

                });
                dialogp.show();

            }
        });


        meeting_duration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogp = new Dialog(Meetingrequst.this);
                // Include dialog.xml file
                dialogp.getWindow();
                dialogp.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogp.setContentView(R.layout.time);
                listview1 = (ListView) dialogp.findViewById(R.id.listView);
                TextView text1 = (TextView) dialogp.findViewById(R.id.tex1);

                text1.setText("Meeting Duration");
                Button ok = (Button) dialogp.findViewById(R.id.ok);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.orzine_iteam, R.id.location_name, values1);


                listview1.setAdapter(adapter);
                listview1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        String itemValue = (String) listview1.getItemAtPosition(position);
                        scheduledTimeSlot = itemValue + ":" + 00;

                        meeting_duration.setText(itemValue);
                        Log.d("scheduledTimeSlot", itemValue);
                        dialogp.dismiss();

                    }

                });
                dialogp.show();

            }
        });


        to = (TextView) findViewById(R.id.to);

        Log.d("Oncreate", "Oncreate");

        cal1 = Calendar.getInstance();
        day = cal1.get(Calendar.DAY_OF_MONTH);
        month = cal1.get(Calendar.MONTH);
        year = cal1.get(Calendar.YEAR);

        hour = cal1.get(Calendar.HOUR);

        mm = cal1.get(Calendar.MINUTE);
        seconds = cal1.get(Calendar.SECOND);
        mel = cal1.get(Calendar.MILLISECOND);

        // cal.setText(year + "-" + (month + 1) + "-" + day);


        requestDateTime = (year + "-" + (month + 1) + "-" + day + " " + hour + ":" + mm + ":" + seconds + "." + mel);

        Log.d("requestDateTime", requestDateTime);


        Intent chat = getIntent();
        if (null != chat) {

            RecipientDetails = getIntent().getStringExtra("keyName");


        }


        addfriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent friend_list = new Intent(getApplicationContext(), FriendList_CheckBox.class);
                startActivity(friend_list);*/

                dialogp = new Dialog(Meetingrequst.this);
                // Include dialog.xml file
                dialogp.getWindow();
                dialogp.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogp.setContentView(R.layout.friendlist);
                friend_list = (ListView) dialogp.findViewById(R.id.friend_list);
                Button ok = (Button) dialogp.findViewById(R.id.ok);

                animation_img = (ImageView) dialogp.findViewById(R.id.animationImage);

                loading_text = (TextView) dialogp.findViewById(R.id.loadtext);

                if (!(list3.isEmpty())) {
                    adp3 = new FriendListaddapater(getApplicationContext(), list3);
                    friend_list.setAdapter(adp3);
                    adp3.notifyDataSetChanged();
                    animation_img.setVisibility(View.INVISIBLE);
                    loading_text.setVisibility(View.INVISIBLE);
                    friend_list.smoothScrollToPosition(0);

                } else if (list3.isEmpty()) {

                    new Friendlst_box_friends().execute();

                }

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogp.dismiss();
                        if ((list1.isEmpty())) {
                            new Friendlst_box().execute();
                            // Toast.makeText(getApplicationContext(), UserIds, Toast.LENGTH_LONG).show();
                        } else {
                            list1.clear();
                            new Friendlst_box().execute();
                        }
                    }
                });
                dialogp.show();
                dialogp.setOnKeyListener(new Dialog.OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if (keyCode == KeyEvent.KEYCODE_BACK) {


                            if ((list1.isEmpty())) {
                                new Friendlst_box().execute();
                                // Toast.makeText(getApplicationContext(), UserIds, Toast.LENGTH_LONG).show();
                            } else {
                                list1.clear();
                                new Friendlst_box().execute();
                            }
                        }
                        return true;
                    }
                });
            }

        });


        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInternetPresent = cd.isConnectingToInternet();
                // check for Internet status
                if (isInternetPresent) {

                    if (!(meetingdesp.getText().toString().equals("")) && (!(meeting_duration.getText().toString().equals("")))) {
                        new MeetingInsert().execute();
                    } else {
                        Toast.makeText(getApplication(), "One or more fields are empty", Toast.LENGTH_LONG).show();

                    }
                } else {
                    Toast.makeText(getApplicationContext(), "You don't have an internet connection", Toast.LENGTH_SHORT).show();

                }


            }
        });
        Scduldatetime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(0);
            }
        });


        to2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialogp = new Dialog(Meetingrequst.this);
                // Include dialog.xml file
                dialogp.getWindow();
                dialogp.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogp.setContentView(R.layout.setdistancey);
                des_setonmap = (LinearLayout) dialogp.findViewById(R.id.des_setonmap);
                flex = (LinearLayout) dialogp.findViewById(R.id.flex);
                defined = (LinearLayout) dialogp.findViewById(R.id.defined);
                des_list = (ListView) dialogp.findViewById(R.id.des_list);
                textset = (TextView) dialogp.findViewById(R.id.textset);
                Button done1 = (Button) dialogp.findViewById(R.id.send);

                done1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialogp.dismiss();
                    }
                });

                flex.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        to.setText("Any Place");
                        Intent setmap = new Intent(getApplicationContext(), MeetingendTime.class);
                        startActivity(setmap);
                        Meetingrequest_Pojo.Meeting_DestinationType = "3";


                    }
                });

                des_setonmap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent setmap = new Intent(getApplicationContext(), com.frissbi.MapLocations.ToLocationsformap.class);
                        startActivity(setmap);
                        to.setText("Selcted Location");
                        Meetingrequest_Pojo.Meeting_DestinationType = "1";

                    }

                });
                defined.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Intent setmap=new Intent(getApplicationContext(),com.frissbi.MapLocations.SetLocationMap.class);
                        // startActivity(setmap);
                        Meetingrequest_Pojo.Meeting_DestinationType = "1";
                        if (!(list.isEmpty())) {

                            adp1 = new OrzineAdapterList(getApplicationContext(), list);
                            des_list.setAdapter(adp1);
                            des_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Orzine_pojo item = (Orzine_pojo) adp1.getItem(position);
                                    textset.setText(item.getLocationName());
                                    to.setText(item.getLocationName());
                                    Meetingrequest_Pojo.Meeting_Latitude_To = Double.parseDouble(item.getLongitude());
                                    Meetingrequest_Pojo.Meeting_Longitude_To = Double.parseDouble(item.getLatitude());
                                    Meetingrequest_Pojo.UserPreferredLocationId = item.getUserPreferredLocationID();


                                }
                            });
                        } else if (list.isEmpty()) {
                            new Destinaen().execute();
                        }
                    }


                });

                dialogp.show();
            }
        });


    }


    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {

        return new DatePickerDialog(this, datePickerListener, year, month, day);


    }

  /*  private DatePickerDialog.OnDateSetListener datePickerListener2 = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {


            String flxdate = (selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay);
            // String DateTime = (s1 + " " + hour + ":" + mm + ":" + seconds + "." + mel);
            // String scheduledDateTime = DateTime;
            flexdate.setText(flxdate);
            senderdate3 = flxdate;

            // cal.setText(DateTime);


        }
    };*/

    @Override
    protected void onResume() {
        super.onResume();

        if ((list1.isEmpty())) {
            new Friendlst_box().execute();
        } else if (!(list1.isEmpty())) {
            adp = new Meeting_mulltyrequstfrs_Addapter(getApplicationContext(), list1);
            horizontalView.setAdapter(adp);
        }
        Log.d("onResume", "onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause", "onPause");
    }

    public class MeetingInsert extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogp1 = new Dialog(Meetingrequst.this);
            // Include dialog.xml file
            dialogp1.getWindow();

            dialogp1.setCancelable(false);
            dialogp1.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogp1.setContentView(R.layout.dailog_box);
            ImageView imgView = (ImageView) dialogp1.findViewById(R.id.animationImage);
            imgView.setVisibility(ImageView.VISIBLE);
            imgView.setBackgroundResource(R.drawable.frame_animation);

            AnimationDrawable frameAnimation = (AnimationDrawable) imgView.getBackground();

            if (frameAnimation.isRunning()) {
                frameAnimation.stop();
            } else {
                frameAnimation.stop();
                frameAnimation.start();
            }
            dialogp1.show();


            meetingDescription = meetingdesp.getText().toString();


            // int timedurattion=Integer.parseInt()

        }

        @Override
        protected String doInBackground(String... params) {
            String scheduledTimeSlot_time[] = (scheduledTimeSlot).split(":");
            int firstH = Integer.parseInt(scheduledTimeSlot_time[0]);
            int firstHm = Integer.parseInt(scheduledTimeSlot_time[1]);
            String meeting_req_time[] = (senderdateandtime).split(":");
            int firstH1 = Integer.parseInt(meeting_req_time[0]);
            int firstHm1 = Integer.parseInt(meeting_req_time[1]);
            int finalMints = firstHm + firstHm1;
            int finalHrs = firstH + firstH1;
            if (finalMints >= 60) {
                finalHrs += 1;
                finalMints %= 60;
            }

            senderToDateTime = (senderdate + " " + finalHrs + ":" + finalMints + ":" + 00 + "." + 000);
            Log.d("senderToDateTime", senderToDateTime);

            TreeSet arrayList1 = new TreeSet();
            if (list3.isEmpty()) {
                arrayList1.add(Friss_Pojo.UseridTo);

            } else if (!(list3.isEmpty())) {
                arrayList1.add(Friss_Pojo.UseridTo);
                list3 = adp3.items;


            }

            for (int i = 0; i < list3.size(); i++) {
                Friend_list_Pojo state = list3.get(i);

                if (state.isSelected()) {
                    //  responseText1.append("," + state.getAvatarPath());
                    arrayList1.add(state.getUserId());
                }
            }

            try {

              /*  for (int i = 0; i < arrayList1.size(); i++) {

                    String data = arrayList1.get(i).toString();

                    int data1 = Integer.parseInt(data);

                    int[] recipients = {data1};*/
                String xmlString = "<Recipients>";
                Iterator<String> itr = arrayList1.iterator();
                while (itr.hasNext()) {
                    String data = itr.next();

                    xmlString += "<RecipientID>";
                    xmlString += data;
                    xmlString += "</RecipientID>";
                    xmlString += "<ResponseDateTime>";
                    xmlString += null;
                    xmlString += "</ResponseDateTime>";
                    xmlString += "<RecipientFromDateTime>";
                    xmlString += null;
                    xmlString += "</RecipientFromDateTime>";
                    xmlString += "<RecipientToDateTime>";
                    xmlString += null;
                    xmlString += "</RecipientToDateTime>";
                    xmlString += "<Status>";
                    xmlString += 0;
                    xmlString += "</Status>";


                    //String xmlString = "<Recipients>";
                    //String [] s1= (String[]) arrayList1.toArray();
              /*  for (int j = 0; j < s1.length; j++) {
                    xmlString += "<RecipientID>";
                    xmlString += s1[j];
                    xmlString += "</RecipientID>";
                    xmlString += "<ResponseDateTime>";
                    xmlString += null;
                    xmlString += "</ResponseDateTime>";
                    xmlString += "<RecipientFromDateTime>";
                    xmlString += null;
                    xmlString += "</RecipientFromDateTime>";
                    xmlString += "<RecipientToDateTime>";
                    xmlString += null;
                    xmlString += "</RecipientToDateTime>";
                    xmlString += "<Status>";
                    xmlString += 0;
                    xmlString += "</Status>";
                }*/
                }
                xmlString += "</Recipients>";

                xmlString = xmlString.replace("/", "@");
                xmlString = xmlString.replace("<", "%3C");
                xmlString = xmlString.replace(">", "%3E");


                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.MEETING_INSERT + Friss_Pojo.UseridFrom + "/" + requestDateTime + "/" + senderFromDateTime + "/" + senderToDateTime + "/" + scheduledTimeSlot + "/" + meetingDescription + "/" + xmlString + "/" + Meetingrequest_Pojo.Meeting_Latitude_To + "/" + Meetingrequest_Pojo.Meeting_Longitude_To + "/" + Meetingrequest_Pojo.Meeting_Latitude + "/" + Meetingrequest_Pojo.Meeting_Longitude + "/" + Meetingrequest_Pojo.UserPreferredLocationId + "/" + "null" + "/" + Meetingrequest_Pojo.Meeting_DestinationType + "/" + Meetingrequest_Pojo.Meeting_geoDateTime;

                url = url.replace(" ", "%20");
                ServiceHandler sh = new ServiceHandler();
                Log.d("url: ", "> " + url);
                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
                Log.d("Response: ", "> " + jsonStr);
                Log.d("valus......", jsonStr);
                return jsonStr;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String meetreq) {
            super.onPostExecute(meetreq);
            dialogp1.dismiss();
            if (jsonStr == null) {
                Toast.makeText(getApplication(), "Oops.. Something's not right", Toast.LENGTH_SHORT).show();
            } else if (!jsonStr.equals("0")) {

                final Dialog dialogp2 = new Dialog(Meetingrequst.this);
                // Include dialog.xml file
                dialogp2.getWindow();
                dialogp2.setCancelable(false);
                dialogp2.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogp2.setContentView(R.layout.meeting_reqdailogbox);
                Button cls = (Button) dialogp2.findViewById(R.id.cls);
                cls.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogp2.dismiss();
                        Intent friend_listclass = new Intent(getApplicationContext(), FriendSerching.class);
                        startActivity(friend_listclass);
                    }
                });
                dialogp2.show();
                dialogp2.setOnKeyListener(new Dialog.OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialogp2.dismiss();
                            Intent friend_listclass = new Intent(getApplicationContext(), FriendSerching.class);
                            startActivity(friend_listclass);
                        }
                        return true;
                    }
                });


            } else {
                ///loginErrorMsg.setText("Username and Password incorrect");
            }

        }
    }

    public class Destinaen extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogp1 = new Dialog(Meetingrequst.this);
            // Include dialog.xml file
            dialogp1.getWindow();

            dialogp1.setCancelable(false);
            dialogp1.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogp1.setContentView(R.layout.dailog_box);


            ImageView imgView = (ImageView) dialogp1.findViewById(R.id.animationImage);
            imgView.setVisibility(ImageView.VISIBLE);
            imgView.setBackgroundResource(R.drawable.frame_animation);

            AnimationDrawable frameAnimation = (AnimationDrawable) imgView.getBackground();

            if (frameAnimation.isRunning()) {
                frameAnimation.stop();
            } else {
                frameAnimation.stop();
                frameAnimation.start();
            }
            dialogp1.show();

        }

        @Override
        public String doInBackground(String... params) {


            try {
                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.ORZIN_DESTLIST + Friss_Pojo.UseridFrom;
                ServiceHandler sh = new ServiceHandler();

                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

                Log.d("url: ", "> " + url);

                Log.d("Response: ", "> " + jsonStr);

                Log.d("valus......", jsonStr.toString());
                aJson = new JSONArray(jsonStr.toString());
                // create apps list


                for (int i = 0; i < aJson.length(); i++) {
                    JSONObject json = aJson.getJSONObject(i);
                    Orzine_pojo orzine = new Orzine_pojo();
                    orzine.setIsDefault(json.getString("IsDefault"));
                    orzine.setLatitude(json.getString("Latitude"));
                    orzine.setLongitude(json.getString("Longitude"));
                    orzine.setLocationType(json.getString("LocationType"));
                    orzine.setUserID(json.getString("UserID"));
                    orzine.setUserPreferredLocationID(json.getString("UserPreferredLocationID"));
                    orzine.setLocationName(json.getString("LocationName"));
                    Log.d("valupedig............", json.getString("LocationName"));

                    Log.d("valupedig............", json.toString());

                    list.add(orzine);

                }
                return jsonStr;
                //notify the activity that fetch data has been complete
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            dialogp1.dismiss();
            if (list.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Oops.. Something's not right", Toast.LENGTH_LONG).show();
            } else {
                adp1 = new OrzineAdapterList(getApplicationContext(), list);
                des_list.setAdapter(adp1);

                des_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Orzine_pojo item = (Orzine_pojo) adp1.getItem(position);
                        textset.setText(item.getLocationName());
                        to.setText(item.getLocationName());
                        Meetingrequest_Pojo.Meeting_Latitude_To = Double.parseDouble(item.getLongitude());
                        Meetingrequest_Pojo.Meeting_Longitude_To = Double.parseDouble(item.getLatitude());
                        Meetingrequest_Pojo.UserPreferredLocationId = item.getUserPreferredLocationID();


                    }
                });

            }
        }
    }

    public class Friendlst_box extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //  progressDialog = new ProgressDialog(Meetingrequst.this);
            // progressDialog.setTitle("Loading Frissbi Data........................");

            //  progressDialog.setIndeterminate(false);
            //  progressDialog.setCancelable(false);
            // progressDialog.show();
        }

        @Override
        public String doInBackground(String... params) {

            // ArrayList arrayList = new ArrayList();

            TreeSet arrayList = new TreeSet();

            if (list3.isEmpty()) {

                arrayList.add(Friss_Pojo.AvatarPathTo);

            } else if (!(list3.isEmpty())) {
                arrayList.add(Friss_Pojo.AvatarPathTo);
                list3 = adp3.items;


            }

            for (int i = 0; i < list3.size(); i++) {
                Friend_list_Pojo state = list3.get(i);

                if (state.isSelected()) {
                    //  responseText1.append("," + state.getAvatarPath());
                    arrayList.add(state.getAvatarPath());
                }
            }


            try {


                Iterator<String> itr = arrayList.iterator();
                while (itr.hasNext()) {
                    String data = itr.next();
                    //Code to add a new element to the TreeSet ts
                    //String data = s[i];
                    Image_Pojo imgpojo = new Image_Pojo();
                    imgpojo.setImage_id(data);
                    list1.add(imgpojo);
                    //String [] s= (String[]) arrayList.toArray();
                }
              /*  for (int i = 0; i < s.length; i++) {

                    String data = s[i];
                    Image_Pojo imgpojo = new Image_Pojo();
                    imgpojo.setImage_id(data);
                    list1.add(imgpojo);


                }*/

            } catch (Exception e) {
                e.printStackTrace();


            }
            return null;
        }


        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            // progressDialog.dismiss();
            // adapter = new ArrayAdapter<String>(getApplicationContext(),
            // android.R.layout.simple_list_item_1, android.R.id.text1, list);

            adp = new Meeting_mulltyrequstfrs_Addapter(getApplicationContext(), list1);
            horizontalView.setAdapter(adp);
           /* userids_prf = getSharedPreferences("USERIDS", Context.MODE_PRIVATE);
            userids_editor = userids_prf.edit();
            userids_editor.clear();
            userids_editor.commit();*/


        }
    }

    public class Friendlst_box_friends extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        public String doInBackground(String... params) {
            try {
                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.USER_FRIENDSlIST + Friss_Pojo.UserNameFrom;
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

                    list3.add(friendlistPojo);

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

            /*dialogp1 = new Dialog(Meetingrequst.this);
            // Include dialog.xml file
            dialogp1.getWindow();
            dialogp1.setCancelable(false);
            dialogp1.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogp1.setContentView(R.layout.dailog_box);
            ImageView imgView = (ImageView)dialogp1.findViewById(R.id.animationImage);
            //imgView.setVisibility(ImageView.VISIBLE);
            imgView.setVisibility(View.VISIBLE);
            imgView.setBackgroundResource(R.drawable.frame_animation);

            AnimationDrawable frameAnimation =(AnimationDrawable) imgView.getBackground();

            if (frameAnimation.isRunning()) {
                frameAnimation.stop();
            }
            else {
                frameAnimation.stop();
                frameAnimation.start();
            }
            dialogp1.show();*/

            if (!(list3.isEmpty())) {

                adp3 = new FriendListaddapater(getApplicationContext(), list3);
                friend_list.setAdapter(adp3);
                adp3.notifyDataSetChanged();
                friend_list.smoothScrollToPosition(0);
                animation_img.setVisibility(View.INVISIBLE);
                loading_text.setVisibility(View.INVISIBLE);
            } else {
                Toast.makeText(getApplicationContext(), "Let's make some friends first", Toast.LENGTH_LONG).show();
            }

        }

    }

    private class ViewHolder {
        TextView firstlastname;
        CheckBox friend_chekbox;
        ImageView imageViewRound;
        //LinearLayout change_clr;
    }

    public class FriendListaddapater extends ArrayAdapter<Friend_list_Pojo> {
        ViewHolder holder = null;
        private List<Friend_list_Pojo> items;

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
                        Friend_list_Pojo _state = (Friend_list_Pojo) cb.getTag();
                        // Toast.makeText(getApplicationContext(), "Clicked on Checkbox: " + cb.getText() + " is " + cb.isChecked(), Toast.LENGTH_LONG).show();
                        _state.setSelected(cb.isChecked());
                    }
                });


            } else {
                holder = (ViewHolder) v.getTag();
            }


            Friend_list_Pojo state = items.get(position);

            holder.firstlastname.setText(state.getFirstName() + " " + state.getLastName());
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






