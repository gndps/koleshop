package com.koleshop.appkoleshop.model.parcel;

import com.koleshop.appkoleshop.model.demo.SellerInfo;

import org.parceler.Parcel;

import java.util.List;

/**
 * Created by Gundeep on 07/02/16.
 */

@Parcel
public class SellerSearchResults {

    SellerInfo sellerInfo;
    int totalSearchResultsCount;
    List<EditProduct> products;

    public SellerSearchResults() {
    }

    public SellerSearchResults(SellerInfo sellerInfo, int totalSearchResultsCount, List<EditProduct> products) {
        this.sellerInfo = sellerInfo;
        this.totalSearchResultsCount = totalSearchResultsCount;
        this.products = products;
    }

    public SellerInfo getSellerInfo() {
        return sellerInfo;
    }

    public void setSellerInfo(SellerInfo sellerInfo) {
        this.sellerInfo = sellerInfo;
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
