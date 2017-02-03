package com.frissbi.frissbi;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.*;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.Frissbi_Pojo.Register_pojo;
import com.frissbi.Frissbi_img_crop.CropImage;
import com.frissbi.R;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.RoundedImageView;
import com.frissbi.Utility.ServiceHandler;
import com.frissbi.Utility.UserRegistration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

public class Register extends Activity {




    public static final String TAG = "Profile_Pic";

    public static final String TEMP_PHOTO_FILE_NAME = "temp_photo.jpg";

    public static final int REQUEST_CODE_GALLERY      = 0x1;
    public static final int REQUEST_CODE_TAKE_PICTURE = 0x2;
    public static final int REQUEST_CODE_CROP_IMAGE   = 0x3;
    private File mFileTemp;
    Bitmap bitmap;
    String imageEncoded="";
    SharedPreferences profilepic;

    SharedPreferences.Editor profile_pic_editor;
    RoundedImageView roundedImageView;

    ImageButton pic;
    EditText inputPhone, inputdob;
    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;
    Dialog dialogp;

    /*End Layout Items*/
    TextView next,terms,privacy;
    String valid_phone = null;
    String jsonStr;
    CheckBox chekpriv;
    /*Start Layout Items*/
    private Calendar cal;
    private int day;
    private int month;
    private int year;
    private ProgressDialog pDialog;
    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            inputdob.setText(selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay);


        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login3);

		/*
          Start Defining All Layout Items
		 */

        inputPhone = (EditText) findViewById(R.id.phone);
        inputdob = (EditText) findViewById(R.id.dob);
        next = (TextView) findViewById(R.id.next);
        pic=(ImageButton)findViewById(R.id.pic);
        roundedImageView=(RoundedImageView)findViewById(R.id.roundpic);
        chekpriv= (CheckBox) findViewById(R.id.chekpriv);

        privacy = (TextView) findViewById(R.id.privacy);
        terms = (TextView) findViewById(R.id.terms);
        privacy.setPaintFlags(privacy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        terms.setPaintFlags(terms.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        privacy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Privacypolicy.class);
                startActivity(intent);
            }
        });
        terms.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),GetTermsandConditions.class);
                startActivity(intent);
            }
        });


        //btnRegister = (Button) findViewById(R.id.register);
        cd = new ConnectionDetector(getApplicationContext());
        next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isInternetPresent = cd.isConnectedToInternet();
                // check for Internet status
                if (isInternetPresent) {
                    if ((!inputdob.getText().toString().equals("")) && (!inputPhone.getText().toString().equals(""))) {
                        new User().execute();

                    } else {
                        Toast.makeText(getApplicationContext(),
                                "One or more fields are empty", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Toast.makeText(getApplicationContext(),"You don't have an internet connection", Toast.LENGTH_SHORT).show();

                }




            }
        });


        pic.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              dialogp = new Dialog(Register.this);
                                              // Include dialog.xml file
                                              dialogp.getWindow();
                                              dialogp.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                              dialogp.setContentView(R.layout.profilepic_dailog);

                                              LinearLayout gal = (LinearLayout) dialogp.findViewById(R.id.gal);

                                              LinearLayout cam = (LinearLayout) dialogp.findViewById(R.id.cam);
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

                                          }
                                      });






        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mFileTemp = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
        }
        else {
            mFileTemp = new File(getFilesDir(), TEMP_PHOTO_FILE_NAME);
        }




        // read = (TextView) findViewById(R.id.read);
        // chek = (CheckBox) findViewById(R.id.chek);
        cal = Calendar.getInstance();
        day = cal.get(Calendar.DAY_OF_MONTH);
        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);

        inputdob.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(0);
            }
        });

       /* read.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // Create custom dialog object
                final Dialog dialog = new Dialog(Register.this);
                // Include dialog.xml file
                dialog.getWindow();
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.terms);
                // Set dialog title
                //dialog.setTitle("Custom Dialog");
                // set values for custom dialog components - text, image and button
                Button cls = (Button) dialog.findViewById(R.id.cls);
                dialog.show();
                cls.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Close dialog
                        dialog.dismiss();
                    }
                });

            }
        });*/
        /* end Terms and conditions*/

        //**** start Users accepts reading the terms and conditions.*******//

       chekpriv.setOnClickListener(new OnClickListener() {
            @Override

            public void onClick(View v) {
              if (((CheckBox) v).isChecked()) {
                next.setVisibility(View.VISIBLE);

                } else {
                    next.setVisibility(View.INVISIBLE);
                }


            }

        });
        //**** end Users accepts reading the terms and conditions.*******//


        //***** Start Validations for Phone ******//
        inputPhone.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub


            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

                Is_Valid_Phone(inputPhone);


            }
        });
    }

    public void Is_Valid_Phone(EditText edt) {
        if (edt.getText().toString() == null) {
            edt.setError("Invalid Phone Number");
            valid_phone = null;
        } else if (isValidPhoneNumber(edt.getText().toString()) == false) {
            edt.setError("Invalid Phone Number");
            valid_phone = null;
        } else {
            valid_phone = edt.getText().toString();
        }
    }

    boolean isValidPhoneNumber(CharSequence phone) {
        if (phone.length() != 10) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(phone).matches();
        }
    }

    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        return new DatePickerDialog(this, datePickerListener, year, month, day);
    }



    public class User extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
          dialogp = new Dialog(Register.this);
            // Include dialog.xml file
            dialogp.getWindow();
            dialogp.setCancelable(false);
            dialogp.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogp.setContentView(R.layout.dailog_box);
             ImageView imgView = (ImageView)dialogp.findViewById(R.id.animationImage);
            imgView.setVisibility(ImageView.VISIBLE);
            imgView.setBackgroundResource(R.drawable.frame_animation);
            AnimationDrawable frameAnimation =(AnimationDrawable) imgView.getBackground();
            if (frameAnimation.isRunning()) {
                frameAnimation.stop();
            }
            else {
                frameAnimation.stop();
                frameAnimation.start();
            }
            dialogp.show();
            Register_pojo.Regi_ContactNumber = inputPhone.getText().toString();
            Register_pojo.Regi_dob = inputdob.getText().toString();
        }

        @Override
        public String doInBackground(String... strings) {


            try {
             String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.USER_REGISTRATION + Register_pojo.Regi_UserName + "/" + Register_pojo.Regi_UserPassword + "/"
                        + Register_pojo.Regi_EmailName + "/" + Register_pojo.Regi_ContactNumber + "/" + Register_pojo.Regi_dob + "/" + Register_pojo.Regi_FirstName + "/"
                        + Register_pojo.Regi_LastName + "/" + 0;
                ServiceHandler sh = new ServiceHandler();

                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
                Log.d("url: ", "> " + url);

                Log.d("Response: ", "> " + jsonStr);

                Friss_Pojo.UseridFrom=jsonStr.toString();
                Log.d("valus......", jsonStr.toString());

                 return jsonStr;

            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (jsonStr==null){
                Toast.makeText(getApplication(), "Oops... Something's not right", Toast.LENGTH_SHORT).show();
            }


           else if (!(jsonStr.equals("0"))) {


                 if (!(imageEncoded.equals("0"))) {

                    new upload1().execute();
                }else {
                    final Dialog  dialogp2 = new Dialog(Register.this);
                    // Include dialog.xml file
                    dialogp2.getWindow();
                    dialogp2.setCancelable(false);
                    dialogp2.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialogp2.setContentView(R.layout.registerdailog_box);
                    TextView text= (TextView) dialogp2.findViewById(R.id.msg);
                    // ImageButton img= (ImageButton) dialogp2.findViewById(R.id.img);


                    text.setText("Account Registered Successfully. \n" +
                            "Please check your inbox to Activate your account");
                    Button cls= (Button) dialogp2.findViewById(R.id.cls);
                    cls.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogp2.dismiss();
                            Intent intent = new Intent(getApplicationContext(), Login.class);
                            startActivity(intent);

                        }
                    });
                    dialogp2.show();
                    dialogp2.setOnKeyListener(new Dialog.OnKeyListener() {

                        @Override
                        public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                            // TODO Auto-generated method stub
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                dialogp2.dismiss();
                                Intent intent = new Intent(getApplicationContext(), Login.class);
                                startActivity(intent);

                            }
                            return true;
                        }
                    });
                }
            } else {
                Toast.makeText(getApplication(), "Account not created", Toast.LENGTH_LONG).show();
            }

            dialogp.dismiss();

            // int userid=userID;


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

        Intent intent = new Intent(Register.this, CropImage.class);
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

                bitmap = BitmapFactory.decodeFile(mFileTemp.getPath());


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);
                imageEncoded=new String();
                imageEncoded = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.NO_PADDING | android.util.Base64.CRLF);
                Log.d("Encoded", imageEncoded);
                Log.d("compress", "AFTER. Height: " + bitmap.getHeight() + " Width: " + bitmap.getWidth());
                Log.d("imageeee", bitmap.toString());
                roundedImageView.setImageBitmap(bitmap);
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
            dialogp = new Dialog(Register.this);
            // Include dialog.xml file
            dialogp.getWindow();
            dialogp.setCancelable(false);
            dialogp.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogp.setContentView(R.layout.dailog_box);
            ImageView imgView = (ImageView)dialogp.findViewById(R.id.animationImage);
            imgView.setVisibility(ImageView.VISIBLE);
            imgView.setBackgroundResource(R.drawable.frame_animation);
            AnimationDrawable frameAnimation =(AnimationDrawable) imgView.getBackground();
            if (frameAnimation.isRunning()) {
                frameAnimation.stop();
            }
            else {
                frameAnimation.stop();
                frameAnimation.start();
            }
            dialogp.show();

        }
        @Override
        protected String doInBackground(String... params) {

            try {



                profilepic = getSharedPreferences("PROFILE_PIC", Context.MODE_PRIVATE);
                profile_pic_editor = profilepic.edit();
                profile_pic_editor.putString("IMG", imageEncoded);
                profile_pic_editor.commit();

                UserRegistration image=new UserRegistration();
                int userid=Integer.parseInt(Friss_Pojo.UseridFrom.toString());
                Log.d("userid: ", "> " + userid);
                Log.d("imageEncoded: ", "> " + imageEncoded);
                jsonStr = image.insertImage(userid, imageEncoded);

                Log.d("Response: ", "> " + jsonStr);
                Log.d("valus......" , jsonStr);
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
            if (jsonStr.equals("0")){



                final Dialog  dialogp2 = new Dialog(Register.this);
                // Include dialog.xml file
                dialogp2.getWindow();
                dialogp2.setCancelable(false);
                dialogp2.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogp2.setContentView(R.layout.registerdailog_box);
                TextView text= (TextView) dialogp2.findViewById(R.id.msg);
                // ImageButton img= (ImageButton) dialogp2.findViewById(R.id.img);


                text.setText("Account Registered Successfully. \n" +
                        "Please check your inbox to Activate your account");
                Button cls= (Button) dialogp2.findViewById(R.id.cls);
                cls.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogp2.dismiss();
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        startActivity(intent);

                    }
                });
                dialogp2.show();
                dialogp2.setOnKeyListener(new Dialog.OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialogp2.dismiss();
                            Intent intent = new Intent(getApplicationContext(), Login.class);
                            startActivity(intent);

                        }
                        return true;
                    }
                });

            }

            else if (jsonStr.equals("1")){
                Toast.makeText(getApplicationContext(), "Oops.. Picture not saved", Toast.LENGTH_LONG).show();



            }
        }


    }
    @Override
    public void onBackPressed() {
 finish();



    }

}


