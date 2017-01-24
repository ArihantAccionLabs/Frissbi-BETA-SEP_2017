package com.frissbi.models;

import com.orm.SugarRecord;

/**
 * Created by thrymr on 18/1/17.
 */

public class Friends extends SugarRecord {
    private Long friendId;
    private String userName;

    public Friends() {

    }

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    @Override
    public String toString() {
        return "Friends{" +
                "friendId=" + friendId +
                ", userName='" + userName + '\'' +
                '}';
    }
}
