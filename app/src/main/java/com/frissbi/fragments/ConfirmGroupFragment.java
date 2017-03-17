package com.frissbi.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.CursorLoader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.frissbi.R;
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.Utility;
import com.frissbi.activities.CreateGroupActivity;
import com.frissbi.adapters.GroupParticipantAdapter;
import com.frissbi.interfaces.UploadPhotoListener;
import com.frissbi.models.Friend;
import com.frissbi.models.FrissbiContact;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.frissbi.Utility.Utility.CAMERA_REQUEST;
import static com.frissbi.Utility.Utility.SELECT_FILE;


public class ConfirmGroupFragment extends Fragment implements UploadPhotoListener {
    private OnFragmentInteractionListener mListener;
    private List<FrissbiContact> mSelectedFriendList;
    private RecyclerView mSelectedParticipantRecyclerView;
    private EditText mGroupNameEditText;
    private ImageView mGroupIcon;
    private GroupParticipantAdapter mGroupParticipantAdapter;
    private byte[] mImageByteArray;
    private String mPictureImagePath;
    private UploadPhotoListener mUploadPhotoListener;

    public ConfirmGroupFragment() {
        // Required empty public constructor
    }

    public void setSelectedFriendList(List<FrissbiContact> groupSelectedFriendList) {
        mSelectedFriendList = groupSelectedFriendList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_confirm_group, container, false);
        mUploadPhotoListener=(UploadPhotoListener)this;
        setUpViews(view);
        return view;
    }

    private void setUpViews(View view) {
        mSelectedParticipantRecyclerView = (RecyclerView) view.findViewById(R.id.selected_participant_recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 4);
        mSelectedParticipantRecyclerView.setLayoutManager(gridLayoutManager);
        mSelectedParticipantRecyclerView.setHasFixedSize(true);
        mGroupNameEditText = (EditText) view.findViewById(R.id.group_name_et);
        mGroupIcon = (ImageView) view.findViewById(R.id.group_icon);
        setUpSelectedParticipantsList();
        Button confirmGroupButton = (Button) view.findViewById(R.id.confirm_group_button);
        confirmGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGroupNameEditText.getText().toString().trim().length() > 0) {
                    sendGroupDetailsToServer();
                } else {
                    Toast.makeText(getActivity(), "Please enter group name", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mGroupIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showDialogForAttachment();
                UploadPhotoDialogFragment uploadPhotoDialogFragment = new UploadPhotoDialogFragment();
                uploadPhotoDialogFragment.setUploadPhotoListener(mUploadPhotoListener, Utility.PROFILE_IMAGE);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                uploadPhotoDialogFragment.show(fragmentManager,"UploadPhotoDialogFragment");
            }
        });
    }

    private void sendGroupDetailsToServer() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", SharedPreferenceHandler.getInstance(getActivity()).getUserId());
            jsonObject.put("groupName", mGroupNameEditText.getText().toString());
            if (mImageByteArray != null) {
                jsonObject.put("groupImage", Base64.encodeToString(mImageByteArray, Base64.DEFAULT));
            }
            JSONArray participantsArray = new JSONArray();
            for (int i = 0; i < mSelectedFriendList.size(); i++) {
                participantsArray.put(mSelectedFriendList.get(i).getUserId());
            }
            jsonObject.put("friendList", participantsArray);
            String url = Utility.REST_URI + Utility.CREATE_GROUP;
            TSNetworkHandler.getInstance(getActivity()).getResponse(url, jsonObject, new TSNetworkHandler.ResponseHandler() {
                @Override
                public void handleResponse(TSNetworkHandler.TSResponse response) {
                    if (response != null) {

                        if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                            try {
                                JSONObject responseJsonObject = new JSONObject(response.response);
                                if (responseJsonObject.getBoolean("groupCreated")) {
                                    Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getActivity(), CreateGroupActivity.class);
                                    startActivity(intent);
                                    getActivity().finish();
                                } else {
                                    Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                            Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void setUpSelectedParticipantsList() {
        mGroupParticipantAdapter = new GroupParticipantAdapter(getActivity(), mSelectedFriendList);
        mSelectedParticipantRecyclerView.setAdapter(mGroupParticipantAdapter);
    }


    @Override
    public void captureImage(int typeOfImage) {
        String imageFileName = System.currentTimeMillis() / 1000 + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        mPictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
        File file = new File(mPictureImagePath);
        Uri outputFileUri = Uri.fromFile(file);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        FLog.d("ConfirmGroupFragment", "CAMERA_REQUEST" + CAMERA_REQUEST);
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
            Bitmap bitmap = Utility.getInstance().decodeFile(imgFile);
            mGroupIcon.setImageBitmap(bitmap);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            mImageByteArray = baos.toByteArray();

        } else if (requestCode == SELECT_FILE && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            String[] projection = {MediaStore.MediaColumns.DATA};
            CursorLoader cursorLoader = new CursorLoader(getActivity(), selectedImageUri, projection, null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            String selectedImagePath = cursor.getString(column_index);
            File imgFile = new File(selectedImagePath);
            Bitmap bitmap = Utility.getInstance().decodeFile(imgFile);
            mGroupIcon.setImageBitmap(bitmap);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 5, baos);
            mImageByteArray = baos.toByteArray();

        }
    }
}
