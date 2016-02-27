package com.koleshop.koleshopbackend.db.models;

import java.util.List;

/**
 * Created by Gundeep on 25/02/16.
 */
public class SellerSearchResults {

    SellerSettings sellerSettings;
    int totalSearchResultsCount;
    List<InventoryProduct> products;

    public SellerSearchResults() {
    }

    public SellerSearchResults(SellerSettings sellerSettings, int totalSearchResultsCount, List<InventoryProduct> products) {
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

    public List<InventoryProduct> getProducts() {
        return products;
    }

    public void setProducts(List<InventoryProduct> products) {
        this.products = products;
    }
}
