package com.kolshop.kolshopmaterial.model.genericjson;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProduct;

import java.util.List;

/**
 * Created by Gundeep on 04/11/15.
 */
public class GenericJsonListInventoryProduct extends GenericJson {

    @Key
    public List<InventoryProduct> list;

    public List<InventoryProduct> getList() {
        return list;
    }

    public void setList(List<InventoryProduct> list) {
        this.list = list;
    }
}
