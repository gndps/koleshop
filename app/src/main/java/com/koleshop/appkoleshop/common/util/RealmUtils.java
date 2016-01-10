package com.koleshop.appkoleshop.common.util;

import android.content.Context;

import com.koleshop.appkoleshop.model.realm.Product;
import com.koleshop.appkoleshop.model.realm.ProductCategory;
import com.koleshop.appkoleshop.singletons.KoleshopSingleton;

import java.util.Iterator;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Gundeep on 25/07/15.
 */
public class RealmUtils {

    public static boolean saveProduct(Context context, Product product) {
        Realm realm = Realm.getInstance(context);
        //RealmQuery - create realm query to save product
        return true;
    }

    public static Long getParentCategoryIdForCategoryId(Long categoryId) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<ProductCategory> query = realm.where(ProductCategory.class)
                .equalTo("id", categoryId);
        ProductCategory category = query.findFirst();
        if (category != null && category.getParentCategoryId() > 0l) {
            return category.getParentCategoryId();
        } else {
            return null;
        }
    }

}
