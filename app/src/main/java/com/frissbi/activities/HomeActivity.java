package com.frissbi.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.Frissbi_Friends.FriendSerching;
import com.frissbi.Frissbi_profilePic.Profile_Pic;
import com.frissbi.R;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.TSLocationManager;
import com.frissbi.Utility.Utility;
import com.frissbi.fragments.FriendRequestFragment;
import com.frissbi.fragments.MeetingAlertFragment;
import com.frissbi.fragments.MeetingLogFragment;
import com.frissbi.fragments.TimeLineFragment;
import com.frissbi.models.Contacts;
import com.frissbi.models.EmailContacts;
import com.frissbi.models.Friend;
import com.frissbi.models.FrissbiContact;
import com.frissbi.networkhandler.TSNetworkHandler;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1000;
    private static final int REQUEST_CHECK_SETTINGS = 2000;
    private static final String TAG = "HomeActivity";
    private TextView mUserNameTextView;
    private android.support.design.widget.FloatingActionButton fab;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private Button mAddMeetingButton;
    private Long mUserId;
    private String mUserName;
    private ProgressDialog mProgressDialog;
    private LinearLayout mDimBackgroundLayout;
    private FloatingActionMenu mFloatingActionMenu;
    private Animation rotate_forward;
    private Animation rotate_backward;
    private EmailIdsAsync mEmailIdsAsync;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        mProgressDialog = new CustomProgressDialog(this);
        mEmailIdsAsync = new EmailIdsAsync();

        mUserId = SharedPreferenceHandler.getInstance(this).getUserId();
        mUserName = SharedPreferenceHandler.getInstance(this).getUserName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                if (EmailContacts.listAll(EmailContacts.class).size() == 0) {
                    //   getNameEmailDetails(mEmailSharedPreferences.getString("mail", "editor"));
                    mEmailIdsAsync.execute();
                }
                if (Contacts.listAll(Contacts.class).size() == 0) {
                    // readContacts();
                }
            }


            if (ContextCompat.checkSelfPermission(HomeActivity.this
                    , android.Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this,
                        android.Manifest.permission.READ_CONTACTS)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(HomeActivity.this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        } else {

          /*  if (EmailContacts.listAll(EmailContacts.class).size() == 0) {
                //getNameEmailDetails(mEmailSharedPreferences.getString("mail", "editor"));
                mEmailIdsAsync.execute();
            }*/

            if (FrissbiContact.listAll(FrissbiContact.class).size() == 0) {
                saveAllContactsInLocal();
            }
            //  getContactIdByEmail(mSharedPreferences.getString("mail", "editor"));

        }

        mTabLayout = (TabLayout) toolbar.findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager(mViewPager);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(0).setIcon(R.drawable.clock);
        mTabLayout.getTabAt(1).setIcon(R.drawable.calendar);
        mTabLayout.getTabAt(2).setIcon(R.drawable.group);
        mTabLayout.getTabAt(3).setIcon(R.drawable.notification);
        mDimBackgroundLayout = (LinearLayout) findViewById(R.id.dim_background);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View navHeaderView = navigationView.getHeaderView(0);

        mUserNameTextView = (TextView) navHeaderView.findViewById(R.id.user_name_tv);
        mUserNameTextView.setText(mUserName.toUpperCase());
        mAddMeetingButton = (Button) findViewById(R.id.add_meeting);

        fab = (android.support.design.widget.FloatingActionButton) findViewById(R.id.add_floating_button);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        // Create an icon
        ImageView icon = new ImageView(this);
        icon.setImageResource(R.mipmap.icon);


     /*   FloatingActionButton actionButton = new FloatingActionButton.Builder(this)
                .setContentView(icon)
                .build();*/


        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        // repeat many times:
        ImageView itemIcon1 = new ImageView(this);

        itemIcon1.setImageResource(R.drawable.icon_location);

        ImageView itemIcon2 = new ImageView(this);
        itemIcon2.setImageResource(R.drawable.icon_alert);

        ImageView itemIcon3 = new ImageView(this);
        itemIcon3.setImageResource(R.drawable.icon_calendar);

        ImageView itemIcon4 = new ImageView(this);
        itemIcon4.setImageResource(R.drawable.icon_friends);

        ImageView itemIcon5 = new ImageView(this);
        itemIcon5.setImageResource(R.drawable.icon_chat);

        SubActionButton locationButton = itemBuilder.setContentView(itemIcon1).build();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);
        locationButton.setLayoutParams(layoutParams);
        SubActionButton addAlertIcon = itemBuilder.setContentView(itemIcon2).build();
        addAlertIcon.setLayoutParams(layoutParams);
        SubActionButton addMeetingIcon = itemBuilder.setContentView(itemIcon3).build();
        addMeetingIcon.setLayoutParams(layoutParams);
        SubActionButton button4 = itemBuilder.setContentView(itemIcon4).build();
        button4.setLayoutParams(layoutParams);
        SubActionButton button5 = itemBuilder.setContentView(itemIcon5).build();
        button5.setLayoutParams(layoutParams);


        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        addMeetingIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDimBackgroundLayout.setVisibility(View.GONE);
                mFloatingActionMenu.close(true);
                Intent intent = new Intent(HomeActivity.this, MeetingActivity.class);
                startActivity(intent);
            }
        });

        //attach the sub buttons to the main button
        mFloatingActionMenu = new FloatingActionMenu.Builder(this).setStartAngle(180).setEndAngle(360)
                .addSubActionView(locationButton)
                .addSubActionView(addAlertIcon)
                .addSubActionView(addMeetingIcon)
                .addSubActionView(button4)
                .addSubActionView(button5)
                .attachTo(fab)
                .build();


        mFloatingActionMenu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu floatingActionMenu) {
                mDimBackgroundLayout.setVisibility(View.VISIBLE);
                // fab.setImageResource(R.drawable.ic_action_cancel);
                rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
                fab.setAnimation(rotate_forward);

            }

            @Override
            public void onMenuClosed(FloatingActionMenu floatingActionMenu) {
                mDimBackgroundLayout.setVisibility(View.GONE);
                //fab.setImageResource(R.drawable.ic_action_add);
                rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);
                fab.setAnimation(rotate_backward);
            }
        });


        mDimBackgroundLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDimBackgroundLayout.setVisibility(View.GONE);
                mFloatingActionMenu.close(true);
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        navigationView.setNavigationItemSelectedListener(this);


        //mViewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));


        mAddMeetingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, MeetingActivity.class);
                startActivity(intent);
            }
        });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });


    }

    private void saveAllContactsInLocal() {
        if (ConnectionDetector.getInstance(this).isConnectedToInternet()) {
            getFriendsFromServer();
        } else {
            Toast.makeText(getApplicationContext(), "You don't have an internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void getAllContacts() {

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Toast.makeText(HomeActivity.this, "Name: " + name
                                + ", Phone No: " + phoneNo, Toast.LENGTH_SHORT).show();
                    }
                    pCur.close();
                }
            }
        }


        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            Toast.makeText(getApplicationContext(), name + phoneNumber, Toast.LENGTH_LONG).show();

        }
        phones.close();


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (mFloatingActionMenu.isOpen()) {
                mFloatingActionMenu.close(true);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(getApplication(), FriendSerching.class);
            startActivity(intent);
        } else if (id == R.id.nav_meeting) {


            // check for Internet status
            if (ConnectionDetector.getInstance(this).isConnectedToInternet()) {
                Intent intent = new Intent(getApplication(), MeetingCalendarActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "You don't have an internet connection", Toast.LENGTH_SHORT).show();

            }


        } else if (id == R.id.nav_friends) {

            Intent intent = new Intent(getApplication(), FriendsListActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_groups) {

            Intent intent = new Intent(getApplication(), GroupsActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_profile) {

            Intent intent = new Intent(getApplication(), ProfileActivity.class);
            startActivity(intent);


        } else if (id == R.id.nav_setting) {
            Intent intent = new Intent(getApplication(), Profile_Pic.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            SharedPreferenceHandler.getInstance(this).clearUserDetails();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        // Retrieve the SearchView and plug it into SearchManager
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new TimeLineFragment());
        viewPagerAdapter.addFragment(new MeetingLogFragment());
        viewPagerAdapter.addFragment(new FriendRequestFragment());
        viewPagerAdapter.addFragment(new MeetingAlertFragment());
        viewPager.setAdapter(viewPagerAdapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();

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


        public void addFragment(Fragment fragment) {
            mFragmentList.add(fragment);


        }

    }


    private void getFriendsFromServer() {
        mProgressDialog.show();
        String url = Utility.REST_URI + Utility.USER_FRIENDSLIST + mUserId;
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
                              /*  Friend friend = new Friend();
                                friend.setUserId(friendJsonObject.getLong("userId"));
                                friend.setFullName(friendJsonObject.getString("fullName"));
                                friend.setEmailId(friendJsonObject.getString("email"));
                                friend.save();*/

                                FrissbiContact frissbiContact = new FrissbiContact();
                                frissbiContact.setUserId(friendJsonObject.getLong("userId"));
                                frissbiContact.setName(friendJsonObject.getString("fullName"));
                                frissbiContact.setEmailId(friendJsonObject.getString("email"));
                                if (friendJsonObject.has("profileImage")) {
                                    frissbiContact.setImage(friendJsonObject.getString("profileImage"));
                                }
                                if (friendJsonObject.has("phoneNumber")) {
                                    frissbiContact.setPhoneNumber(friendJsonObject.getString("phoneNumber"));
                                }
                                frissbiContact.setType(1);
                                frissbiContact.save();
                            }
                            FLog.d("HomeActivity", "FrissbiContactList++++++++1------" + FrissbiContact.listAll(FrissbiContact.class));
                            mEmailIdsAsync.execute();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(HomeActivity.this, response.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Something went wrong at server end", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    class EmailIdsAsync extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            EmailContacts.deleteAll(EmailContacts.class);
            ArrayList<String> emlRecs = new ArrayList<String>();
            HashSet<String> emlRecsHS = new HashSet<String>();
            Context context = HomeActivity.this;
            ContentResolver cr = context.getContentResolver();


            String[] PROJECTION = new String[]{ContactsContract.RawContacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.PHOTO_ID,
                    ContactsContract.CommonDataKinds.Email.DATA,
                    ContactsContract.CommonDataKinds.Photo.CONTACT_ID};
            String order = "CASE WHEN "
                    + ContactsContract.Contacts.DISPLAY_NAME
                    + " NOT LIKE '%@%' THEN 1 ELSE 2 END, "
                    + ContactsContract.Contacts.DISPLAY_NAME
                    + ", "
                    + ContactsContract.CommonDataKinds.Email.DATA
                    + " COLLATE NOCASE";
            String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";
       /* Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_LOOKUP_URI, Uri.encode(emailName));
        Cursor cur = getContentResolver().query(uri,
                new String[]{ContactsContract.CommonDataKinds.Email.CONTACT_ID, ContactsContract.CommonDataKinds.Email.DISPLAY_NAME, ContactsContract.CommonDataKinds.Email.DATA},
                null, null, null);*/
            Cursor cur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, filter, null, order);
            Log.d("AddFriendsToMeeting", "cur" + cur.getCount());
            if (cur.moveToFirst()) {
                do {
                    // names comes in hand sometimes
                    String name = cur.getString(1);
                    String emailId = cur.getString(3);
                    /*EmailContacts emailContacts = new EmailContacts();
                    emailContacts.setName(name);
                    emailContacts.setEmailId(emailId);
                    emailContacts.save();*/


                    if (FrissbiContact.findWithQuery(FrissbiContact.class, "select * from frissbi_contact where email_id=?", emailId).size() == 0) {
                        FrissbiContact frissbiContact = new FrissbiContact();
                        frissbiContact.setName(name);
                        frissbiContact.setEmailId(emailId);
                        frissbiContact.setType(2);
                        frissbiContact.save();
                    }


                    // keep unique only
                    if (emlRecsHS.add(emailId.toLowerCase())) {
                        emlRecs.add(emailId);
                    }
                } while (cur.moveToNext());

            }

            cur.close();
            return "emailIds saved successfully";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            FLog.d("HomeActivity", "FrissbiContactList-----2========" + FrissbiContact.listAll(FrissbiContact.class));
            ContactsAsync contactsAsync = new ContactsAsync();
            contactsAsync.execute();
        }
    }


    /*private void getNameEmailDetails(String emailName) {

        mProgressDialog.show();
        EmailContacts.deleteAll(EmailContacts.class);
        ArrayList<String> emlRecs = new ArrayList<String>();
        HashSet<String> emlRecsHS = new HashSet<String>();
        Context context = HomeActivity.this;
        ContentResolver cr = context.getContentResolver();


        String[] PROJECTION = new String[]{ContactsContract.RawContacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_ID,
                ContactsContract.CommonDataKinds.Email.DATA,
                ContactsContract.CommonDataKinds.Photo.CONTACT_ID};
        String order = "CASE WHEN "
                + ContactsContract.Contacts.DISPLAY_NAME
                + " NOT LIKE '%@%' THEN 1 ELSE 2 END, "
                + ContactsContract.Contacts.DISPLAY_NAME
                + ", "
                + ContactsContract.CommonDataKinds.Email.DATA
                + " COLLATE NOCASE";
        String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";
       *//* Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_LOOKUP_URI, Uri.encode(emailName));
        Cursor cur = getContentResolver().query(uri,
                new String[]{ContactsContract.CommonDataKinds.Email.CONTACT_ID, ContactsContract.CommonDataKinds.Email.DISPLAY_NAME, ContactsContract.CommonDataKinds.Email.DATA},
                null, null, null);*//*
        Cursor cur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, filter, null, order);
        Log.d("AddFriendsToMeeting", "cur" + cur.getCount());
        if (cur.moveToFirst()) {
            do {
                // names comes in hand sometimes
                String name = cur.getString(1);
                String emailId = cur.getString(3);
                EmailContacts emailContacts = new EmailContacts();
                emailContacts.setName(name);
                emailContacts.setEmailId(emailId);
                emailContacts.save();


                // keep unique only
                if (emlRecsHS.add(emailId.toLowerCase())) {
                    emlRecs.add(emailId);
                }
            } while (cur.moveToNext());

        }

        cur.close();
        mProgressDialog.dismiss();
    }*/

    class ContactsAsync extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null);

            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                        String phone = "0";
                        // get the phone number
                        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            phone = pCur.getString(
                                    pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        }
                       /* Contacts contacts = new Contacts();
                        contacts.setName(name);
                        contacts.setPhoneNumber(phone);
                        contacts.save();*/

                        if (FrissbiContact.findWithQuery(FrissbiContact.class, "select * from frissbi_contact where phone_number=?", phone).size() == 0) {
                            FrissbiContact frissbiContact = new FrissbiContact();
                            frissbiContact.setName(name);
                            frissbiContact.setPhoneNumber(phone);
                            frissbiContact.setType(3);
                            frissbiContact.save();
                        }
                        pCur.close();
                    }
                }
            }
            return "Contacts saved successfully";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mProgressDialog.dismiss();
            FLog.d("HomeActivity", "FrissbiContactList----3" + FrissbiContact.listAll(FrissbiContact.class));
        }
    }

   /* public void readContacts() {
        mProgressDialog.show();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Contacts contacts = new Contacts();
                    contacts.setName(name);

                    String phone = "0";
                    // get the phone number
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        phone = pCur.getString(
                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        System.out.println();
                        System.out.println("name : " + name + "   phone" + phone);
                    }
                    contacts.setPhoneNumber(phone);
                    contacts.save();
                    pCur.close();
                }
            }
        }
        mProgressDialog.dismiss();
    }*/


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if (EmailContacts.listAll(EmailContacts.class).size() == 0) {
                        // getNameEmailDetails(mEmailSharedPreferences.getString("mail", "editor"));
                        mEmailIdsAsync.execute();
                    }
                    if (Contacts.listAll(Contacts.class).size() == 0) {
                        // readContacts();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!TSLocationManager.getInstance(this).isLocationOn()) {

            displayLocationSettingsRequest(HomeActivity.this);
          /*  AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Alert!");
            builder.setMessage("Your device location is off. Turn on to continue." );
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    locationAlertDialog.dismiss();
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onBackPressed();
                    locationAlertDialog.dismiss();
                }
            });
            locationAlertDialog = builder.create();
            locationAlertDialog.show();*/
        }
    }


    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(HomeActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK: {

                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        finish();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }

    }
}
