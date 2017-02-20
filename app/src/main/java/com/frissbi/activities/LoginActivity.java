package com.frissbi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.frissbi.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final int RC_SIGN_IN = 100;
    private static final String APPLICATION_NAME = "Frissbi";
    private GoogleApiClient mGoogleApiClient;
    private String TAG = "LoginActivity";
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(this);

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
                .addApi(Plus.API)
                .build();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.sign_in_button:
                loginWithGoogle();
                break;
        }

    }

    private void loginWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
            Log.d("LoginActivity", "photoUrl" + acct.getPhotoUrl());
            Log.d("LoginActivity", "DisplayName" + acct.getDisplayName());
            Log.d("LoginActivity", "GivenName" + acct.getGivenName());
            Log.d("LoginActivity", "Email:" + acct.getEmail());
            emailId = acct.getEmail();
            Log.d("LoginActivity", "Id:" + acct.getId());
            Log.d("LoginActivity", "IdToken:" + acct.getIdToken());

            Plus.PeopleApi.load(mGoogleApiClient, acct.getId()).setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
                @Override
                public void onResult(@NonNull People.LoadPeopleResult loadPeopleResult) {
                    Person person = loadPeopleResult.getPersonBuffer().get(0);
                    Log.d(TAG,"Person loaded");
                    Log.d(TAG,"GivenName "+person.getName().getGivenName());
                    Log.d(TAG,"FamilyName "+person.getName().getFamilyName());
                    Log.d(TAG,("DisplayName "+person.getDisplayName()));
                    Log.d(TAG,"Gender "+person.getGender());
                    Log.d(TAG,"Url "+person.getUrl());
                    Log.d(TAG,"CurrentLocation "+person.getCurrentLocation());
                    Log.d(TAG,"AboutMe "+person.getAboutMe());
                    Log.d(TAG,"Birthday "+person.getBirthday());
                    Log.d(TAG,"Image "+person.getImage());
                }
            });

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

        }
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
            Log.d(TAG, "Image " + person.getImage());
        }*/
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
