package com.kolshop.kolshopbackend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.kolshop.kolshopbackend.beans.Product;
import com.kolshop.kolshopbackend.db.models.RestCallResponse;
import com.kolshop.kolshopbackend.services.ProductService;

import java.util.List;

/**
 * Created by Gundeep on 12/05/15.
 */
@Api(name = "productEndpoint", version = "v1", namespace = @ApiNamespace(ownerDomain = "kolshopserver.gndps.com", ownerName = "kolshopserver.gndps.com", packagePath = ""))
public class ProductEndpoint {

    @ApiMethod(name = "getProductsList")
    public List<Product> getProductList(@Named("shopId") int shopId,
                                        @Named("startIndex") int startIndex,
                                        @Named("count") int count) {
        ProductService productService = new ProductService();
        List<Product> productList = productService.getProduct(shopId, startIndex, count);
        return productList;
    }

    @ApiMethod(name = "saveProduct")
    public RestCallResponse saveProduct(Product product) {
        RestCallResponse restCallResponse = new RestCallResponse();
        ProductService productService = new ProductService();
        try {
            if(productService.saveProduct(product))
            {
                //todo send back the generated product id
                restCallResponse.setData("");
                restCallResponse.setStatus("success");
                restCallResponse.setReason("");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            restCallResponse.setData(null);
            restCallResponse.setReason(e.getMessage());
            restCallResponse.setStatus("failure");
        }
        return restCallResponse;
    }

}
