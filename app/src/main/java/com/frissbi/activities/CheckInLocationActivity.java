package com.frissbi.activities;

import android.app.ProgressDialog;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.TSLocationManager;
import com.frissbi.Utility.Utility;
import com.frissbi.adapters.CheckInLocationAdapter;
import com.frissbi.interfaces.CheckInLocationListener;
import com.frissbi.models.LocationSuggestion;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CheckInLocationActivity extends AppCompatActivity implements CheckInLocationListener {
    private static final String API_KEY = "AIzaSyCmJAbD3ijBFz_oFjOLvNJnh5e9chInBdc";
    private Location mLocation;
    List<String> placeIdsStringList;
    private PlaceDetailsAsync mPlaceDetailsAsync;
    private CheckInPlacesIdAsync mCheckInPlacesId;
    private int counter;
    ProgressDialog mProgressDialog;
    List<LocationSuggestion> mLocationSuggestions;
    private CheckInLocationAdapter mCheckInLocationAdapter;
    private RecyclerView mCheckInRecyclerView;
    private CheckInLocationListener mCheckInLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_location);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mCheckInLocationListener = (CheckInLocationListener) this;
        mCheckInRecyclerView = (RecyclerView) findViewById(R.id.checkIn_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mCheckInRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mCheckInRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mCheckInRecyclerView.addItemDecoration(dividerItemDecoration);
        placeIdsStringList = new ArrayList<>();
        mLocationSuggestions = new ArrayList<>();
        mProgressDialog = new CustomProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mLocation = TSLocationManager.getInstance(this).getCurrentLocation();

        mPlaceDetailsAsync = new PlaceDetailsAsync();
        mCheckInPlacesId = new CheckInPlacesIdAsync();
        mProgressDialog.show();
        mCheckInPlacesId.execute();
    }

    @Override
    public void selectedCheckInLocation(final LocationSuggestion locationSuggestion) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.check_in_message_alert, null);
        builder.setView(view);
        TextView checkInTv = (TextView) view.findViewById(R.id.check_in_loc_tv);
        final EditText locDescriptionEt = (EditText) view.findViewById(R.id.loc_description_et);
        ImageView submitCheckIn = (ImageView) view.findViewById(R.id.submit_checkIn_imageView);
        checkInTv.setText(locationSuggestion.getName());
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        submitCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                sendChexkInLocationToServer(locationSuggestion, locDescriptionEt.getText().toString());
            }
        });


    }


    private void sendChexkInLocationToServer(LocationSuggestion locationSuggestion, String description) {
        mProgressDialog.show();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", SharedPreferenceHandler.getInstance(this).getUserId());
            jsonObject.put("address", locationSuggestion.getName());
            jsonObject.put("latitude", locationSuggestion.getLatitude());
            jsonObject.put("longitude", locationSuggestion.getLongitude());

            if (description.trim().length() > 0) {
                jsonObject.put("description", description);
            }

            String url = Utility.REST_URI + Utility.CHECK_IN_LOCATION;
            TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {

                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            Toast.makeText(CheckInLocationActivity.this, response.message, Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(CheckInLocationActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CheckInLocationActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mProgressDialog.dismiss();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public class CheckInPlacesIdAsync extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... params) {

            JSONObject placesIdJsonObject = null;
            HttpURLConnection conn = null;
            StringBuilder jsonResults = new StringBuilder();
            try {
                StringBuilder sb = new StringBuilder(
                        "https://maps.googleapis.com/maps/api/place"
                                + "/radarsearch" + "/json?");
                sb.append("location=").append(mLocation.getLatitude()).append(",").append(mLocation.getLongitude());
                sb.append("&radius=500");
                sb.append("&types=restaurant");
                sb.append("&sensor=true");
                sb.append("&key=").append(API_KEY);


                URL url = new URL(sb.toString());
                FLog.d("CheckInLocationActivity", "CheckIn---URL" + url);
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());
                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }


                try {
                    placesIdJsonObject = new JSONObject(jsonResults.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
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
            return placesIdJsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            setPlacesIds(jsonObject);
        }
    }

    private void setPlacesIds(JSONObject jsonObject) {

        try {
            JSONArray resultJsonArray = jsonObject.getJSONArray("results");
            for (int i = 0; i < resultJsonArray.length(); i++) {
                JSONObject placeJsonObject = resultJsonArray.getJSONObject(i);
                placeIdsStringList.add(placeJsonObject.getString("place_id"));
            }

            getPlaceDetailsById();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void getPlaceDetailsById() {

        mPlaceDetailsAsync.execute(placeIdsStringList.get(counter));
    }

    class PlaceDetailsAsync extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject resultJsonObject = null;

            HttpURLConnection conn = null;
            StringBuilder jsonResults = new StringBuilder();
            try {
                StringBuilder sb = new StringBuilder(
                        "https://maps.googleapis.com/maps/api/place"
                                + "/details" + "/json?");
                sb.append("placeid=").append(params[0]);
                sb.append("&key=").append(API_KEY);


                URL url = new URL(sb.toString());
                FLog.d("CheckInLocationActivity", "PlaceDetails----URL" + url);
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());
                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }


                try {
                    resultJsonObject = new JSONObject(jsonResults.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
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


            return resultJsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            counter++;
            setLocationDetails(jsonObject);
            if (counter == placeIdsStringList.size()) {
                mProgressDialog.dismiss();
                mCheckInLocationAdapter = new CheckInLocationAdapter(CheckInLocationActivity.this, mLocationSuggestions, mCheckInLocationListener);
                mCheckInRecyclerView.setAdapter(mCheckInLocationAdapter);
            } else {
                mPlaceDetailsAsync = new PlaceDetailsAsync();
                mPlaceDetailsAsync.execute(placeIdsStringList.get(counter));
            }
        }
    }

    private void setLocationDetails(JSONObject jsonObject) {
        try {
            JSONObject resultJsonObject = jsonObject.getJSONObject("result");
            LocationSuggestion locationSuggestion = new LocationSuggestion();

            locationSuggestion.setImageUrl(resultJsonObject.getString("icon"));
            locationSuggestion.setName(resultJsonObject.getString("name"));
            locationSuggestion.setPlaceId(resultJsonObject.getString("place_id"));

            JSONObject geometryJsonObject = resultJsonObject.getJSONObject("geometry");

            JSONObject locationJsonObject = geometryJsonObject.getJSONObject("location");

            locationSuggestion.setLatitude(locationJsonObject.getDouble("lat"));
            locationSuggestion.setLongitude(locationJsonObject.getDouble("lng"));

            mLocationSuggestions.add(locationSuggestion);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }


}