package com.frissbi.Frissbi_Meetings;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.Frissbi_Pojo.Meeting_summaryPojo;
import com.frissbi.MapLocations.ToLocationsformap;
import com.frissbi.R;
import com.frissbi.Utility.HorizontalView;
import com.frissbi.Utility.ServiceHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Created by KNPL003 on 07-09-2015.
 */

public class MeetingSummary extends FragmentActivity implements LocationListener {

    JSONArray aJson = null;
    ImageView imageViewRound, imageViewRound1, imageViewRound2, imageViewRound3;
    Button add1;
    TextView meeting_description, Sender_FromDateTime, Sender_Time, place_address;
    String jsonStr;
    Location location;
    Marker mMarker;
    GoogleMap googleMap;
    double latitude;
    double longitude;
    LatLng latLng;
    HorizontalView horizontalView;
    List<Meeting_summaryPojo> list = new ArrayList<Meeting_summaryPojo>();
    Meeting_summary_adapter adp;
    private ProgressDialog progressDialog;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    Dialog dialogp;

    String Meetingid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.meetingsummary);
        meeting_description = (TextView) findViewById(R.id.meeting_description);
        Sender_FromDateTime = (TextView) findViewById(R.id.Sender_FromDateTime);
        Sender_Time = (TextView) findViewById(R.id.Sender_Time);
        place_address = (TextView) findViewById(R.id.place_address);
        horizontalView = (HorizontalView) findViewById(R.id.hor_list);


        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                googleMap = map;
                if (ActivityCompat.checkSelfPermission(MeetingSummary.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MeetingSummary.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                googleMap.setMyLocationEnabled(true);
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                String bestProvider = locationManager.getBestProvider(criteria, true);
                Location location = locationManager.getLastKnownLocation(bestProvider);
            }
        });


        // onLocationChanged(location);
        Intent intent = getIntent();
        if (null != intent) {
            Meetingid = intent.getStringExtra(Friss_Pojo.MEETING_ID);

            Friss_Pojo.MeetingID = Meetingid.toString();

            Log.d("Meeting id", Meetingid);
        }
        new MeetingSummary_Details().execute();
    }




    public void onLocationChanged(Location location) {

        LatLng latLng = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions().position(latLng));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        //locationTv.setText("Latitude:" + latitude + ", Longitude:" + longitude);
        Toast.makeText(getApplicationContext(), "Latitude:" + latitude + ", Longitude:" + longitude, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    public class MeetingSummary_Details extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialogp = new Dialog(MeetingSummary.this);
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
        public String doInBackground(String... params) {


            try {

                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.MEETING_SUMMARY + Friss_Pojo.MeetingID;  //Meeting Id BY
                ServiceHandler sh = new ServiceHandler();
                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
                Log.d("Response: ", "> " + url);
                //  JSONObject json = new JSONObject(jsonStr);
                Log.d("valus......", jsonStr.toString());
                JSONObject jsonObject = new JSONObject(jsonStr);
                JSONArray jsonArray = jsonObject.getJSONArray("Recipients");
                for (int i = 0; i < jsonArray.length(); i++) {
                    Meeting_summaryPojo app1 = new Meeting_summaryPojo();
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    app1.setFirstName(jsonObject1.getString("FirstName"));
                    app1.setLastName(jsonObject1.getString("LastName"));
                    app1.setAvatarPath(jsonObject1.getString("AvatarPath"));

                    list.add(app1);
                }


                return jsonStr;
                //notify the activity that fetch data has been complete

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String jsonStr) {
            super.onPostExecute(jsonStr);
            dialogp.dismiss();


            try {
                JSONObject jsonObject = new JSONObject(jsonStr);
                Meeting_summaryPojo app = new Meeting_summaryPojo();
                app.setDestinationAddress(jsonObject.getString("DestinationAddress"));
                app.setLatitude(jsonObject.getString("Latitude"));
                app.setLongitude(jsonObject.getString("Longitude"));
                app.setMeetingDescription(jsonObject.getString("MeetingDescription"));
                app.setSenderFromDateTime(jsonObject.getString("SenderFromDateTime"));
                app.setSenderToDateTime(jsonObject.getString("SenderToDateTime"));


                meeting_description.setText(app.getMeetingDescription());

                String s = app.getMeetingDescription();
                String s1 = app.getSenderFromDateTime();
                String s2 = app.getSenderToDateTime();
                String s3 = app.getLatitude();
                String s4 = app.getLongitude();
                Log.d("s", s);
                Log.d("s1", s1);
                Log.d("s2", s2);
                Log.d("s3", s3);
                Log.d("s4", s4);


                String SenderDate = app.getSenderFromDateTime();
                if (!(SenderDate.equalsIgnoreCase(""))) {
                    Log.d("Value", SenderDate);
                    String SenderDate1 = SenderDate.substring(0, 10);
                    String SenderDate2 = SenderDate.substring(10, 16);
                    Sender_FromDateTime.setText(SenderDate1);


                    String Timesender = app.getSenderToDateTime();
                    String Timesender1 = Timesender.substring(0, 10);
                    String Timesender2 = Timesender.substring(10, 16);
                    Sender_Time.setText(SenderDate2 + "-" + Timesender2);
                    place_address.setText(app.getDestinationAddress());
                    latitude = Double.parseDouble(app.getLatitude());
                    longitude = Double.parseDouble(app.getLongitude());
                    Log.d("Values", String.valueOf(latitude));


                    Log.d("Value", s);
                    Log.d("Value", s1);
                    Log.d("Value", s2);
                    Log.d("Value", s3);
                    Log.d("Value", s4);
                    onLocationChanged(location);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            adp = new Meeting_summary_adapter(getApplicationContext(), list);
            horizontalView.setAdapter(adp);





                /* Meeting_summaryPojo app = new Meeting_summaryPojo();

                    String SenderDate = app.getSenderFromDateTime();
                    Log.d("Value",SenderDate);
                    String SenderDate1 = SenderDate.substring(0, 10);
                    String SenderDate2 = SenderDate.substring(10, 16);
                    Sender_FromDateTime.setText(SenderDate1);
                    String Timesender = app.getSenderToDateTime();
                    String Timesender1 = Timesender.substring(0, 10);
                    String Timesender2 = Timesender.substring(10, 16);
                    Sender_Time.setText(SenderDate2 + "-" + Timesender2);
                    place_address.setText(app.getDestinationAddress());
                    latitude = Double.parseDouble(app.getLatitude());
                    longitude = Double.parseDouble(app.getLongitude());
                    Log.d("Values", String.valueOf(latitude));

                    //latLng = new LatLng(17.44, 78.36);
                    Log.d("Values", String.valueOf(latLng));
                    onLocationChanged(location);
                   // adp=new Meeting_summary_adapter(getApplicationContext(),list);
                   // horizontalView.setAdapter(adp);*/
        }


    }



}
