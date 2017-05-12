package com.frissbi.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.Utility.Utility;
import com.frissbi.fragments.UploadPhotoDialogFragment;
import com.frissbi.frissbi.GetTermsandConditions;
import com.frissbi.frissbi.Privacypolicy;
import com.frissbi.interfaces.UploadPhotoListener;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.frissbi.Utility.Utility.CAMERA_REQUEST;
import static com.frissbi.Utility.Utility.SELECT_FILE;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener, UploadPhotoListener {

    private static final int DATE_DIALOG_ID = 1111;
    private ImageView mRegProfileImageView;
    private EditText mRegNameEt;
    private EditText mRegEmailEt;
    private EditText mRegPasswordEt;
    private EditText mRegPhoneEt;
    private EditText mRegDobEt;
    private Calendar calendar;
    private CheckBox mTermsConditionsCheckBox;
    private UploadPhotoDialogFragment mUploadPhotoDialogFragment;
    private UploadPhotoListener mUploadPhotoListener;
    private String mPictureImagePath;
    private byte[] mImageByteArray;
    private CustomProgressDialog mProgressDialog;
    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        calendar = Calendar.getInstance();
        mProgressDialog = new CustomProgressDialog(this);
        setUpViews();
    }

    private void setUpViews() {
        mRegProfileImageView = (ImageView) findViewById(R.id.reg_profile_imageView);
        mRegNameEt = (EditText) findViewById(R.id.reg_name_et);
        mRegEmailEt = (EditText) findViewById(R.id.reg_email_et);
        mRegPasswordEt = (EditText) findViewById(R.id.reg_password_et);
        mRegPhoneEt = (EditText) findViewById(R.id.reg_phone_et);
        mRegDobEt = (EditText) findViewById(R.id.reg_dob_et);
        mTermsConditionsCheckBox = (CheckBox) findViewById(R.id.terms_conditions_checkBox);
        findViewById(R.id.reg_dob_rl).setOnClickListener(this);
        findViewById(R.id.terms_conditions_tv).setOnClickListener(this);
        findViewById(R.id.privacy_tv).setOnClickListener(this);
        mRegProfileImageView.setOnClickListener(this);
        findViewById(R.id.create_button).setOnClickListener(this);
        mUploadPhotoDialogFragment = new UploadPhotoDialogFragment();
        mUploadPhotoListener = (UploadPhotoListener) this;
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reg_dob_rl:
                onCreateDialog(DATE_DIALOG_ID).show();
                break;
            case R.id.terms_conditions_tv:
                Intent intent = new Intent(getApplicationContext(), GetTermsandConditions.class);
                startActivity(intent);
                break;
            case R.id.privacy_tv:
                Intent intent1 = new Intent(getApplicationContext(), Privacypolicy.class);
                startActivity(intent1);
                break;
            case R.id.reg_profile_imageView:
                mUploadPhotoDialogFragment.setUploadPhotoListener(mUploadPhotoListener, Utility.PROFILE_IMAGE);
                mUploadPhotoDialogFragment.show(getSupportFragmentManager(), "UploadPhotoDialogFragment");
                break;
            case R.id.create_button:
                if (validateFieldValues()) {
                    sendDetailsToServer();
                }
                break;
        }
    }


    private void sendDetailsToServer() {
        mProgressDialog.show();
        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("firstName", mRegNameEt.getText().toString());
            jsonObject.put("email", mRegEmailEt.getText().toString());
            jsonObject.put("contactno", mRegPhoneEt.getText().toString());
            jsonObject.put("password", mRegPasswordEt.getText().toString());
            jsonObject.put("isGmailLogin", false);
            jsonObject.put("dob", mRegDobEt.getText().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = Utility.REST_URI + Utility.REGISTRATION;
        TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {

                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        Toast.makeText(RegistrationActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        emailReset();
                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(RegistrationActivity.this, response.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegistrationActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
                mProgressDialog.dismiss();

            }
        });
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                DatePickerDialog dateDialog = new DatePickerDialog(RegistrationActivity.this, myDateListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                dateDialog.getDatePicker().setMaxDate(System.currentTimeMillis());


                return dateDialog;

        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(android.widget.DatePicker arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day

            int year, month, day;
            year = arg1;
            month = arg2;
            day = arg3;

            Calendar cal = Calendar.getInstance();
            int tyear = cal.get(Calendar.YEAR);
            int tmonth = cal.get(Calendar.MONTH);
            int tday = cal.get(Calendar.DAY_OF_MONTH);

            if (tyear == year) {
                if (month == tmonth) {
                    if (tday > day) {
                        day = 1;
                    }
                    showDate(year, tmonth + 1, day);
                } else {
                    showDate(year, month + 1, day);
                }
            } else {
                showDate(arg1, month + 1, day);
            }

        }
    };


    private void showDate(int year, int month, int day) {
        mRegDobEt.setText(new StringBuilder().append(year).append("-").append(month).append("-").append(day));
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
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            File imgFile = new File(mPictureImagePath);
            Bitmap bitmap1 = Utility.getInstance().decodeFile(imgFile);
            Bitmap bitmap = Utility.getInstance().rotateImage(bitmap1, mPictureImagePath);
            mRegProfileImageView.setImageBitmap(bitmap);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            mImageByteArray = baos.toByteArray();
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
            mRegProfileImageView.setImageBitmap(bitmap);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            mImageByteArray = baos.toByteArray();
        }
    }

    private boolean validateFieldValues() {

        if (mRegNameEt.getText().toString().trim().length() > 0) {

            if (mRegEmailEt.getText().toString().trim().length() > 0) {

                if (mRegEmailEt.getText().toString().matches("[a-zA-Z0-9._-]+@[a-z]+.[a-z]+")) {
                    if (!mRegEmailEt.getText().toString().matches("[a-zA-Z0-9._-]+@gmail+.com+")) {
                        if (mRegPasswordEt.getText().toString().trim().length() > 0) {
                            if (isValidPassword(mRegPasswordEt.getText().toString().trim())) {

                                if (mRegPhoneEt.getText().toString().trim().length() > 0) {
                                    if (mRegDobEt.getText().toString().trim().length() > 0) {
                                        if (mTermsConditionsCheckBox.isChecked()) {
                                            return true;
                                        } else {
                                            Toast.makeText(this, "Please check terms and condition", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(this, "Please select DOB", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
                                }

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

        } else {
            Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
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


    private void emailReset() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
        builder.setTitle("Alert!");
        builder.setMessage("Please check mail and login..");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAlertDialog.dismiss();
                onBackPressed();
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
