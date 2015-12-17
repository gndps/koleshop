package com.koleshop.appkoleshop.common.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.services.CommonIntentService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Gundeep on 06/12/15.
 */
public class ImageUtils {

    public static Bitmap getResizedBitmap(int targetW, int targetH, String imagePath) {

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        //inJustDecodeBounds = true <-- will not load the bitmap into memory
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        return (bitmap);
    }

    public static void uploadBitmap(Context context, String filepath, String filename, String varietyTag) {

        Intent uploadIntent = new Intent(context, CommonIntentService.class);
        uploadIntent.setAction(Constants.ACTION_UPLOAD_IMAGE);
        uploadIntent.putExtra("filepath", filepath);
        uploadIntent.putExtra("filename", filename);
        uploadIntent.putExtra("tag", varietyTag);
        context.startService(uploadIntent);

    }

    public static byte[] getByteArrayFromBitmap(Bitmap bitmap, int quality) {
        try {
            //Convert to byte array
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
            byte[] byteArray = stream.toByteArray();
            return byteArray;
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] getImageByteArrayForUpload(String imagePath) {
        Bitmap bm = getResizedBitmap(Constants.IMAGE_UPLOAD_DIMENSIONS, Constants.IMAGE_UPLOAD_DIMENSIONS, imagePath);
        byte[] ba = getByteArrayFromBitmap(bm, 90);
        return ba;
    }

    public static Bitmap getBitmapFromByteArray(byte[] byteArray) {
        try {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    public static void saveBitmap(final Context mContext, final Bitmap bitmap, final String filename) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                //create image file
                File storageDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                File image = null;
                try {
                    image = File.createTempFile(
                            filename,  /* prefix */
                            ".jpg",         /* suffix */
                            storageDir      /* directory */
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String imagepath = image.getAbsolutePath();

                //write the bitmap to storage
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(imagepath);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                    // PNG is a lossless format, the compression factor (100) is ignored
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //add picture to gallery
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File f = new File(imagepath);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                mContext.sendBroadcast(mediaScanIntent);

                return null;

            }
        }.execute(null, null, null);
    }

}
