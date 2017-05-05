package com.frissbi.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.ImageCacheHandler;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.Utility;
import com.frissbi.fragments.UploadPhotoDialogFragment;
import com.frissbi.interfaces.UploadPhotoListener;
import com.frissbi.models.Profile;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static com.frissbi.Utility.Utility.CAMERA_REQUEST;
import static com.frissbi.Utility.Utility.SELECT_FILE;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, UploadPhotoListener {

    private ImageView mProfileUserImageView;
    private EditText mUsernameEt;
    private EditText mEmailEt;
    private EditText mPhoneEt;
    private UploadPhotoDialogFragment mUploadPhotoDialogFragment;
    private UploadPhotoListener mUploadPhotoListener;
    private String mPictureImagePath;
    private byte[] mImageByteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setUpViews();
    }

    private void setUpViews() {
        mProfileUserImageView = (ImageView) findViewById(R.id.profile_user_imageView);
        mUsernameEt = (EditText) findViewById(R.id.username_et);
        mEmailEt = (EditText) findViewById(R.id.email_et);
        mPhoneEt = (EditText) findViewById(R.id.phone_et);
        findViewById(R.id.profile_imageUpdate_tv).setOnClickListener(this);
        mUploadPhotoDialogFragment = new UploadPhotoDialogFragment();
        mUploadPhotoListener = (UploadPhotoListener) this;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_imageUpdate_tv:
                mUploadPhotoDialogFragment.setUploadPhotoListener(mUploadPhotoListener, Utility.PROFILE_IMAGE);
                mUploadPhotoDialogFragment.show(getSupportFragmentManager(), "UploadPhotoDialogFragment");
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpValues();
    }

    private void setUpValues() {
        Profile profile = Profile.first(Profile.class);
        Log.d("SettingsActivity", "profile" + profile);
        mUsernameEt.setText(profile.getUserName());
        mEmailEt.setText(profile.getEmail());
        if (profile.getContactNumber() != null) {
            mPhoneEt.setText(profile.getContactNumber());
        }
        if (profile.getImageId() != null) {
            ImageCacheHandler.getInstance(SettingsActivity.this).setImage(mProfileUserImageView, profile.getImageId());
        }
    }

    @Override
    public void captureImage(int typeOfImage) {
        String imageFileName = System.currentTimeMillis() / 1000 + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        mPictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
        File file = new File(mPictureImagePath);
        // Uri outputFileUri = Uri.fromFile(file);

        Uri outputFileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    public void chooseAFile(int typeOfImage) {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(
                Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        FLog.d("ConfirmGroupFragment", "onActivityResult-----" + requestCode + "resultCode" + resultCode);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            File imgFile = new File(mPictureImagePath);
            Bitmap bitmap1 = Utility.getInstance().decodeFile(imgFile);
            Bitmap bitmap = Utility.getInstance().rotateImage(bitmap1, mPictureImagePath);
            mProfileUserImageView.setImageBitmap(bitmap);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            mImageByteArray = baos.toByteArray();
            sendProfileImageToServer();
        } else if (requestCode == SELECT_FILE && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            String[] projection = {MediaStore.MediaColumns.DATA};
            CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            String selectedImagePath = cursor.getString(column_index);
            File imgFile = new File(selectedImagePath);
            Bitmap bitmap = Utility.getInstance().decodeFile(imgFile);
            mProfileUserImageView.setImageBitmap(bitmap);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            mImageByteArray = baos.toByteArray();
            sendProfileImageToServer();
        }
    }


    /*
    Sending profile image to server
     */
    private void sendProfileImageToServer() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", SharedPreferenceHandler.getInstance(this).getUserId());
            jsonObject.put("file", Base64.encodeToString(mImageByteArray, Base64.DEFAULT));

            String url = Utility.REST_URI + Utility.INSERT_PROFILE_IMAGE;
            TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {
                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            Toast.makeText(SettingsActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(SettingsActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SettingsActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

}
