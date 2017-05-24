package com.frissbi.app.Utility;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by thrymr on 22/2/17.
 */
public class SharedPreferenceHandler {
    private static SharedPreferenceHandler ourInstance;
    private SharedPreferences mSharedPreferences;
    private static final String PREF_NAME = "frissbiPref";
    private static final String IS_LOGGED_IN = "isLoggedIn";
    private static final String USER_ID = "userId";
    private Context mContext;


    public static SharedPreferenceHandler getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new SharedPreferenceHandler(context);
        }
        return ourInstance;
    }

    private SharedPreferenceHandler(Context context) {
        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void storeLoginDetails(Long userId) {
        mSharedPreferences.edit().putLong(USER_ID, userId).apply();
        mSharedPreferences.edit().putBoolean(IS_LOGGED_IN, true).apply();
    }

    public boolean isLoggedIn() {
        return mSharedPreferences.getBoolean(IS_LOGGED_IN, false);
    }

    public Long getUserId() {
        return mSharedPreferences.getLong(USER_ID, 0L);
    }


    public void clearUserDetails() {
        mSharedPreferences.edit().clear().apply();
    }
}
