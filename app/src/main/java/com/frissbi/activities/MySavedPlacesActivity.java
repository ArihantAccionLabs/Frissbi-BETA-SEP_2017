package com.frissbi.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.R;
import com.frissbi.SelectLocationListener;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.adapters.MyPlacesAdapter;
import com.frissbi.models.MyPlaces;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MySavedPlacesActivity extends AppCompatActivity implements SelectLocationListener {

    private static final int LOCATION_REQ_CODE = 100;
    private RecyclerView mMyPlacesRecyclerView;
    private List<MyPlaces> mMyPlacesList;
    private MyPlacesAdapter mMyPlacesAdapter;
    private SelectLocationListener mSelectLocationListener;
    private FloatingActionButton mAddLocationFloatingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_saved_places);
        mMyPlacesRecyclerView = (RecyclerView) findViewById(R.id.myPlaces_recyclerView);
        mAddLocationFloatingButton = (FloatingActionButton) findViewById(R.id.add_location_floating_button);
        mMyPlacesList = new ArrayList<>();
        mSelectLocationListener = (SelectLocationListener) this;
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mMyPlacesRecyclerView.setLayoutManager(layoutManager);

        getMyPlacesFromServer();
        mAddLocationFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MySavedPlacesActivity.this, SelectLocationAndSaveActivity.class);
                startActivityForResult(intent, LOCATION_REQ_CODE);
            }
        });
    }

    private void getMyPlacesFromServer() {
        SharedPreferences preferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        String userId = preferences.getString("USERID_FROM", "editor");
        Log.d("MySavedPlacesActivity", "UserId" + userId);
        String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.ORZIN_DESTLIST + userId;
        TSNetworkHandler.getInstance(this).getResponse(url, new JSONObject(),"GET", new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            JSONObject responseJsonObject = new JSONObject(response.response);
                            JSONArray savedLocJsonArray = responseJsonObject.getJSONArray("preferred_location_array");

                            for (int index = 0; index < savedLocJsonArray.length(); index++) {
                                JSONObject placeJsonObject = savedLocJsonArray.getJSONObject(index);
                                MyPlaces myPlaces = new MyPlaces();
                                myPlaces.setLatitude(placeJsonObject.getDouble("latitude"));
                                myPlaces.setLongitude(placeJsonObject.getDouble("longitude"));
                                myPlaces.setName(placeJsonObject.getString("locationName"));
                                myPlaces.setAddress(placeJsonObject.getString("address"));
                                myPlaces.setPlaceId(placeJsonObject.getInt("userPreferredLocationID"));
                                mMyPlacesList.add(myPlaces);
                            }

                            mMyPlacesAdapter = new MyPlacesAdapter(MySavedPlacesActivity.this, mMyPlacesList, mSelectLocationListener);
                            mMyPlacesRecyclerView.setAdapter(mMyPlacesAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(MySavedPlacesActivity.this, response.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MySavedPlacesActivity.this, "Something went wrong at server end", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void sendSelectLocation(MyPlaces myPlaces) {
        Intent intent = new Intent();
        Log.d("MySavedPlacesActivity", "myPlaces" + myPlaces);
        intent.putExtra("selected_place", myPlaces);
        intent.putExtra("selected_from", "savedLocation");
        setResult(RESULT_OK, intent);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCATION_REQ_CODE && resultCode == RESULT_OK) {
            String selectedFrom = data.getStringExtra("selected_from");
            MyPlaces myPlaces = (MyPlaces) data.getSerializableExtra("selected_place");
            Log.d("MySavedPlacesActivity", "myPlaces" + myPlaces);
            Intent intent = new Intent();
            intent.putExtra("selected_place", myPlaces);
            intent.putExtra("selected_from", selectedFrom);
            setResult(RESULT_OK, intent);
            finish();

        }
    }
}
