package com.frissbi.Utility;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by thrymr on 11/1/17.
 */

public class LocationInfo {


    public void getLocationInfo(double lat, double lng) {

        String LOCATION_DETAILS_URL = "http://maps.google.com/maps/api/geocode/json?";
        String LOCATION_SENSOR = "&sensor=true";
        String LOCATION_BY_ADDRESS = "address=";
        String LOCATION_BY_LOCATION = "latlng=";
        String url = LOCATION_DETAILS_URL + LOCATION_BY_LOCATION + lat + "," + lng + LOCATION_SENSOR;


    }


    private class LocationDetails extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {
            try {
                downloadUrl(strings[0]);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
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
