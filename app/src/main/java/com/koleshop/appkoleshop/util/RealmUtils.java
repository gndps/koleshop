package com.koleshop.appkoleshop.util;

import android.content.Context;

import com.koleshop.appkoleshop.model.realm.ProductCategory;

import io.realm.Realm;
import io.realm.RealmQuery;

/**
 * Created by Gundeep on 25/07/15.
 */
public class RealmUtils {

    public static Long getParentCategoryIdForCategoryId(Long categoryId) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<ProductCategory> query = realm.where(ProductCategory.class)
                .equalTo("id", categoryId);
        ProductCategory category = realm.copyFromRealm(query.findFirst());
        if (category != null && category.getParentCategoryId() > 0l) {
            realm.close();
            return category.getParentCategoryId();
        } else {
            realm.close();
            return null;
        }
    }

}
