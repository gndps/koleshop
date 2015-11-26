package com.kolshop.kolshopmaterial.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.CommonUtils;
import com.kolshop.kolshopmaterial.common.util.ProductUtil;
import com.kolshop.kolshopmaterial.model.ProductSelectionRequest;
import com.kolshop.kolshopmaterial.services.CommonIntentService;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProduct;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProductVariety;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Gundeep on 17/11/15.
 */
public class ViewInventoryProductExpanded extends RelativeLayout implements View.OnClickListener {

    private LayoutInflater inflater;
    private Context mContext;
    private InventoryProduct product;
    private TextView textViewProductTitle;
    private View view;
    private LinearLayout container;
    List<ViewInventoryProductVariety> varietyViews;
    Long categoryId;
    int positionInParent;

    public ViewInventoryProductExpanded(Context context, View view) {
        super(context);
        this.mContext = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //inflate title view and variety containers
        this.view = view;
        textViewProductTitle = (TextView) view.findViewById(R.id.tv_title_inventory_product_expanded);
        textViewProductTitle.setOnClickListener(this);
        container = (LinearLayout) view.findViewById(R.id.ll_inventory_product_varieties);
    }

    public void setProduct(InventoryProduct product, Map<Long, Boolean> checkboxesProgress, int positionInParent, Long categoryId) {
        List<InventoryProductVariety> varieties = product.getVarieties();
        this.product = product;
        this.positionInParent = positionInParent;
        this.categoryId = categoryId;
        textViewProductTitle.setText(product.getName());
        varietyViews = new ArrayList<>();
        container.removeAllViews();
        for (InventoryProductVariety var : varieties) {
            ViewInventoryProductVariety viewInventoryProductVariety = new ViewInventoryProductVariety(mContext, var, checkboxesProgress.get(var.getId()), null);
            final Long varietyId = var.getId();
            final boolean varietySelected = var.getSelected();
            viewInventoryProductVariety.setClickListenerArea1(this);
            viewInventoryProductVariety.setClickListenerArea2(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestProductSelection(varietyId, varietySelected);
                }
            });
            varietyViews.add(viewInventoryProductVariety);
            container.addView(viewInventoryProductVariety);
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
