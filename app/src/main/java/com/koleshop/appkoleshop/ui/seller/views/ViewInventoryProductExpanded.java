package com.koleshop.appkoleshop.ui.seller.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.realm.Product;
import com.koleshop.appkoleshop.model.realm.ProductVariety;
import com.koleshop.appkoleshop.ui.buyer.views.ViewProductVarietyBuyer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Gundeep on 17/11/15.
 */
public class ViewInventoryProductExpanded extends RelativeLayout implements View.OnClickListener {

    private LayoutInflater inflater;
    private Context mContext;
    private Product product;
    private TextView textViewProductTitle;
    private View view;
    private LinearLayout container;
    List<ViewInventoryProductVariety> varietyViews;
    Long categoryId;
    int positionInParent;
    boolean customerView;

    public ViewInventoryProductExpanded(Context context, View view, boolean customerView) {
        super(context);
        this.mContext = context;
        this.customerView = customerView;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //inflate title view and variety containers
        this.view = view;
        textViewProductTitle = (TextView) view.findViewById(com.koleshop.appkoleshop.R.id.tv_title_inventory_product_expanded);
        textViewProductTitle.setOnClickListener(this);
        container = (LinearLayout) view.findViewById(com.koleshop.appkoleshop.R.id.ll_inventory_product_varieties);
    }

    public void setProduct(Product product, Map<Long, Boolean> checkboxesProgress, int positionInParent) {
        List<ProductVariety> varieties = product.getVarieties();
        this.product = product;
        this.positionInParent = positionInParent;
        this.categoryId = product.getCategoryId();
        textViewProductTitle.setText(product.getName());
        varietyViews = new ArrayList<>();
        container.removeAllViews();
        for (ProductVariety var : varieties) {
            if(!customerView) {
                ViewInventoryProductVariety viewInventoryProductVariety = new ViewInventoryProductVariety(mContext, var, checkboxesProgress.get(var.getId()), null, customerView);
                final Long varietyId = var.getId();
                final boolean varietySelected = var.isVarietyValid();
                viewInventoryProductVariety.setClickListenerArea1(this);
                viewInventoryProductVariety.setClickListenerArea2(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestProductSelection(varietyId, varietySelected);
                    }
                });
                varietyViews.add(viewInventoryProductVariety);
                container.addView(viewInventoryProductVariety);
            } else {
                ViewProductVarietyBuyer viewProductVarietyBuyer = new ViewProductVarietyBuyer(mContext, var, customerView, false, product.getName());
                final Long varietyId = var.getId();
                viewProductVarietyBuyer.setClickListenerArea1(this);
                viewProductVarietyBuyer.setPlusButtonClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        increaseVarietyCountInCart(varietyId);
                    }
                });
                viewProductVarietyBuyer.setMinusButtonClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        decreaseVarietyCountInCart(varietyId);
                    }
                });
                container.addView(viewProductVarietyBuyer);
            }
        }
        view.setBackgroundColor(Color.parseColor("#FFFFFF"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(4);
        }

    }

    private void requestProductSelection(Long varietyId, boolean varietySelected) {
        Intent intentInternal = new Intent(Constants.ACTION_NOTIFY_PRODUCT_SELECTION_VARIETY_TO_PARENT);
        intentInternal.putExtra("varietyId", varietyId);
        intentInternal.putExtra("position", positionInParent);
        intentInternal.putExtra("varietySelected", varietySelected);
        intentInternal.putExtra("requestCategoryId", categoryId);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intentInternal);
    }

    private void increaseVarietyCountInCart(Long varietyId) {
        Intent intentInternal = new Intent(Constants.ACTION_INCREASE_VARIETY_COUNT);
        intentInternal.putExtra("varietyId", varietyId);
        intentInternal.putExtra("position", positionInParent);
        intentInternal.putExtra("requestCategoryId", categoryId);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intentInternal);
    }

    private void decreaseVarietyCountInCart(Long varietyId) {
        Intent intentInternal = new Intent(Constants.ACTION_DECREASE_VARIETY_COUNT);
        intentInternal.putExtra("varietyId", varietyId);
        intentInternal.putExtra("position", positionInParent);
        intentInternal.putExtra("requestCategoryId", categoryId);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intentInternal);
    }

    @Override
    public void onClick(View v) {
        if(positionInParent>0) {
            Intent collapseProductIntent = new Intent(Constants.ACTION_COLLAPSE_EXPANDED_PRODUCT);
            collapseProductIntent.putExtra("position", positionInParent);
            collapseProductIntent.putExtra("categoryId", categoryId);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(collapseProductIntent);
        }
    }
}
