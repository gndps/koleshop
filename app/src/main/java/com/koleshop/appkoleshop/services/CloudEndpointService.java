package com.koleshop.appkoleshop.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.model.realm.Product;

import org.parceler.Parcels;

import io.realm.Realm;


public class CloudEndpointService extends IntentService {

    public CloudEndpointService() {
        super("CloudEndpointService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Constants.ACTION_SAVE_PRODUCT.equals(action)) {
                Bundle bundle = intent.getExtras();
                if (bundle == null) return;
                Product product = Parcels.unwrap(bundle.getParcelable("product"));
                saveProductToRealm(product);
            }
        }
    }

    private void saveProductToRealm(Product product) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(product);
        realm.commitTransaction();
        realm.close();
        Toast.makeText(getApplicationContext(), "Product saved", Toast.LENGTH_SHORT).show();
    }
}
