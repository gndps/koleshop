package com.koleshop.appkoleshop.util;

import com.koleshop.appkoleshop.singletons.KoleshopSingleton;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProduct;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProductVariety;

import java.util.List;

/**
 * Created by Gundeep on 27/09/15.
 */
public class ProductUtil {

    public static int getProductSelectionCount(InventoryProduct product) {
        if(product==null) {
            return 0;
        }
        int count = 0;
        for(InventoryProductVariety ipv : product.getVarieties()) {
            if(ipv.getValid()) {
                count++;
            }
        }
        return count;
    }
}
