package com.koleshop.koleshopbackend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.koleshop.koleshopbackend.db.models.InventoryProduct;
import com.koleshop.koleshopbackend.db.models.KoleResponse;
import com.koleshop.koleshopbackend.db.models.SellerSearchResults;
import com.koleshop.koleshopbackend.db.models.SellerSettings;
import com.koleshop.koleshopbackend.services.BuyerService;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Gundeep on 16/02/16.
 */


@Api(name = "buyerEndpoint",
        version = "v1",
        namespace = @ApiNamespace(ownerDomain = "api.koleshop.com", ownerName = "koleshopserver", packagePath = ""))
public class BuyerEndpoint {

    private static final Logger logger = Logger.getLogger(BuyerEndpoint.class.getName());

    @ApiMethod(name = "getNearbyShops", httpMethod = ApiMethod.HttpMethod.POST)
    public KoleResponse getNearbyShops(@Nullable @Named("customerId") Long customerId, @Nullable @Named("sessionId") String sessionId,
                                       @Named("gpsLong") Double gpsLong, @Named("gpsLat") Double gpsLat, @Named("homeDeliveryOnly") boolean homeDeliveryOnly,
                                       @Named("openShopsOnly") boolean openShopsOnly, @Named("limit") int limit, @Named("offset") int offset) {

        KoleResponse response = new KoleResponse();
        List<SellerSettings> listOfNearbyShops = null;
        try {
            listOfNearbyShops = new BuyerService().getNearbyShops(customerId, gpsLat, gpsLong, homeDeliveryOnly, openShopsOnly, limit, offset);
        } catch (Exception e) {
            response.setData(e.getLocalizedMessage());
        }
        if (listOfNearbyShops != null) {
            response.setSuccess(true);
            response.setData(listOfNearbyShops);
        } else {
            response.setSuccess(false);
        }
        return response;
    }

    @ApiMethod(name = "searchProductsMultipleSeller", httpMethod = ApiMethod.HttpMethod.POST)
    public KoleResponse searchProductsMultipleSeller(@Nullable @Named("customerId") Long customerId, @Nullable @Named("sessionId") String sessionId,
                                       @Named("gpsLong") Double gpsLong, @Named("gpsLat") Double gpsLat, @Named("homeDeliveryOnly") boolean homeDeliveryOnly,
                                       @Named("openShopsOnly") boolean openShopsOnly, @Named("limit") int limit, @Named("offset") int offset, @Named("searchQuery") String searchQuery) {

        KoleResponse response = new KoleResponse();
        List<SellerSearchResults> listOfSellerSearchResults = null;
        try {
            listOfSellerSearchResults = new BuyerService().searchProductsMultipleSellers(customerId, gpsLat, gpsLong, homeDeliveryOnly, openShopsOnly, limit, offset, searchQuery);
        } catch (Exception e) {
            response.setData(e.getLocalizedMessage());
        }
        if (listOfSellerSearchResults != null) {
            response.setSuccess(true);
            response.setData(listOfSellerSearchResults);
        } else {
            response.setSuccess(false);
        }
        return response;
    }

    @ApiMethod(name = "searchProducts", httpMethod = ApiMethod.HttpMethod.POST)
    public KoleResponse searchProducts(@Nullable @Named("sellerId") Long sellerId, @Named("limit") int limit
            , @Named("offset") int offset, @Named("searchQuery") String searchQuery) {

        KoleResponse response = new KoleResponse();
        List<InventoryProduct> productSearchResults = null;
        try {
            productSearchResults = new BuyerService().searchProducts(sellerId, limit, offset, searchQuery);
        } catch (Exception e) {
            response.setData(e.getLocalizedMessage());
        }
        if (productSearchResults != null) {
            response.setSuccess(true);
            response.setData(productSearchResults);
        } else {
            response.setSuccess(false);
        }
        return response;
    }

}
