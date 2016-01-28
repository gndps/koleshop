package com.koleshop.appkoleshop.util;

import com.koleshop.appkoleshop.model.realm.Product;
import com.koleshop.appkoleshop.model.realm.ProductVariety;

/**
 * Created by Gundeep on 27/09/15.
 */
public class ProductUtil {

    public static int getProductSelectionCount(Product product) {
        if(product==null) {
            return 0;
        }
        int count = 0;
        for(ProductVariety ipv : product.getVarieties()) {
            if(ipv.isVarietyValid()) {
                count++;
            }
        }
        return count;
    }
}
