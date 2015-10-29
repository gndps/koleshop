package com.kolshop.kolshopbackend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.kolshop.kolshopbackend.db.models.InventoryCategory;
import com.kolshop.kolshopbackend.db.models.KolResponse;
import com.kolshop.kolshopbackend.services.InventoryService;
import com.kolshop.kolshopbackend.db.models.InventoryCategory;
import com.kolshop.kolshopbackend.services.SessionService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gundeep on 16/10/15.
 */

@Api(name = "inventoryEndpoint", version = "v1", namespace = @ApiNamespace(ownerDomain = "server.kolshop.com", ownerName = "kolshopserver", packagePath = "yolo"))
public class InventoryEndpoint {

    @ApiMethod(name = "getCategories")
    public KolResponse getCategories(@Named("userId") Long userId, @Named("sessionId") String sessionId) {

        KolResponse response = new KolResponse();
        List<InventoryCategory> list = null;
        try {
            if(SessionService.verifyUserAuthenticity(userId, sessionId)) {
                list = new InventoryService().getCategories();
            }
        } catch (Exception e) {
            response.setData(e.getLocalizedMessage());
        }
        if(list!=null) {
            response.setSuccess(true);
            response.setData(list);
        } else {
            response.setSuccess(false);
        }
        return response;
    }

    //this method is just for exposure of ListInventoryCategory
    @ApiMethod(name = "exposeInventoryCategory")
    public List<InventoryCategory> justatest() {
        List<InventoryCategory> list = new ArrayList<>();
        InventoryCategory cat = new InventoryCategory(1L, "myinventory", "cool things", "someurl");
        list.add(cat);
        return list;
    }

}
