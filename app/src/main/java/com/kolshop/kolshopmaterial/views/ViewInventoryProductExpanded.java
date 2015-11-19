package com.kolshop.kolshopmaterial.views;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProduct;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProductVariety;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gundeep on 17/11/15.
 */
public class ViewInventoryProductExpanded extends RelativeLayout {

    private LayoutInflater inflater;
    private Context mContext;
    private InventoryProduct product;
    private TextView textViewProductTitle;
    private View view;
    private LinearLayout container;
    List<ViewInventoryProductVariety> varietyViews;

    public ViewInventoryProductExpanded(Context context, View view) {
        super(context);
        this.mContext = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //inflate title view and variety containers
        this.view = view;
        textViewProductTitle = (TextView) view.findViewById(R.id.tv_title_inventory_product_expanded);
        container = (LinearLayout) view.findViewById(R.id.ll_inventory_product_varieties);
    }

    public void setProduct(InventoryProduct product, boolean checkboxProgress) {
        List<InventoryProductVariety> varieties = product.getVarieties();
        this.product = product;
        textViewProductTitle.setText(product.getName());
        varietyViews = new ArrayList<>();
        container.removeAllViews();
        for (InventoryProductVariety var : varieties) {
            ViewInventoryProductVariety viewInventoryProductVariety = new ViewInventoryProductVariety(mContext, var, checkboxProgress);
            varietyViews.add(viewInventoryProductVariety);
            container.addView(viewInventoryProductVariety);
        }
        view.setBackgroundColor(Color.parseColor("#FFFFFF"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(4);
        }

    }
}
