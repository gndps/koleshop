package com.koleshop.appkoleshop.ui.buyer.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.cart.ProductVarietyCount;
import com.koleshop.appkoleshop.model.realm.Cart;
import com.koleshop.appkoleshop.services.DemoIntentService;
import com.koleshop.appkoleshop.singletons.CartsSingleton;
import com.koleshop.appkoleshop.ui.buyer.views.ViewProductVarietyBuyer;
import com.koleshop.appkoleshop.util.CartUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CartActivity extends AppCompatActivity {

    @Bind(R.id.ll_cart_container)
    LinearLayout linearLayoutContainer;

    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        mContext = this;
        ButterKnife.bind(this);
        setupView();
    }

    private void setupView() {
        /*final Cart cart = CartsSingleton.getSharedInstance().getCart(null);
        if(cart == null) {
            return;
        }
        List<ProductVarietyCount> list = cart.getProductVarietyCountList();
        if(list==null) {
            TextView textView = new TextView(mContext);
            textView.setText("No items in cart");
            linearLayoutContainer.addView(textView);
        } else {
            for(final ProductVarietyCount productVarietyCount : list) {
                final ViewProductVarietyBuyer viewProductVarietyBuyer = new ViewProductVarietyBuyer(mContext, productVarietyCount.getProductVariety(),
                        true, true, cart.getSellerSettings().getAddress().getName(), sellerSettings);
                viewProductVarietyBuyer.setPlusButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CartUtils.increaseCount(productVarietyCount.getProductVariety(), cart.getSellerSettings());
                        viewProductVarietyBuyer.setCartCount(CartUtils.getCountOfVariety(productVarietyCount.getProductVariety(), cart.getSellerSettings()));
                    }
                });
                viewProductVarietyBuyer.setMinusButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CartUtils.decreaseCount(productVarietyCount.getProductVariety(), cart.getSellerSettings());
                        int newCount = CartUtils.getCountOfVariety(productVarietyCount.getProductVariety(), cart.getSellerSettings());
                        if (newCount >= 0) {
                            viewProductVarietyBuyer.setCartCount(newCount);
                        } else {
                            linearLayoutContainer.removeView(viewProductVarietyBuyer);
                        }
                    }
                });
                linearLayoutContainer.addView(viewProductVarietyBuyer);
            }
        }*/
    }

    public void order(View view) {
        //String jsonOrder = new Gson().toJson(DemoSingleton.getSharedInstance().getCart());
        DemoIntentService.sendToSeller(mContext, "order", "");
    }
}
