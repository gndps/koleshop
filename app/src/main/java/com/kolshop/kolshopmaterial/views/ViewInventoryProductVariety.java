package com.kolshop.kolshopmaterial.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.constant.Constants;
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
    private LinearLayout clickArea1,clickArea2;
    private OnClickListener clickListenerArea1, clickListenerArea2;

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
        clickArea1 = (LinearLayout) v.findViewById(R.id.ll_variety_click_area_1);
        clickArea2 = (LinearLayout) v.findViewById(R.id.ll_variety_click_area_2);
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
        textViewPrice.setText(Constants.INDIAN_RUPEE_SYMBOL + variety.getPrice());
        setChecked(variety.getSelected());
        //todo set the image url for this shit
        if(productCheckboxProgress) {
            imageViewCheckbox.setVisibility(GONE);
            progressBarCheckbox.setVisibility(VISIBLE);
        } else {
            imageViewCheckbox.setVisibility(VISIBLE);
            progressBarCheckbox.setVisibility(GONE);
        }
    }

    public void setChecked(boolean checked) {
        if(checked) {
            imageViewCheckbox.setImageResource(R.drawable.ic_checkbox_true);
        } else {
            imageViewCheckbox.setImageResource(R.drawable.ic_checkbox_false_focused);
        }
    }

    public void setClickListenerArea1(OnClickListener clickListenerArea1) {
        this.clickListenerArea1 = clickListenerArea1;
        clickArea1.setOnClickListener(this.clickListenerArea1);
    }

    public void setClickListenerArea2(OnClickListener clickListenerArea2) {
        this.clickListenerArea2 = clickListenerArea2;
        clickArea2.setOnClickListener(this.clickListenerArea2);
        imageViewCheckbox.setOnClickListener(this.clickListenerArea2);
    }
}
