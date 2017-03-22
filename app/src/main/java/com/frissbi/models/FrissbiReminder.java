package com.frissbi.models;

import com.orm.SugarRecord;

/**
 * Created by thrymr on 21/3/17.
 */

public class FrissbiReminder extends SugarRecord {
    private Long reminderId;
    private String message;
    private String time;

    public Long getReminderId() {
        return reminderId;
    }

    public void setReminderId(Long reminderId) {
        this.reminderId = reminderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    @Override
    public String toString() {
        return "FrissbiReminder{" +
                "reminderId=" + reminderId +
                ", message='" + message + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
