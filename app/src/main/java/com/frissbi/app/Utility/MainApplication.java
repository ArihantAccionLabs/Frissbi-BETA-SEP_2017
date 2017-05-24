package com.frissbi.app.Utility;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.multidex.MultiDex;

import com.frissbi.app.models.Friend;
import com.frissbi.app.models.FrissbiContact;
import com.frissbi.app.models.FrissbiGroup;
import com.frissbi.app.models.FrissbiReminder;
import com.frissbi.app.models.Meeting;
import com.frissbi.app.models.MeetingDate;
import com.frissbi.app.models.MeetingFriends;
import com.frissbi.app.models.Participant;
import com.frissbi.app.models.Profile;
import com.orm.SugarApp;

/**
 * Created by thrymr on 10/1/17.
 */

public class MainApplication extends SugarApp {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeSugarRecordClasses();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(Activity activity,
                                          Bundle savedInstanceState) {

                // new activity created; force its orientation to portrait
                activity.setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }

        });
    }

    private void initializeSugarRecordClasses() {
        new Profile();
        new Participant();
        new Meeting();
        new MeetingFriends();
        new MeetingDate();
        new FrissbiReminder();
        new FrissbiGroup();
        new FrissbiContact();
        new Friend();
    }
}
