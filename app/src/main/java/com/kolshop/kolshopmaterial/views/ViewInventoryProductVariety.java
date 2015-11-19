package com.kolshop.kolshopmaterial.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProductVariety;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gundeep on 17/11/15.
 */
public class ViewInventoryProductVariety extends RelativeLayout {

    private InventoryProductVariety variety;
    private LayoutInflater inflater;
    private View v;
    private CircleImageView imageView;
    private boolean productCheckboxProgress;
    private TextView textViewQuantity, textViewPrice;
    private ImageView imageViewCheckbox;
    private ProgressBar progressBarCheckbox;

    public ViewInventoryProductVariety(Context context) {
        super(context);
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.view_inventory_product_variety, this, true);
        imageView = (CircleImageView) v.findViewById(R.id.civ_inventory_product_variety);
        textViewQuantity = (TextView) v.findViewById(R.id.tv_inventory_product_variety_quantity);
        textViewPrice = (TextView) v.findViewById(R.id.tv_inventory_product_variety_price);
        imageViewCheckbox = (ImageView) v.findViewById(R.id.iv_inventory_product_variety);
        progressBarCheckbox = (ProgressBar) v.findViewById(R.id.pb_inventory_product_variety);
    }

    public ViewInventoryProductVariety(Context context, InventoryProductVariety variety, boolean productCheckboxProgress,
                                       OnClickListener productVarietyOnClickListener) {
        this(context);
        this.variety = variety;
        this.productCheckboxProgress = productCheckboxProgress;
        imageViewCheckbox.setOnClickListener(productVarietyOnClickListener);
        loadTheData();
    }

    private void loadTheData() {
        textViewQuantity.setText(variety.getQuantity());
        textViewPrice.setText("Rs. " + variety.getPrice());
        //todo set the image url for this shit
        if(productCheckboxProgress) {
            imageViewCheckbox.setVisibility(GONE);
            progressBarCheckbox.setVisibility(VISIBLE);
        } else {
            imageViewCheckbox.setVisibility(VISIBLE);
            progressBarCheckbox.setVisibility(GONE);
        }
    }
}
