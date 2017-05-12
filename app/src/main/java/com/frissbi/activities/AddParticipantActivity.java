package com.frissbi.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.frissbi.R;
import com.frissbi.Utility.Utility;
import com.frissbi.adapters.FriendsAdapter;
import com.frissbi.interfaces.GroupParticipantListener;
import com.frissbi.models.Friend;
import com.frissbi.models.FrissbiContact;

import java.util.List;

public class AddParticipantActivity extends AppCompatActivity implements GroupParticipantListener {

    private RecyclerView mAddParticipantRecyclerView;
    private List<Friend> mFriendList;
    private GroupParticipantListener mGroupParticipantListener;
    private AlertDialog mAlertDialog;
    private List<FrissbiContact> mFrissbiContactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_participant);
        mGroupParticipantListener = (GroupParticipantListener) this;
        mAddParticipantRecyclerView = (RecyclerView) findViewById(R.id.add_participant_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mAddParticipantRecyclerView.setLayoutManager(layoutManager);
        mFriendList = Friend.listAll(Friend.class);
        mFrissbiContactList = FrissbiContact.findWithQuery(FrissbiContact.class, "select * from frissbi_contact where type=?", Utility.FRIEND_TYPE + "");
        FriendsAdapter friendsAdapter = new FriendsAdapter(this, mFrissbiContactList, true, mGroupParticipantListener);
        mAddParticipantRecyclerView.setAdapter(friendsAdapter);
    }

    @Override
    public void selectedGroupParticipant(final FrissbiContact frissbiContact) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert!");
        builder.setMessage("Do you want to add " + frissbiContact.getName());

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAlertDialog.dismiss();
                Intent intent = new Intent();
                intent.putExtra("friendId", frissbiContact.getUserId());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                mAlertDialog.dismiss();

            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }
}
