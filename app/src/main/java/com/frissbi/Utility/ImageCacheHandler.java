package com.frissbi.Utility;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;


import com.frissbi.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ImageCacheHandler {

    public static ImageCacheHandler instance;

    private Context mContext;
    private ContextWrapper contextWrapper;
    private File directory;

    private Bitmap bitmap = null;

    private ImageCacheHandler(Context context) {

        mContext = context;
        contextWrapper = new ContextWrapper(mContext);
        directory = contextWrapper.getDir("image_cache", Context.MODE_PRIVATE);

    }

    public static ImageCacheHandler getInstance(Context context) {

        if (instance == null)
            instance = new ImageCacheHandler(context);
        return instance;

    }

    public void setImage(final ImageView imageView, String userId, String imageURL) {
        final String fileName = userId;
        bitmap = findImageFromMemory(fileName);
        imageView.setImageResource(R.drawable.frissbi_logo_home);
        if (bitmap != null) {
          /*  if (CommonUtility.progressBar != null) {
                CommonUtility.progressBar.setVisibility(View.INVISIBLE);
            }*/
            imageView.setImageBitmap(bitmap);
        } else {
            new DownloadImageAsync(fileName, new DownloadCallback() {
                @Override
                public void downloadFinished(boolean status) {
                    if (status) {
                        bitmap = findImageFromMemory(fileName);

                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        }


                    }
                }
            }).execute(imageURL);

        }

    }

    /**
     * Method to save image to the memory
     *
     * @param name   name with which the image should be saved
     * @param bitmap bitmap of the image
     * @return returns true if the operation is successful
     * @throws IOException
     */
    private boolean saveImageToMemory(String name, Bitmap bitmap) throws IOException {

        FileOutputStream fileOutputStream = null;

        try {
            File image = new File(directory, name + ".jpeg");
            fileOutputStream = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fileOutputStream != null)
                fileOutputStream.close();
        }
    }


    public Bitmap findImageFromMemory(String userId) {


        FLog.d("ImageID", userId);
        try {
            File file = new File(directory, userId + ".jpeg");
            FLog.d("ImageID", file + "");
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Callback interface which need to be implemented by the calling method
     */
    private interface DownloadCallback {
        public void downloadFinished(boolean status);
    }

    /**
     * Asynctask to download the image from the specified url
     */
    private class DownloadImageAsync extends AsyncTask<String, Void, Boolean> {

        String fileName;
        DownloadCallback downloadCallback;

        public DownloadImageAsync(String fileName, DownloadCallback callback) {
            // TODO Auto-generated constructor stub
            this.fileName = fileName;
            downloadCallback = callback;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                InputStream is = new URL(params[0]).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                saveImageToMemory(fileName, bitmap);

                return true;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        @Override
        protected void onPostExecute(Boolean result) {
            downloadCallback.downloadFinished(result);
          /*  if (CommonUtility.progressBar != null && CommonUtility.progressBar.isShown()) {
                CommonUtility.progressBar.setVisibility(View.INVISIBLE);
            }*/
        }

    }

}
