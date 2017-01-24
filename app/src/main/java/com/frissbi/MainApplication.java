package com.frissbi;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.orm.SugarApp;
import com.orm.SugarRecord;

/**
 * Created by thrymr on 10/1/17.
 */

public class MainApplication extends SugarApp {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
