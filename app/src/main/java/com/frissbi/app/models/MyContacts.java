package com.frissbi.app.models;

/**
 * Created by thrymr on 9/2/17.
 */

public class MyContacts implements Comparable<MyContacts> {

    private String name;
    private String number;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "MyContacts{" +
                "name='" + name + '\'' +
                ", number='" + number + '\'' +
                '}';
    }

    @Override
    public int compareTo(MyContacts myContacts) {
        return this.getName().compareTo(myContacts.getName());
    }
}
