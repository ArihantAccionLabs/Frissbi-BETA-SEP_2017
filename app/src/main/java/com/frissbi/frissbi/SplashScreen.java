package com.frissbi.frissbi;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.activities.HomeActivity;
import com.frissbi.R;

public class SplashScreen extends Activity implements AnimationListener {
    private static int SPLASH_TIME_OUT = 1000;
    AnimationDrawable animation;
    Animation a, animMove;
    ImageView img;
    // TODO Auto-generated method stub
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String UserName = "";
    String UserId = "";
    String UserEmail;
    private AlertDialog networkAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        //animMove = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.top);
        //animMove.setAnimationListener(this);
        //	img=(ImageView)findViewById(R.id.img);
        //img.startAnimation(animMove);
        preferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    private void redirectToHomeOrLoginScreen() {
        new Handler().postDelayed(new Runnable() {

			/*
             * Showing splash screen with a1 timer. This will be useful when you
			 * want to show case your app logo / company
			 */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                //SharedOreferences using

                UserId = preferences.getString("USERID_FROM", "editor");
                Log.d("value is", UserId);
                UserEmail = preferences.getString("EMAIL", "editor");
                UserName = preferences.getString("USERNAME_FROM", "editor");
                Log.d("value is", UserName);
                editor.commit();
                //String password = myPrefs.getString("PASSWORD",null);
                if (!(UserId.equals("editor"))) {

                    //username and password are present, do your stuff
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {

                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    startActivity(intent);
                    finish();
                    // Check for login status Using SharedPreference
                }

            }
        }, SPLASH_TIME_OUT);

    }

    @Override
    public void onAnimationStart(Animation animation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // TODO Auto-generated method stub

    }
    // Method to check Login Status


    @Override
    protected void onResume() {
        super.onResume();
        if (ConnectionDetector.getInstance(SplashScreen.this).isConnectedToInternet()) {
            redirectToHomeOrLoginScreen();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Alert!");
            builder.setMessage("You don't have an internet connection. Do you want unable mobile data or wifi");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    networkAlertDialog.dismiss();
                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onBackPressed();
                    networkAlertDialog.dismiss();
                }
            });
            networkAlertDialog = builder.create();
            networkAlertDialog.show();
        }
    }
}




