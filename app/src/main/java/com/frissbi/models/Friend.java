package com.frissbi.models;

import android.support.annotation.NonNull;

import com.orm.SugarRecord;

import java.io.Serializable;

/**
 * Created by thrymr on 18/1/17.
 */

public class Friend extends SugarRecord implements Serializable, Comparable<Friend> {
    private Long userId;
    private String fullName;
    private String emailId;
    private String image;
    private String status;
    private String dob;
    private String gender;
    private boolean isSelected;

    public Friend() {

    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "userId=" + userId +
                ", fullName='" + fullName + '\'' +
                ", emailId='" + emailId + '\'' +
                ", image='" + image + '\'' +
                ", status='" + status + '\'' +
                ", dob='" + dob + '\'' +
                ", gender='" + gender + '\'' +
                ", isSelected=" + isSelected +
                '}';
    }

    @Override
    public int compareTo(@NonNull Friend friend) {
        return this.getFullName().compareTo(friend.getFullName());
    }
}
