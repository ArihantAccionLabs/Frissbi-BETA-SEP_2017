package com.frissbi.MapLocations;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.Frissbi_Pojo.Meetingrequest_Pojo;
import com.frissbi.R;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.ServiceHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class SetLocationMap extends FragmentActivity implements LocationListener {
    public static String locationName;
    public static Double Latitude1;
    public static Double Longitude1;
    public static Integer IfLocationExists;
    public static Integer IsDefaultExists;
    public static String isDefault = "0";
    public Location location1;
    public EditText nametitle;
    public CheckBox setorzin;
    public Integer updateid;
    AutoCompleteTextView atvPlaces;
    PlacesTask placesTask;
    ParserTask parserTask;
    Button clk, send;
    GoogleMap googleMap;
    MarkerOptions markerOptions;
    LatLng latLng;
    double latitude;
    double longitude;
    Dialog dialogp;
    Dialog dialog;
    String jsonStr;
    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.maplocation);
        clk = (Button) findViewById(R.id.clk);
        send = (Button) findViewById(R.id.done);
        atvPlaces = (AutoCompleteTextView) findViewById(R.id.atv_places);
        setorzin = (CheckBox) findViewById(R.id.setorzin);
        Meetingrequest_Pojo.Meeting_GoogleAddress = atvPlaces.getText().toString();
        setorzin.setText("Save to My Places");
        cd = new ConnectionDetector(getApplicationContext());


        LocationManager locationManager1 = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager1.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Toast.makeText(this, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Goto Settings Page To Enable GPS",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent callGPSSettingIntent = new Intent(
                                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(callGPSSettingIntent);
                                }
                            });
            alertDialogBuilder.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setorzin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                if (cb.isChecked()) {
                    dialog = new Dialog(SetLocationMap.this);
                    // Include dialog.xml file
                    dialog.getWindow();
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.setorzine);
                    // Set dialog title
                    //dialog.setTitle("Custom Dialog");
                    // set values for custom dialog components - text, image and button
                    TextView name = (TextView) dialog.findViewById(R.id.name);
                    name.setText("Save to My Places");
                    nametitle = (EditText) dialog.findViewById(R.id.nametitle);



                   /* setdefault.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (setdefault.isChecked()){
                                if(IsDefaultExists==1){
                                    final Dialog dialog1 = new Dialog(ToLocationsformap.this);
                                    dialog1.getWindow();
                                    dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    dialog1.setContentView(R.layout.existdefault);
                                    dialog1.show();
                                    Button ok= (Button) dialog1.findViewById(R.id.ok);
                                    Button no= (Button) dialog1.findViewById(R.id.no);
                                    ok.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            isDefault="1";
                                            dialog1.dismiss();
                                        }
                                    });
                                    no.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            isDefault="0";
                                            dialog1.dismiss();
                                        }
                                    });

                                }
                                else if(IsDefaultExists==0){
                                    isDefault="1";
                                }
                            }
                            else {
                                isDefault="0";
                            }
                        }
                    });*/

                    Button done = (Button) dialog.findViewById(R.id.done);
                    dialog.show();
                    done.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Close dialog

                            locationName = nametitle.getText().toString();
                            isInternetPresent = cd.isConnectedToInternet();
                            // check for Internet status
                            if (isInternetPresent) {

                                if ((!(locationName.equals("")))) {
                                    new ExistChek().execute(locationName);
                                } else {

                                }


                            } else {
                                //Toast.makeText(getApplicationContext(),"You don't have an internet connection", Toast.LENGTH_SHORT).show();

                            }

                        }
                    });
                }
            }
        });
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // Getting a1 reference to the map
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                googleMap = map;


                setUpMap();


            }
        });


        clk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location3 = atvPlaces.getText().toString();

                isInternetPresent = cd.isConnectedToInternet();
                // check for Internet status
                if (isInternetPresent) {

                    if (location3 != null && !location3.equals("")) {
                        new GeocoderTask().execute(location3);
                        //	Toast.makeText(getApplicationContext(), atvPlaces.getText().toString(), Toast.LENGTH_LONG).show();
                    }


                } else {
                    //Toast.makeText(getApplicationContext(),"You don't have an internet connection", Toast.LENGTH_SHORT).show();

                }


            }
        });

        if (isInternetPresent) {

            String location4 = atvPlaces.getText().toString();
            if (location4 != null && !location4.equals("")) {
                new GeocoderTask().execute(location4);
                // Toast.makeText(getApplicationContext(), atvPlaces.getText().toString(), Toast.LENGTH_LONG).show();
            }

        } else {
            //Toast.makeText(getApplicationContext(),"You don't have an internet connection", Toast.LENGTH_SHORT).show();

        }


        atvPlaces.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesTask = new PlacesTask();
                placesTask.execute(s.toString());


                if (isInternetPresent) {

                    String location2 = atvPlaces.getText().toString();

                    if (location2 != null && !location2.equals("")) {
                        new GeocoderTask().execute(location2);
                        // Toast.makeText(getApplicationContext(), atvPlaces.getText().toString(), Toast.LENGTH_LONG).show();
                    }

                } else {
                    //Toast.makeText(getApplicationContext(),"You don't have an internet connection", Toast.LENGTH_SHORT).show();

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


            }
        });
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            // Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    public void onLocationChanged(Location loca) {

	/*	double latitude = location.getLatitude();
        double longitude = location.getLongitude();
		Toast.makeText(getApplicationContext(),"Latitude:" + latitude + ", Longitude:" + longitude ,Toast.LENGTH_LONG).show();*/


        googleMap.addMarker(new MarkerOptions().position(latLng).title("current Location").snippet("Hyderabad").icon(BitmapDescriptorFactory.fromResource(R.drawable.location2)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
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

    // Fetches all places from GooglePlaces AutoComplete Web Service
    private class PlacesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... place) {
            // For storing data from web service
            String data = "";

            // Obtain browser key from https://code.google.com/apis/console
            String key = "key=AIzaSyCmJAbD3ijBFz_oFjOLvNJnh5e9chInBdc";

            String input = "";

            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }


            // place type to be searched
            String types = "types=geocode";

            // Sensor enabled
            String sensor = "sensor=false";

            // Building the parameters to the web service
            String parameters = input + "&" + types + "&" + sensor + "&" + key;

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/" + output + "?" + parameters;

            try {
                // Fetching the data from web service in background
                data = downloadUrl(url);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Creating ParserTask
            parserTask = new ParserTask();

            // Starting Parsing the JSON string returned by Web Service
            parserTask.execute(result);
        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;

            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try {
                jObject = new JSONObject(jsonData[0]);

                // Getting the parsed data as a1 List construct
                places = placeJsonParser.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            String[] from = new String[]{"description"};
            int[] to = new int[]{android.R.id.text1};

            // Creating a1 SimpleAdapter for the AutoCompleteTextView
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);

            // Setting the adapter
            atvPlaces.setAdapter(adapter);
        }
    }

    // An AsyncTask class for accessing the GeoCoding Web Service
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;


            try {
                // Getting a1 maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);


            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }


        @Override
        protected void onPostExecute(List<Address> addresses) {

            if (addresses == null || addresses.size() == 0) {
                //Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }

            // Clears all the existing markers on the map
            googleMap.clear();

            // Adding Markers on Google Map for each matching address
            for (int i = 0; i < addresses.size(); i++) {

                Address address = (Address) addresses.get(i);

                // Creating an instance of GeoPoint, to display in Google Map
                Meetingrequest_Pojo.Meeting_Latitude = address.getLatitude();
                Meetingrequest_Pojo.Meeting_Longitude = address.getLongitude();
                latLng = new LatLng(Meetingrequest_Pojo.Meeting_Latitude, Meetingrequest_Pojo.Meeting_Longitude);

                String addressText = String.format("%s, %s", address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getCountryName());

                onLocationChanged(location1);


			/*	markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
				markerOptions.title(addressText);
				googleMap.addMarker(markerOptions);

				// Locate the first location
				if (i == 0)
					googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
				     googleMap.animateCamera(zoom);*/
            }
        }
    }

    public class ToLocation extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogp = new Dialog(SetLocationMap.this);
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

                int locationType = 2;

                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.LOCATION_INSERT + Friss_Pojo.UseridFrom + "/" + Meetingrequest_Pojo.Meeting_Latitude + "/" + Meetingrequest_Pojo.Meeting_Longitude + "/" + locationName + "/" + locationType + "/" + isDefault;
                url = url.replace(" ", "%20");
                ServiceHandler sh = new ServiceHandler();
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

                Log.d("valus......", jsonStr.toString());

                return jsonStr;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String S) {


            super.onPostExecute(S);
            dialogp.dismiss();
            if (!(jsonStr.equals("0"))) {
                Toast.makeText(getApplication(), "Place saved", Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(getApplicationContext(), Friend_PendingList.class);
                // startActivity(intent);


            } else {
                ///loginErrorMsg.setText("Username and Password incorrect");
                //Toast.makeText(getApplication(), "Successful Update", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }

        }
    }

    public class ExistChek extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogp = new Dialog(SetLocationMap.this);
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

                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.ORZIN_EXISTCHEK + Friss_Pojo.UseridFrom + "/" + (params[0]);
                url = url.replace(" ", "%20");
                ServiceHandler sh = new ServiceHandler();
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
                Log.d("valus......", jsonStr.toString());


                JSONObject json = new JSONObject(jsonStr);
                IfLocationExists = Integer.parseInt(json.getString("IfLocationExists"));
                IsDefaultExists = Integer.parseInt(json.getString("IsDefaultExists"));
                Log.d("valus......", jsonStr.toString());
                //  Friss_Pojo.UserNameFrom = json.getString("UserName");


                // Intent intent=new Intent(getApplicationContext(),FriendSerching.class);
                // startActivity(intent);


                return jsonStr;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String S) {


            super.onPostExecute(S);
            dialogp.dismiss();
            if ((IfLocationExists) > 0) {
                //Toast.makeText(getApplication(), "Successful Update", Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(getApplicationContext(), Friend_PendingList.class);
                // startActivity(intent);
                nametitle.setError("Try a different name");
            } else {
                ///loginErrorMsg.setText("Username and Password incorrect");
                isInternetPresent = cd.isConnectedToInternet();
                // check for Internet status
                if (isInternetPresent) {

                    new ToLocation().execute();
                } else {

                }

            }

        }
    }


    private void setUpMap() {
        if (ActivityCompat.checkSelfPermission(SetLocationMap.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SetLocationMap.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

      /*  LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);*/


        Location location = getCurrentLocation();
        Log.d("ToLocationsformap", "Location" + location);
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.d("ToLocationsformap", "Location---latitude" + latitude + "longitude---" + longitude);
            //atvPlaces.setThreshold(1);


            //locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);
            //double latitude=12.916523125961666;
            //double longitude=77.61959824603072;
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses1 = null;
            try {
                addresses1 = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String cityName = addresses1.get(0).getAddressLine(0);
            String stateName = addresses1.get(0).getAddressLine(1);
            String countryName = addresses1.get(0).getAddressLine(2);

            atvPlaces.setText(cityName + "," + stateName + "," + countryName);
            //atvPlaces.append(cityName);
            //atvPlaces.append(countryName1);
            //atvPlaces.append(countryName2);
            //atvPlaces.append(countryName3);
            //Toast.makeText(getApplicationContext(), cityName + " " + stateName, Toast.LENGTH_LONG).show();

        }
        Log.d("ToLocationsformap", "latitude" + latitude + "longitude" + longitude);

        LatLng latLng = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions().position(latLng).title("current Location").snippet("Hyderabad").icon(BitmapDescriptorFactory.fromResource(R.drawable.location2)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    public Location getCurrentLocation() {


        if (ActivityCompat.checkSelfPermission(SetLocationMap.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(SetLocationMap.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Log.d("ToLocationsformap", "networkLocation---latitude" + networkLocation.getLatitude() + "longitude" + networkLocation.getLongitude());
        return getBetterLocation(gpsLocation, networkLocation);

    }

    protected Location getBetterLocation(Location loc1, Location loc2) {
        if (loc1 == null && loc2 != null) {

            return loc2;
        } else if (loc2 == null && loc1 != null) {

            return loc1;
        } else if (loc1 != null) {

            if (loc1.getAccuracy() < loc2.getAccuracy()) {
                return loc1;
            } else {
                return loc2;
            }
        } else {
            return null;
        }


    }


}
