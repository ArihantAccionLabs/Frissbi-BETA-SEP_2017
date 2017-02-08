package com.frissbi.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.frissbi.R;
import com.frissbi.Utility.FLog;
import com.frissbi.adapters.LocationSuggestionAdapter;
import com.frissbi.models.LocationSuggestion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SuggestionsActivity extends AppCompatActivity {

    private RecyclerView mLocationSuggestionRecyclerView;
    private List<LocationSuggestion> mLocationSuggestionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);
        mLocationSuggestionList = new ArrayList<>();
        mLocationSuggestionRecyclerView = (RecyclerView) findViewById(R.id.location_suggestion_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mLocationSuggestionRecyclerView.setLayoutManager(layoutManager);
        String s = getIntent().getExtras().getString("locationSuggestionJson");
        try {
            setSuggestedLocations(new JSONObject(s));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setSuggestedLocations(JSONObject locationSuggestionJson) {
        try {
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
            LocationSuggestionAdapter locationSuggestionAdapter = new LocationSuggestionAdapter(SuggestionsActivity.this, mLocationSuggestionList);
            mLocationSuggestionRecyclerView.setAdapter(locationSuggestionAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
