package com.frissbi.Utility;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import com.frissbi.R;

/**
 * Created by thrymr on 2/1/17.
 */

public class CustomProgressDialog extends ProgressDialog {
    public CustomProgressDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_progress_dialog);
    }


    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }


}
