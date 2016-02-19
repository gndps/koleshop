package com.koleshop.appkoleshop.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.gson.Gson;
import com.koleshop.api.productEndpoint.model.InventoryProductVariety;
import com.koleshop.api.productEndpoint.model.InventoryProduct;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.model.parcel.EditProductVar;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.Product;
import com.koleshop.appkoleshop.model.realm.ProductVariety;
import com.koleshop.appkoleshop.singletons.KoleshopSingleton;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

/**
 * Created by Gundeep on 22/12/15.
 */
public class KoleshopUtils {

    public static InventoryProduct getInventoryProductFromEditProduct(EditProduct product) {
        //prepare data
        InventoryProduct inventoryProduct = new InventoryProduct();
        inventoryProduct.setId(product.getId());
        inventoryProduct.setName(product.getName());
        inventoryProduct.setBrand(product.getBrand());
        List<InventoryProductVariety> vars = new ArrayList<>();
        for (EditProductVar var : product.getEditProductVars()) {
            InventoryProductVariety variety = new InventoryProductVariety();
            variety.setId(var.getId());
            variety.setQuantity(var.getQuantity());
            variety.setImageUrl(var.getImageUrl());
            variety.setLimitedStock(var.getLimitedStock());
            variety.setPrice(var.getPrice());
            variety.setValid(var.isValid());
            vars.add(variety);
        }
        inventoryProduct.setVarieties(vars);
        return inventoryProduct;
    }

    public static com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProduct getInventoryProductFromEditProduct2(EditProduct product) {
        //prepare data
        com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProduct inventoryProduct = new com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProduct();
        inventoryProduct.setId(product.getId());
        inventoryProduct.setName(product.getName());
        inventoryProduct.setBrand(product.getBrand());
        List<com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProductVariety> vars = new ArrayList<>();
        for (EditProductVar var : product.getEditProductVars()) {
            com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProductVariety variety = new com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProductVariety();
            variety.setId(var.getId());
            variety.setQuantity(var.getQuantity());
            variety.setImageUrl(var.getImageUrl());
            variety.setLimitedStock(var.getLimitedStock());
            variety.setPrice(var.getPrice());
            variety.setValid(var.isValid());
            vars.add(variety);
        }
        inventoryProduct.setVarieties(vars);
        return inventoryProduct;
    }

    public static Product getProductFromEditProduct(EditProduct editProduct) {
        //prepare data
        Product product = new Product();
        product.setId(editProduct.getId());
        product.setName(editProduct.getName());
        product.setBrand(editProduct.getBrand());
        List<ProductVariety> vars = new ArrayList<>();
        for (EditProductVar var : editProduct.getEditProductVars()) {
            ProductVariety variety = new ProductVariety();
            variety.setId(var.getId());
            variety.setQuantity(var.getQuantity());
            variety.setImageUrl(var.getImageUrl());
            variety.setLimitedStock(var.getLimitedStock());
            variety.setPrice(var.getPrice());
            variety.setVarietyValid(var.isValid());
            vars.add(variety);
        }
        product.setVarieties(new RealmList<>(vars.toArray(new ProductVariety[vars.size()])));
        return product;
    }

    public static SellerSettings getSettingsFromCache(Context context) {
        String settingsString = PreferenceUtils.getPreferences(context, Constants.KEY_SELLER_SETTINGS);
        SellerSettings settings = null;
        if (settingsString != null && !settingsString.isEmpty()) {
            //load settings from cache
            SellerSettings sellerSettings = new Gson().fromJson(settingsString, SellerSettings.class);
            settings = sellerSettings;
        }
        return settings;
    }

    public static TextDrawable getTextDrawable(Context context, String name, boolean round) {
        return getTextDrawable(context, name, 40, round);
    }

    public static TextDrawable getTextDrawable(Context context, String name, int widthInDp, boolean round) {
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, widthInDp, r.getDisplayMetrics());
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(name);
        TextDrawable textDrawable;
        TextDrawable.IShapeBuilder textDrawableBuilder = TextDrawable.builder()
                .beginConfig()
                .width((int) px)  // width in px
                .height((int) px) // height in px
                .endConfig();
        if(round) {
            textDrawable = textDrawableBuilder.buildRound(name.substring(0, 1), color);
        } else {
            textDrawable = textDrawableBuilder.buildRect(name.substring(0, 1), color);
        }

        return textDrawable;
    }

}
