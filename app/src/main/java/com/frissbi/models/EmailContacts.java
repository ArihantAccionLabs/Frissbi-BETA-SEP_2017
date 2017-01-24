package com.frissbi.models;

import com.orm.SugarRecord;

/**
 * Created by thrymr on 17/1/17.
 */

public class EmailContacts extends SugarRecord {
    private String name;
    private String emailId;


    public EmailContacts() {

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

    @Override
    public String toString() {
        return "EmailContacts{" +
                "name='" + name + '\'' +
                ", emailId='" + emailId + '\'' +
                '}';
    }
}
