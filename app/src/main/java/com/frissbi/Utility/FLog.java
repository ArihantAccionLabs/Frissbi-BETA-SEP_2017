package com.frissbi.Utility;

import android.util.Log;

/**
 * Created by thrymr on 3/2/17.
 */

public class FLog {

    public static boolean SHOW_LOGS = true;

    public static void e(String tag, String message) {

        if (SHOW_LOGS)
            Log.e(tag, message);

    }

    public static void d(String tag, String message) {

        if (SHOW_LOGS)
            Log.d(tag, message);

    }

}
