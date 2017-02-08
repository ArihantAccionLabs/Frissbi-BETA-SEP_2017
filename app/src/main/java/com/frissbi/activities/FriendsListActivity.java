package com.frissbi.activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.R;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.adapters.FriendsAdapter;
import com.frissbi.models.Friends;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class FriendsListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private ProgressDialog mProgressDialog;
    private SharedPreferences mSharedPreferences;
    private String mUserId;
    private List<Friends> mFriendsList;
    private RecyclerView mFriendsRecyclerView;
    private SwipeRefreshLayout mFriendsSwipeRefreshLayout;
    private FriendsAdapter mFriendsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        mProgressDialog = new CustomProgressDialog(FriendsListActivity.this);
        mSharedPreferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("USERID_FROM", "editor");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFriendsRecyclerView = (RecyclerView) findViewById(R.id.friends_recyclerView);
        mFriendsSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.friends_swipeRefreshLayout);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mFriendsRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mFriendsRecyclerView.addItemDecoration(dividerItemDecoration);
        mFriendsRecyclerView.setLayoutManager(layoutManager);

        mFriendsList = Friends.listAll(Friends.class);

        if (mFriendsList.size() == 0) {
            if (ConnectionDetector.getInstance(FriendsListActivity.this).isConnectedToInternet()) {
                getFriendsFromServer();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
            }
        } else {
            mFriendsAdapter = new FriendsAdapter(FriendsListActivity.this, mFriendsList);
            mFriendsRecyclerView.setAdapter(mFriendsAdapter);
        }
        mFriendsSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (ConnectionDetector.getInstance(FriendsListActivity.this).isConnectedToInternet()) {
                    getFriendsFromServer();
                } else {
                    Toast.makeText(FriendsListActivity.this, getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void getFriendsFromServer() {
        Friends.deleteAll(Friends.class);
        mFriendsList.clear();
        if (!mFriendsSwipeRefreshLayout.isRefreshing()) {
            mProgressDialog.show();
        }
        String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.USER_FRIENDSlIST + mUserId;
        TSNetworkHandler.getInstance(this).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            JSONObject responseJsonObject = new JSONObject(response.response);
                            JSONArray friendsListJsonArray = responseJsonObject.getJSONArray("friends_array");
                            for (int index = 0; index < friendsListJsonArray.length(); index++) {
                                JSONObject friendJsonObject = friendsListJsonArray.getJSONObject(index);
                                Friends friends = new Friends();
                                friends.setFriendId(friendJsonObject.getLong("UserId"));
                                friends.setUserName(friendJsonObject.getString("UserName"));
                                friends.save();
                                mFriendsList.add(friends);
                            }
                            mFriendsAdapter = new FriendsAdapter(FriendsListActivity.this, mFriendsList);
                            mFriendsRecyclerView.setAdapter(mFriendsAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(FriendsListActivity.this, response.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(FriendsListActivity.this, "Something went wrong at server end", Toast.LENGTH_SHORT).show();
                }
                mProgressDialog.dismiss();
                mFriendsSwipeRefreshLayout.setRefreshing(false);
            }
        });
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_friends, menu);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.friends_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        return true;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mFriendsAdapter.getFilter().filter(newText);
        return false;
    }
}
