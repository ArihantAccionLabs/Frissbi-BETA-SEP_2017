package com.frissbi.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import com.frissbi.R;
import com.frissbi.adapters.FriendsAdapter;
import com.frissbi.adapters.GroupParticipantAdapter;
import com.frissbi.fragments.ConfirmGroupFragment;
import com.frissbi.fragments.NewGroupFragment;
import com.frissbi.interfaces.GroupParticipantListener;
import com.frissbi.models.Friend;
import com.frissbi.models.FrissbiContact;

import java.util.List;

public class CreateGroupActivity extends AppCompatActivity implements NewGroupFragment.OnFragmentInteractionListener, ConfirmGroupFragment.OnFragmentInteractionListener {

    private ImageView mGroupIcon;
    private EditText mGroupNameEditText;
    private RelativeLayout mParticipantRLayout;
    private RecyclerView mParticipantRecyclerView;
    private RecyclerView mSelectParticipantRecyclerView;
    private List<Friend> mFriendList;
    private GroupParticipantListener mGroupParticipantListener;
    private SearchView mSearchFriends;
    private List<Friend> mGroupSelectedFriendList;
    private GroupParticipantAdapter mGroupParticipantAdapter;
    private FriendsAdapter mFriendsAdapter;
    private ConfirmGroupFragment mConfirmGroupFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        startFragment(new NewGroupFragment(), "newGroup");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    onBackPressed();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void startFragment(Fragment fragment, String tag) {
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
}
