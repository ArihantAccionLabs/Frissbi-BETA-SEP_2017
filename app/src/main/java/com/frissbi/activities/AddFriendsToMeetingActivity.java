package com.frissbi.activities;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.frissbi.R;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.Utility.FLog;
import com.frissbi.fragments.ContactsFragment;
import com.frissbi.fragments.EmailFriendsFragment;
import com.frissbi.fragments.FrissbiFriendsFragment;
import com.frissbi.fragments.GroupsFragment;
import com.frissbi.interfaces.ContactsSelectedListener;
import com.frissbi.models.Contacts;
import com.frissbi.models.EmailContacts;
import com.frissbi.models.Friend;
import com.frissbi.models.FrissbiContact;

import java.util.ArrayList;
import java.util.List;

public class AddFriendsToMeetingActivity extends AppCompatActivity implements ContactsSelectedListener, SearchView.OnQueryTextListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1000;
    private static final int CONTACT_PICKER_RESULT = 999;
    private TabLayout mAddFriendsTabLayout;
    private ViewPager mFriendsViewPager;
    private List<EmailContacts> mEmailContactsList;
    private EmailFriendsFragment mEmailFriendsFragment;
    private List<Contacts> mContactsList;
    private ContactsFragment mContactsFragment;
    private FloatingActionButton mAddFriendsFloatingButton;
    private FrissbiFriendsFragment mFrissbiFriendsFragment;
    private SharedPreferences mSharedPreferences;
    private CustomProgressDialog mProgressDialog;
    private List<Friend> mFriendList;
    private List<Long> mFriendsSelectedIdList;
    private List<Long> mEmailsSelectedIdsList;
    private List<Long> mContactsSelectedIdsList;
    private GroupsFragment mGroupsFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends_to_meeting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAddFriendsTabLayout = (TabLayout) findViewById(R.id.add_friends_tabLayout);
        mFriendsViewPager = (ViewPager) findViewById(R.id.add_friends_viewPager);
        mAddFriendsFloatingButton = (FloatingActionButton) findViewById(R.id.add_friends_floating_button);
        mProgressDialog = new CustomProgressDialog(this);
        mEmailFriendsFragment = new EmailFriendsFragment();
        mContactsFragment = new ContactsFragment();
        mFrissbiFriendsFragment = new FrissbiFriendsFragment();
        mGroupsFragment = new GroupsFragment();
        mFriendList = new ArrayList<>();
        mEmailContactsList = new ArrayList<>();
        mContactsList = new ArrayList<>();
        mFriendsSelectedIdList = new ArrayList<>();
        mEmailsSelectedIdsList = new ArrayList<>();
        mContactsSelectedIdsList = new ArrayList<>();

        mSharedPreferences = getSharedPreferences("GMAIL_REG", Context.MODE_PRIVATE);
        Log.d("AddFriendsToMeeting", "email" + mSharedPreferences.getString("mail", "editor"));

        FLog.d("AddFriendsToMeeting", "FrissbiContactList" + FrissbiContact.listAll(FrissbiContact.class));

        setupViewPager(mFriendsViewPager);
        mAddFriendsTabLayout.setupWithViewPager(mFriendsViewPager);
        mAddFriendsFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
      /*  viewPagerAdapter.addFragment(mFrissbiFriendsFragment, "Friend");
        viewPagerAdapter.addFragment(mEmailFriendsFragment, "Emails");*/
        viewPagerAdapter.addFragment(mContactsFragment, "Contacts");
        viewPagerAdapter.addFragment(mGroupsFragment, "Groups");
        viewPager.setAdapter(viewPagerAdapter);
    }

    @Override
    public void setFriendsSelectedId(Long id) {
        mFriendsSelectedIdList.add(id);
    }

    @Override
    public void setEmailsSelectedId(Long id) {
        mEmailsSelectedIdsList.add(id);
    }

    @Override
    public void setContactsSelectedId(Long id) {

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

    }


    public Long getContactIdByEmail(String email) {
        //   Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI, Uri.encode(email));
        String name = "?";
        long contactId = 0;

        ContentResolver contentResolver = getContentResolver();
        //    Cursor contactLookup = contentResolver.query(uri, new String[]{ContactsContract.Data.CONTACT_ID, ContactsContract.Data.DISPLAY_NAME}, null, null, null);
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(email));
        Cursor contactLookup = contentResolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        Log.d("AddFriendsToMeeting", "getContactIdByEmail--contactLookup" + contactLookup.getCount());
        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {

                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                Log.d("AddFriendsToMeeting", "name" + name);
                contactId = contactLookup.getLong(contactLookup.getColumnIndex(ContactsContract.Data.CONTACT_ID));

            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        return contactId;
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

       /* if (mFrissbiFriendsFragment.isMenuVisible()) {

            mFrissbiFriendsFragment.filterFriends(newText);
        } else if (mEmailFriendsFragment.isMenuVisible()) {

            mEmailFriendsFragment.filterEmails(newText);
        } else */
        if (mContactsFragment.isMenuVisible()) {
            mContactsFragment.filterContacts(newText);
        } else if (mGroupsFragment.isMenuVisible()) {
            mGroupsFragment.filterGroups(newText);
        }
        return false;
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

}
