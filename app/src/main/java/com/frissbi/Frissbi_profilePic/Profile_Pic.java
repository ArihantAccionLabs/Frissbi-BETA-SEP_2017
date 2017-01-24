package com.frissbi.Frissbi_profilePic;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.Frissbi_img_crop.CropImage;
import com.frissbi.R;
import com.frissbi.Utility.UserRegistration;


public class Profile_Pic extends Activity {

    public static final String TAG = "Profile_Pic";

    public static final String TEMP_PHOTO_FILE_NAME = "temp_photo.jpg";

    public static final int REQUEST_CODE_GALLERY = 0x1;
    public static final int REQUEST_CODE_TAKE_PICTURE = 0x2;
    public static final int REQUEST_CODE_CROP_IMAGE = 0x3;

    private ImageView mImageView;
    Bitmap bitmap;
    private File mFileTemp;
    String imageEncoded = "";
    ProgressDialog pDialog;
    String add_frd;
    String jsonStr;
    TextView pic;
    ImageButton editbutton;

    SharedPreferences preferences, profilepic;
    SharedPreferences.Editor editor;
    SharedPreferences.Editor profile_pic_editor, profile_pic_editor1;

    byte[] b;
    Button send;
    Dialog dialogp;
    LinearLayout gal, cam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
        setContentView(R.layout.profice_pic);
        send = (Button) findViewById(R.id.done);
        mImageView = (ImageView) findViewById(R.id.image);
        editbutton = (ImageButton) findViewById(R.id.editbutton);
        preferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        editor = preferences.edit();
        Friss_Pojo.UseridFrom = preferences.getString("USERID_FROM", "editor");
        Friss_Pojo.UserNameFrom = preferences.getString("USERNAME_FROM", "editor");
        //Friss_Pojo.UseridFrom= userid;
        // Friss_Pojo.UserNameFrom=user_name;
        Log.d("value is", Friss_Pojo.UseridFrom.toString());
        editor.commit();


        profilepic = getSharedPreferences("PROFILE_PIC", Context.MODE_PRIVATE);
        profile_pic_editor1 = profilepic.edit();
        String img = profilepic.getString("IMG", "editor");
        profile_pic_editor1.commit();


        byte[] encodeByte = android.util.Base64.decode(img, android.util.Base64.DEFAULT);
        final Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        mImageView.setImageBitmap(bitmap);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new upload1().execute();
               /* if(!(imageEncoded==null)) {
                    new upload1().execute();
                }else {
                    Toast.makeText(getApplicationContext(), "Select Image From Gallery", Toast.LENGTH_LONG).show();
                }*/

            }
        });

        editbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogp = new Dialog(Profile_Pic.this);
                // Include dialog.xml file
                dialogp.getWindow();
                dialogp.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogp.setContentView(R.layout.profilepic_dailog);

                gal = (LinearLayout) dialogp.findViewById(R.id.gal);

                cam = (LinearLayout) dialogp.findViewById(R.id.cam);
                dialogp.show();
                gal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openGallery();
                        dialogp.dismiss();
                    }
                });

                cam.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        takePicture();
                        dialogp.dismiss();
                    }
                });


                /*findViewById(R.id.gallery).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        openGallery();
                    }
                });

                findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        takePicture();
                    }
                });*/
            }
        });
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mFileTemp = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
        } else {
            mFileTemp = new File(getFilesDir(), TEMP_PHOTO_FILE_NAME);
        }

    }

    private void takePicture() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            Uri mImageCaptureUri = null;
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                mImageCaptureUri = Uri.fromFile(mFileTemp);
            }

            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
        } catch (ActivityNotFoundException e) {

            Log.d(TAG, "cannot take picture", e);
        }
    }

    private void openGallery() {

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_CODE_GALLERY);
    }

    private void startCropImage() {

        Intent intent = new Intent(Profile_Pic.this, CropImage.class);
        intent.putExtra(CropImage.IMAGE_PATH, mFileTemp.getPath());
        intent.putExtra(CropImage.SCALE, true);
        intent.putExtra(CropImage.ASPECT_X, 4);
        intent.putExtra(CropImage.ASPECT_Y, 4);
        startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {

            return;
        }
        switch (requestCode) {

            case REQUEST_CODE_GALLERY:

                try {

                    InputStream inputStream = getContentResolver().openInputStream(data.getData());
                    FileOutputStream fileOutputStream = new FileOutputStream(mFileTemp);
                    copyStream(inputStream, fileOutputStream);
                    fileOutputStream.close();
                    inputStream.close();

                    startCropImage();

                } catch (Exception e) {

                    Log.e(TAG, "Error while creating temp file", e);
                }

                break;
            case REQUEST_CODE_TAKE_PICTURE:

                startCropImage();
                break;
            case REQUEST_CODE_CROP_IMAGE:

                String path = data.getStringExtra(CropImage.IMAGE_PATH);
                if (path == null) {

                    return;
                }

                //  bitmap = BitmapFactory.decodeFile(mFileTemp.getPath());


                bitmap = decodeFile(mFileTemp);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);

                mImageView.setImageBitmap(bitmap);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public static void copyStream(InputStream input, OutputStream output)
            throws IOException {

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    public class upload1 extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            dialogp = new Dialog(Profile_Pic.this);
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

        @Override
        protected String doInBackground(String... params) {

            try {


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);
                imageEncoded = new String();
                imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.NO_PADDING | Base64.CRLF);
                Log.d("Encoded", imageEncoded);
                Log.d("compress", "AFTER. Height: " + bitmap.getHeight() + " Width: " + bitmap.getWidth());
                Log.d("imageeee", bitmap.toString());

                profilepic = getSharedPreferences("PROFILE_PIC", Context.MODE_PRIVATE);
                profile_pic_editor = profilepic.edit();

                profile_pic_editor.putString("IMG", imageEncoded);

                profile_pic_editor.commit();


                UserRegistration image = new UserRegistration();
                int userid = Integer.parseInt(Friss_Pojo.UseridFrom.toString());
                jsonStr = image.insertImage(userid, imageEncoded);

                Log.d("Response: ", "> " + jsonStr);
                Log.d("valus......", jsonStr);
                return jsonStr;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String responseBody) {
            super.onPostExecute(responseBody);
            dialogp.dismiss();
            if (jsonStr.equals("0")) {
                Toast.makeText(getApplicationContext(), "Image uploaded", Toast.LENGTH_LONG).show();

            } else if (jsonStr.equals("1")) {
                Toast.makeText(getApplicationContext(), "Image not saved", Toast.LENGTH_LONG).show();


            }
        }

    }


    public Bitmap decodeFile(File f) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            final int REQUIRED_SIZE = 25;

            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


}
