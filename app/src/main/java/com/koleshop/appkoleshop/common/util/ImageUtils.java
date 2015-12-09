package com.koleshop.appkoleshop.common.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.services.CommonIntentService;

import java.io.ByteArrayOutputStream;

/**
 * Created by Gundeep on 06/12/15.
 */
public class ImageUtils {

    public static Bitmap getResizedBitmap(int targetW, int targetH,  String imagePath) {

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        //inJustDecodeBounds = true <-- will not load the bitmap into memory
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        return(bitmap);
    }

    public static void uploadBitmap(Context context, Bitmap bitmap, String filename, String varietyTag) {

        //Convert to byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        Intent uploadIntent = new Intent(context, CommonIntentService.class);
        uploadIntent.setAction(Constants.ACTION_UPLOAD_IMAGE);
        uploadIntent.putExtra("image", byteArray);
        uploadIntent.putExtra("filename", filename);
        uploadIntent.putExtra("tag", varietyTag);
        context.startService(uploadIntent);

    }

}
