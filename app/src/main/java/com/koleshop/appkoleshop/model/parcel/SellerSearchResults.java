package com.koleshop.appkoleshop.model.parcel;

import org.parceler.Parcel;

import java.util.List;

/**
 * Created by Gundeep on 07/02/16.
 */

@Parcel
public class SellerSearchResults {

    SellerSettings sellerSettings;
    int totalSearchResultsCount;
    List<EditProduct> products;

    public SellerSearchResults() {
    }

    public SellerSearchResults(SellerSettings sellerSettings, int totalSearchResultsCount, List<EditProduct> products) {
        this.sellerSettings = sellerSettings;
        this.totalSearchResultsCount = totalSearchResultsCount;
        this.products = products;
    }

    public SellerSettings getSellerSettings() {
        return sellerSettings;
    }

    public void setSellerSettings(SellerSettings sellerSettings) {
        this.sellerSettings = sellerSettings;
    }

    public int getTotalSearchResultsCount() {
        return totalSearchResultsCount;
    }

    public void setTotalSearchResultsCount(int totalSearchResultsCount) {
        this.totalSearchResultsCount = totalSearchResultsCount;
    }

    public List<EditProduct> getProducts() {
        return products;
    }

    public void setProducts(List<EditProduct> products) {
        this.products = products;
    }
}
