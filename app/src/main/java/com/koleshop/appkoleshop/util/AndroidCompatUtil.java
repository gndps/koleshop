package com.koleshop.appkoleshop.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.koleshop.appkoleshop.R;

/**
 * Created by Gundeep on 02/02/16.
 */
public class AndroidCompatUtil {

    public static Drawable getDrawable(Context mContext, int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return mContext.getResources().getDrawable(resId, mContext.getTheme());
        } else {
            return mContext.getResources().getDrawable(resId);
        }
    }

    public static int getColor(Context context, int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getColor(colorId, context.getTheme());
        } else {
            return context.getResources().getColor(colorId);
        }
    }

}
