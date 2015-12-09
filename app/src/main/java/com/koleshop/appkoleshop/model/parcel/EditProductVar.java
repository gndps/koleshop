package com.koleshop.appkoleshop.model.parcel;

import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProductVariety;
import com.koleshop.appkoleshop.common.util.CommonUtils;

import org.parceler.Parcel;

/**
 * Created by Gundeep on 07/12/15.
 */

@Parcel
public class EditProductVar {

    Long id;
    String quantity;
    float price;
    String imageUrl;
    boolean selected;
    int limitedStock;
    String tag;
    boolean showImageProcessing;

    public EditProductVar() {
    }

    public EditProductVar(Long id, String quantity, float price, String imageUrl, boolean selected, int limitedStock, String tag) {
        this.id = id;
        this.quantity = quantity;
        this.price = price;
        this.imageUrl = imageUrl;
        this.selected = selected;
        this.limitedStock = limitedStock;
        this.tag = tag;
    }

    public EditProductVar(InventoryProductVariety inventoryProductVariety) {
        this.id = inventoryProductVariety.getId();
        this.quantity = inventoryProductVariety.getQuantity();
        this.price = inventoryProductVariety.getPrice();
        this.imageUrl = inventoryProductVariety.getImageUrl();
        this.selected = inventoryProductVariety.getSelected();
        this.limitedStock = inventoryProductVariety.getLimitedStock();
        this.tag = CommonUtils.randomString(10);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getLimitedStock() {
        return limitedStock;
    }

    public void setLimitedStock(int limitedStock) {
        this.limitedStock = limitedStock;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isShowImageProcessing() {
        return showImageProcessing;
    }

    public void setShowImageProcessing(boolean showImageProcessing) {
        this.showImageProcessing = showImageProcessing;
    }
}
