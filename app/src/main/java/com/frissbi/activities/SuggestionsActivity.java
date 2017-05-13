package com.frissbi.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Utility.SharedPreferenceHandler;
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
    private JSONObject locationSuggestionJson;
    private boolean isFromMeetingSummary;
    private RecyclerView mMeetingFriendsRecyclerView;
    private String title;
    private TextView suggestionMeetingDesTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);
        mLocationSuggestionList = new ArrayList<>();
        mLocationSuggestionRecyclerView = (RecyclerView) findViewById(R.id.location_suggestion_recyclerView);
        mMeetingFriendsRecyclerView = (RecyclerView) findViewById(R.id.meeting_friends_recyclerView);
        mLoadPlacesButton = (Button) findViewById(R.id.load_places_button);
        mSubmitPlaceButton = (Button) findViewById(R.id.submit_place_button);
        suggestionMeetingDesTv = (TextView) findViewById(R.id.suggestion_meeting_des_tv);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mLocationSuggestionRecyclerView.setLayoutManager(layoutManager);
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mMeetingFriendsRecyclerView.setLayoutManager(layoutManager1);
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("locationSuggestionJson")) {
            String jsonObject = bundle.getString("locationSuggestionJson");
            try {
                locationSuggestionJson = new JSONObject(jsonObject);
                setMeetingFriends(locationSuggestionJson.getJSONArray("friendsJsonArray"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            suggestionMeetingDesTv.setText(bundle.getString("meetingMessage"));
            setSuggestedLocations(locationSuggestionJson);
            if (bundle.containsKey("meetingId")) {
                mMeetingId = bundle.getLong("meetingId");
            }
        }


        if (bundle.containsKey("isCallFromSummary")) {
            if (bundle.getBoolean("isCallFromSummary")) {
                mMeetingId = bundle.getLong("summaryMeetingId");
                title = bundle.getString("title");
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

    private void setMeetingFriends(JSONArray friendsJsonArray) {
        List<FrissbiContact> frissbiContactList = new ArrayList<>();
        try {
            if (friendsJsonArray.length() > 0) {
                for (int i = 0; i < friendsJsonArray.length(); i++) {
                    JSONObject jsonObject = friendsJsonArray.getJSONObject(i);
                    FrissbiContact frissbiContact = new FrissbiContact();
                    frissbiContact.setUserId(jsonObject.getLong("userId"));
                    frissbiContact.setName(jsonObject.getString("fullName"));
                    if (jsonObject.has("profileImageId")) {
                        frissbiContact.setImageId(jsonObject.getString("profileImageId"));
                    }
                    frissbiContactList.add(frissbiContact);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        GroupParticipantAdapter groupParticipantAdapter = new GroupParticipantAdapter(this, frissbiContactList);
        mMeetingFriendsRecyclerView.setAdapter(groupParticipantAdapter);
    }

    private void submitSelectedPlace() {
        if (mLocationSuggestionAdapter.getSelectedLocation() != null) {
            LocationSuggestion locationSuggestion = mLocationSuggestionAdapter.getSelectedLocation();
            JSONObject jsonObject = new JSONObject();
            try {

                jsonObject.put("meetingId", mMeetingId);
                jsonObject.put("latitude", locationSuggestion.getLatitude());
                jsonObject.put("longitude", locationSuggestion.getLongitude());
                jsonObject.put("placeName", locationSuggestion.getName());
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
                                            onBackPressed();
                                        } else {
                                            Toast.makeText(SuggestionsActivity.this, response.message, Toast.LENGTH_SHORT).show();
                                            onBackPressed();
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                                Toast.makeText(SuggestionsActivity.this, response.message, Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(SuggestionsActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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
        String url = Utility.REST_URI + Utility.MORE_LOCATIONS + mMeetingId + "/" + size + "/" + SharedPreferenceHandler.getInstance(this).getUserId();
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
            if (isFromMeetingSummary) {
                setMeetingFriends(locationSuggestionJson.getJSONArray("friendsJsonArray"));
                suggestionMeetingDesTv.setText(title);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
