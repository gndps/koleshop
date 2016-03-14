package com.koleshop.appkoleshop.ui.common.activities;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.klinker.android.sliding.SlidingActivity;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.Order;

import org.parceler.Parcels;

public class OrderDetailsActivity extends SlidingActivity {

    Context mContext;
    Order order;

    @Override
    public void init(Bundle savedInstanceState) {
        mContext = this;
        setTitle("Order Details");
        setPrimaryColors(
                ContextCompat.getColor(mContext, R.color.primary),
                ContextCompat.getColor(mContext, R.color.primary_dark)
        );
        setContent(R.layout.activity_order_details);

        if(getIntent()!=null && getIntent().getExtras()!=null) {
            Parcelable parcelableOrder = getIntent().getExtras().getParcelable("order");
            this.order = Parcels.unwrap(parcelableOrder);
        }
        loadOrderContent();
    }

    private void loadOrderContent() {

    }
}
