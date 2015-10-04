package com.kolshop.kolshopmaterial.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.CommonUtils;
import com.kolshop.kolshopmaterial.model.android.Product;

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
        Realm realm = CommonUtils.getRealmInstance(getApplicationContext());
        realm.beginTransaction();
        realm.copyToRealm(product);
        realm.commitTransaction();
        Toast.makeText(getApplicationContext(), "Product saved", Toast.LENGTH_SHORT).show();
    }
}
