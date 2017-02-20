package com.frissbi.Gmail;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.frissbi.Frissbi_Friends.FriendSerching;
import com.frissbi.Frissbi_Pojo.Friss_Pojo;
import com.frissbi.R;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.ServiceHandler;
import com.frissbi.Utility.UserRegistration;
import com.frissbi.activities.HomeActivity;
import com.frissbi.frissbi.Login;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author manish
 */
public class ProfileGetActivity extends Activity {
    ImageView imageProfile;
    TextView textViewName, textViewEmail, textViewGender, textViewBirthday;
    String textName, textEmail, textGender, textBirthday, userImageUrl;
    String jsonStr;
    String Username;
    SharedPreferences preferences1, preferences;
    SharedPreferences.Editor editor_gmail, editor_regid;
    SharedPreferences.Editor editor;
    String FirstName, lastName;
    String regid = Login.REG_ID;
    String imageEncoded;
    String Usre_ID;
    private ProgressDialog pDialog;
    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;

    Dialog dialogp;
    private String pictureImagePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
      /*  imageProfile = (ImageView) findViewById(R.id.imageView1);
        textViewName = (TextView) findViewById(R.id.textViewNameValue);
        textViewEmail = (TextView) findViewById(R.id.textViewEmailValue);
        textViewGender = (TextView) findViewById(R.id.textViewGenderValue);
        textViewBirthday = (TextView) findViewById(R.id.textViewBirthdayValue);*/
        cd = new ConnectionDetector(getApplicationContext());
        Intent intent = getIntent();
        textEmail = intent.getStringExtra("email_id");
        preferences1 = getSharedPreferences("GMAIL_REG", Context.MODE_PRIVATE);
        editor_gmail = preferences1.edit();
        editor_gmail.putString("mail", textEmail);


        //StringTokenizer stringTokenizer = new StringTokenizer(textEmail,"@");
        textEmail.split("@");
        String[] Tokens = textEmail.split("@");
        Username = Tokens[0].trim();
        Friss_Pojo.UserNameFrom = Username.toString();

        preferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString("USERNAME_FROM", Username.toString());


        Log.d("UserName", Username.toString());
        System.out.println(textEmail);
        // textViewEmail.setText(textEmail);

        /**
         * get user data from google account
         */

        try {
            System.out.println("On Home Page***" + AbstractGetNameTask.GOOGLE_USER_DATA);
            JSONObject profileData = new JSONObject(AbstractGetNameTask.GOOGLE_USER_DATA);
            Log.d("ProfileGetActivity", "profileData" + profileData);
            if (profileData.has("picture")) {
                userImageUrl = profileData.getString("picture");
                new GetImageFromUrl().execute(userImageUrl);
            }
            if (profileData.has("name")) {
                textName = profileData.getString("name");
                // textViewName.setText(textName);
            }
            if (profileData.has("gender")) {
                textGender = profileData.getString("gender");
                // textViewGender.setText(textGender);
            }
            if (profileData.has("birthday")) {
                textBirthday = profileData.getString("birthday");

                // textViewBirthday.setText(textBirthday);
            }
            if (profileData.has("given_name")) {
                FirstName = profileData.getString("given_name");
                //textViewBirthday.setText(textBirthday);
                editor_gmail.putString("FirstName", FirstName);

                Log.d("FirstName", FirstName);
                //FirstName=
            }
            if (profileData.has("family_name")) {
                lastName = profileData.getString("family_name");
                //textViewBirthday.setText(textBirthday);
                editor_gmail.putString("lastName", lastName);
                editor_gmail.commit();
                Log.d("lastName", lastName);
                //FirstName=
            }


        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public class GetImageFromUrl extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap map = null;
            for (String url : urls) {
                map = downloadImage(url);
            }
            return map;
        }

        // Sets the Bitmap returned by doInBackground
        @Override
        protected void onPostExecute(Bitmap result) {
            // imageProfile.setImageBitmap(result);

            String fileName = saveToInternalStorage(result);
            Log.d("fileName-------", fileName);
            Bitmap bitmap = decodeFile(new File(fileName));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);
            imageEncoded = new String();
            imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.NO_PADDING | Base64.CRLF);
            // imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            Log.d("Encoded", "length" + imageEncoded.length());
            Log.d("compress", "AFTER. Height: " + result.getHeight() + " Width: " + result.getWidth());
            Log.d("imageeee", result.toString());

            new UsernameExist().execute();
        }

        // Creates Bitmap from InputStream and returns it
        private Bitmap downloadImage(String src) {
           /* Bitmap bitmap = null;
            InputStream stream = null;
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 1;

            try {
                stream = getHttpConnection(url);
                bitmap = BitmapFactory.decodeStream(stream, null, bmOptions);
                stream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return bitmap;*/


            try {
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                // Log exception
                return null;
            }


        }

        // Makes HttpURLConnection and returns InputStream
        private InputStream getHttpConnection(String urlString)
                throws IOException {
            InputStream stream = null;
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();

            try {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();

                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return stream;
        }
    }

    public class UsernameExist extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            dialogp = new Dialog(ProfileGetActivity.this);
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


                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.USERNAME_EMAIL_EXIST + Username.trim() + "/" + null;
                Log.d("TAG", "url : " + url);
                ServiceHandler sh = new ServiceHandler();

                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

                Log.d("Response: ", "> " + jsonStr);
                Log.d("valus......", jsonStr.toString());

                return jsonStr.toString();


            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String ifExists) {
            super.onPostExecute(ifExists);
            dialogp.dismiss();

            if (jsonStr == null) {
                // Toast.makeText(getApplication(), "Server not responding", Toast.LENGTH_SHORT).show();
            } else if (jsonStr.equals("1")) {

                new UserIdGet().execute();
            } else {

                new EmailExist().execute();
            }


        }
    }

    public class UserIdGet extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialogp = new Dialog(ProfileGetActivity.this);
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

                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.User_Id_Get + Username.trim();


                Log.d("Url: ", "> " + url);
                ServiceHandler sh = new ServiceHandler();

                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

                Log.d("Response: ", "> " + jsonStr);
                Log.d("valus......", jsonStr.toString());

                return jsonStr.toString();


            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String ifExists) {
            super.onPostExecute(ifExists);
            dialogp.dismiss();

            if (jsonStr == null) {
                // Toast.makeText(getApplication(), "Server not responding", Toast.LENGTH_SHORT).show();
            } else if (!jsonStr.equals("")) {
                Log.d("UserId", jsonStr);
                Friss_Pojo.UseridFrom = jsonStr.toString();

                editor.putString("USERID_FROM", jsonStr);
                editor.commit();
                new UserAthenticat().execute();
            } else {


            }


        }
    }

    public class EmailExist extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogp = new Dialog(ProfileGetActivity.this);
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

                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.USERNAME_EMAIL_EXIST + null + "/" + textEmail;

                ServiceHandler sh = new ServiceHandler();

                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

                Log.d("Response: ", "> " + jsonStr);
                Log.d("valus......", jsonStr.toString());

                return jsonStr.toString();


            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String ifExists) {
            super.onPostExecute(ifExists);
            dialogp.dismiss();

            if (jsonStr == null) {
                // Toast.makeText(getApplication(), "Server not responding", Toast.LENGTH_SHORT).show();
            } else if (jsonStr.equals("1")) {

                Toast.makeText(getApplication(), "E-mail id Already Exists. Please use another one.", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);


            } else {

                new User().execute();
            }


        }
    }

    public class User extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogp = new Dialog(ProfileGetActivity.this);
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
        public String doInBackground(String... strings) {


            try {

                //lastName=lastName.replace(" ","%20");

                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.USER_REGISTRATION + Username + "/" + null + "/"
                        + textEmail + "/" + null + "/" + "2005-5-8" + "/" + FirstName + "/" + lastName + "/" + 1;
                url = url.replace(" ", "%20");
                Log.d("ProfileGetActivity", "Registration--URL" + url);

                ServiceHandler sh = new ServiceHandler();

                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);


                Log.d("Response: ", "> " + jsonStr);

                //JSONObject json = new JSONObject(jsonStr);


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

            if (jsonStr == null) {
                //Toast.makeText(getApplication(), "Server not responding", Toast.LENGTH_SHORT).show();
            } else if (jsonStr.length() > 0) {
                Toast.makeText(getApplication(), "We're almost done", Toast.LENGTH_LONG).show();
                Usre_ID = jsonStr.toString();
                Log.d("ProfileGetActivity", "jsonStr--UserId" + Usre_ID);
                Friss_Pojo.UseridFrom = jsonStr.toString();
                editor.putString("USERID_FROM", Usre_ID);
                editor.commit();
                new UserAthenticat().execute();
            } else {
                // Toast.makeText(getApplication(), "User Not Registrations", Toast.LENGTH_LONG).show();
            }

            dialogp.dismiss();

            // int userid=userID;



        }
    }

    public class UserAthenticat extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogp = new Dialog(ProfileGetActivity.this);
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

                String url = Friss_Pojo.REST_URI + "/" + "rest" + Friss_Pojo.USER_AUTHENTICATIONUSER + Friss_Pojo.UseridFrom + "/" + Friss_Pojo.REG_ID;
                ServiceHandler sh = new ServiceHandler();
                Log.d("Url: ", "> " + url);
                // Making a1 request to url and getting response
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
                Log.d("Response", jsonStr);

                return jsonStr;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String S) {
            super.onPostExecute(S);
            dialogp.dismiss();

            if (jsonStr == null) {
                // Toast.makeText(getApplication(), "Server not responding", Toast.LENGTH_SHORT).show();
            } else if (jsonStr.equals("1")) {
                Toast.makeText(getApplication(), "Yayyy!!! We're In", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
               // new upload1().execute();

            } else {
                //loginErrorMsg.setText("Username and Password incorrect");
            }


        }

        public class upload1 extends AsyncTask<String, String, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialogp = new Dialog(ProfileGetActivity.this);
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


				/*ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);
				imageEncoded=new String();
				imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.NO_PADDING | Base64.CRLF);
				Log.d("Encoded", imageEncoded);
				Log.d("compress", "AFTER. Height: " + bitmap.getHeight() + " Width: " + bitmap.getWidth());
				Log.d("imageeee", bitmap.toString());

				profilepic = getSharedPreferences("PROFILE_PIC", Context.MODE_PRIVATE);
				profile_pic_editor = profilepic.edit();

				profile_pic_editor.putString("IMG", imageEncoded);

				profile_pic_editor.commit();*/


                    UserRegistration image = new UserRegistration();
                    int userid = Integer.parseInt(Friss_Pojo.UseridFrom);
                    Log.d("Profile", "imageEncoded--length" + imageEncoded.length());
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

                if (jsonStr == null) {
                    // Toast.makeText(getApplication(), "Server not responding", Toast.LENGTH_SHORT).show();
                } else if (jsonStr.equals("0")) {
                    //  Toast.makeText(getApplicationContext(), "UploadedPic", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);

                } else if (jsonStr.equals("1")) {
                    // Toast.makeText(getApplicationContext(), "Not UploadedPic", Toast.LENGTH_LONG).show();


                }
            }

        }

    }

    @Override
    public void onBackPressed() {

    }

    private String saveToInternalStorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        // File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        String imageFileName = System.currentTimeMillis() / 1000 + ".jpg";

        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        pictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
        File mypath = new File(pictureImagePath);


        //  File mypath = new File(directory, "profile.png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return pictureImagePath;
    }


    public Bitmap decodeFile(File f) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            final int REQUIRED_SIZE = 30;

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

