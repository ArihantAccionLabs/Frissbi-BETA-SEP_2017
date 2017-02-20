package com.frissbi.Gmail;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.frissbi.R;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * @author manish
 */
public class GmailSyncActivity extends Activity  {
    Context mContext = GmailSyncActivity.this;
    AccountManager mAccountManager;
    String token;
    int serverCode;
    Dialog dialogp;
    private GoogleApiClient mGoogleApiClient;
    private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Splash screen view
        setContentView(R.layout.activity_splash);
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

        }

        syncGoogleAccount();


        dialogp = new Dialog(GmailSyncActivity.this);
        // Include dialog.xml file
        dialogp.getWindow();

        dialogp.setCancelable(false);
        dialogp.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogp.setContentView(R.layout.dailog_box);


        ImageView imgView = (ImageView) dialogp.findViewById(R.id.animationImage);
        imgView.setVisibility(ImageView.VISIBLE);
        imgView.setBackgroundResource(R.drawable.frame_animation);

        AnimationDrawable frameAnimation = (AnimationDrawable) imgView.getBackground();

        if (frameAnimation.isRunning()) {
            frameAnimation.stop();
        } else {
            frameAnimation.stop();
            frameAnimation.start();
        }
        dialogp.show();

    }

    private String[] getAccountNames() {
        mAccountManager = AccountManager.get(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        Account[] accounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        String[] names = new String[accounts.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = accounts[i].name;
            Log.d("GmailSyncActivity", "accountName" + names[i]);
        }

        return names;


    }

    private AbstractGetNameTask getTask(GmailSyncActivity activity, String email, String scope) {
        return new GetNameInForeground(activity, email, scope);

    }

    public void syncGoogleAccount() {
        if (isNetworkAvailable() == true) {
            String[] accountarrs = getAccountNames();
            if (accountarrs.length > 0) {
                //you can set here account for login
                 getTask(GmailSyncActivity.this, accountarrs[0], SCOPE).execute();
            } else {
                Toast.makeText(GmailSyncActivity.this, "No Google Account Sync!",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(GmailSyncActivity.this, "No Network Service!",
                    Toast.LENGTH_SHORT).show();
        }
    }


    public boolean isNetworkAvailable() {

        ConnectivityManager cm = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.e("Network Testing", "***Available***");
            return true;
        }
        Log.e("Network Testing", "***Not Available***");
        return false;
    }

    @Override
    public void onBackPressed() {

    }


}