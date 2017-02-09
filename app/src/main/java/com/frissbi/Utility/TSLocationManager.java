package com.frissbi.Utility;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;

import com.frissbi.activities.SelectLocationAndSaveActivity;

/**
 * Created by thrymr on 31/1/17.
 */
public class TSLocationManager {
    private static TSLocationManager ourInstance;
    private Context mContext;
    private LocationManager mLocationManager;

    public static TSLocationManager getInstance(Context context) {
        if (ourInstance == null)
            ourInstance = new TSLocationManager(context);
        return ourInstance;
    }

    private TSLocationManager(Context context) {
        mContext = context;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }


    public Location getCurrentLocation() {


        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Location gpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location networkLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        return getBetterLocation(gpsLocation, networkLocation);

    }

    private Location getBetterLocation(Location loc1, Location loc2) {
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

    public boolean isLocationOn() {

        FLog.d("TLocationManager", "Gps" + LocationManager.GPS_PROVIDER + "Network" + LocationManager.NETWORK_PROVIDER);
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

    }

}
