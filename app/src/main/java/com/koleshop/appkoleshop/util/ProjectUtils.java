package com.koleshop.appkoleshop.util;

import com.koleshop.appkoleshop.model.demo.Cart;
import com.koleshop.appkoleshop.model.realm.ProductVariety;

import java.util.Random;

/**
 * Created by Gundeep on 11/02/16.
 */
public class ProjectUtils {

    public static int getCountOfVariety(ProductVariety variety) {
        int countOfVariety = new Random().nextInt(3);
        return countOfVariety;
    }

    public static void increaseCount(ProductVariety productVariety, String productName) {
        //dummy method
    }

    public static void decreaseCount(ProductVariety productVariety) {
        //dummy method
    }

    public static boolean isStarred(ProductVariety productVariety) {
        int countOfVariety = new Random().nextInt(1);
        if(countOfVariety>0) {
            return true;
        } else {
            return false;
        }
    }


}
