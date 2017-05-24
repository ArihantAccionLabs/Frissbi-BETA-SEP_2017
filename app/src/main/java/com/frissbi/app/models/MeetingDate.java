package com.frissbi.app.models;

import com.orm.SugarRecord;

/**
 * Created by thrymr on 16/2/17.
 */

public class MeetingDate extends SugarRecord {

    private String date;
    private Integer count;
    private String  month;

    public MeetingDate() {

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    @Override
    public String toString() {
        return "MeetingDate{" +
                "date='" + date + '\'' +
                ", count=" + count +
                ", month='" + month + '\'' +
                '}';
    }
}
