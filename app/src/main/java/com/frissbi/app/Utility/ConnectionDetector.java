package com.frissbi.app.Utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionDetector {

    private Context _context;
    private boolean connected;
    public static ConnectionDetector instance;

    public static ConnectionDetector  getInstance(Context context) {
        if (instance == null)
            instance = new ConnectionDetector(context);
        return instance;
    }


    public ConnectionDetector(Context context) {
        this._context = context;
    }

    public boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                connected = true;
            } else {
                connected = false;
            }
        } else {
            connected = false;
        }

        return connected;
    }
}