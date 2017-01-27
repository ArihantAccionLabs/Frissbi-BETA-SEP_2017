package com.frissbi.Utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by thrymr on 24/1/17.
 */
public class Utility {
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_ACCEPT = 1;
    public static final int STATUS_REJECT = 2;
    public static final int STATUS_COMPLETED = 3;
    private static Utility ourInstance = new Utility();

    public static Utility getInstance() {
        return ourInstance;
    }

    private Utility() {
    }

    public String convertTime(String time) {
        String convertedTime = null;


        Date _24HourDt = null;
        try {
            SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm");
            SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a");
            _24HourDt = _24HourSDF.parse(time);
            convertedTime = _12HourSDF.format(_24HourDt);

        } catch (ParseException e) {
            e.printStackTrace();
        }


        return convertedTime;
    }

}
