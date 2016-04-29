package com.koleshop.appkoleshop.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.helpers.CircleTransform;
import com.koleshop.appkoleshop.services.CommonIntentService;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Gundeep on 06/12/15.
 */
public class ImageUtils {

    public static Bitmap getResizedBitmap(int targetW, int targetH, String imagePath, boolean squareCropImage) {

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

        if(squareCropImage) {
            bitmap = cropToSquare(bitmap);
        }

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

    public static void uploadProfilePicture(Context context, String filepath, String filename, String imageRequestTag, boolean isHeaderImage) {

        //get session type (buyer/seller)
        String sessionType = PreferenceUtils.getPreferences(context, Constants.KEY_USER_SESSION_TYPE);
        boolean userIsSeller = false;
        if(sessionType.equals(Constants.SESSION_TYPE_SELLER)) {
            userIsSeller = true;
        }

        //start profile picture image upload service
        Intent uploadIntent = new Intent(context, CommonIntentService.class);
        uploadIntent.setAction(Constants.ACTION_UPLOAD_PROFILE_IMAGE);
        uploadIntent.putExtra("filepath", filepath);
        uploadIntent.putExtra("filename", filename);
        uploadIntent.putExtra("isHeaderImage", isHeaderImage);
        uploadIntent.putExtra("userIsSeller", userIsSeller);
        uploadIntent.putExtra("tag", imageRequestTag);
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
        Bitmap bm = getResizedBitmap(Constants.IMAGE_UPLOAD_DIMENSIONS, Constants.IMAGE_UPLOAD_DIMENSIONS, imagePath, true);
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

    public static Bitmap getBitmapFromDrawableResource(Context context, @DrawableRes int drawableResource) {
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                drawableResource);
        return icon;
    }

    public static Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getBitmapFromURL(Context context, String strURL, boolean round) {
        Bitmap bitmap = getBitmapFromURL(strURL);
        if(bitmap!=null) {
            RoundedBitmapDrawable roundDrawable = getRoundedDrawable(context, bitmap);
            bitmap = drawableToBitmap(roundDrawable);
            return bitmap;
        } else {
            return null;
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static RoundedBitmapDrawable getRoundedDrawable(Context context, Bitmap bitmap) {
        // Create the RoundedBitmapDrawable.
        RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
        roundDrawable.setCircular(true);
        return roundDrawable;
    }

    /*public Bitmap getImage(Context context, String url, final Bitmap bitmapImage) {
        Picasso.with(context)
                .load(url)
                .transform(new CircleTransform())
                .error(R.drawable.koleshop_logo)      // optional// optional// optional
                .resize(90,90)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        bitmapImage = bitmap;

                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });

        return bitmapImage;

    }*/

    public static Bitmap cropToSquare(Bitmap bitmap){
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width)? height - ( height - width) : height;
        int cropW = (width - height) / 2;
        cropW = (cropW < 0)? 0: cropW;
        int cropH = (height - width) / 2;
        cropH = (cropH < 0)? 0: cropH;
        Bitmap cropImg = Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);

        return cropImg;
    }


}
