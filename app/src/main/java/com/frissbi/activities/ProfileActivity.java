package com.frissbi.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.FriendStatus;
import com.frissbi.Utility.ImageCacheHandler;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.Utility;
import com.frissbi.adapters.UserActivitiesAdapter;
import com.frissbi.enums.ActivityType;
import com.frissbi.fragments.PostFreeTimeDialogFragment;
import com.frissbi.fragments.UploadPhotoDialogFragment;
import com.frissbi.fragments.ViewImageDialogFragment;
import com.frissbi.interfaces.MeetingDetailsListener;
import com.frissbi.interfaces.PostFreeTimeListener;
import com.frissbi.interfaces.UploadPhotoListener;
import com.frissbi.interfaces.ViewImageListener;
import com.frissbi.models.Activities;
import com.frissbi.models.Friend;
import com.frissbi.models.Profile;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.frissbi.Utility.Utility.CAMERA_REQUEST;
import static com.frissbi.Utility.Utility.SELECT_FILE;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener, UploadPhotoListener, PostFreeTimeListener, MeetingDetailsListener, ViewImageListener, SwipeRefreshLayout.OnRefreshListener {

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1000;
    private ImageView mProfileUserImageView;
    private TextView mProfileUserNameTextView;
    private Friend mFriend;
    private Long mUserId;
    private ProgressDialog mProgressDialog;
    private EditText mStatusMessageEditText;
    private ImageView mSubmitStatusImageView;
    private UploadPhotoListener mUploadPhotoListener;
    private UploadPhotoDialogFragment mUploadPhotoDialogFragment;
    private int mTypeOfImage;
    private String mPictureImagePath;
    private RelativeLayout mCoverPhotoLayout;
    private byte[] mProfileImageByteArray;
    private byte[] mCoverImageByteArray;
    private RecyclerView mActivitiesRecyclerView;
    private FragmentManager mFragmentManager;
    private PostFreeTimeListener mPostFreeTimeListener;
    private Button mEditProfileImageButton;
    private Button mEditCoverPhotoButton;
    private boolean isProfileImageEdited;
    private boolean isCoverPhotoEdited;
    private List<Activities> mActivitiesList;
    private Button mViewMoreButton;
    private int mActivityOffSetValue;
    private UserActivitiesAdapter mUserActivitiesAdapter;
    private FloatingActionButton mScrollTopFloatingButton;
    private NestedScrollView mNestedScrollView;
    private MeetingDetailsListener mMeetingDetailsListener;
    private ViewImageListener mViewImageListener;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            }


            if (ContextCompat.checkSelfPermission(ProfileActivity.this
                    , android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(ProfileActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                } else {
                    ActivityCompat.requestPermissions(ProfileActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            }
        } else {

        }

        mProgressDialog = new CustomProgressDialog(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mActivitiesList = new ArrayList<>();
        mUserId = SharedPreferenceHandler.getInstance(this).getUserId();
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.profile_swipeRefreshLayout);
        mNestedScrollView = (NestedScrollView) findViewById(R.id.profile_nestedScrollView);
        mProfileUserImageView = (ImageView) findViewById(R.id.profile_user_image);
        mProfileUserNameTextView = (TextView) findViewById(R.id.profile_username_tv);
        mStatusMessageEditText = (EditText) findViewById(R.id.status_message_et);
        mSubmitStatusImageView = (ImageView) findViewById(R.id.submit_status_imageView);
        mCoverPhotoLayout = (RelativeLayout) findViewById(R.id.cover_photo_rl);
        mActivitiesRecyclerView = (RecyclerView) findViewById(R.id.activities_recyclerView);
        mEditProfileImageButton = (Button) findViewById(R.id.edit_profile_image_button);
        mEditCoverPhotoButton = (Button) findViewById(R.id.edit_cover_photo_button);
        mViewMoreButton = (Button) findViewById(R.id.view_more_button);
        mScrollTopFloatingButton = (FloatingActionButton) findViewById(R.id.scroll_top_floating_button);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this); /*{
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };*/
        mActivitiesRecyclerView.setLayoutManager(layoutManager);
        mActivitiesRecyclerView.setNestedScrollingEnabled(false);
        mUploadPhotoListener = (UploadPhotoListener) this;
        mPostFreeTimeListener = (PostFreeTimeListener) this;
        mMeetingDetailsListener = (MeetingDetailsListener) this;
        mViewImageListener = (ViewImageListener) this;
        mFragmentManager = getSupportFragmentManager();
        mUploadPhotoDialogFragment = new UploadPhotoDialogFragment();

        Bundle bundle = getIntent().getExtras();
        /*if (bundle != null) {
            mAddFriendButton.setVisibility(View.VISIBLE);
            if (bundle.containsKey("friend")) {
                mFriend = (Friend) bundle.getSerializable("friend");
                setValues();
            } else if (bundle.containsKey("friendUserId")) {
                getProfileDetailsFromServer(bundle.getLong("friendUserId"));
            }
        } else {
            mAddFriendButton.setVisibility(View.GONE);
            getProfileDetails();
        }
        mAddFriendButton.setOnClickListener(this);*/

        mProfileUserNameTextView.setText(SharedPreferenceHandler.getInstance(this).getUserName());
        //  mProfileUserImageView.setOnClickListener(this);
        mSubmitStatusImageView.setOnClickListener(this);
        mEditCoverPhotoButton.setOnClickListener(this);
        mEditProfileImageButton.setOnClickListener(this);
        findViewById(R.id.post_free_time_button).setOnClickListener(this);
        mViewMoreButton.setOnClickListener(this);
        mScrollTopFloatingButton.setOnClickListener(this);
        mStatusMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    mSubmitStatusImageView.setVisibility(View.VISIBLE);
                } else {
                    mSubmitStatusImageView.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mNestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                FLog.d("ProfileActivity", "onScrollChange---scrollX" + scrollX + "scrollY---" + scrollY + "====oldScrollX--" + oldScrollX + "oldScrollY---" + oldScrollY);
                if (scrollY == 0) {
                    mScrollTopFloatingButton.setVisibility(View.GONE);
                } else {
                    mScrollTopFloatingButton.setVisibility(View.VISIBLE);
                }
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(this);

        getProfileDetails();
        getUserActivities();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
           /* case R.id.add_friend_button:
                sendFriendRequest();
                break;*/
            case R.id.edit_profile_image_button:
                if (!isProfileImageEdited) {
                    mUploadPhotoDialogFragment.setUploadPhotoListener(mUploadPhotoListener, Utility.PROFILE_IMAGE);
                    mUploadPhotoDialogFragment.show(mFragmentManager, "UploadPhotoDialogFragment");
                } else {
                    sendProfileImageToServer();
                }
                break;
            case R.id.submit_status_imageView:
                if (mStatusMessageEditText.getText().toString().trim().length() > 0) {
                    submitStatusMessage();
                } else {
                    Toast.makeText(this, getString(R.string.enter_status), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.edit_cover_photo_button:
                if (!isCoverPhotoEdited) {
                    mUploadPhotoDialogFragment.setUploadPhotoListener(mUploadPhotoListener, Utility.COVER_IMAGE);
                    mUploadPhotoDialogFragment.show(mFragmentManager, "UploadPhotoDialogFragment");
                } else {
                    sendCoverImageToServer();
                }
                break;
            case R.id.post_free_time_button:
                PostFreeTimeDialogFragment postFreeTimeDialogFragment = new PostFreeTimeDialogFragment();
                postFreeTimeDialogFragment.setFreeTimeListener(mPostFreeTimeListener);
                postFreeTimeDialogFragment.show(mFragmentManager, "PostFreeTimeDialogFragment");
                break;
            case R.id.view_more_button:
                getMoreActivities();
                break;
            case R.id.scroll_top_floating_button:
                mNestedScrollView.fullScroll(ScrollView.FOCUS_UP);
                break;

        }
    }


    /*To Get Profile Details
       */
    private void getProfileDetails() {
        mProgressDialog.show();
        String url = Utility.REST_URI + Utility.VIEW_PROFILE + mUserId;
        TSNetworkHandler.getInstance(this).getResponse(url, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {

                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            JSONObject responseJsonObject = new JSONObject(response.response);
                            FLog.d("ProfileActivity", "responseJsonObject" + responseJsonObject);
                            Profile profile = new Profile();
                            JSONObject profileJsonObject = responseJsonObject.getJSONObject("viewProfile");
                            profile.setUserName(profileJsonObject.getString("userName"));
                            profile.setFirstName(profileJsonObject.getString("firstName"));
                            profile.setLastName(profileJsonObject.getString("lastName"));
                            profile.setEmail(profileJsonObject.getString("email"));
                            if (profileJsonObject.has("contactNumber")) {
                                profile.setContactNumber(profileJsonObject.getString("contactNumber"));
                            }

                            if (profileJsonObject.has("profileImageId")) {
                                profile.setImageId(profileJsonObject.getString("profileImageId"));
                            }

                            if (profileJsonObject.has("coverImageId")) {
                                profile.setCoverImageId(profileJsonObject.getString("coverImageId"));
                            }

                            if (profileJsonObject.has("gender")) {
                                profile.setGender(profileJsonObject.getString("gender"));
                            }

                            if (profileJsonObject.has("dob")) {
                                profile.setDob(profileJsonObject.getString("dob"));
                            }
                            setProfileDetails(profile);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                    }
                }
                mProgressDialog.dismiss();
            }
        });
    }

    /*
    Profile and Cover Images setting.
     */
    private void setProfileDetails(Profile profile) {
        mProfileUserNameTextView.setText(profile.getUserName());
        if (profile.getImageId() != null) {
            ImageCacheHandler.getInstance(this).setImage(mProfileUserImageView, profile.getImageId());
        }
        if (profile.getCoverImageId() != null) {
            Bitmap bitmap = ImageCacheHandler.getInstance(this).findImageFromMemory(profile.getCoverImageId());
            if (bitmap != null) {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                mCoverPhotoLayout.setBackground(drawable);
            } else {
                getCoverPhotoFromServer(Utility.REST_URI + Utility.GET_IMAGE + profile.getCoverImageId());
            }
        }
    }

    /*
    Getting cover image from server
     */
    private void getCoverPhotoFromServer(String imageURL) {
        TSNetworkHandler.getInstance(ProfileActivity.this).getResponse(imageURL, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.response);
                            byte[] decodedString = Base64.decode(jsonObject.getString("uriImage"), Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                            mCoverPhotoLayout.setBackground(drawable);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

   /* private void setValues() {
        mProfileUserNameTextView.setText(mFriend.getFullName());
        mProfileUserEmail.setText(mFriend.getEmailId());
        mDobTextView.setText("DOB : " + mFriend.getDob());
        if (mFriend.getGender() != null) {
            mGenderTextView.setVisibility(View.VISIBLE);
            mGenderTextView.setText("Gender : " + mFriend.getGender());
        } else {
            mGenderTextView.setVisibility(View.GONE);
        }

        if (mFriend.getStatus().equalsIgnoreCase(FriendStatus.UNFRIEND.toString())) {
            mAddFriendButton.setText("Add");
        } else if (mFriend.getStatus().equalsIgnoreCase(FriendStatus.WAITING.toString())) {
            mAddFriendButton.setText("Req Sent");
        } else if (mFriend.getStatus().equalsIgnoreCase(FriendStatus.CONFIRM.toString())) {
            mAddFriendButton.setText("Accept");
        } else if (mFriend.getStatus().equalsIgnoreCase(FriendStatus.FRIENDS.toString())) {
            mAddFriendButton.setText("Friends");
        }
    }*/

    private void getProfileDetailsFromServer(long friendUserId) {
        mProgressDialog.show();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", mUserId);
            jsonObject.put("friendId", friendUserId);
            String url = Utility.REST_URI + Utility.VIEW_OTHER_PROFILE;
            TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {
                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            try {
                                JSONObject responseJsonObject = new JSONObject(response.response);
                                JSONObject profileJsonObject = responseJsonObject.getJSONObject("viewProfile");
                                mFriend = new Friend();
                                mFriend.setUserId(profileJsonObject.getLong("userId"));
                                mFriend.setFullName(profileJsonObject.getString("fullName"));
                                mFriend.setEmailId(profileJsonObject.getString("emailId"));
                                mFriend.setStatus(profileJsonObject.getString("status"));
                                if (profileJsonObject.has("dob")) {
                                    mFriend.setDob(profileJsonObject.getString("dob"));
                                }
                                if (profileJsonObject.has("gender")) {
                                    mFriend.setGender(profileJsonObject.getString("gender"));
                                }
                                Log.d("ProfileActivity", "mFriend" + mFriend);
                                // setValues();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "Something went wrong at server side", Toast.LENGTH_SHORT).show();
                    }
                    mProgressDialog.dismiss();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /*
    Submit status message to server
     */
    private void submitStatusMessage() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", mUserId);
            jsonObject.put("description", mStatusMessageEditText.getText().toString());

            String url = Utility.REST_URI + Utility.STATUS_MESSAGE;

            TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {
                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /*
    Sending cover image to server
     */
    private void sendCoverImageToServer() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", mUserId);
            jsonObject.put("file", Base64.encodeToString(mCoverImageByteArray, Base64.DEFAULT));

            String url = Utility.REST_URI + Utility.INSERT_COVER_IMAGE;
            TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {
                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                            isCoverPhotoEdited = false;
                            mEditCoverPhotoButton.setBackground(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.icon_edit_profile_pic_black));
                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
    Sending profile image to server
     */
    private void sendProfileImageToServer() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", mUserId);
            jsonObject.put("file", Base64.encodeToString(mProfileImageByteArray, Base64.DEFAULT));

            String url = Utility.REST_URI + Utility.INSERT_PROFILE_IMAGE;
            TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {
                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                            isProfileImageEdited = false;
                            mEditProfileImageButton.setBackground(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.icon_edit_profile_pic_black));
                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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

    public void sendFriendRequest() {
        mProgressDialog.show();
        if (mFriend.getStatus().equalsIgnoreCase(FriendStatus.UNFRIEND.toString())) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("userId", mUserId);
                jsonObject.put("friendId", mFriend.getUserId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String url = Utility.REST_URI + Utility.ADD_FRIEND;
            TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {

                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                            //mAddFriendButton.setText("Req Sent");
                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(ProfileActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }

                }
            });
        } else if (mFriend.getStatus().equalsIgnoreCase(FriendStatus.CONFIRM.toString())) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("userId", mUserId);
                jsonObject.put("friendId", mFriend.getUserId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String url = Utility.REST_URI + Utility.APPROVE_FRIEND;
            TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {

                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                            //mAddFriendButton.setText("Friends");
                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(ProfileActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }
        mProgressDialog.dismiss();
    }

    @Override
    public void captureImage(int typeOfImage) {
        mTypeOfImage = typeOfImage;
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
        mTypeOfImage = typeOfImage;
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(
                Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            File imgFile = new File(mPictureImagePath);
            Bitmap bitmap = Utility.getInstance().decodeFile(imgFile);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (mTypeOfImage == Utility.PROFILE_IMAGE) {
                mProfileUserImageView.setImageBitmap(bitmap);
                isProfileImageEdited = true;
                mEditProfileImageButton.setBackground(ContextCompat.getDrawable(this, R.drawable.icon_tick));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                mProfileImageByteArray = baos.toByteArray();
            } else {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                mCoverPhotoLayout.setBackground(drawable);
                isCoverPhotoEdited = true;
                mEditCoverPhotoButton.setBackground(ContextCompat.getDrawable(this, R.drawable.icon_tick));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                mCoverImageByteArray = baos.toByteArray();
            }


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
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (mTypeOfImage == Utility.PROFILE_IMAGE) {
                mProfileUserImageView.setImageBitmap(bitmap);
                isProfileImageEdited = true;
                mEditProfileImageButton.setBackground(ContextCompat.getDrawable(this, R.drawable.icon_tick));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                mProfileImageByteArray = baos.toByteArray();
            } else {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                mCoverPhotoLayout.setBackground(drawable);
                isCoverPhotoEdited = true;
                mEditCoverPhotoButton.setBackground(ContextCompat.getDrawable(this, R.drawable.icon_tick));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                mCoverImageByteArray = baos.toByteArray();
            }


        }
    }

    @Override
    public void sendFreeTime(String time, String duration) {
        mProgressDialog.show();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", mUserId);
            jsonObject.put("freeDateTime", time);
            jsonObject.put("duration", duration);
            jsonObject.put("isConflicted", false);

            FLog.d("ProfileActivity", "jsonObject" + jsonObject);
            String url = Utility.REST_URI + Utility.POST_FREE_TIME;
            TSNetworkHandler.getInstance(this).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {

                    if (response != null) {
                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mProgressDialog.dismiss();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void getUserActivities() {

        TSNetworkHandler.getInstance(this).getResponse(Utility.REST_URI + Utility.USER_ACTIVITIES + mUserId, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            JSONObject resposeJsonObject = new JSONObject(response.response);
                            FLog.d("ProfileActivity", "jsonObject" + resposeJsonObject);
                            JSONArray userActivityJsonArray = resposeJsonObject.getJSONArray("userActivityArray");

                            for (int i = 0; i < userActivityJsonArray.length(); i++) {
                                JSONObject userActivityJsonObject = userActivityJsonArray.getJSONObject(i);
                                Activities activities = new Activities();
                                activities.setDate(userActivityJsonObject.getString("date"));

                                if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.STATUS_TYPE.toString())) {
                                    activities.setStatusMessage(userActivityJsonObject.getString("status"));
                                    activities.setDate(userActivityJsonObject.getString("date"));
                                    activities.setType(ActivityType.valueOf(ActivityType.STATUS_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.MEETING_TYPE.toString())) {
                                    activities.setMeetingMessage(userActivityJsonObject.getString("meetingMessage"));
                                    activities.setMeetingId(userActivityJsonObject.getLong("meetingId"));
                                    activities.setType(ActivityType.valueOf(ActivityType.MEETING_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.PROFILE_TYPE.toString())) {
                                    activities.setProfileImageId(userActivityJsonObject.getString("profileImageId"));
                                    activities.setType(ActivityType.valueOf(ActivityType.PROFILE_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.COVER_TYPE.toString())) {
                                    activities.setCoverImageId(userActivityJsonObject.getString("coverImageId"));
                                    activities.setType(ActivityType.valueOf(ActivityType.COVER_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.UPLOAD_TYPE.toString())) {
                                    activities.setImageCaption(userActivityJsonObject.getString("imageDescription"));
                                    activities.setUploadedImageId(userActivityJsonObject.getString("imageId"));
                                    activities.setType(ActivityType.valueOf(ActivityType.UPLOAD_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.LOCATION_TYPE.toString())) {
                                    activities.setLocationAddress(userActivityJsonObject.getString("address"));
                                    activities.setType(ActivityType.valueOf(ActivityType.LOCATION_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.FREE_TIME_TYPE.toString())) {
                                    activities.setFreeTimeDate(userActivityJsonObject.getString("freeDate"));
                                    activities.setFreeTimeFromTime(userActivityJsonObject.getString("freeFromTime"));
                                    activities.setFreeTimeToTime(userActivityJsonObject.getString("freeToTime"));
                                    activities.setType(ActivityType.valueOf(ActivityType.FREE_TIME_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.JOIN_DATE_TYPE.toString())) {
                                    activities.setJoinedDate(userActivityJsonObject.getString("registrationDate"));
                                    activities.setType(ActivityType.valueOf(ActivityType.JOIN_DATE_TYPE.toString()).ordinal());
                                }
                                mActivitiesList.add(activities);
                            }

                            FLog.d("ProfileActivity", "mActivitiesList" + mActivitiesList);

                            mUserActivitiesAdapter = new UserActivitiesAdapter(ProfileActivity.this, mActivitiesList, mMeetingDetailsListener, mViewImageListener);
                            mActivitiesRecyclerView.setAdapter(mUserActivitiesAdapter);

                            if (resposeJsonObject.getBoolean("isNextActivityExist")) {
                                mViewMoreButton.setVisibility(View.VISIBLE);
                                mActivityOffSetValue++;
                            } else {
                                mViewMoreButton.setVisibility(View.GONE);
                            }

                            if (mSwipeRefreshLayout.isRefreshing()) {
                                mSwipeRefreshLayout.setRefreshing(false);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void getMoreActivities() {
        TSNetworkHandler.getInstance(this).getResponse(Utility.REST_URI + Utility.USER_ACTIVITIES + mUserId + "/" + mActivityOffSetValue, new HashMap<String, String>(), TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
            @Override
            public void handleResponse(TSNetworkHandler.TSResponse response) {
                if (response != null) {
                    if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                        try {
                            JSONObject resposeJsonObject = new JSONObject(response.response);
                            FLog.d("ProfileActivity", "jsonObject" + resposeJsonObject);
                            JSONArray userActivityJsonArray = resposeJsonObject.getJSONArray("userActivityArray");
                            List<Activities> activitiesList = new ArrayList<Activities>();

                            for (int i = 0; i < userActivityJsonArray.length(); i++) {
                                JSONObject userActivityJsonObject = userActivityJsonArray.getJSONObject(i);
                                Activities activities = new Activities();
                                activities.setDate(userActivityJsonObject.getString("date"));

                                if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.STATUS_TYPE.toString())) {
                                    activities.setStatusMessage(userActivityJsonObject.getString("status"));
                                    activities.setDate(userActivityJsonObject.getString("date"));
                                    activities.setType(ActivityType.valueOf(ActivityType.STATUS_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.MEETING_TYPE.toString())) {
                                    activities.setMeetingMessage(userActivityJsonObject.getString("meetingMessage"));
                                    activities.setMeetingId(userActivityJsonObject.getLong("meetingId"));
                                    activities.setType(ActivityType.valueOf(ActivityType.MEETING_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.PROFILE_TYPE.toString())) {
                                    activities.setProfileImageId(userActivityJsonObject.getString("profileImageId"));
                                    activities.setType(ActivityType.valueOf(ActivityType.PROFILE_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.COVER_TYPE.toString())) {
                                    activities.setCoverImageId(userActivityJsonObject.getString("coverImageId"));
                                    activities.setType(ActivityType.valueOf(ActivityType.COVER_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.UPLOAD_TYPE.toString())) {
                                    activities.setImageCaption(userActivityJsonObject.getString("imageDescription"));
                                    activities.setUploadedImageId(userActivityJsonObject.getString("imageId"));
                                    activities.setType(ActivityType.valueOf(ActivityType.UPLOAD_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.LOCATION_TYPE.toString())) {
                                    activities.setLocationAddress(userActivityJsonObject.getString("address"));
                                    activities.setType(ActivityType.valueOf(ActivityType.LOCATION_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.FREE_TIME_TYPE.toString())) {
                                    activities.setFreeTimeDate(userActivityJsonObject.getString("freeDate"));
                                    activities.setFreeTimeFromTime(userActivityJsonObject.getString("freeFromTime"));
                                    activities.setFreeTimeToTime(userActivityJsonObject.getString("freeToTime"));
                                    activities.setType(ActivityType.valueOf(ActivityType.FREE_TIME_TYPE.toString()).ordinal());
                                } else if (userActivityJsonObject.getString("type").equalsIgnoreCase(ActivityType.JOIN_DATE_TYPE.toString())) {
                                    activities.setJoinedDate(userActivityJsonObject.getString("registrationDate"));
                                    activities.setType(ActivityType.valueOf(ActivityType.JOIN_DATE_TYPE.toString()).ordinal());
                                }
                                activitiesList.add(activities);
                            }

                            FLog.d("ProfileActivity", "MORE=====activitiesList" + activitiesList);
                            mUserActivitiesAdapter.setMoreActivities(activitiesList);

                            if (resposeJsonObject.getBoolean("isNextActivityExist")) {
                                mViewMoreButton.setVisibility(View.VISIBLE);
                                mActivityOffSetValue++;
                            } else {
                                mViewMoreButton.setVisibility(View.GONE);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                        Toast.makeText(ProfileActivity.this, response.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void showMeetingDetails(Long meetingId) {
        Intent intent = new Intent(ProfileActivity.this, MeetingDetailsActivity.class);
        intent.putExtra("meetingId", meetingId);
        startActivity(intent);
    }

    @Override
    public void viewImage(String imageId) {
        ViewImageDialogFragment viewImageDialogFragment = new ViewImageDialogFragment();
        viewImageDialogFragment.setImageId(imageId);
        viewImageDialogFragment.show(mFragmentManager, "ViewImageDialogFragment");
    }

    @Override
    public void onRefresh() {
        getUserActivities();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_EXTERNAL_STORAGE: {
            }
        }
    }

}
