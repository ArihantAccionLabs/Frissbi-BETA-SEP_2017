package com.frissbi.models;

import com.orm.SugarRecord;

/**
 * Created by thrymr on 18/1/17.
 */

public class Contacts extends SugarRecord {
    private String name;
    private String phoneNumber;

    public Contacts() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "Contacts{" +
                "name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
