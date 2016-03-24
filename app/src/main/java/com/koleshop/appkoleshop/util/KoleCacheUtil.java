package com.koleshop.appkoleshop.util;

import android.util.Log;

import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.model.realm.Product;
import com.koleshop.appkoleshop.model.realm.ProductCategory;
import com.koleshop.appkoleshop.model.realm.ProductVariety;
import com.koleshop.appkoleshop.singletons.KoleshopSingleton;
import com.koleshop.appkoleshop.model.genericjson.GenericJsonListInventoryCategory;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryCategory;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Gundeep on 25/11/15.
 */
public class KoleCacheUtil {

    public static final String TAG = "KoleCacheUtil";
    //todo update all these cached on async task

    public static boolean cacheProductsListInRealm(List<Product> products, boolean containsOldComplementaryDate, boolean updateCategories) {
        //complementary date is : if this is the case of MyShop, then Warehouse update date is complementary...otherwise MyShopUpdate date is complementary
        Realm realm = Realm.getDefaultInstance();
        try {

            //01 UPDATE THE CATEGORIES CACHE
            if (updateCategories && products != null && products.get(0) != null && products.get(0).getId() > 0) {
                boolean categoryExistsInMyShop = false;
                outerloop:
                for (Product product : products) {
                    for (ProductVariety productVariety : product.getVarieties()) {
                        if (productVariety.isVarietyValid()) {
                            categoryExistsInMyShop = true;
                            break outerloop;
                        }
                    }
                }

                RealmQuery<ProductCategory> query = realm.where(ProductCategory.class);
                List<ProductCategory> productCategories = query.equalTo("id", products.get(0).getCategoryId()).findAll();
                if (productCategories != null && productCategories.size() > 0 && productCategories.get(0) != null) {
                    ProductCategory productCategory = productCategories.get(0);
                    productCategory.setAddedToMyShop(categoryExistsInMyShop);
                    realm.beginTransaction();
                    realm.copyToRealmOrUpdate(productCategory);
                    realm.commitTransaction();
                }
            }

            //02 UPDATE THE PRODUCTS CACHE
            if (!containsOldComplementaryDate) {

                //02.1 get products in this category
                List<Product> alreadyCachedProducts = null;
                boolean myShop = false;
                try {
                    myShop = products.get(0).getVarieties().get(0).isVarietyValid();
                    alreadyCachedProducts = getAllCachedProducts(products.get(0).getCategoryId(), products.get(0).getSellerId());
                } catch (Exception e) {
                    //problem in getting already cached products
                }

                //02.2 if complementary date is NOT contained && if already cached products exist in this category, then update the cache...else add the products to cache
                if (alreadyCachedProducts != null && alreadyCachedProducts.size() > 0) {

                    //02.2.1 update the already existing product objects
                    for (Product cachedProduct : alreadyCachedProducts) {
                        for (Product updatedProduct : products) {
                            if (updatedProduct.getId() == cachedProduct.getId()) {
                                if (myShop) {
                                    // if myInventory/myShop, then keep the old wareHouseDate
                                    updatedProduct.setUpdateDateWareHouse(cachedProduct.getUpdateDateWareHouse());
                                } else {
                                    //else keep the old my shop date
                                    updatedProduct.setUpdateDateMyShop(cachedProduct.getUpdateDateMyShop());
                                }
                                break;
                            }
                        }
                    }

                    //02.2.2 update the products in realm cache
                    realm.beginTransaction();
                    realm.copyToRealmOrUpdate(products);
                    realm.commitTransaction();
                    realm.close();
                    return true;
                } else {
                    //else add the products to cache
                    realm.beginTransaction();
                    realm.copyToRealmOrUpdate(products);
                    realm.commitTransaction();
                    realm.close();
                    return true;
                }

            } else {
                //else update the products to cache
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(products);
                realm.commitTransaction();
                realm.close();
                return true;

            }
        } catch (Exception e) {
            Log.e(TAG, "some exception while saving the products in cache", e);
            realm.close();
            return false;
        }
    }

    public static List<Product> getCachedProducts(boolean myInventory, Long categoryId, Long sellerId) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<Product> query = realm.where(Product.class);

        //1. query realm objects and sort by BrandName, ProductName
        RealmResults<Product> realmProducts = query.equalTo("categoryId", categoryId)
                .equalTo("sellerId", sellerId)
                .greaterThanOrEqualTo(myInventory ? "updateDateMyShop" : "updateDateWareHouse", CommonUtils.getDate(new Date(), -1 * Constants.TIME_TO_LIVE_PRODUCT_CACHE))
                .findAllSorted("brand", Sort.ASCENDING, "name", Sort.ASCENDING);

        List<Product> products = realm.copyFromRealm(realmProducts);

        //2. if my shop, then remove the products that are not added to my shop
        if (myInventory) {
            for (Product product : products) {
                List<ProductVariety> productVarieties = product.getVarieties();
                if (product != null && productVarieties != null && productVarieties.size() > 0) {
                    boolean atLeastOneVarietyIsAddedToMyShop = false;

                    //2.1 remove the varieties that are not in my shop
                    for (ProductVariety productVariety : productVarieties) {
                        if (productVariety.isVarietyValid()) {
                            atLeastOneVarietyIsAddedToMyShop = true;
                        } else {
                            productVarieties.remove(productVariety);
                        }
                    }

                    //2.2 if no varieties in this product are in my shop, then remove this product from products list
                    if (!atLeastOneVarietyIsAddedToMyShop) {
                        products.remove(product);
                    }

                } else {
                    products.remove(product);
                }
            }
        }
        realm.close();
        return products;
    }

    public static List<Product> getAllCachedProducts(Long categoryId, Long sellerId) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<Product> query = realm.where(Product.class);

        //1. query realm objects and sort by BrandName, ProductName
        RealmResults<Product> realmProducts = query.equalTo("categoryId", categoryId)
                .equalTo("sellerId", sellerId)
                .beginGroup()
                .greaterThanOrEqualTo("updateDateMyShop", CommonUtils.getDate(new Date(), -1 * Constants.TIME_TO_LIVE_PRODUCT_CACHE))
                .or()
                .greaterThanOrEqualTo("updateDateWareHouse", CommonUtils.getDate(new Date(), -1 * Constants.TIME_TO_LIVE_PRODUCT_CACHE))
                .endGroup()
                .findAllSorted("brand", Sort.ASCENDING, "name", Sort.ASCENDING);

        List<Product> products = realm.copyFromRealm(realmProducts);
        realm.close();
        return products;
    }

    public static void addUpdateProductInRealmCache(EditProduct editProduct) {
        //product is added to cache only when it's in my shop....it is called only after a product save success
        //it can't be called from customer view

        //01 GET EXISTING PRODUCT WITH SAME ID
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<Product> query = realm.where(Product.class);
        Product existingProduct = query.equalTo("id", editProduct.getId()).findFirst();
        Product product = KoleshopUtils.getProductFromEditProduct(editProduct);
        product.setSellerId(0l);

        Date updateDateMyShop = null;
        Date updateDateWareHouse = null;

        //02 SET THE PRODUCT TIME STAMPS IF PRODUCT ALREADY EXISTS OR SAME CATEGORY PRODUCTS EXIST
        if (existingProduct != null) {
            //if the product with this id already exists, then update the data (PRODUCT UPDATED)
            updateDateMyShop = existingProduct.getUpdateDateMyShop();
            updateDateWareHouse = existingProduct.getUpdateDateWareHouse();
        } else {
            //else if there are other products in this category, copy their 'update time stamps' and set them to this product as well and then save the product (NEW PRODUCT ADDED)
            List<Product> products = getCachedProducts(true, editProduct.getCategoryId(), 0l);
            if (products != null && products.size() > 0 && products.get(0) != null && products.get(0).getId() > 0) {
                updateDateMyShop = products.get(0).getUpdateDateMyShop();
                updateDateWareHouse = products.get(0).getUpdateDateWareHouse();
            } else {
                //else add the product without any timestamp
            }
        }

        product.setUpdateDateMyShop(updateDateMyShop);
        product.setUpdateDateWareHouse(updateDateWareHouse);

        //03 UPDATE THE PRODUCT IN REALM
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(product);
        realm.commitTransaction();

        //04 UPDATE THE CATEGORY(addedToMyShop = true) IF IT EXISTS IN THE REALM DATABASE
        RealmQuery<ProductCategory> query2 = realm.where(ProductCategory.class);
        ProductCategory productCategory = query2.equalTo("id", product.getCategoryId()).findFirst();
        if (productCategory != null && productCategory.getId() > 0) {
            productCategory.setAddedToMyShop(true);
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(productCategory);
            realm.commitTransaction();
        }

        realm.close();

    }

    public static boolean cacheCategoriesInRealm(List<ProductCategory> cats) {

        try {

            //01 PERSIST OLD COMPLEMENTARY UPDATE DATE
            boolean myInventory = cats.get(0).isAddedToMyShop();
            Long parentCategoryId = cats.get(0).getParentCategoryId();
            List<ProductCategory> cachedProductCategories = getAllCachedProductCategoriesFromRealm(parentCategoryId, cats.get(0).getSellerId());
            if (cachedProductCategories != null && cachedProductCategories.size() > 0) {
                for (ProductCategory category : cachedProductCategories) {
                    for (ProductCategory cat : cats) {
                        if (cat.getId() == category.getId()) {
                            if (myInventory) {
                                cat.setWarehouseUpdateDate(category.getWarehouseUpdateDate());
                            } else {
                                cat.setAddedToMyShop(category.isAddedToMyShop());
                                cat.setMyShopUpdateDate(category.getMyShopUpdateDate());
                            }
                        }
                    }
                }
            }

            //02 UPDATE THE CATEGORIES IN REALM
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(cats);
            realm.commitTransaction();
            realm.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "exception while caching categories", e);
            return false;
        }

    }

    public static List<ProductCategory> getCachedProductCategoriesFromRealm(
            boolean myInventory, Long parentCategoryId, Long sellerId) {

        Realm realm = Realm.getDefaultInstance();
        RealmQuery<ProductCategory> query = realm.where(ProductCategory.class);

        if (parentCategoryId == null) {
            parentCategoryId = 0l;
        }

        if (myInventory) {
            query = query.equalTo("addedToMyShop", myInventory);
        }

        RealmResults<ProductCategory> productCategories = query.equalTo("parentCategoryId", parentCategoryId)
                .equalTo("sellerId", sellerId)
                .greaterThanOrEqualTo(myInventory ? "myShopUpdateDate" : "warehouseUpdateDate", CommonUtils.getDate(new Date(), -1 * Constants.TIME_TO_LIVE_CATEGORY_CACHE))
                .findAllSorted("sortOrder", Sort.ASCENDING);

        if (productCategories.size() == 0) {
            realm.close();
            return null;
        } else {
            List<ProductCategory> categories = realm.copyFromRealm(productCategories);
            realm.close();
            return categories;
        }

    }

    public static List<ProductCategory> getAllCachedProductCategoriesFromRealm(Long parentCategoryId, Long sellerId) {

        Realm realm = Realm.getDefaultInstance();
        RealmQuery<ProductCategory> query = realm.where(ProductCategory.class);

        if (parentCategoryId == null) {
            parentCategoryId = 0l;
        }

        RealmResults<ProductCategory> productCategories = query.equalTo("parentCategoryId", parentCategoryId)
                .equalTo("sellerId", sellerId)
                .beginGroup()
                .greaterThanOrEqualTo("myShopUpdateDate", CommonUtils.getDate(new Date(), -1 * Constants.TIME_TO_LIVE_CATEGORY_CACHE))
                .or()
                .greaterThanOrEqualTo("warehouseUpdateDate", CommonUtils.getDate(new Date(), -1 * Constants.TIME_TO_LIVE_CATEGORY_CACHE))
                .endGroup()
                .findAllSorted("sortOrder", Sort.ASCENDING);

        if (productCategories.size() == 0) {
            realm.close();
            return null;
        } else {
            List<ProductCategory> categories = realm.copyFromRealm(productCategories);
            realm.close();
            return categories;
        }

    }

    private static Date getDateMinusDays(int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, -1 * days);
        Date dt = c.getTime();
        return dt;
    }

    public static void invalidateProductsCache(long categoryId, boolean myShop) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<Product> query = realm.where(Product.class);

        RealmResults<Product> products = query.equalTo("categoryId", categoryId)
                .equalTo("sellerId", 0)
                .findAll();


        if (products != null && products.size() > 0) {
            realm.beginTransaction();

            for (int i=0; i<products.size(); i++) {
                if (myShop) {
                    products.get(i).setUpdateDateMyShop(CommonUtils.getDate(new Date(), -2 * Constants.TIME_TO_LIVE_PRODUCT_CACHE));
                } else {
                    products.get(i).setUpdateDateWareHouse(CommonUtils.getDate(new Date(), -2 * Constants.TIME_TO_LIVE_PRODUCT_CACHE));
                }
            }

            realm.copyToRealmOrUpdate(products);
            realm.commitTransaction();
        }

        realm.close();

    }

    public static void invalidateProductCategoriesWithParentCategoryId(Long parentCategoryId, boolean myShop) {

        Realm realm = Realm.getDefaultInstance();
        RealmQuery<ProductCategory> query = realm.where(ProductCategory.class);

        if (parentCategoryId == null) {
            parentCategoryId = 0l;
        }

        RealmResults<ProductCategory> productCategories = query
                .equalTo("parentCategoryId", parentCategoryId)
                .equalTo("sellerId", 0)
                .findAll();

        if (productCategories != null && productCategories.size() > 0) {
            realm.beginTransaction();
            for (int i=0; i<productCategories.size(); i++) {
                if (myShop) {
                    productCategories.get(i).setMyShopUpdateDate(CommonUtils.getDate(new Date(), -2 * Constants.TIME_TO_LIVE_CATEGORY_CACHE));
                } else {
                    productCategories.get(i).setWarehouseUpdateDate(CommonUtils.getDate(new Date(), -2 * Constants.TIME_TO_LIVE_CATEGORY_CACHE));
                }
            }
            realm.commitTransaction();
        }
        realm.close();

    }
}
