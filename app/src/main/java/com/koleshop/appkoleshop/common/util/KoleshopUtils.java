package com.koleshop.appkoleshop.common.util;

import com.koleshop.api.productEndpoint.model.InventoryProductVariety;
import com.koleshop.api.productEndpoint.model.InventoryProduct;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.model.parcel.EditProductVar;

import java.util.ArrayList;
import java.util.List;

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
        for(EditProductVar var : product.getEditProductVars()) {
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
        for(EditProductVar var : product.getEditProductVars()) {
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

}
