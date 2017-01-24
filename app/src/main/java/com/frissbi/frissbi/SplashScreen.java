package com.frissbi.frissbi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

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
                } else {

                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    startActivity(intent);
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

}




