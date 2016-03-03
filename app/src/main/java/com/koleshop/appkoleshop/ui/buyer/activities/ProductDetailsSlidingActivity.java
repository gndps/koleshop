package com.koleshop.appkoleshop.ui.buyer.activities;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import com.klinker.android.sliding.SlidingActivity;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.model.parcel.EditProductVar;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.Product;
import com.koleshop.appkoleshop.model.realm.ProductVariety;
import com.koleshop.appkoleshop.ui.buyer.fragments.ProductVarietyDetailsFragment;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;
import com.koleshop.appkoleshop.util.KoleshopUtils;

import org.parceler.Parcels;

import java.util.List;


public class ProductDetailsSlidingActivity extends SlidingActivity {

    private static final String EXTRA_PRODUCT = "com.koleshop.appkoleshop.ui.buyer.activities.EXTRA_PRODUCT";
    private static final String EXTRA_SELLER_SETTINGS = "com.koleshop.appkoleshop.ui.buyer.activities.EXTRA_SELLER_SETTINGS";
    Product product;
    SellerSettings sellerSettings;

    public static void startActivityWithProduct(Context context, Product product, SellerSettings sellerSettings) {
        Intent intent = new Intent(context, ProductDetailsSlidingActivity.class);
        if(product!=null) {
            intent.putExtra(EXTRA_PRODUCT, Parcels.wrap(new EditProduct(product)));
            intent.putExtra(EXTRA_SELLER_SETTINGS, Parcels.wrap(sellerSettings));
            context.startActivity(intent);
        }
    }

    @Override
    public void init(Bundle savedInstanceState) {

        setPrimaryColors(
                AndroidCompatUtil.getColor(this, R.color.primary),
                AndroidCompatUtil.getColor(this, R.color.primary_dark)
        );
        setContent(R.layout.activity_product_details);
        product = KoleshopUtils.getProductFromEditProduct((EditProduct) Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_PRODUCT)));
        sellerSettings = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_SELLER_SETTINGS));
        setTitle(product.getName());
        List<ProductVariety> varieties = product.getVarieties();

        for(int i = 0;i<varieties.size();i++)
        {
            //ProductVariety no. i
            Bundle bundle=new Bundle();
            EditProductVar editProductVar = new EditProductVar(varieties.get(i));
            Parcelable parcelableProduct= Parcels.wrap(editProductVar);
            Parcelable parcelableSettings= Parcels.wrap(sellerSettings);
            bundle.putParcelable("variety", parcelableProduct);
            bundle.putParcelable("sellerSettings", parcelableSettings);

            //Fragment no. i
            Fragment newFragment = new ProductVarietyDetailsFragment();
            newFragment.setArguments(bundle);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.linear_layout_product_details_activity, newFragment).commit();

        }
    }
}