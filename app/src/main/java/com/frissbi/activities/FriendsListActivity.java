package com.frissbi.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.frissbi.R;
import com.frissbi.fragments.MyContactsFragment;
import com.frissbi.fragments.MyFriendsFragment;
import com.frissbi.fragments.PeopleFragment;

import java.util.ArrayList;
import java.util.List;

public class FriendsListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private SharedPreferences mSharedPreferences;
    private String mUserId;
    private TabLayout mFriendsTabLayout;
    private ViewPager mFriendsViewPager;
    private MyFriendsFragment mMyFriendsFragment;
    private MyContactsFragment mMyContactsFragment;
    private PeopleFragment mPeopleFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        mSharedPreferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("USERID_FROM", "editor");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFriendsTabLayout = (TabLayout) findViewById(R.id.friends_tabLayout);
        mFriendsViewPager = (ViewPager) findViewById(R.id.friends_viewPager);
        mMyFriendsFragment = new MyFriendsFragment();
        mMyContactsFragment = new MyContactsFragment();
        mPeopleFragment = new PeopleFragment();
        setupViewPager(mFriendsViewPager);

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
        if (mPeopleFragment.isMenuVisible()) {
            mPeopleFragment.searchFriends(query);
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (mMyFriendsFragment.isMenuVisible()) {
            mMyFriendsFragment.filterFriends(newText);
        } else if (mMyContactsFragment.isMenuVisible()) {
            mMyContactsFragment.filterContacts(newText);
        }
        return false;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(mMyFriendsFragment, "Friends");
        viewPagerAdapter.addFragment(mMyContactsFragment, "Contacts");
        viewPagerAdapter.addFragment(mPeopleFragment, "People");
        viewPager.setAdapter(viewPagerAdapter);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

    }
}
