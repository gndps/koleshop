package com.koleshop.appkoleshop.common.util;

import com.koleshop.appkoleshop.model.ProductVarietyProperty;
import com.koleshop.appkoleshop.model.realm.AttributeValue;
import com.koleshop.appkoleshop.model.realm.VarietyAttribute;
import com.koleshop.appkoleshop.singletons.KoleshopSingleton;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProduct;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProductVariety;

import java.util.List;

/**
 * Created by Gundeep on 27/09/15.
 */
public class ProductUtil {

    public static VarietyAttribute getVarietyAttributeFromList(List<VarietyAttribute> listVarietyAttribute, String varietyAttributeName)
    {
        for(VarietyAttribute va : listVarietyAttribute)
        {
            if(va.getName().equalsIgnoreCase(varietyAttributeName))
            {
                return va;
            }
        }
        return null;
    }

    public static AttributeValue getAttributeValueFromList(List<AttributeValue> listAttributeValue, String varietyAttributeId)
    {
        for(AttributeValue av : listAttributeValue)
        {
            if(av.getProductVarietyAttributeId().equalsIgnoreCase(varietyAttributeId))
            {
                return av;
            }
        }
        return null;
    }

    public static ProductVarietyProperty getProductVarietyPropertyFromList(List<VarietyAttribute> listVarietyAttribute, List<AttributeValue> listAttributeValue, String propertyName) {
        VarietyAttribute va = getVarietyAttributeFromList(listVarietyAttribute, propertyName);
        if(va!=null) {
            AttributeValue av = getAttributeValueFromList(listAttributeValue, va.getId());
            if(av!=null) {
                ProductVarietyProperty productVarietyProperty = new ProductVarietyProperty();
                productVarietyProperty.setAttributeValue(av);
                productVarietyProperty.setVarietyAttribute(va);
                return productVarietyProperty;
            }
        }
        return null;
    }

    public static ProductVarietyProperty getDefaultPriceProductVarietyProperty(String productVarietyId) {
        ProductVarietyProperty priceProperty = new ProductVarietyProperty();
        VarietyAttribute va = new VarietyAttribute();
        va.setId("random" + CommonUtils.randomString(6));
        va.setMeasuringUnitId(KoleshopSingleton.getSharedInstance().getDefaultPriceMeasuringUnitId());
        va.setName("price");
        AttributeValue av = new AttributeValue();
        av.setSortOrder(0);
        av.setId("random" + CommonUtils.randomString(6));
        av.setProductVarietyAttributeId(va.getId());
        av.setProductVarietyId(productVarietyId);
        av.setValue("");
        priceProperty.setVarietyAttribute(va);
        priceProperty.setAttributeValue(av);
        return priceProperty;
    }

    public static int getProductSelectionCount(InventoryProduct product) {
        if(product==null) {
            return 0;
        }
        int count = 0;
        for(InventoryProductVariety ipv : product.getVarieties()) {
            if(ipv.getValid()) {
                count++;
            }
        }
        return count;
    }
}
