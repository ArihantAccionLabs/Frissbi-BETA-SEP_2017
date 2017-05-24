package com.frissbi.app.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.frissbi.app.R;
import com.frissbi.app.Utility.CustomProgressDialog;
import com.frissbi.app.Utility.FLog;
import com.frissbi.app.Utility.SharedPreferenceHandler;
import com.frissbi.app.Utility.Utility;
import com.frissbi.app.models.Profile;
import com.frissbi.app.networkhandler.TSNetworkHandler;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.os.Build.VERSION.SDK_INT;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final int RC_SIGN_IN = 100;
    private static final String APPLICATION_NAME = "Frissbi";
    private GoogleApiClient mGoogleApiClient;
    private String TAG = "LoginActivity";
    private ProgressDialog mProgressDialog;
    private Bitmap mBitmap;
 /*   *//**
     * Global instance of the HTTP transport.
     *//*
    private static HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    */
    /**
     * Global instance of the JSON factory.
     *//*
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();*/
    private String emailId;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private SharedPreferenceHandler mSharedPreferenceHandler;
    private byte[] imageByteArray;
    private RelativeLayout mGoogleLoginLayout;
    private EditText mLoginEmailEt;
    private EditText mLoginPasswordEt;
    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

        }
        mSharedPreferenceHandler = SharedPreferenceHandler.getInstance(this);
        mGoogleLoginLayout = (RelativeLayout) findViewById(R.id.google_login_rl);
        mLoginEmailEt = (EditText) findViewById(R.id.login_email_et);
        mLoginPasswordEt = (EditText) findViewById(R.id.login_password_et);
        findViewById(R.id.get_started_button).setOnClickListener(this);
        findViewById(R.id.create_account_button).setOnClickListener(this);
        findViewById(R.id.forgot_password).setOnClickListener(this);
        mGoogleLoginLayout.setOnClickListener(this);
        mProgressDialog = new CustomProgressDialog(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestScopes(new Scope(Scopes.PROFILE))
                //   .requestScopes(new Scope(PeopleScopes.CONTACTS_READONLY))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.google_login_rl:
                loginWithGoogle();
                break;
            case R.id.create_account_button:
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
                break;
            case R.id.get_started_button:
                if (validateFieldValues()) {
                    login();
                }
                break;
            case R.id.forgot_password:
                showForgotPasswordDialog();
                break;
        }

    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password");
        View view = LayoutInflater.from(this).inflate(R.layout.forgot_password_alert, null);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        final EditText emailEt = (EditText) view.findViewById(R.id.email_et);
        final EditText passwordEt = (EditText) view.findViewById(R.id.password_et);
        final EditText confirmPasswordEt = (EditText) view.findViewById(R.id.confirm_password_et);
        Button submitButton = (Button) view.findViewById(R.id.submit_button);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (emailEt.getText().toString().trim().length() > 0) {
                    if (passwordEt.getText().toString().trim().length() > 0) {
                        if (confirmPasswordEt.getText().toString().trim().length() > 0) {
                            if (validateEmailPassword(emailEt.getText().toString(), passwordEt.getText().toString(), confirmPasswordEt.getText().toString())) {
                                alertDialog.dismiss();
                                sendNewPasswordToServer(emailEt.getText().toString(), passwordEt.getText().toString());
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Enter confirm password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Enter email Id", Toast.LENGTH_SHORT).show();
                }

            }
        });

        alertDialog.show();
    }

    private boolean validateEmailPassword(String email, String password, String confirmPassword) {

        if (email.matches("[a-zA-Z0-9._-]+@[a-z]+.[a-z]+")) {


            if (isValidPassword(password)) {
                if (isValidPassword(confirmPassword)) {
                    if (password.equals(confirmPassword)) {
                        return true;
                    } else {
                        Toast.makeText(this, "Password and Confirm Password must match", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Confirm Password must be 6 characters with at least  on special character", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Password must be 6 characters with at least  on special character", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, "Please enter valid email", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void sendNewPasswordToServer(String email, String password) {
        mProgressDialog.show();
        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("email", email);
            jsonObject.put("password", password);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = Utility.REST_URI + Utility.FORGOT;
        TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {

                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        Toast.makeText(LoginActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        emailReset();
                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(LoginActivity.this, response.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
                mProgressDialog.dismiss();

            }
        });
    }

    private boolean validateFieldValues() {

        if (mLoginEmailEt.getText().toString().trim().length() > 0) {

            if (mLoginEmailEt.getText().toString().matches("[a-zA-Z0-9._-]+@[a-z]+.[a-z]+")) {
                if (!mLoginEmailEt.getText().toString().matches("[a-zA-Z0-9._-]+@gmail+.com+")) {
                    if (mLoginPasswordEt.getText().toString().trim().length() > 0) {
                        if (isValidPassword(mLoginPasswordEt.getText().toString().trim())) {
                            return true;
                        } else {
                            Toast.makeText(this, "Password must be 6 characters with atleast  on special character", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Please login through google", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter valid email", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
        }


        return false;
    }

    public boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "((?=.*[@#$%^&+=]).{6,12})";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

    private void login() {
        mProgressDialog.show();
        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("email", mLoginEmailEt.getText().toString());
            jsonObject.put("password", mLoginPasswordEt.getText().toString());
            jsonObject.put("isGmailLogin", false);
            jsonObject.put("deviceRegistrationId", FirebaseInstanceId.getInstance().getToken());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = Utility.REST_URI + Utility.LOGIN;
        TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {

                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            JSONObject responseJsonObject = new JSONObject(response.response);
                            mSharedPreferenceHandler.storeLoginDetails(responseJsonObject.getLong("userId"));
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(LoginActivity.this, response.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
                mProgressDialog.dismiss();

            }
        });
    }

    private void loginWithGoogle() {
        mProgressDialog.show();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("LoginActivity", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Profile profile = new Profile();
            if (acct.getDisplayName() != null) {
                profile.setUserName(acct.getDisplayName());
            } else {
                profile.setUserName(acct.getGivenName());
            }
            profile.setFirstName(acct.getGivenName());
            profile.setLastName(acct.getFamilyName());
            profile.setEmail(acct.getEmail());
            if (acct.getPhotoUrl() != null) {
                try {
                    mBitmap = getBitmapFromURL(new URL(acct.getPhotoUrl().toString()).toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                if (mBitmap != null) {
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    imageByteArray = baos.toByteArray();
                }
            }

            shareLoginDetailsWithServer(profile);

            /*Plus.PeopleApi.load(mGoogleApiClient, acct.getId()).setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
                @Override
                public void onResult(@NonNull People.LoadPeopleResult loadPeopleResult) {
                    Person person = loadPeopleResult.getPersonBuffer().get(0);
                    Log.d(TAG, "Person loaded");
                    Log.d(TAG, "GivenName " + person.getName().getGivenName());
                    Log.d(TAG, "FamilyName " + person.getName().getFamilyName());
                    Log.d(TAG, ("DisplayName " + person.getDisplayName()));
                    Log.d(TAG, "Gender " + person.getGender());
                    Log.d(TAG, "Url " + person.getUrl());
                    Log.d(TAG, "CurrentLocation " + person.getCurrentLocation());
                    Log.d(TAG, "AboutMe " + person.getAboutMe());
                    Log.d(TAG, "Birthday " + person.getBirthday());
                    Log.d(TAG, "Image " + person.getImageId().getUrl());
                    mBitmap = getBitmapFromURL(person.getImageId().getUrl());
                    FLog.d("LoginActivity", "Bitmap" + mBitmap.getByteCount());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageByteArray = baos.toByteArray();
                    FLog.d("LoginActivity", "imageByteArray" + imageByteArray.length);
                    mProgressDialog.dismiss();
                }
            });*/

          /*  // On worker thread
            GoogleAccountCredential credential =
                    GoogleAccountCredential.usingOAuth2(LoginActivity.this, Collections.singleton(PeopleScopes.CONTACTS_READONLY));
            credential.setSelectedAccount(
                    new Account(emailId, "com.google"));
            People service = new People.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME *//* whatever you like *//*)
                    .build();
            ListConnectionsResponse response = null;
            try {
                response = service.people().connections()
                        .list("people/me")
                        // request 20 contacts
                        .setPageSize(20)
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<com.google.api.services.people.v1.model.Person> connections = response.getConnections();
            if (connections != null && connections.size() > 0) {
                for (com.google.api.services.people.v1.model.Person person : connections) {
                    List<Name> names = person.getNames();
                    if (names != null && names.size() > 0) {
                        Log.i(TAG, "Name: " + person.getNames().get(0).getDisplayName());
                    } else {
                        Log.i(TAG, "No names available for connection.");
                    }
                    List<Gender> genders = person.getGenders();
                    String ageRange = person.getAgeRange();
                    List<com.google.api.services.people.v1.model.Birthday> birthdays = person.getBirthdays();

                }
*/
        } else {
            Toast.makeText(this, "Unable to login try again..", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareLoginDetailsWithServer(Profile profile) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deviceRegistrationId", FirebaseInstanceId.getInstance().getToken());
            jsonObject.put("userName", profile.getUserName());
            jsonObject.put("password", "");
            jsonObject.put("email", profile.getEmail());
            jsonObject.put("firstName", profile.getFirstName());
            jsonObject.put("firstName", profile.getFirstName());
            jsonObject.put("lastName", profile.getLastName());
            jsonObject.put("isGmailLogin", true);
            jsonObject.put("dob", "");
            FLog.d("LoginActivity", "jsonObject" + jsonObject);
            if (imageByteArray != null) {
                jsonObject.put("image", Base64.encodeToString(imageByteArray, Base64.DEFAULT));
            }

            FLog.d("LoginActivity", "jsonObject" + jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = Utility.REST_URI + Utility.USER_REGISTRATION;
        TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            JSONObject responseJsonObject = new JSONObject(response.response);
                            mSharedPreferenceHandler.storeLoginDetails(responseJsonObject.getLong("userId"));
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(LoginActivity.this, response.message, Toast.LENGTH_SHORT).show();
                    }
                }
                mProgressDialog.dismiss();
            }
        });


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        Log.d(TAG, "PeopleApi " + Plus.PeopleApi.getCurrentPerson(mGoogleApiClient));
        /*if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            Log.d(TAG, "GivenName " + person.getName().getGivenName());
            Log.d(TAG, "FamilyName " + person.getName().getFamilyName());
            Log.d(TAG, ("DisplayName " + person.getDisplayName()));
            Log.d(TAG, "Gender " + person.getGender());
            Log.d(TAG, "Url " + person.getUrl());
            Log.d(TAG, "CurrentLocation " + person.getCurrentLocation());
            Log.d(TAG, "AboutMe " + person.getAboutMe());
            Log.d(TAG, "Birthday " + person.getBirthday());
            Log.d(TAG, "Image " + person.getImageId());
        }*/
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void emailReset() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Alert!");
        builder.setMessage("Please check mail and login..");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAlertDialog.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAlertDialog.dismiss();
            }
        });

        mAlertDialog = builder.create();
        mAlertDialog.show();


    }

}
