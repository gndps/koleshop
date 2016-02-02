package com.koleshop.appkoleshop.model.demo;

import com.koleshop.appkoleshop.model.realm.ProductVariety;
import com.koleshop.appkoleshop.singletons.DemoSingleton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gundeep on 28/01/16.
 */
public class Cart {

    List<ProductVarietyCount> productVarietyCountList;

    public Cart(List<ProductVarietyCount> productVarietyCountList) {
        this.productVarietyCountList = productVarietyCountList;
    }

    public Cart() {
        productVarietyCountList = new ArrayList<>();
    }

    public List<ProductVarietyCount> getProductVarietyCountList() {
        return productVarietyCountList;
    }

    public void setProductVarietyCountList(List<ProductVarietyCount> productVarietyCountList) {
        this.productVarietyCountList = productVarietyCountList;
    }

    public static class ProductVarietyCount {
        ProductVariety productVariety;
        int cartCount;
        String title;

        public ProductVarietyCount(ProductVariety productVariety, int cartCount, String title) {
            this.productVariety = productVariety;
            this.cartCount = cartCount;
            this.title = title;
        }

        public ProductVariety getProductVariety() {
            return productVariety;
        }

        public void setProductVariety(ProductVariety productVariety) {
            this.productVariety = productVariety;
        }

        public int getCartCount() {
            return cartCount;
        }

        public void setCartCount(int cartCount) {
            this.cartCount = cartCount;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public static void increaseCount(ProductVariety productVariety, String title) {
        Cart cart = DemoSingleton.getSharedInstance().getCart();
        List<ProductVarietyCount> list = cart.getProductVarietyCountList();
        boolean countIncreased = false;
        for(ProductVarietyCount provarcount : list) {
            ProductVariety currentProductVariety = provarcount.getProductVariety();
            if(currentProductVariety!=null && currentProductVariety.getId().equals(productVariety.getId())) {
                provarcount.cartCount++;
                countIncreased = true;
                break;
            }
        }
        if(!countIncreased) {
            ProductVarietyCount productVarietyCount = new ProductVarietyCount(productVariety, 1, title);
            list.add(productVarietyCount);
            cart.setProductVarietyCountList(list);
        }
    }

    public static void decreaseCount(ProductVariety productVariety) {
        Cart cart = DemoSingleton.getSharedInstance().getCart();
        List<ProductVarietyCount> list = cart.getProductVarietyCountList();
        boolean countDecreased = false;
        for(ProductVarietyCount provarcount : list) {
            ProductVariety currentProductVariety = provarcount.getProductVariety();
            if(currentProductVariety!=null && currentProductVariety.getId().equals(productVariety.getId())) {
                if(provarcount.cartCount <= 1) {
                    list.remove(provarcount);
                } else {
                    provarcount.cartCount--;
                }
                countDecreased = true;
                break;
            }
        }
    }

    public static int getCountOfVariety(ProductVariety productVariety) {
        Cart cart = DemoSingleton.getSharedInstance().getCart();
        List<ProductVarietyCount> list = cart.getProductVarietyCountList();
        int count = 0;
        for(ProductVarietyCount provarcount : list) {
            ProductVariety currentProductVariety = provarcount.getProductVariety();
            if(currentProductVariety!=null && currentProductVariety.getId().equals(productVariety.getId())) {
                if(provarcount.cartCount >= 1) {
                    count = provarcount.cartCount;
                }
                break;
            }
        }
        return count;
    }


}
