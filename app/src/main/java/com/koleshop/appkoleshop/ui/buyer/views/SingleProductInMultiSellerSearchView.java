package com.koleshop.appkoleshop.ui.buyer.views;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.Product;
import com.koleshop.appkoleshop.model.realm.ProductVariety;
import com.koleshop.appkoleshop.ui.buyer.activities.ProductDetailsSlidingActivity;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;
import com.koleshop.appkoleshop.util.KoleshopUtils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gundeep on 28/02/16.
 */
public class SingleProductInMultiSellerSearchView extends RelativeLayout implements View.OnClickListener {

    public TextView textViewTitleProductMasterList;
    public TextView textViewSubtitleProductMasterList;
    public CircleImageView circleImageViewProductMasterList;
    private LinearLayout clickArea1, clickArea2;
    private RelativeLayout rootLayout;
    private Context mContext;
    private View verticalDivider;
    private Product product;
    private SellerSettings sellerSettings;
    private int settingsPosition;
    private int productPosition;

    public SingleProductInMultiSellerSearchView(Context context, EditProduct product, SellerSettings sellerSettings, int settingsPosition, int productPosition) {
        super(context);
        this.mContext = context;
        this.product = KoleshopUtils.getProductFromEditProduct(product);
        this.sellerSettings = sellerSettings;
        this.settingsPosition = settingsPosition;
        this.productPosition = productPosition;
        initView();
        bindData();
    }

    private void initView() {

        View view = inflate(getContext(), R.layout.item_rv_inventory_product, null);
        addView(view);

        rootLayout = (RelativeLayout) view.findViewById(R.id.root_layout_irip);
        rootLayout.setBackgroundColor(AndroidCompatUtil.getColor(mContext, R.color.white));
        clickArea1 = (LinearLayout) view.findViewById(R.id.ll_inventory_product_click_area_1);
        clickArea1.setOnClickListener(this);
        clickArea2 = (LinearLayout) view.findViewById(R.id.ll_inventory_product_click_area_2);
        verticalDivider = view.findViewById(R.id.view_vertical_divider);
        verticalDivider.setVisibility(View.GONE);
        textViewTitleProductMasterList = (TextView) view.findViewById(R.id.textViewTitleProductMasterListItem);
        textViewSubtitleProductMasterList = (TextView) view.findViewById(R.id.textViewSubtitleProductMasterListItem);

        circleImageViewProductMasterList = (CircleImageView) view.findViewById(R.id.circleImageViewProductMasterListItem);
        circleImageViewProductMasterList.setOnClickListener(this);

        //hide the product selection option
        clickArea2.setVisibility(View.GONE);
        verticalDivider.setVisibility(View.GONE);
    }

    public void bindData() {
        String title = product.getBrand() + " - " + product.getName();

        setTitle(title);
        setSubtitle(makeDescription());
        List<ProductVariety> varieties = product.getVarieties();
        if (varieties != null) {
            String imageUrl = varieties.get(0).getImageUrl();
            String smallSizeImageUrl = "";
            if (imageUrl != null)
                smallSizeImageUrl = imageUrl.replaceFirst("small", "prod-image/286X224");
            //setImageUrl(smallSizeImageUrl);
            //todo launch this request when kolserver image server is working
            //holder.sendImageFetchRequest(context);
        }
    }

    public void setTitle(String title) {
        textViewTitleProductMasterList.setText(title);
    }

    public void setSubtitle(String subtitle) {
        textViewSubtitleProductMasterList.setText(subtitle);
    }

    private String makeDescription() {
        if (product == null) {
            return "";
        } else {
            String description = "";
            boolean first = true;
            for (ProductVariety ipv : product.getVarieties()) {
                String price = Constants.INDIAN_RUPEE_SYMBOL + " " + ipv.getPrice();
                if (price.endsWith(".0")) {
                    price = price.substring(0, price.length() - 2);
                }
                String desc = ipv.getQuantity() + " - " + price;
                if (ipv.isVarietyValid() && !ipv.isLimitedStock()) {
                    desc += " (OUT OF STOCK)";
                }
                if (first) {
                    description += desc;
                    first = false;
                } else {
                    description += " | " + desc;
                }
            }
            return description;
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(Constants.ACTION_COLLAPSE_EXPANDED_PRODUCT);
        intent.putExtra("settingsPosition", settingsPosition);
        intent.putExtra("productPosition", productPosition);
        intent.putExtra("expand", true);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        //ProductDetailsSlidingActivity.startActivityWithProduct(mContext, product, sellerSettings);
    }
}
