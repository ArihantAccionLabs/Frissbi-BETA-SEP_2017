package com.frissbi.Utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by thrymr on 30/1/17.
 */
public class MeetingAlarmReceiver extends BroadcastReceiver {
    private Context mContext;
    private Long mMeetingId;
    private boolean mIsLocationSelected;
    private double mLatitude;
    private double mLongitude;
    private SharedPreferences mSharedPreferences;
    private String mUserId;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MeetingAlarmReceiver", "meetingId" + intent.getExtras().getLong("meetingId"));
        Log.d("MeetingAlarmReceiver", "isLocationSelected" + intent.getExtras().getBoolean("isLocationSelected"));
        mContext = context;
        mSharedPreferences = context.getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("USERID_FROM", "editor");
        mMeetingId = intent.getExtras().getLong("meetingId");
        mIsLocationSelected = intent.getExtras().getBoolean("isLocationSelected");
        if (!mIsLocationSelected) {
            Location location = TSLocationManager.getInstance(context).getCurrentLocation();
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
        }
        sendUserDetailsForMeetingSummaryToServer();
    }

    private void sendUserDetailsForMeetingSummaryToServer() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", mUserId);
            jsonObject.put("meetingId", mMeetingId);
            jsonObject.put("isLocationSelected", mIsLocationSelected);
            if (!mIsLocationSelected) {
                jsonObject.put("latitude", mLatitude);
                jsonObject.put("longitude", mLongitude);
            }
            String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.MEETING_SUMMARY_BY_LOCATION;
            TSNetworkHandler.getInstance(mContext).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {
                    if (response != null) {
                        Log.d("MeetingAlarmReceiver", "response" + response.response);
                        Toast.makeText(mContext, response.message, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
