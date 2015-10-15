package com.kolshop.kolshopmaterial.common.util;

import android.content.Context;

import com.kolshop.kolshopmaterial.model.RealmPrefs;
import com.kolshop.kolshopmaterial.model.realm.Product;

import io.realm.Realm;
import io.realm.RealmQuery;

/**
 * Created by Gundeep on 25/07/15.
 */
public class RealmUtils {

    public static boolean saveRealmPrefs(Context context, String key, String value)
    {
        try {
            RealmPrefs prefs = new RealmPrefs(key, value);
            Realm realm = Realm.getInstance(context);
            realm.beginTransaction();
            realm.copyToRealm(prefs);
            realm.commitTransaction();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

    }

    public static String getRealmPrefs(Context context, String key)
    {
        try {
            Realm realm = Realm.getInstance(context);
            RealmQuery<RealmPrefs> query = realm.where(RealmPrefs.class);
            query.equalTo("key", key);
            final RealmPrefs prefs = query.findFirst();
            return prefs.getValue();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean saveProduct(Context context, Product product)
    {
        Realm realm = Realm.getInstance(context);
        //RealmQuery - create realm query to save product
        return true;
    }

}
