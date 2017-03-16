package com.frissbi.fragments;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.frissbi.R;
import com.frissbi.interfaces.UploadPhotoListener;

public class UploadPhotoDialogFragment extends DialogFragment implements View.OnClickListener {

    private UploadPhotoListener mUploadPhotoListener;
    private int mTypeOfImage;

    public void setUploadPhotoListener(UploadPhotoListener uploadPhotoListener, int typeOfImage){
        mUploadPhotoListener=uploadPhotoListener;
        mTypeOfImage=typeOfImage;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_upload_photo_dialog, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        view.findViewById(R.id.take_photo_tv).setOnClickListener(this);
        view.findViewById(R.id.choose_file_tv).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.take_photo_tv:
                dismiss();
                mUploadPhotoListener.captureImage(mTypeOfImage);
                break;
            case R.id.choose_file_tv:
                dismiss();
                mUploadPhotoListener.chooseAFile(mTypeOfImage);
                break;
        }

    }

}
