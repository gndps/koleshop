package com.kolshop.kolshopmaterial.model.genericjson;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryCategory;

import java.util.List;

/**
 * Created by Gundeep on 04/11/15.
 */
public class GenericJsonListInventoryCategory extends GenericJson {

    @Key public List<InventoryCategory> list;

    public List<InventoryCategory> getList() {
        return list;
    }

    public void setList(List<InventoryCategory> list) {
        this.list = list;
    }
}
