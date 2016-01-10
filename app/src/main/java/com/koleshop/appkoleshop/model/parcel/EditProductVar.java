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
    boolean limitedStock;
    String tag;
    boolean showImageProcessing;
    boolean valid;
    String imageFilename;
    int position;
    String imagePath;

    public EditProductVar() {
        this.id = 0l;
        this.quantity = "";
        this.price = 0;
        this.imageUrl = "";
        this.tag = CommonUtils.randomString(10);
        this.limitedStock = true;
        this.valid = true;
    }

    public EditProductVar(Long id, String quantity, float price, String imageUrl, boolean limitedStock, String tag) {
        this.id = id;
        this.quantity = quantity;
        this.price = price;
        this.imageUrl = imageUrl;
        this.limitedStock = limitedStock;
        this.tag = tag;
    }

    public EditProductVar(InventoryProductVariety inventoryProductVariety) {
        this.id = inventoryProductVariety.getId();
        this.quantity = inventoryProductVariety.getQuantity();
        this.price = inventoryProductVariety.getPrice();
        this.imageUrl = inventoryProductVariety.getImageUrl();
        this.valid = inventoryProductVariety.getValid();
        this.limitedStock = inventoryProductVariety.getLimitedStock();
        this.tag = CommonUtils.randomString(10);
        //this.valid = true;
    }

    public EditProductVar(com.koleshop.api.productEndpoint.model.InventoryProductVariety inventoryProductVariety) {
        this.id = inventoryProductVariety.getId();
        this.quantity = inventoryProductVariety.getQuantity();
        this.price = inventoryProductVariety.getPrice();
        this.imageUrl = inventoryProductVariety.getImageUrl();
        this.valid = inventoryProductVariety.getValid();
        this.limitedStock = inventoryProductVariety.getLimitedStock();
        this.tag = CommonUtils.randomString(10);
        //this.valid = true;
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

    public boolean getLimitedStock() {
        return limitedStock;
    }

    public void setLimitedStock(boolean limitedStock) {
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

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getImageFilename() {
        return imageFilename;
    }

    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
