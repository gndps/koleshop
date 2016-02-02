package com.koleshop.appkoleshop.ui.buyer.activities;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.demo.Cart;
import com.koleshop.appkoleshop.services.DemoIntentService;
import com.koleshop.appkoleshop.singletons.DemoSingleton;
import com.koleshop.appkoleshop.ui.buyer.views.ViewProductVarietyBuyer;

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
        List<Cart.ProductVarietyCount> list = DemoSingleton.getSharedInstance().getCart().getProductVarietyCountList();
        if(list==null) {
            TextView textView = new TextView(mContext);
            textView.setText("No items in cart");
            linearLayoutContainer.addView(textView);
        } else {
            for(final Cart.ProductVarietyCount productVarietyCount : list) {
                final ViewProductVarietyBuyer viewProductVarietyBuyer = new ViewProductVarietyBuyer(mContext, productVarietyCount.getProductVariety(),
                        true, true, productVarietyCount.getTitle());
                viewProductVarietyBuyer.setPlusButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cart.increaseCount(productVarietyCount.getProductVariety(), productVarietyCount.getTitle());
                        viewProductVarietyBuyer.setCartCount(Cart.getCountOfVariety(productVarietyCount.getProductVariety()));
                    }
                });
                viewProductVarietyBuyer.setMinusButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cart.decreaseCount(productVarietyCount.getProductVariety());
                        int newCount = Cart.getCountOfVariety(productVarietyCount.getProductVariety());
                        if (newCount >= 0) {
                            viewProductVarietyBuyer.setCartCount(newCount);
                        } else {
                            linearLayoutContainer.removeView(viewProductVarietyBuyer);
                        }
                    }
                });
                linearLayoutContainer.addView(viewProductVarietyBuyer);
            }
        }
    }

    public void order(View view) {
        //String jsonOrder = new Gson().toJson(DemoSingleton.getSharedInstance().getCart());
        DemoIntentService.sendToSeller(mContext, "order", "");
    }
}
