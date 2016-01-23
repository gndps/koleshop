package com.koleshop.appkoleshop.model;

import org.parceler.Parcel;

/**
 * Created by Gundeep on 23/01/16.
 */

@Parcel
public class OrderItem {


    Long productVarietyId;
    int orderCount;
    float pricePerUnit;
    int availableCount;

    public OrderItem() {
    }

}
