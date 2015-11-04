package com.kolshop.kolshopmaterial.common.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryCategory;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.RealmConfiguration;

/**
 * Created by gundeepsingh on 17/08/14.
 */

public class CommonUtils {

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
        String userId = PreferenceUtils.getPreferences(context, Constants.KEY_USER_ID);
        if(userId.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public static void putToDualCache(String key, Object object) {
        @SuppressWarnings("unchecked")
        Class<List<InventoryCategory>> cls = (Class<List<InventoryCategory>>)(Object)List.class;
        /*DualCache<List<InventoryCategory>> cache = new DualCacheBuilder<List<InventoryCategory>>(Constants.CACHE_ID_STRING, Constants.APP_CACHE_VERSION, cls)
                .useReferenceInRam(5120, new SizeOfVehiculeForTesting())
                .useDefaultSerializerInDisk(DISK_MAX_SIZE, true);*/
    }

}
