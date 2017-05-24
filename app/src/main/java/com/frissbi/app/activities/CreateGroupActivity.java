package com.frissbi.app.activities;

import android.app.SearchManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.frissbi.app.R;
import com.frissbi.app.Utility.FLog;
import com.frissbi.app.fragments.ConfirmGroupFragment;
import com.frissbi.app.fragments.NewGroupFragment;
import com.frissbi.app.interfaces.CurrentGroupFragmentListener;
import com.frissbi.app.models.FrissbiContact;

import java.util.List;

public class CreateGroupActivity extends AppCompatActivity implements NewGroupFragment.OnFragmentInteractionListener, ConfirmGroupFragment.OnFragmentInteractionListener, android.support.v7.widget.SearchView.OnQueryTextListener, CurrentGroupFragmentListener {
    public static final String NEW_GROUP = "newGroup";
    public static final String CONFIRM_GROUP = "confirmGroup";
    private ConfirmGroupFragment mConfirmGroupFragment;
    private NewGroupFragment mNewGroupFragment;
    private String mCurrentFragment;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        mNewGroupFragment = new NewGroupFragment();
        startFragment(mNewGroupFragment, "newGroup");
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                    onSupportNavigateUp();
                } else {
                    onBackPressed();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void startFragment(Fragment fragment, String tag) {
        mCurrentFragment = NEW_GROUP;
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (this.getSupportFragmentManager().findFragmentById(R.id.activity_create_group) != null) {
            fragmentTransaction.replace(R.id.activity_create_group, fragment, tag);
            fragmentTransaction.addToBackStack(getPackageName());

        } else
            fragmentTransaction.add(R.id.activity_create_group, fragment, tag);
        fragmentTransaction.commit();

    }

    private void startConformationFragment(Fragment fragment, String tag) {
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.activity_create_group, fragment, tag);
        fragmentTransaction.addToBackStack(getPackageName());
        fragmentTransaction.commit();

    }

    @Override
    public void onFragmentInteraction(List<FrissbiContact> groupSelectedFriendList) {
        mConfirmGroupFragment = new ConfirmGroupFragment();
        mConfirmGroupFragment.setSelectedFriendList(groupSelectedFriendList);
        startConformationFragment(mConfirmGroupFragment, "confirmGroup");
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        FLog.d("CreateGroupActivity", "MENU---mCurrentFragment" + mCurrentFragment);
        getMenuInflater().inflate(R.menu.menu_group, menu);
        MenuItem searchItem = menu.findItem(R.id.group_friends_search);
        final android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(searchItem);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        if (mCurrentFragment.equalsIgnoreCase(NEW_GROUP)) {
            searchItem.setVisible(true);
        } else {
            searchItem.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mNewGroupFragment.setFilterText(newText);
        return false;
    }

    @Override
    public void setCurrentFragment(String currentFragment) {
        mCurrentFragment = currentFragment;
        setActionBarTitle();
        invalidateOptionsMenu();
    }

    private void setActionBarTitle() {
        if (mCurrentFragment.equalsIgnoreCase(NEW_GROUP)) {
            mActionBar.setTitle("New Group");
        } else if (mCurrentFragment.equalsIgnoreCase(CONFIRM_GROUP)) {
            mActionBar.setTitle("Confirm Group");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            mCurrentFragment = NEW_GROUP;
            setActionBarTitle();
            invalidateOptionsMenu();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            onSupportNavigateUp();
        } else {
            super.onBackPressed();
        }
    }
}

