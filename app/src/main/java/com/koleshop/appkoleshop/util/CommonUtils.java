package com.koleshop.appkoleshop.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;

import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryCategory;
import com.koleshop.appkoleshop.constant.Constants;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.RealmConfiguration;

/**
 * Created by gundeepsingh on 17/08/14.
 */

public class CommonUtils {

    private static final String TAG = "CommonUtils";

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    static Random rnd = new Random();

    public static boolean validateEmail(final String hex) {

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(hex);
        return matcher.matches();
    }

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static RealmConfiguration getRealmConfiguration(Context context) {
        RealmConfiguration config = new RealmConfiguration.Builder(context)
                .name("kolshop.realm")
                        //.schemaVersion(42)
                        //.setModules(new MySchemaModule())
                        //.migration(new MyMigration())
                .build();
        return config;
    }

    public static String getGoogleRegistrationId(Context context) {
        return "";
    }

    public static String generateRandomIdForDatabaseObject() {
        String generatedId = "RNDM-" + randomString(8);
        return generatedId;
    }

    public static boolean isUserLoggedIn(Context context) {
        Long userId = getUserId(context);
        if (userId > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static void putToDualCache(String key, Object object) {
        @SuppressWarnings("unchecked")
        Class<List<InventoryCategory>> cls = (Class<List<InventoryCategory>>) (Object) List.class;
        /*DualCache<List<InventoryCategory>> cache = new DualCacheBuilder<List<InventoryCategory>>(Constants.CACHE_ID, Constants.APP_CACHE_VERSION, cls)
                .useReferenceInRam(5120, new SizeOfVehiculeForTesting())
                .useDefaultSerializerInDisk(DISK_MAX_SIZE, true);*/
    }

    public static long getTimeDifferenceInMillis_X_Minus_Y(Date dateX, Date dateY) {
        long millisX = dateX.getTime();
        long millisY = dateY.getTime();
        long timeDiff = millisX - millisY;
        return timeDiff;
    }

    public static Long getUserId(Context context) {
        try {
            Long userId = Long.parseLong(PreferenceUtils.getPreferences(context, Constants.KEY_USER_ID));
            return userId;
        } catch (Exception e) {
            return 0l;
        }
    }

    public static TextView getActionBarTextView(Toolbar mToolBar) {
        TextView titleTextView = null;

        try {
            Field f = mToolBar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);
            titleTextView = (TextView) f.get(mToolBar);
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
        return titleTextView;
    }

    public static String getPathFromUri(Context context, Uri uri) {
        if (Build.VERSION.SDK_INT < 11)
            return RealPathUtil.getRealPathFromURI_BelowAPI11(context, uri);
        else if (Build.VERSION.SDK_INT < 19)
            return RealPathUtil.getRealPathFromURI_API11to18(context, uri);
        else
            return RealPathUtil.getRealPathFromURI_API19(context, uri);
    }

    public static String getPriceStringFromFloat(Float priceFloat) {
        if (priceFloat != null && priceFloat >= 0) {
            String formattedString = String.format("%.02f", priceFloat);
            if (formattedString.endsWith(".00")) {
                formattedString = String.format("%.0f", priceFloat);
            }
            return "" + formattedString;
        } else {
            return "";
        }
    }

    public static String getPriceStringFromFloat(Float priceFloat, boolean rupeeSymbol) {
        if(rupeeSymbol) {
            return Constants.INDIAN_RUPEE_SYMBOL + " " + getPriceStringFromFloat(priceFloat);
        } else {
            return getPriceStringFromFloat(priceFloat);
        }
    }

    @Deprecated
    public static String getSettingsTimeFromDate(Date date) {
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            Calendar cal2 = Calendar.getInstance();
            cal2.set(2015, 11, 31, hour, minute, 0);
            return new SimpleDateFormat("hh:mm a").format(cal2.getTime());
        } else {
            return "N/A";
        }
    }

    @Deprecated
    public static int getSettingsHourFromDate(Date date) {
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            return hour;
        } else {
            return 0;
        }
    }

    @Deprecated
    public static int getSettingsMinutesFromDate(Date date) {
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));
            int minute = cal.get(Calendar.MINUTE);
            return minute;
        } else {
            return 0;
        }
    }

    @Deprecated
    public static Date fixTheTimeForSettings(int hours, int minutes) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(2015, 11, 31, hours, minutes, 0);
        Log.d(TAG, cal.getTime().toString());
        return cal.getTime();
    }

    @Deprecated
    public static Date getDateFromDateFormat(String dateFormat, String dateString) {
        if (dateFormat == null || dateString == null) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date d = sdf.parse(dateString);
            return d;
        } catch (ParseException pe) {
            Log.d(TAG, "parse exception while getting date for format " + dateFormat + "and date string " + dateString, pe);
            return null;
        }
    }

    public static String getTimeStringFromMinutes(int minutes, boolean amPmLowerCase) {
        if (minutes >= 0) {
            String ampm;
            String hourString;
            String minutesString;
            int h = minutes / 60;
            int m = minutes % 60;
            hourString = "" + h;
            if (h > 11) {
                if(amPmLowerCase) {
                    ampm = "pm";
                } else {
                    ampm = "PM";
                }
            } else {
                if(amPmLowerCase) {
                    ampm = "am";
                } else {
                    ampm = "AM";
                }
            }
            if (h > 12) {
                h = h - 12;
                hourString = "" + h;
            }
            if (h == 0) {
                hourString = "" + 12;
            }
            if (m == 0) {
                minutesString = "00";
            } else {
                minutesString = "" + m;
            }

            return hourString + ":" + minutesString + " " + ampm;
        } else {
            return "N/A";
        }
    }

    public static int getNumberOfMinutesFromHourAndMinutes(int hours, int minutes) {
        int totalMinutes = 0;
        totalMinutes += minutes;
        totalMinutes += 60 * hours;
        return totalMinutes;
    }

    public static Date getDateFromNumberOfMinutes(int numberOfMinutes) {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, numberOfMinutes/60);
        cal.set(Calendar.MINUTE, numberOfMinutes%60);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static String getDateStringInFormat(Date date, String format) {
        SimpleDateFormat format1 = new SimpleDateFormat(format);
        return format1.format(date);
    }

    public static String getDayCommonName(Date date) {
        return DateUtils.getRelativeTimeSpanString(date.getTime(), new Date().getTime(), DateUtils.DAY_IN_MILLIS).toString();
    }

    public static Date getDate(Date date, int differenceInSeconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, differenceInSeconds);
        return calendar.getTime();
    }

    public static String getReadableDistanceFromMetres(float distanceInMetres) {
        String distance;
        if (distanceInMetres > 1000) {
            //show distance in kilometres
            float distanceInKm = distanceInMetres / 1000.0f;
            if (distanceInKm < 50) {
                distance = String.format("%.1f", distanceInKm) + " km";
            } else {
                distance = String.format("%d", (int)distanceInKm) + " km";
            }
        } else {
            distance = String.format("%.0f", distanceInMetres) + " m";
        }
        return distance;
    }

    public static float getScreenWidth(Context context) {


        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return dpWidth;

    }

    public static float getScreenWidthInPixels(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    public static float getScreenHeight(Context context) {


        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        return dpHeight;

    }

    public static float getScreenHeightInPixels(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

}
