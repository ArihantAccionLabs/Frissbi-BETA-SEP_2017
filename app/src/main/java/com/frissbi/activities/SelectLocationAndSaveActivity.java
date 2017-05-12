package com.frissbi.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.frissbi.MapLocations.PlaceJSONParser;
import com.frissbi.R;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.TSLocationManager;
import com.frissbi.Utility.Utility;
import com.frissbi.models.MyPlaces;
import com.frissbi.networkhandler.TSNetworkHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

public class SelectLocationAndSaveActivity extends AppCompatActivity {

    private AutoCompleteTextView mLocationSearchAutoCompleteTv;
    private SupportMapFragment mSupportMap;
    private GoogleMap mGoogleMap;
    private double mLatitude;
    private double mLongitude;
    public static final String EMPTY_STRING = "";
    private static final String API_KEY = "AIzaSyCmJAbD3ijBFz_oFjOLvNJnh5e9chInBdc";
    private Location mLocation;
    private List<HashMap<String, String>> placesHashMapList = null;
    private PlaceJSONParser mPlaceJSONParser;
    private AlertDialog mAlertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location_and_save);
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

        }
        mLocationSearchAutoCompleteTv = (AutoCompleteTextView) findViewById(R.id.location_search_autoCompleteTv);
        mSupportMap = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSaveToMyPlacesDialog();
            }
        });
        mPlaceJSONParser = new PlaceJSONParser();
        mSupportMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                setUpMap();
            }
        });

        mLocationSearchAutoCompleteTv.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!mLocationSearchAutoCompleteTv.isPerformingCompletion()) {
                    if (s.length() > 0) {
                        if (new ConnectionDetector(SelectLocationAndSaveActivity.this).isConnectedToInternet()) {

                            getLocationInfo(s.toString());
                        } else {
                            Toast.makeText(getApplicationContext(), "You don't have an internet connection", Toast.LENGTH_SHORT).show();

                        }
                    }
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

        mLocationSearchAutoCompleteTv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String, String> hashMap = placesHashMapList.get(i);
                mLocationSearchAutoCompleteTv.setText(hashMap.get("address"));
                Log.d("SelectLocation", "_id" + hashMap.get("_id"));
                //getLocationDetailsById(hashMap.get("_id"));
                new PlacesTask().execute(hashMap.get("_id"));
            }
        });

    }

    private void showSaveToMyPlacesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.alert_save_to_my_places, null);
        builder.setView(view);
        Button noButton = (Button) view.findViewById(R.id.no_button);
        Button yesButton = (Button) view.findViewById(R.id.yes_button);
        final EditText locationNameEt = (EditText) view.findViewById(R.id.location_name_et);
        mAlertDialog = builder.create();
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlertDialog.dismiss();
                sendLocationDetails("");

            }
        });
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationNameEt.getText().toString().trim().length() > 0) {

                    //  checkIsLocationExist(locationNameEt.getText().toString().trim());
                    sendLocationDetailsToServer(locationNameEt.getText().toString().trim());
                } else {
                    Toast.makeText(SelectLocationAndSaveActivity.this, "Enter location name", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mAlertDialog.show();

    }


    private void sendLocationDetailsToServer(final String locationName) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", SharedPreferenceHandler.getInstance(this).getUserId());
            jsonObject.put("latitude", mLatitude);
            jsonObject.put("longitude", mLongitude);
            jsonObject.put("locationName", locationName);
            jsonObject.put("address", mLocationSearchAutoCompleteTv.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = Utility.REST_URI + Utility.LOCATION_INSERT;
        TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            JSONObject responseJsonObject = new JSONObject(response.response);
                            if (responseJsonObject.getBoolean("isExist")) {
                                Toast.makeText(SelectLocationAndSaveActivity.this, responseJsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            } else {
                                if (responseJsonObject.getBoolean("isInserted")) {
                                    sendLocationDetails(locationName);
                                    Toast.makeText(SelectLocationAndSaveActivity.this, responseJsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SelectLocationAndSaveActivity.this, responseJsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(SelectLocationAndSaveActivity.this, response.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SelectLocationAndSaveActivity.this, "Something went wrong at server end", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendLocationDetails(String locationName) {
        MyPlaces myPlaces = new MyPlaces();
        myPlaces.setLatitude(mLatitude);
        myPlaces.setLongitude(mLongitude);
        myPlaces.setName(locationName);
        myPlaces.setAddress(mLocationSearchAutoCompleteTv.getText().toString());
        Intent intent = new Intent();
        intent.putExtra("selected_from", "map");
        intent.putExtra("selected_place", myPlaces);
        setResult(RESULT_OK, intent);
        finish();
    }


    private void setUpMap() {
        if (ActivityCompat.checkSelfPermission(SelectLocationAndSaveActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SelectLocationAndSaveActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);


        mLocation = TSLocationManager.getInstance(SelectLocationAndSaveActivity.this).getCurrentLocation();
        Log.d("SelectLocation", "Location" + mLocation);

        if (mLocation != null) {
            double latitude = mLocation.getLatitude();
            double longitude = mLocation.getLongitude();
            getLocationNameFromLatLng(latitude, longitude);
            setLocationOnMap(latitude, longitude);
           /* Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses1 = null;
            try {
                addresses1 = geocoder.getFromLocation(mLatitude, mLongitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String cityName = addresses1.get(0).getAddressLine(0);
            String stateName = addresses1.get(0).getAddressLine(1);
            String countryName = addresses1.get(0).getAddressLine(2);
            mLocationSearchAutoCompleteTv.setText(cityName + "," + stateName + "," + countryName);*/
        }
    }

    private void getLocationNameFromLatLng(double latitude, double longitude) {
        TSNetworkHandler.getInstance(this).getResponse("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&key=" + "AIzaSyCmJAbD3ijBFz_oFjOLvNJnh5e9chInBdc", new JSONObject(), "GET", new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {

                try {
                    JSONObject responseJsonObject = new JSONObject(response.response);
                    JSONArray results = (JSONArray) responseJsonObject.get("results");
                    JSONObject resultsObject = (JSONObject) results.get(0);
                    String formattedAddress = (String) resultsObject.get("formatted_address");
                    mLocationSearchAutoCompleteTv.setText(formattedAddress);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    private void getLocationInfo(String input) {

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(
                    "https://maps.googleapis.com/maps/api/place"
                            + "/autocomplete" + "/json");
            sb.append("?sensor=false&key=").append(API_KEY);
            sb.append("&location=").append(URLEncoder.encode(getLatLng(SelectLocationAndSaveActivity.this, mLocation), "utf8"));
            sb.append("&radius=1000");
            sb.append("&input=").append(URLEncoder.encode(input, "utf8"));

            Log.d("ToLocationsformap", "sb" + sb);
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e("placeAPI", "Error processing Places API URL" + e);
        } catch (IOException e) {
            Log.e("placeAPI", "Error connecting to Places API" + e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            placesHashMapList = mPlaceJSONParser.parse(jsonObj);
            Log.d("ToLocationsformap", "places" + placesHashMapList);
            String[] from = new String[]{"description"};
            int[] to = new int[]{android.R.id.text1};
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), placesHashMapList, android.R.layout.simple_list_item_1, from, to);
            mLocationSearchAutoCompleteTv.setAdapter(adapter);
        } catch (JSONException e) {
            Log.e("placeAPI", "Cannot process JSON results" + e);
        }

    }


    private void setLocationOnMap(double latitude, double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
        Log.d("SelectLocation", "Location---latitude" + mLatitude + "longitude---" + mLongitude);
        mGoogleMap.clear();
        LatLng latLng = new LatLng(latitude, longitude);
        Log.d("SelectLocation", "Location---latLng" + latLng);
        mGoogleMap.addMarker(new MarkerOptions().position(latLng).title("Current Location").snippet("Hyderabad").icon(BitmapDescriptorFactory.fromResource(R.drawable.location)));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    /**
     * Get the latitude and longitude from the Location object returned by
     * Location Services.
     *
     * @param currentLocation A Location object containing the current location
     * @return The latitude and longitude of the current location, or null if no
     * location is available.
     */
    public static String getLatLng(Context context, Location currentLocation) {
        // If the location is valid
        if (currentLocation != null) {

            // Return the latitude and longitude as strings
            return context.getString(R.string.latitude_longitude,
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude());
        } else {

            // Otherwise, return the empty string
            return EMPTY_STRING;
        }
    }

    private void getLocationDetailsById(String placeId) {
        String url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeId + "&key=" + API_KEY;
        Log.d("SelectLocation", "URL" + url);
        TSNetworkHandler.getInstance(SelectLocationAndSaveActivity.this).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {

                if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                    try {
                        JSONObject responseJsonObject = new JSONObject(response.response);
                        Log.d("SelectLocation", "responseJsonObject" + responseJsonObject);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {

                }

            }
        });
    }


    private class PlacesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... placeId) {
            // For storing data from web service
            String data = null;
            String url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeId[0] + "&key=" + API_KEY;
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


            parsePlaceResult(result);

        }
    }

    private void parsePlaceResult(String result) {
        try {
            JSONObject responseJsonObject = new JSONObject(result);
            Log.d("SelectLocation", "responseJsonObject" + responseJsonObject);
            JSONObject resultJsonObject = responseJsonObject.getJSONObject("result");
            if (resultJsonObject.has("geometry")) {

                JSONObject geometryJsonObject = resultJsonObject.getJSONObject("geometry");
                if (geometryJsonObject.has("location")) {
                    JSONObject locationJsonObject = geometryJsonObject.getJSONObject("location");
                    setLocationOnMap(locationJsonObject.getDouble("lat"), locationJsonObject.getDouble("lng"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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


}
