package com.frissbi;

import com.frissbi.models.FrissbiContact;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thrymr on 19/1/17.
 */
public class SelectedContacts {
    private static SelectedContacts ourInstance;
    private List<Long> mFriendsSelectedIdList;
    private List<Long> mEmailsSelectedIdsList;
    private List<Long> mContactsSelectedIdsList;
    private List<FrissbiContact> mFrissbiContactList;
    private List<Long> mGroupSelectedIdsList;

    public static SelectedContacts getInstance() {
        if (ourInstance == null)
            return ourInstance = new SelectedContacts();
        return ourInstance;
    }

    private SelectedContacts() {
        mContactsSelectedIdsList = new ArrayList<>();
        mEmailsSelectedIdsList = new ArrayList<>();
        mFriendsSelectedIdList = new ArrayList<>();
        mGroupSelectedIdsList = new ArrayList<>();
        mFrissbiContactList = new ArrayList<>();
    }

    public void clearContacts() {
        mContactsSelectedIdsList.clear();
        mEmailsSelectedIdsList.clear();
        mFriendsSelectedIdList.clear();
        mGroupSelectedIdsList.clear();
        mFrissbiContactList.clear();
    }


    public void setFriendsSelectedId(Long id) {
        mFriendsSelectedIdList.add(id);
    }


    public void setEmailsSelectedId(Long id) {
        mEmailsSelectedIdsList.add(id);
    }


    public void setContactsSelectedId(Long id) {
        mContactsSelectedIdsList.add(id);
    }

    public void deleteFriendsSelectedId(Long id) {
        mFriendsSelectedIdList.remove(id);
    }

    public void deleteEmailsSelectedId(Long id) {
        mEmailsSelectedIdsList.remove(id);
    }

    public void deleteContactsSelectedId(Long id) {
        mContactsSelectedIdsList.remove(id);
    }

    public List<Long> getGroupSelectedIdsList() {
        return mGroupSelectedIdsList;
    }

    public void setGroupSelectedId(Long id) {
        mGroupSelectedIdsList.add(id);
    }

    public void deleteGroupSelectedId(Long id) {
        mGroupSelectedIdsList.remove(id);
    }

    public List<Long> getFriendsSelectedIdList() {
        return mFriendsSelectedIdList;
    }

    public List<Long> getEmailsSelectedIdsList() {
        return mEmailsSelectedIdsList;
    }

    public List<Long> getContactsSelectedIdsList() {
        return mContactsSelectedIdsList;
    }

    public List<FrissbiContact> getFrissbiContactList() {
        return mFrissbiContactList;
    }

    public void setFrissbiContact(FrissbiContact frissbiContact) {
        mFrissbiContactList.add(frissbiContact);
    }

    public void deleteFrissbiContact(FrissbiContact frissbiContact) {
        mFrissbiContactList.remove(frissbiContact);
    }
}
