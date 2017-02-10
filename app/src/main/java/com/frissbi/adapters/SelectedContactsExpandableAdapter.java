package com.frissbi.adapters;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.models.Contacts;
import com.frissbi.models.EmailContacts;
import com.frissbi.models.Friend;

import java.util.List;

/**
 * Created by thrymr on 19/1/17.
 */

public class SelectedContactsExpandableAdapter implements ExpandableListAdapter {

    private Context mContext;
    private List<Friend> mFriendList;
    private List<EmailContacts> mEmailContactsList;
    private List<Contacts> mContactsList;

    public SelectedContactsExpandableAdapter(Context context, List<Friend> friendList, List<EmailContacts> emailContactsList, List<Contacts> contactsList) {
        mContext = context;
        mFriendList = friendList;
        mEmailContactsList = emailContactsList;
        mContactsList = contactsList;
        Log.d("ExpandableListAdapter", "mFriendList----" + mFriendList + "mEmailContactsList-----" + mEmailContactsList+"mContactsList-----"+mContactsList);
    }


    @Override
    public View getGroupView(int groupPosition, boolean b, View view, ViewGroup viewGroup) {
        if (groupPosition == 0) {
            if (mFriendList.size() != 0) {
                view = LayoutInflater.from(mContext).inflate(R.layout.expandable_group_item, viewGroup, false);
                TextView groupHeaderTv = (TextView) view.findViewById(R.id.group_header_tv);
                groupHeaderTv.setText("Friend");
            } else {
                view = LayoutInflater.from(mContext).inflate(R.layout.expandable_empty_item, viewGroup, false);
            }

            return view;
        } else if (groupPosition == 1) {
            if (mEmailContactsList.size() != 0) {
                view = LayoutInflater.from(mContext).inflate(R.layout.expandable_group_item, viewGroup, false);
                TextView groupHeaderTv = (TextView) view.findViewById(R.id.group_header_tv);
                groupHeaderTv.setText("Emails");
            } else {
                view = LayoutInflater.from(mContext).inflate(R.layout.expandable_empty_item, viewGroup, false);
            }
            return view;
        } else if (groupPosition == 2) {
            if (mContactsList.size() != 0) {
                view = LayoutInflater.from(mContext).inflate(R.layout.expandable_group_item, viewGroup, false);
                TextView groupHeaderTv = (TextView) view.findViewById(R.id.group_header_tv);
                groupHeaderTv.setText("Contacts");
            } else {
                view = LayoutInflater.from(mContext).inflate(R.layout.expandable_empty_item, viewGroup, false);
            }
            return view;
        }
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean b, View view, ViewGroup viewGroup) {

        if (groupPosition == 0) {
            if (mFriendList.size() != 0) {
                view = LayoutInflater.from(mContext).inflate(R.layout.friends_item, viewGroup, false);
                ImageView profileImageView = (ImageView) view.findViewById(R.id.profile_image);
                TextView usernameTv = (TextView) view.findViewById(R.id.username_tv);
                profileImageView.setImageResource(R.drawable.pic1);
                usernameTv.setText(mFriendList.get(childPosition).getFullName());

            } else {
                view = LayoutInflater.from(mContext).inflate(R.layout.expandable_empty_item, viewGroup, false);
            }
            return view;
        } else if (groupPosition == 1) {
            if (mEmailContactsList.size() != 0) {
                view = LayoutInflater.from(mContext).inflate(R.layout.friends_item, viewGroup, false);
                ImageView profileImageView = (ImageView) view.findViewById(R.id.profile_image);
                TextView usernameTv = (TextView) view.findViewById(R.id.username_tv);

                profileImageView.setImageResource(R.drawable.email_icon);
                usernameTv.setText(mEmailContactsList.get(childPosition).getEmailId());

            } else {
                view = LayoutInflater.from(mContext).inflate(R.layout.expandable_empty_item, viewGroup, false);
            }
            return view;
        } else if (groupPosition == 2) {
            if (mContactsList.size() != 0) {
                view = LayoutInflater.from(mContext).inflate(R.layout.friends_item, viewGroup, false);
                ImageView profileImageView = (ImageView) view.findViewById(R.id.profile_image);
                TextView usernameTv = (TextView) view.findViewById(R.id.username_tv);
                profileImageView.setImageResource(R.drawable.phone_icon);
                usernameTv.setText(mContactsList.get(childPosition).getName());

            } else {
                view = LayoutInflater.from(mContext).inflate(R.layout.expandable_empty_item, viewGroup, false);
            }
            return view;
        }


        return view;
    }


    @Override
    public int getGroupCount() {
        return 3;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (groupPosition == 0) {
            if (mFriendList.size() == 0) {
                return 1;
            } else {
                return mFriendList.size();
            }
        } else if (groupPosition == 1) {
            if (mEmailContactsList.size() == 0) {
                return 1;
            } else {
                return mEmailContactsList.size();
            }
        } else if (groupPosition == 2) {
            if (mContactsList.size() == 0) {
                return 1;
            } else {
                return mContactsList.size();
            }
        } else {
            return 1;
        }
    }

    @Override
    public Object getGroup(int i) {
        return null;
    }

    @Override
    public Object getChild(int i, int i1) {
        return null;
    }

    @Override
    public long getGroupId(int i) {
        return 0;
    }

    @Override
    public long getChildId(int i, int i1) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }


    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void onGroupExpanded(int i) {

    }

    @Override
    public void onGroupCollapsed(int i) {

    }

    @Override
    public long getCombinedChildId(long l, long l1) {
        return 0;
    }

    @Override
    public long getCombinedGroupId(long l) {
        return 0;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }
}
