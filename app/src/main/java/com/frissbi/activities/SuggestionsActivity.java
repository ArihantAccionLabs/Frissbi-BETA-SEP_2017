package com.frissbi.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Utility.Utility;
import com.frissbi.adapters.GroupParticipantAdapter;
import com.frissbi.adapters.LocationSuggestionAdapter;
import com.frissbi.models.FrissbiContact;
import com.frissbi.models.LocationSuggestion;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SuggestionsActivity extends AppCompatActivity {

    private RecyclerView mLocationSuggestionRecyclerView;
    private List<LocationSuggestion> mLocationSuggestionList;
    private Button mLoadPlacesButton;
    private long mMeetingId;
    private Button mSubmitPlaceButton;
    private LocationSuggestionAdapter mLocationSuggestionAdapter;
    private String locationSuggestionJson;
    private boolean isFromMeetingSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);
        mLocationSuggestionList = new ArrayList<>();
        mLocationSuggestionRecyclerView = (RecyclerView) findViewById(R.id.location_suggestion_recyclerView);
        mLoadPlacesButton = (Button) findViewById(R.id.load_places_button);
        mSubmitPlaceButton = (Button) findViewById(R.id.submit_place_button);
        TextView suggestionMeetingDesTv = (TextView) findViewById(R.id.suggestion_meeting_des_tv);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mLocationSuggestionRecyclerView.setLayoutManager(layoutManager);
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("locationSuggestionJson")) {
            locationSuggestionJson = bundle.getString("locationSuggestionJson");
            setMeetingFriends(bundle.getString("meetingFriendsArray"));
            suggestionMeetingDesTv.setText(bundle.getString("meetingMessage"));
            try {
                setSuggestedLocations(new JSONObject(locationSuggestionJson));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (bundle.containsKey("meetingId")) {
                mMeetingId = Long.parseLong(getIntent().getExtras().getString("meetingId"));
                Log.d("SuggestionsActivity", "meetingId" + mMeetingId);
            }
        }


        if (bundle.containsKey("isCallFromSummary")) {
            if (bundle.getBoolean("isCallFromSummary")) {
                mMeetingId = getIntent().getExtras().getLong("summaryMeetingId");
                isFromMeetingSummary = true;
                getMorePlacesFromServer(0);
            }

        }

       /* if (bundle.containsKey("summaryMeetingId")) {

            Log.d("SuggestionsActivity", "meetingId" + mMeetingId);
        }*/

        mLoadPlacesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMorePlacesFromServer(mLocationSuggestionList.size());
            }
        });

        mSubmitPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitSelectedPlace();
            }
        });

    }

    private void setMeetingFriends(String meetingFriendsArray) {
        List<FrissbiContact> frissbiContactList = new ArrayList<>();
        try {
            JSONArray meetingFriendsJsonArray = new JSONArray(meetingFriendsArray);
            for (int i = 0; i < meetingFriendsArray.length(); i++) {
                JSONObject jsonObject = meetingFriendsJsonArray.getJSONObject(i);
                FrissbiContact frissbiContact = new FrissbiContact();
                frissbiContact.setUserId(jsonObject.getLong("userId"));
                frissbiContact.setName(jsonObject.getString("fullName"));
                frissbiContact.setImageId(jsonObject.getString("profileImageId"));
                frissbiContactList.add(frissbiContact);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        GroupParticipantAdapter groupParticipantAdapter = new GroupParticipantAdapter(this, frissbiContactList);
        mLocationSuggestionRecyclerView.setAdapter(groupParticipantAdapter);
    }

    private void submitSelectedPlace() {
        if (mLocationSuggestionAdapter.getSelectedLocation() != null) {
            LocationSuggestion locationSuggestion = mLocationSuggestionAdapter.getSelectedLocation();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("meetingId", mMeetingId);
                jsonObject.put("latitude", locationSuggestion.getLatitude());
                jsonObject.put("longitude", locationSuggestion.getLongitude());
                jsonObject.put("address", locationSuggestion.getAddress());
                jsonObject.put("isFromMeetingSummary", isFromMeetingSummary);

                TSNetworkHandler.getInstance(this).getResponse(Utility.REST_URI + Utility.SUBMIT_MEETING_LOCATION, jsonObject, new TSNetworkHandler.ResponseHandler() {
                    @Override
                    public void handleResponse(TSNetworkHandler.TSResponse response) {
                        if (response != null) {
                            if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                                try {
                                    JSONObject responseJsonObject = new JSONObject(response.response);
                                    if (responseJsonObject.has("isLocationUpdate")) {
                                        if (responseJsonObject.getBoolean("isLocationUpdate")) {
                                            Toast.makeText(SuggestionsActivity.this, response.message, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                                Toast.makeText(SuggestionsActivity.this, response.message, Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(SuggestionsActivity.this, "Something went wrong at server side", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Please select place before submit", Toast.LENGTH_SHORT).show();
        }
    }

    private void getMorePlacesFromServer(int size) {
        String url = Utility.REST_URI  + Utility.MORE_LOCATIONS + mMeetingId + "/" + size;
        TSNetworkHandler.getInstance(this).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            JSONObject responseJsonObject = new JSONObject(response.response);
                            setSuggestedLocations(responseJsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(SuggestionsActivity.this, response.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SuggestionsActivity.this, "Something went wrong at server side", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setSuggestedLocations(JSONObject locationSuggestionJson) {
        try {
            if (locationSuggestionJson.getBoolean("isNextLocationExist")) {
                mLoadPlacesButton.setVisibility(View.VISIBLE);
            } else {
                mLoadPlacesButton.setVisibility(View.GONE);
            }
            JSONArray locationJsonArray = locationSuggestionJson.getJSONArray("frissbiLocationArray");
            for (int i = 0; i < locationJsonArray.length(); i++) {
                LocationSuggestion locationSuggestion = new LocationSuggestion();
                JSONObject locationJsonObject = locationJsonArray.getJSONObject(i);
                locationSuggestion.setLocationId(locationJsonObject.getLong("frissbiLocationID"));
                locationSuggestion.setLatitude(locationJsonObject.getDouble("latitude"));
                locationSuggestion.setLongitude(locationJsonObject.getDouble("longitude"));
                locationSuggestion.setAddress(locationJsonObject.getString("address"));
                locationSuggestion.setName(locationJsonObject.getString("placeName"));
                locationSuggestion.setImageUrl(locationJsonObject.getString("icon"));
                locationSuggestion.setRating(locationJsonObject.getString("rating"));
                mLocationSuggestionList.add(locationSuggestion);
            }
            mLocationSuggestionAdapter = new LocationSuggestionAdapter(SuggestionsActivity.this, mLocationSuggestionList);
            mLocationSuggestionRecyclerView.setAdapter(mLocationSuggestionAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
