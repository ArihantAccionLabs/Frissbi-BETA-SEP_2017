package com.frissbi.models;

import java.io.Serializable;

/**
 * Created by thrymr on 30/1/17.
 */

public class MeetingFriends implements Serializable {
    private String name;
    private String profileImage;
    private String type;
    private int status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MeetingFriends{" +
                "name='" + name + '\'' +
                ", profileImage='" + profileImage + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
