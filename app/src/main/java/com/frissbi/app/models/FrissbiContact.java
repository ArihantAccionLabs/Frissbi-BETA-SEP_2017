package com.frissbi.app.models;

import android.support.annotation.NonNull;

import com.frissbi.app.Utility.FLog;
import com.orm.SugarRecord;

/**
 * Created by thrymr on 8/3/17.
 */

public class FrissbiContact extends SugarRecord implements Comparable<FrissbiContact> {
    private Long userId;
    private String name;
    private String emailId;
    private String phoneNumber;
    private String imageId;
    private int type;
    private boolean isSelected;
    private String status;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "FrissbiContact{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", emailId='" + emailId + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", imageId='" + imageId + '\'' +
                ", type=" + type +
                ", isSelected=" + isSelected +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {

        if ((obj == null) || (obj.getClass() != this.getClass()) || !(obj instanceof FrissbiContact) || this.userId == null) {
            return false;
        }

        FrissbiContact frissbiContact = (FrissbiContact) obj;
        FLog.d(frissbiContact.userId + "  FrissbiContact", "this.userId" + this.userId);
        if (frissbiContact.getUserId() != null) {
            return frissbiContact.getUserId().equals(this.userId);
        } else {
            return false;
        }
    }


    @Override
    public int compareTo(@NonNull FrissbiContact frissbiContact) {
        return this.getName().compareTo(frissbiContact.getName());
    }
}

